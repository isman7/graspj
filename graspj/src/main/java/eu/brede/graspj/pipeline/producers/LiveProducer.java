package eu.brede.graspj.pipeline.producers;

import ij.IJ;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ShortBuffer;

import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brede.graspj.utils.Buffers;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.pipeline.NeedsProduct;
import eu.brede.common.pipeline.producers.AbstractProducer;
import eu.brede.common.util.ExceptionTools;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.datatypes.bufferholder.BufferHolder;
import eu.brede.graspj.datatypes.cycle.Cycle;
import eu.brede.graspj.datatypes.cycle.CycleWithOffset;
import eu.brede.graspj.utils.Utils;

public class LiveProducer extends AbstractProducer<AnalysisItem> implements
		NeedsProduct<AnalysisItem>, Configurable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient AnalysisItem product;
	private EnhancedConfig config;

	final static Logger logger = LoggerFactory.getLogger(LiveProducer.class);

	public static ObjectChoice<ProductionEngine> getEngineChoice() {
		ObjectChoice<ProductionEngine> engineChoice = new ObjectChoice<>();

		engineChoice.addChosen(new RawProductionEngine());
		engineChoice.getChoices().add(new LociProductionEngine());
		engineChoice.getChoices().add(new ImagePlusProductionEngine());

		return engineChoice;
	}

	public LiveProducer() {
		// this.template = template;
		// this.config = new EnhancedConfig();
		// config.initMetaData();
		//
		// config.put("acquisitionConfig", acquisitionConfig);
		//
		//
		//
		// config.put("productionEngine", getEngineChoice());
		//
		// Option detectEnd = new Option();
		// detectEnd.put("interval", 200);
		// detectEnd.put("attempts", 25);
		// detectEnd.setSelected(true);
		// config.put("detectEnd", detectEnd);

		config = new EnhancedConfig() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public EnhancedConfig getRequiredDefault() {
				EnhancedConfig requiredConfig = super.getRequiredDefault();
				requiredConfig.setPrefix("LiveProducerConfig.");

				requiredConfig.initMetaData();
				requiredConfig
						.put("acquisitionConfig", new AcquisitionConfig());
				requiredConfig.put("framesPerPackage", 1024);
				requiredConfig.put("productionEngine", getEngineChoice());

				Option detectEnd = new Option();
				detectEnd.put("interval", 200);
				detectEnd.put("attempts", 25);

                Option frameLimit = new Option();
                frameLimit.put("numFrames", 0);
                frameLimit.setSelected(false);

                detectEnd.put("frameLimit", frameLimit);

				detectEnd.setSelected(true);
				requiredConfig.put("detectEnd", detectEnd);



				return requiredConfig;
			}

		};

		config.ensureCompliance();
	}

	// public FramePackageProducerFromStack() { // TODO use this at all?
	// // TODO calculate & set size of queue
	// new LinkedBlockingQueue<FramePackage>();
	// }

	public void updateImageDim() {
		AcquisitionConfig acqConfig = getConfig().gett("acquisitionConfig");
		ObjectChoice<ProductionEngine> engineChoice = config
				.gett("productionEngine");
		ProductionEngine engine = engineChoice.getChosen();

		acqConfig.setDimensions(engine.getImageDim());
	}

	public void initProduct(AnalysisItem product) {
		updateImageDim();
		product.setAcquisitionConfig(getConfig().<AcquisitionConfig> gett(
				"acquisitionConfig"));
	}

	@Override
	public Integer call() throws Exception {
		logger.debug("{} started", this.getClass().getSimpleName());
		// numWaits CHECK CHANGED!!!!!!!!!!!!!!!!!!!!
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		int packageNr = 0;
		// AcquisitionConfig acqConfig = template.getAcquisitionConfig();
		AcquisitionConfig acqConfig = getConfig().gett("acquisitionConfig");
		ObjectChoice<ProductionEngine> engineChoice = config
				.gett("productionEngine");
		ProductionEngine engine = engineChoice.getChosen();

		updateImageDim();

		product.setAcquisitionConfig(acqConfig);
		int frameByteSize = (int) acqConfig.getDimensions().frameByteSize();
		int framesPerItem = getConfig().getInt("framesPerPackage");

		long fullItemByteSize = frameByteSize * framesPerItem;

		if (fullItemByteSize > Integer.MAX_VALUE) {
			// TODO: Easy to implement, cut in several reads!
			throw new Error("package sizes bigger than " + Integer.MAX_VALUE
					+ " bytes are currently not supported");
		}

		int itemByteSize = (int) fullItemByteSize;

		// ByteBuffer finalBuffer = null;
		ShortBuffer packageBuffer = null;
		// BufferedInputStream reader = null;

		// engine.setAcquisitionConfig(acqConfig);

		boolean running = true;

		int numWaits = 0;

		Option detectEnd = config.gett("detectEnd");
		int interval = detectEnd.get("interval", (int) 200);
		int attempts = detectEnd.get("attempts", (int) 25);

        Option frameLimit = detectEnd.get("frameLimit", new Option());

        boolean hasFrameLimit = frameLimit.isSelected();
        
        // TODO fix reading XML saved files. 
        
        int numFrameLimit = frameLimit.gett("numFrames");
        
        

        long byteLimit = frameByteSize * numFrameLimit;


		while (running && !getController().isStopped()) {

            long packagesBytes = packageNr * fullItemByteSize;

			long bytesMissing = itemByteSize;

			int numExceptions = 0;
			int allocationAttempts = 3; // TODO parameter?
			boolean successfulAllocation = false;

			while (numExceptions < allocationAttempts) {
				try {
					packageBuffer = Buffers
							.newDirectShortBuffer(itemByteSize / 2);
					successfulAllocation = true;
					break;
				}
				catch (OutOfMemoryError e) {
					numExceptions++;
					logger.warn(
							"LiveProducer ran out of memory, running GC and retrying ({}/{})",
							(numExceptions + 1), allocationAttempts);
					System.gc();
				}
			}
			
			// Buffer Allocation failed! Show error message and end analysis.
			if(!successfulAllocation) {
				Utils.showMessage("LiveProducer.OutOfMemory");
				return packageNr;
			}

			int actuallyRead = 0;
			StopWatch stopWatch = new StopWatch();
			while (running && bytesMissing > 0 && !getController().isStopped()) {
				while (getController().isPaused()) {
					synchronized (this) {
						wait();
					}
				}
				bytesMissing = itemByteSize - 2 * packageBuffer.position();

				if (actuallyRead >= 0) {
					// int bytesToRead = (int) Math.min(available,bytesMissing);
					int bytesToRead = (int) bytesMissing;
					
					try {
						actuallyRead = engine.readBytes(bytesToRead, packageBuffer);
					}
					catch (FileNotFoundException e) {
						logger.error(e.getMessage(), e);
						e.printStackTrace();
						Utils.showError("LiveProducer.fileNotFound", e);
						running = false;
					}
					catch (IOException e) {
						logger.error(e.getMessage(), e);
						e.printStackTrace();
						Utils.showError("LiveProducer.ioException", e);
						running = false;
					}
					
					if (actuallyRead > 0) {
						numWaits = 0;
					}
				}
                else {
					try {
						// TODO: create own config with sleep time & waits max
						Thread.sleep(interval);
						// Thread.sleep(500);
						numWaits++;
						actuallyRead = 0;
					} catch (InterruptedException ex) {
						running = false;
					}
				}
				if (detectEnd.isSelected()) {
					if (numWaits > attempts) {
						// fill buffer with zeros?
						running = false;
					}
                    if(hasFrameLimit) {
                        long currentBytes = packagesBytes + (itemByteSize - bytesMissing);
//                        System.out.println("currentBytes: " + currentBytes);
//                        System.out.println("packagesBytes: " + packagesBytes);
//                        System.out.println("itemByteSize: " + itemByteSize);
//                        System.out.println("bytesMissing: " + bytesMissing);
//                        System.out.println("byteLimit: " + byteLimit);
                        if(currentBytes >= byteLimit) {
//                            System.out.println("byteLimit reeched");
                            running = false;
                        }
                    }
				}
			}
			stopWatch.stop();
			
			// in case last package is empty don't enqueue it
			if(packageBuffer.position()>0) {				
				// AnalysisItem newItem = AnalysisItem.newFromTemplate(template);
				AnalysisItem newItem = AnalysisItem.newFromConfig(acqConfig);
				
				newItem.getAcquisitionConfig().put(
						"frameCycle",
						new CycleWithOffset((Cycle) acqConfig.get("frameCycle"),
								packageNr * framesPerItem));
				
				// newItem.getAcquisition().setFramePackage(
				// new FramePackage(packageBuffer, packageNr));
				
				BufferHolder<ShortBuffer> bh = new BufferHolder<ShortBuffer>();
				bh.setBuffer(packageBuffer);
				
				newItem.getNotes().put("frameBuffer", bh);
				newItem.getNotes().put("packageNr", packageNr);
				
				long elapsedTime = stopWatch.getElapsedTime();
				
				double speed = (itemByteSize) / (1e6 * (elapsedTime));
				logger.info("Read package {} with {} MB/s", packageNr, speed);
				logger.debug("itemByteSize: {}", itemByteSize);
				logger.debug("elapsedTime: {}", elapsedTime);
				
				// perhaps use add and incorporate in waiting procedure from above
				try {
					// if(packageNr>4)
					queue.put(newItem);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// queue.add(newItem);
				
				packageNr++;
			}
			
		}
		logger.debug("{} finished", this.getClass().getSimpleName());
		return packageNr;
	}

	@Override
	public EnhancedConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = config;
	}

	@Override
	public void setProduct(AnalysisItem product) {
		this.product = product;
	}

}
