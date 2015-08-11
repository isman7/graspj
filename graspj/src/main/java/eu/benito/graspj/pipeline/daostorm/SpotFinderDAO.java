package eu.benito.graspj.pipeline.daostorm;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.Resizer;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brede.graspj.utils.Buffers;
import com.jogamp.opencl.CLCommandQueue;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.util.DeepCopy;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.configs.finding.FindConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.bufferholder.BufferHolder;
import eu.brede.graspj.datatypes.cycle.Cycle;
import eu.brede.graspj.datatypes.spotcollection.BufferSpotCollection;
import eu.brede.graspj.opencl.utils.CLSystemGJ;
import eu.brede.graspj.pipeline.processors.AbstractAIProcessor;
import eu.brede.graspj.pipeline.processors.finder.SpotFinderCL;

public class SpotFinderDAO extends AbstractAIProcessor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected BufferHolder<ShortBuffer> candidates = new BufferHolder<ShortBuffer>();
	private FindConfig config;
	private int stepDAO = 0;
	
	final static Logger logger = LoggerFactory.getLogger(SpotFinderDAO.class);
	
	public SpotFinderDAO() {
		super();
		config = new FindConfig();
	}
	
	public SpotFinderDAO(SpotFinderCL finder) {
		config = DeepCopy.copy(finder.getConfig());
	}
	
	
	@Override
	public void process(AnalysisItem item) {
		super.process(item);

        int framesPerWorker = 32;

		BufferHolder<ShortBuffer> frameBufferHolder = item.getNotes().gett("frameBuffer");
		
		ShortBuffer frameBuffer = 
				frameBufferHolder.getBuffer();
		frameBuffer.rewind();
		short[][] frameMat = Buff2Mat(frameBuffer, 256); 
		
		
		long frameByteSize = item.getAcquisitionConfig().getDimensions().frameByteSize();
		int bytesPerShort = 2;
		int framesPerPackage = (int) Math.floor(
				((double)(bytesPerShort*frameBuffer.limit()))/frameByteSize);
		
		
		ShortBuffer counts = Buffers.newDirectShortBuffer(framesPerPackage);
		counts.limit(counts.capacity());
		counts.rewind();
		
		int maxSpotsPerFrame = getConfig().getInt("maxSpotsPerFrame");
		int maxSpotsPerPackage = maxSpotsPerFrame*framesPerPackage;
		

		
		int parametersPerCandidate = 3;
		int DEBUGcandidatesBufferSize = maxSpotsPerPackage * parametersPerCandidate;
		
		
		int frameWidth = item.getAcquisitionConfig().getDimensions().frameWidth;
		int frameHeight = item.getAcquisitionConfig().getDimensions().frameHeight;
		int frameBufferSize = framesPerPackage*frameWidth*frameHeight; //TODO from other config!!


		ShortBuffer candidates = Buffers.newDirectShortBuffer(DEBUGcandidatesBufferSize);
		candidates.limit(candidates.capacity());
		
		
		CLSystem cl = CLSystemGJ.getDefault();
		// No CLResourceManager needed as all CLBuffers need to stay alive for fitting
//		CLResourceManager manager = new CLResourceManager();
		
		// not watched, will be released manually
		CLCommandQueue queue = cl.pollQueue();
		
		queue.putWriteBuffer(frameBufferHolder.getCLBuffer(cl), true);
		
//		ByteBuffer maskBuffer = Buffers.newDirectByteBuffer(frameBuffer.capacity());
		ByteBuffer maskBuffer = Buffers.newDirectByteBuffer(frameBufferSize);
		maskBuffer.limit(frameBufferSize);
		maskBuffer.rewind();
		
		
		frameBuffer.rewind();
		
		WorkerInfo info = new WorkerInfo();
		info.threshold = getConfig().getInt("threshold")/(stepDAO+1);
		info.maxSpotsPerFrame = getConfig().getInt("maxSpotsPerFrame");
		info.boxRadius = getConfig().getInt("boxRadius");
		info.frameWidth = frameWidth;
		info.frameHeight = frameHeight;
		info.imageBuffer = frameBuffer;
		info.mask = maskBuffer;
		info.candidates = candidates;
		info.counts = counts;
        info.subtractLocalBg = getConfig().getBoolean("subtractLocalBg");
        info.framesPerWorker = framesPerWorker;
        info.framesTotal = framesPerPackage;
		
		
		ArrayList<Future<Integer>> futures = new ArrayList<>(framesPerPackage);
		// TODO make config option!
		Cycle cycle = item.getAcquisitionConfig().gett("frameCycle");
		for(int frameNr=0;frameNr<framesPerPackage;frameNr++) {
            if(!cycle.isActivation(frameNr)) {
                futures.add(Global.INSTANCE.getExecutor().submit(new Worker(frameNr,info)));
            }
        }

//        for(int frameNr=0;frameNr<framesPerPackage;frameNr+=framesPerWorker) {
//            if(!cycle.isActivation(frameNr)) {
//                futures.add(Global.INSTANCE.getExecutor().submit(new Worker(frameNr,info)));
//            }
//        }
		
		// wait for all to finish
		for(Future<Integer> future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		counts.rewind();
		maskBuffer.rewind();
		candidates.rewind();
		
		BufferSpotCollection oldSpots = item.getSpots();
		
		item.setSpots(new BufferSpotCollection());
		item.getSpots().getCounts().setBuffer(counts);
		item.getSpots().getCounts().setCLBuffer(null); // necessary?
		queue.putWriteBuffer(
				item.getSpots().getCounts().getCLBuffer(cl), true);
		queue.putReadBuffer(
				item.getSpots().getCounts().getCLBuffer(cl), true);
		
		short[] candidatesArray = new short[DEBUGcandidatesBufferSize];
		candidates.get(candidatesArray, 0, candidatesArray.length);
		candidates.clear();
		
		short[] countsArray = new short[framesPerPackage];
		counts.get(countsArray, 0, countsArray.length);

		
		int numSpots = item.getSpots().getSpotCount();
		
		logger.info("Spots found: {}", numSpots);
		
		int trimmedCandidatesBufferSize = numSpots * parametersPerCandidate;
		if(trimmedCandidatesBufferSize==0) trimmedCandidatesBufferSize=1;

		ShortBuffer trimmedCandidates = Buffers.newDirectShortBuffer(trimmedCandidatesBufferSize);
		

		// TODO is this right? compare to declaration of countsArray
		int framesInThisPackage = item.getSpots().getFrameCount();

		trimmedCandidates.rewind();

		for(int frameNr=0; frameNr<framesInThisPackage; frameNr++) {

			int srcOffsetNew = maxSpotsPerFrame*frameNr*parametersPerCandidate;
			int shortsToCopy = countsArray[frameNr]*parametersPerCandidate;
			trimmedCandidates.put(candidatesArray, srcOffsetNew, shortsToCopy);

		}
		trimmedCandidates.rewind();
		
		this.candidates.setBuffer(trimmedCandidates);
		this.candidates.setCLBuffer(null);
		queue.putWriteBuffer(this.candidates.getCLBuffer(cl), true);
		// necessary?
		queue.putReadBuffer(this.candidates.getCLBuffer(cl), true);
		
		BufferHolder<ShortBuffer> candidatesOld = item.getNotes().gett(
				"candidates");
		
		/*if (stepDAO > 0){
			//item.getNotes().put("candidates", this.candidates);
			//item.getSpots().appendSpotCollection(oldSpots);
			String candidatesOldKey = "candidates" + (stepDAO-1);
			item.getNotes().put(candidatesOldKey, candidatesOld);
			String oldSpotsKey = "spots" + (stepDAO-1);
			item.getNotes().put(oldSpotsKey, oldSpots);
		}*/
		
		item.getNotes().put("candidates", this.candidates);
		if (stepDAO == (2-1)){
			stepDAO = 0; 
		} else {
			stepDAO++;
		}
		cl.returnQueue(queue);
		return;
	}

	@Override
	public FindConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = (FindConfig) config; // TODO wrap instead!
	}
	
	private class WorkerInfo {
		int threshold;
		int maxSpotsPerFrame;
		int boxRadius;
		int frameWidth;
		int frameHeight;
        int framesPerWorker;
        int framesTotal;
		ShortBuffer imageBuffer;
		ByteBuffer mask;
		ShortBuffer candidates;
		ShortBuffer counts;
        Boolean subtractLocalBg;
	}
	
	private class Worker implements Callable<Integer> {
		private int frameNr;
		private WorkerInfo info;
        private int framesThisWorker;
		
		public Worker(int frameNr, WorkerInfo info) {
			super();
			this.frameNr = frameNr;
			this.info = info;
		}

		@Override
		public Integer call() throws Exception {
            if(info.subtractLocalBg) {
                framesThisWorker = Math.min(info.framesPerWorker, info.framesTotal-frameNr);

//                System.out.println("frameNr: " + frameNr);
//                System.out.println("framesThisWorker: " + framesThisWorker);

                int orgFrameNr = frameNr;

                ShortProcessor spMean1 = new ShortProcessor(info.frameWidth,
                        info.frameHeight, getPixels(), null);
                RankFilters rankFilters = new RankFilters();
                rankFilters.rank(spMean1, 8, RankFilters.MEAN);

                frameNr = frameNr + framesThisWorker - 1;

                ShortProcessor spMean2 = new ShortProcessor(info.frameWidth,
                        info.frameHeight, getPixels(), null);
//                RankFilters rankFilters = new RankFilters();
                rankFilters.rank(spMean2, 8, RankFilters.MEAN);

                frameNr = orgFrameNr;


                for(int i=0; i<framesThisWorker; i++) {



                    findSpotsLocalBG(spMean1, spMean2);
                    frameNr++;
                }
                return 0;
            }
            else {
                return findSpots();
            }
		}
		
		private int img_index(int row, int column) {
//			return (info.frameHeight * info.frameWidth * frameNr) + ((column) * info.frameHeight) + (row);
			return (info.frameHeight * info.frameWidth * frameNr) + ((column) * info.frameWidth) + (row);
		}

        private short[] getPixels() {
            short[] pixels = new short[info.frameHeight*info.frameWidth];
            int i,j;
            int k=0;
            for(i=0; i<info.frameWidth; i++) {
                for(j=0; j<info.frameHeight; j++) {
                    pixels[k] = info.imageBuffer.get(img_index(j,i));
                    k++;
                }
            }
            return pixels;
        }

        private int findSpotsLocalBG(ShortProcessor spMean1, ShortProcessor spMean2) {
            int max_box_overlap = 2;
            int inner_box_radius = 2*info.boxRadius-max_box_overlap;
            int number_of_candidates = 0;

            int parameters_per_spot = 3; // TODO read from config

            ShortProcessor sp = new ShortProcessor(info.frameWidth,
                    info.frameHeight, getPixels(), null);

            int i,j;
            for (i=0; i < info.frameWidth; i++) {
                for (j=0; j < info.frameHeight; j++) {

                    sp.putPixel(i, j, sp.get(i,j) - (short)(0.5f * spMean1.get(i,j))
                        - (short)(0.5f * spMean2.get(i,j)));
                }
            }

//            int i,j;
            for (i=info.boxRadius; i < info.frameWidth - info.boxRadius; i++)
            {
                for (j=info.boxRadius; j < info.frameHeight - info.boxRadius; j++)
                {
                    int intensity = sp.get(i,j);

                    if(info.mask.get(img_index(i,j)) > 0) {continue;}

                    if(intensity > info.threshold)
                    {
                        if (intensity > sp.get(i+1,j))
                            if (intensity > sp.get(i-1,j+1))
                                if (intensity > sp.get(i,j+1))
                                    if (intensity > sp.get(i+1,j+1))
                                        if (intensity >= sp.get(i-1,j-1))
                                            if (intensity >= sp.get(i,j-1))
                                                if (intensity >= sp.get(i+1,j-1))
                                                    if (intensity >= sp.get(i-1,j))
                                                    {
                                                        if (number_of_candidates < info.maxSpotsPerFrame)
                                                        {

                                                            //int this_cand_index = cand_index(number_of_candidates);
                                                            int this_cand_index = frameNr * info.maxSpotsPerFrame * parameters_per_spot + number_of_candidates*parameters_per_spot;
                                                            info.candidates.put(this_cand_index+0, (short)i);
                                                            info.candidates.put(this_cand_index+1, (short)j);
                                                            info.candidates.put(this_cand_index+2, (short)frameNr);

                                                            int k,l;
                                                            for (k=i-inner_box_radius; k < i + inner_box_radius; k++) {
                                                                if(k<0) {continue;}
                                                                if(k>=info.frameHeight) {continue;}

                                                                for (l=j-inner_box_radius; l < j + inner_box_radius; l++)
                                                                {
                                                                    if(l<0) {continue;}
                                                                    if(l>=info.frameWidth) {continue;}

//																	int index = img_index(k,l);
//																	if(index>=info.mask.limit()) {
//																		System.out.println(index);
//																	}

                                                                    info.mask.put(img_index(k,l),(byte)1);

                                                                }
                                                            }

                                                            number_of_candidates++;
                                                            //printf("x=%d, y=%d, f=%d, i=%d\n",i,j,frame_nr,intensity);
                                                        }
                                                    }
                    } // end for intensity if
                } // end for col (j)
            } // end for row (i)

            info.counts.put(frameNr, (short)number_of_candidates);
            return number_of_candidates;
        }
		
		private int findSpots() {
			int max_box_overlap = 2;
			int inner_box_radius = 2*info.boxRadius-max_box_overlap;
			int number_of_candidates = 0;
//			int frame_nr = get_global_id(0);

//			#define img_index(row,column) (frame_height * frame_width * frame_nr) + ((column) * frame_height) + (row)
			
			int parameters_per_spot = 3; // TODO read from config
//			int frame_spot_offset = frame_nr * info.max_spots_per_frame * parameters_per_spot;
			
//			#define cand_index(candidate_nr) frame_spot_offset + (candidate_nr * parameters_per_spot)
			
			int i,j;
			for (i=info.boxRadius; i < info.frameWidth - info.boxRadius; i++)
			{
				for (j=info.boxRadius; j < info.frameHeight - info.boxRadius; j++)
				{
					int intensity = info.imageBuffer.get(img_index(i,j));
					
					if(info.mask.get(img_index(i,j)) > 0) {continue;}
					
					if(intensity > info.threshold)
					{
                        if (intensity > (info.imageBuffer.get(img_index(i+1,j))))
                            if (intensity > (info.imageBuffer.get(img_index(i-1,j+1))))
                                if (intensity > (info.imageBuffer.get(img_index(i,j+1))))
                                    if (intensity > (info.imageBuffer.get(img_index(i+1,j+1))))
                                        if (intensity >= (info.imageBuffer.get(img_index(i-1,j-1))))
                                            if (intensity >= (info.imageBuffer.get(img_index(i,j-1))))
                                                if (intensity >= (info.imageBuffer.get(img_index(i+1,j-1))))
                                                    if (intensity >= (info.imageBuffer.get(img_index(i-1,j))))
													{
														if (number_of_candidates < info.maxSpotsPerFrame)
														{
																	
															//int this_cand_index = cand_index(number_of_candidates);
															int this_cand_index = frameNr * info.maxSpotsPerFrame * parameters_per_spot + number_of_candidates*parameters_per_spot;
															info.candidates.put(this_cand_index+0, (short)i);
															info.candidates.put(this_cand_index+1, (short)j);
															info.candidates.put(this_cand_index+2, (short)frameNr);
															
															int k,l;
															for (k=i-inner_box_radius; k < i + inner_box_radius; k++) {
																if(k<0) {continue;}
																if(k>=info.frameHeight) {continue;}
																
																for (l=j-inner_box_radius; l < j + inner_box_radius; l++)
																{
																	if(l<0) {continue;}
																	if(l>=info.frameWidth) {continue;}
																	
//																	int index = img_index(k,l);
//																	if(index>=info.mask.limit()) {
//																		System.out.println(index);
//																	}
																	
																	info.mask.put(img_index(k,l),(byte)1);
																	
																}
															}
															
															number_of_candidates++;
															//printf("x=%d, y=%d, f=%d, i=%d\n",i,j,frame_nr,intensity);
														}
													}
					} // end for intensity if
				} // end for col (j)
			} // end for row (i)
			
			info.counts.put(frameNr, (short)number_of_candidates);
			return number_of_candidates;
		}
	}
	
	
	private short[][] Buff2Mat(ShortBuffer buffer, int jj){
		int ii = buffer.capacity()/jj;
		short[][] matrix = new short[ii][jj];
		for (int i=0; i<ii; i++){
			for (int j=0; j<jj; j++){
				matrix[i][j] = buffer.get(); 
			}
		}
		
		return matrix;	
		
	}

}
