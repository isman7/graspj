package eu.brede.graspj.datatypes;

import ij.gui.ImageWindow;
import ij.gui.Plot;

import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Point3f;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.util.DeepCopy;
import eu.brede.common.util.MathTools;
import eu.brede.common.util.SimpleLock;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.datatypes.cycle.Cycle;
import eu.brede.graspj.datatypes.cycle.CycleWithOffset;
import eu.brede.graspj.datatypes.filtermask.FilterMask;
import eu.brede.graspj.datatypes.spot.Spot;
import eu.brede.graspj.datatypes.spotcollection.BufferSpotCollection;
import eu.brede.graspj.datatypes.spotcollection.BufferSpotCollection.SplitPoints;
import eu.brede.graspj.gui.RenderingCanvas;
import eu.brede.graspj.pipeline.processors.renderer.Gaussian2D;
import eu.brede.graspj.utils.GroupManager.GroupNames;
import eu.brede.graspj.utils.Utils;

public class AnalysisItem implements Serializable, Configurable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private AcquisitionConfig acquisitionConfig;
	private BufferSpotCollection spots;
	private FilterMask mask;
	private Drift drift;
	private Notes notes; // TODO make final?
//	private MetaData metaData = new MetaData();
	private EnhancedConfig config;
	
	// TODO quick&dirty concurrency fix
	protected SimpleLock lock = new SimpleLock();
	
	public AnalysisItem() {
		super();
		this.notes = new Notes();
		this.mask = new FilterMask();
		this.drift = new Drift();
		this.spots = new BufferSpotCollection();
		this.acquisitionConfig = new AcquisitionConfig();
		
		config = new EnhancedConfig() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public EnhancedConfig getRequiredDefault() {
				EnhancedConfig requiredConfig = super.getRequiredDefault();
				requiredConfig.setPrefix("AnalysisItemConfig.");
				requiredConfig.initMetaData();
				
				return requiredConfig;
			}
			
		};
		
		config.ensureCompliance();
	}
	
	
	public AnalysisItem shallowCopy() {
		lock();
		AnalysisItem newShallow = new AnalysisItem();
		newShallow.setAcquisitionConfig(getAcquisitionConfig());
		newShallow.setNotes(getNotes());
		newShallow.setDrift(getDrift());
		newShallow.setMask(getMask());
		newShallow.setSpots(getSpots());
//		newShallow.lock = this.lock;
		newShallow.lock = new SimpleLock();
		unlock();
		return newShallow;
	}
	
	public static class CopyInstructions extends Option {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CopyInstructions() {
			super();
			Option newSpotCollection = new Option();
			newSpotCollection.put("copyOrgData", false);
			
			Option newMask = DeepCopy.copy(newSpotCollection);
			Option newDrift = DeepCopy.copy(newSpotCollection);
			
			put("newSpotCollection", newSpotCollection);
			put("newMask", newMask);
			put("newDrift", newDrift);
		}
		
		
		
	}
	
	public AnalysisItem copy(CopyInstructions instructions) {
		AnalysisItem item = this;
		if(instructions.isSelected()) {
			item  = item.shallowCopy();
			this.lock();
			
			Option newSpotCollection = instructions.gett("newSpotCollection");
			Option newMask = instructions.gett("newMask");
			Option newDrift = instructions.gett("newDrift");
			
			if(newSpotCollection.isSelected()) {
				if(newSpotCollection.gett("copyOrgData")) {
					item.setSpots(new BufferSpotCollection(item.getSpots()));
				}
				else {
					item.setSpots(new BufferSpotCollection());
				}
			}
			
			if(newMask.isSelected()) {				
				if(newMask.gett("copyOrgData")) {
					item.setMask(new FilterMask(item.getMask()));
				}
				else {
					item.setMask(new FilterMask());
				}
			}
			
			if(newDrift.isSelected()) {				
				if(newDrift.gett("copyOrgData")) {
					item.setDrift(new Drift(item.getDrift()));
				}
				else {
					item.setDrift(new Drift());
				}
			}
			this.unlock();
		}
		return item;
	}
	
	public AnalysisItem(AnalysisItem item) {
		// TODO implement copy constructor
		throw new Error("missing implementation");
	}
	
	public ArrayList<AnalysisItem> splitByFrame(int framesPerPackage) {
		SplitPoints splitPoints = getSpots().getSplitPoints(framesPerPackage);
		
		ArrayList<BufferSpotCollection> spotCollections = 
				getSpots().splitByFrames(framesPerPackage);
		
		ArrayList<FilterMask> masks = 
				getMask().split(splitPoints.spotsSplitPoints);
		
		ArrayList<Drift> drifts = 
				getDrift().split(
						MathTools.multiplyAllElements(splitPoints.framesSplitPoints, 2));
		
		ArrayList<AnalysisItem> items = new ArrayList<>();
		for (int packageNr = 0; packageNr < splitPoints.numPackages; packageNr++) {
			AnalysisItem item = AnalysisItem.newFromTemplate(this);
			item.setSpots(spotCollections.get(packageNr));
			item.setDrift(drifts.get(packageNr));
			item.setMask(masks.get(packageNr));
			item.getNotes().put("packageNr", packageNr);
			item.getAcquisitionConfig().put(
					"frameCycle",
					new CycleWithOffset((Cycle) getAcquisitionConfig().get(
							"frameCycle"), packageNr * framesPerPackage));
			items.add(item);
		}
		
		return items;
	}
	
	public Notes getNotes() {
		return notes;
	}

	public AcquisitionConfig getAcquisitionConfig() {
		return acquisitionConfig;
	}
	public void setAcquisitionConfig(AcquisitionConfig acquisition) {
		this.acquisitionConfig = acquisition;
	}
	public BufferSpotCollection getSpots() {
//		resource.acquireUninterruptibly();
//		resource.release();
		return spots;
	}
	public void setSpots(BufferSpotCollection spots) {
		this.spots = spots;
	}
	public FilterMask getMask() {
//		resource.acquireUninterruptibly();
//		resource.release();
		return mask;
	}
	public void setMask(FilterMask mask) {
		this.mask = mask;
	}
	public Drift getDrift() {
//		resource.acquireUninterruptibly();
//		resource.release();
		return drift;
	}
	public void setDrift(Drift drift) {
		this.drift = drift;
	}
	
	void setNotes(Notes notes) {
		this.notes = notes;
	}
	
	public static AnalysisItem newFromTemplate(AnalysisItem template) {
		return newFromConfig(template.getAcquisitionConfig());
	}
	
	public static AnalysisItem newFromConfig(AcquisitionConfig config) {
		AnalysisItem newItem = new AnalysisItem();
		newItem.setAcquisitionConfig(DeepCopy.copy(config));
		return newItem;
	}
	
	public void append(AnalysisItem item) {
		lock.lock();
		
		// TODO merge acquisition
		spots.appendSpotCollection(item.getSpots());
		mask.append(item.getMask());
		drift.append(item.getDrift());
		
		lock.unlock();
	}
	
	public void free() {
		// TODO what about Notes & Acquisition?
		spots.free();
		mask.free();
		drift.free();
	}
	
	public void lock() {
		lock.lock();
		spots.lock();
		mask.lock();
		drift.lock();
	}
	
	public void unlock() {
		lock.unlock();
		spots.unlock();
		mask.unlock();
		drift.unlock();
	}

	@Override
	public EnhancedConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
//		metaData = (MetaData) config;
		this.config = config;
		// ensure compliance?
	}
	
//	public MetaData getMetaData() {
//		return metaData;
//	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		free();
	}


	@Override
	public String toString() {
		return Utils.buildSimpleName(this);
	}

	public Point3f calcAverageLocalizationPrecision() {
		int spotNr = 0;
		int positives = 0;
		Double x = 0d;
		Double y = 0d;
		Double z = 0d;
		boolean checkMask = false;
		if(mask!=null) {
			if(mask.holdsBuffer()) {
				checkMask = true;
			}
		}
		if(checkMask) {			
			for(Spot spot : getSpots()) {
				if(mask.getBuffer().get((int)spotNr)>0) {
					x = x + spot.x().uncertainty();
					y = y + spot.y().uncertainty();
					z = z + spot.z().uncertainty();
					positives++;
				}
				spotNr++;
			}
		}
		else {
			for(Spot spot : getSpots()) {
				x = x + spot.x().uncertainty();
				y = y + spot.y().uncertainty();
				z = z + spot.z().uncertainty();
				positives++;
			}
		}
		x = x/positives;
		y = y/positives;
		z = z/positives;
		return new Point3f(x.floatValue(),y.floatValue(),z.floatValue());
	}

	public EnhancedConfig getResultSummary() {
		
		JPanel preview = new JPanel();
		preview.setLayout(new HorizontalLayout(5));
		
		Cycle cycle = getAcquisitionConfig().gett("frameCycle");
		
		EnhancedConfig result = new EnhancedConfig();
		result.setPrefix(this.getClass().getSimpleName() + ".ResultSummary.");
		
		if(getSpots().getSpotCount()>0) {			
			Option pushActions = new Option();
			pushActions.put("groupNames", new GroupNames("preview" + hashCode()));
			pushActions.setSelected(true);
			
			for(Integer color : cycle.getColors()) {
				JPanel chPanel = new JPanel(new VerticalLayout());
				chPanel.add(new JLabel("Channel " + String.valueOf(color)));
				Gaussian2D renderer = new Gaussian2D();
				renderer.getConfig().put("renderWidth", 128);
				renderer.getConfig().put("renderHeight", 128);
				renderer.setChannel(color);
				
				RenderingCanvas canvas = 
						new RenderingCanvas(this, renderer, pushActions, pushActions, true);
				canvas.requestRefresh();
//			chPanel.add(LiveRenderer.createRenderingWindow(canvas));
				chPanel.add(canvas);
				preview.add(chPanel);
			}
			
			result.put("preview", preview);
		}
		
		result.put("spotCount", getSpots().getSpotCount());
		result.put("positiveMaskCount", getMask().countPositives());
		/*try {
		    Thread.sleep(1000);                 //1000 milliseconds.
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		result.put("avgLocalizationPrecision", calcAverageLocalizationPrecision());
		*/
		int numFrames = getSpots().getFrameCount();
//		float[] drift = new float[numFrames];
		float[] frameNrs = new float[numFrames];
//		getDrift().getBuffer().rewind();
//		getDrift().getBuffer().get(drift);	
		for(int i=1;i<=numFrames;i++) {
			frameNrs[i-1] = i;
		}
		
		// if drift correction was done
		if(getDrift().getBuffer()!=null) {			
			float[][] drift = getDrift().asArrays();
			Plot plotx = new Plot("Drift in x [nm]", "frame nr", "drift in x [nm]",
					frameNrs, drift[0]);
//			plot.show();
			plotx.setSize(192, 128);
			ImageWindow windowx = new ImageWindow(plotx.getImagePlus());
			
			Plot ploty = new Plot("Drift in y [nm]", "frame nr", "drift in y [nm]",
					frameNrs, drift[1]);
//			plot.show();
			ploty.setSize(192, 128);
			ImageWindow windowy = new ImageWindow(ploty.getImagePlus());
			
			
			JPanel driftPanel = new JPanel();
			driftPanel.setLayout(new HorizontalLayout(5));
//		driftPanel.add(new ImageCanvas());
			driftPanel.add(windowx.getCanvas());
			driftPanel.add(windowy.getCanvas());
			windowx.setVisible(false);
			windowy.setVisible(false);
			result.put("drift", driftPanel);
		}
		
		return result;
	}
	
	
}
