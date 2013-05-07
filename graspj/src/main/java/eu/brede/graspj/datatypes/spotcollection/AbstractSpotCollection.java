package eu.brede.graspj.datatypes.spotcollection;

import java.util.Iterator;

import eu.brede.graspj.datatypes.spot.Spot;


public abstract class AbstractSpotCollection implements SpotCollection {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public class SpotCollectionIterator implements Iterator<Spot> {
		
		private final class SpotID {
			public int frameNr;
			public int spotNr;
			public SpotID(int frameNr, int spotNr) {
				this.frameNr = frameNr;
				this.spotNr = spotNr;
			}
			public SpotID(SpotID spotID) {
				this.frameNr = spotID.frameNr;
				this.spotNr = spotID.spotNr;
			}
		}
		
		private SpotID currentSpotID;
		
		SpotCollectionIterator() {
			currentSpotID = new SpotID(0,-1);
		}

		@Override
		public boolean hasNext() {
			return exists(getNextSpotID());
		}

		@Override
		public Spot next() {
			currentSpotID = getNextSpotID();
			return getSpot(currentSpotID.frameNr,currentSpotID.spotNr);
		}
		
		private SpotID getNextSpotID() {
			return getNextSpotIDrecursive(new SpotID(currentSpotID));
		}
		
		private SpotID getNextSpotIDrecursive (SpotID currentSpotID) { //TODO: confusing naming? currentSpotID is not the one from the class
			currentSpotID.spotNr++;
			if (!exists(currentSpotID) && currentSpotID.frameNr < getFrameCount()) {
				currentSpotID.frameNr++;
				currentSpotID.spotNr = -1;
				return getNextSpotIDrecursive(currentSpotID);
			}
			else {
				return currentSpotID;
			}
		}
		
		private boolean exists(SpotID spotID) {
			if (spotID.frameNr >= getFrameCount()) {
				return false;
			}
			return (spotID.spotNr < getSpotCount(spotID.frameNr));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public Iterator<Spot> iterator() {
		return new SpotCollectionIterator();
	}

}
