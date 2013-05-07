package eu.brede.graspj.pipeline.processors.filterer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import eu.brede.graspj.utils.Buffers;

import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.filtermask.FilterMask;
import eu.brede.graspj.datatypes.spotcollection.BufferSpotCollection;
import eu.brede.graspj.pipeline.processors.AbstractAIProcessor;


public abstract class SpotFilterer extends AbstractAIProcessor {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void process(AnalysisItem item) {
		FilterMask newMask = calcMask(item);
		// don't make changes to the original mask to keep compatibility
		// with shallow copies of AnalysisItems
		FilterMask oldMask = item.getMask();
		
		getConfig().ensureCompliance();
		ObjectChoice<String> combinationMode = getConfig().gett("combinationMode");
		
		item.setMask(newMask);
		switch(combinationMode.getChosen()) {
			case "multiply":
				item.getMask().multiply(oldMask);
				break;
			case "override":
				break;
		}

		
		if(getConfig().gett("applyMask")) {
			applyMaskBinary(item);
		}
//		applyMaskBinary(item);
	}
	
	protected abstract FilterMask calcMask(AnalysisItem item);
	
	public void applyMaskBinary(AnalysisItem item) {
		BufferSpotCollection spotCollection = item.getSpots();
		IntBuffer frameNrs = spotCollection.getFrameNrs().getBuffer();
		FloatBuffer spotsBuffer = spotCollection.getSpots().getBuffer();
		int floatsPerSpot = 10; // TODO read from spotColl

		// using a short array makes sure it's initialized to 0!
		ShortBuffer newCounts = Buffers
			.newDirectShortBuffer(new short[spotCollection.getFrameCount()]);
		
		int positiveEntries = item.getMask().countPositives();
		
		FloatBuffer newSpotsBuffer = Buffers
			.newDirectFloatBuffer(positiveEntries*floatsPerSpot);
		ByteBuffer maskBuffer = 
				Buffers.newDirectByteBuffer(positiveEntries);
		
		
		
		for(int spotNr=0; spotNr<item.getMask().getBuffer().limit(); spotNr++) {
			byte maskEntry = item.getMask().getBuffer().get(spotNr);
			if(maskEntry>0) {
				maskBuffer.put(maskEntry);
				spotsBuffer.position(spotNr*floatsPerSpot);
				for(int i=0;i<floatsPerSpot;i++) {
					newSpotsBuffer.put(spotsBuffer.get());
				}
				int frameNr = frameNrs.get(spotNr);
				//newCounts.position(frameNr);
				newCounts.put(frameNr,(short)(newCounts.get(frameNr)+1));
			}
		}
		newSpotsBuffer.rewind();
		newCounts.rewind();
		maskBuffer.rewind();
		item.setSpots(
				new BufferSpotCollection(newCounts,newSpotsBuffer));
		item.getMask().setBuffer(maskBuffer);

		// TODO find right way to invalidate;
//		item.setDrift(null);
//		item.setMask(null);

		return;
	}
}
