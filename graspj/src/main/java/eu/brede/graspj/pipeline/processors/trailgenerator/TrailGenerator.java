package eu.brede.graspj.pipeline.processors.trailgenerator;

import static eu.brede.common.util.MathTools.combineSigmas;
import static eu.brede.common.util.MathTools.weightedAverage;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brede.graspj.utils.Buffers;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.trail.TrailConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.cycle.Cycle;
import eu.brede.graspj.datatypes.spotcollection.BufferSpotCollection;
import eu.brede.graspj.pipeline.processors.AbstractAIProcessor;

// TODO: add maxTrailLength feature

public class TrailGenerator extends AbstractAIProcessor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int fitDimension = 5;
//	int locationTolerance = 6; // in sigmas
//	int frameSkipTolerance = 2;
	private TrailConfig config;
	final static Logger logger = LoggerFactory.getLogger(TrailGenerator.class);

	public TrailGenerator() {
		super();
		config = new TrailConfig();
	}

	// TODO make it conserve drift, done?
	
	@Override
	public void process(AnalysisItem item) {
		
		// in sigmas:
//		int locationTolerance = getConfig().getInt("locationTolerance");
		float locationToleranceNM = getConfig().getFloat("locationToleranceNM");
		int frameSkipTolerance = getConfig().getInt("frameSkipTolerance");
		
//		long startTime = System.currentTimeMillis();
		BufferSpotCollection spotCollection = item.getSpots();
		Cycle cycle = (Cycle) item.getAcquisitionConfig().get("frameCycle");
		int numSpots = spotCollection.getSpotCount();
		int numFrames = spotCollection.getFrameCount();
		IntBuffer frameNrs = spotCollection.getFrameNrs().getBuffer();

		IntBuffer aggCounts = spotCollection.getCuCounts().getBuffer();
		int floatsPerSpot = fitDimension*2; // TODO needs to go to spotCollection

		// using a short array makes sure it's initialized to 0!
		ShortBuffer newCounts = Buffers
			.newDirectShortBuffer(new short[numFrames]);
		
		ByteBuffer alreadyUsed = Buffers
			.newDirectByteBuffer(new byte[numSpots]);
		
		FloatBuffer newSpotsBuffer = Buffers
			.newDirectFloatBuffer(numSpots*floatsPerSpot);
		
		newSpotsBuffer.limit(0);
		
		ByteBuffer newMaskBuffer = Buffers.newDirectByteBuffer(numSpots);
		newMaskBuffer.limit(0);
		
//		int tenth = numSpots/10;
//		System.out.print("Trail gen.: [");
		int connCount = 0;
		for(int spotNr=0; spotNr<numSpots; spotNr++) {
//			if((spotNr%tenth)==0) {
//				System.out.print("=");
//			}
			if(item.getMask().getBuffer().get(spotNr)==0) {
				continue;
			}
			if(alreadyUsed.get(spotNr)==0) {
				float[] curTrail = spotCollection.getSpotArray(spotNr);
				int curFrameNr = frameNrs.get(spotNr);
//				boolean inPrevFrame = true;
				int numFramesNotSeen = 0;
				for(int frameNr=curFrameNr+1; frameNr<numFrames; frameNr++) {
				
					// if this frame is an activation frame, ignore it
					// (makes trails over activation frames possible)
					if(cycle.isActivation(frameNr)) {
						continue;
					}
//					if(inPrevFrame) {
					if(numFramesNotSeen > frameSkipTolerance) {
						break; // stops search through frames for this trail
					}
					else {
//						inPrevFrame = false;
						numFramesNotSeen++;
						for(int localSpotNr=aggCounts.get(frameNr-1);
								localSpotNr<aggCounts.get(frameNr);
								localSpotNr++) {
							if(item.getMask().getBuffer().get(localSpotNr)==0) {
								continue;
							}
							float[] localSpotArray = spotCollection
								.getSpotArray(localSpotNr);
							
							
//							if(areWithinNsigma(curTrail,localSpotArray,locationTolerance)) {
							if(areWithinNM(curTrail,localSpotArray,locationToleranceNM)) {
								connCount++;
								curTrail = combineTrails(curTrail,localSpotArray);
								alreadyUsed.put(localSpotNr, (byte) 1);
//								inPrevFrame = true;
								numFramesNotSeen = 0;
								break; // stops search through spots of this frame
							}
						}
					}
//					else {
//						break; // stops search through frames for this trail
//					}
				}
				// increase limit of newSpotsBuffer to take new trail mask value
				newMaskBuffer.limit(newMaskBuffer.limit()+1);
				// write mask value to the new Mask
				newMaskBuffer.put(item.getMask().getBuffer().get(spotNr));
				// increase limit of newSpotsBuffer to take new trail
				newSpotsBuffer.limit(newSpotsBuffer.limit()+curTrail.length);
				// write trail to newSpotsBuffer
				newSpotsBuffer.put(curTrail);
				// increase spotCount for current frame
				newCounts.put(curFrameNr,(short)(newCounts.get(curFrameNr)+1));
			}
		}
		logger.info("connections made: {}",connCount);
		
		newSpotsBuffer.rewind();
		newCounts.rewind();
		item.setSpots(
				new BufferSpotCollection(newCounts,newSpotsBuffer));
		
		newMaskBuffer.rewind();
		
		// TODO how to invalidate the right way?
//		item.getDrift().free();
		item.getMask().free();
		
		item.getMask().setBuffer(newMaskBuffer);
		
//		System.out.println("Total Trailgeneration: " + (System.currentTimeMillis()-startTime) + "ms");
    	return;
	}

	private float[] combineTrails(float[] curTrail, float[] spot) {
		float[] newTrail = new float[curTrail.length];
		float curN = curTrail[3];
		float spotN = spot[3];
		int j;
		for(int i=0;i<fitDimension;i++) {
			j = i+fitDimension; // j is index for sigma of value(i)
			
			// i=3 is Intensity which is taken as N (not perfect) and therefore needs
			// to be combined differently
			if(i==3) {
				newTrail[i] = curN+spotN;
				newTrail[j] = weightedAverage(curTrail[j],curN,spot[j],spotN);
			}
			else {				
				newTrail[i] = weightedAverage(curTrail[i],curN,spot[i],spotN);
				newTrail[j] = combineSigmas(curTrail[j],curTrail[i],curN,
										spot[j],spot[i],spotN);
			}
		}
		return newTrail;
	}

	private boolean areWithinNsigma(float[] curTrail, float[] newSpot, int numSigmas) {
		for(int i=0;i<2;i++) {
			if(Math.abs(curTrail[i]-newSpot[i])>numSigmas*newSpot[fitDimension+i]) {
				return false;
			}
		}
		return true;
	}
	
	private boolean areWithinNM(float[] curTrail, float[] newSpot, float locationToleranceNM) {
		for(int i=0;i<2;i++) {
			if(Math.abs(curTrail[i]-newSpot[i])>locationToleranceNM) {
				return false;
			}
		}
		return true;
	}

	@Override
	public TrailConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = (TrailConfig) config; // TODO wrap instead"
	}

	
}
