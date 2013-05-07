package eu.brede.graspj.datatypes.spotcollection;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collection;


import eu.brede.graspj.utils.Buffers;

import eu.brede.common.util.MathTools;
import eu.brede.common.util.SimpleLock;
import eu.brede.graspj.datatypes.bufferholder.BufferHolder;
import eu.brede.graspj.datatypes.spot.SimpleSpot;
import eu.brede.graspj.datatypes.spot.Spot;

/**
 * @author Norman
 *
 */
public class BufferSpotCollection extends AbstractSpotCollection implements Serializable { // check for direct!
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// spots buffer format: x,y,z,I,B,sx,sy,sz,sI,sB
	protected BufferHolder<FloatBuffer> spots = new BufferHolder<FloatBuffer>(); 
	protected BufferHolder<ShortBuffer> counts = new BufferHolder<ShortBuffer>();
	protected BufferHolder<IntBuffer> cuCounts = new BufferHolder<IntBuffer>();
	protected BufferHolder<IntBuffer> frameNrs = new BufferHolder<IntBuffer>();
	
	protected SimpleLock lock = new SimpleLock();

	private int fitDimension=5;
	private int varsPerDimension=2;

	private BufferHolder<?>[] getBuffers() {
		return new BufferHolder[] {spots,counts,cuCounts,frameNrs}; // order important!
	}
	
	public BufferSpotCollection() {
		// TODO if I do that, getSpotCount makes no sense anymore, is bad for item/spotCollection building
//		spots.setBuffer(Buffers.newDirectFloatBuffer(0));
	}
	
	public class SplitPoints {
		final public ArrayList<Integer> spotsSplitPoints;
		final public ArrayList<Integer> framesSplitPoints;
		final public int numPackages;
		
		public SplitPoints(ArrayList<Integer> spotsSplitPoints,
				ArrayList<Integer> framesSplitPoints, int numPackages) {
			super();
			this.spotsSplitPoints = spotsSplitPoints;
			this.framesSplitPoints = framesSplitPoints;
			this.numPackages = numPackages;
		}
		
		
	}
	
	public SplitPoints getSplitPoints(int framesPerPackage) {
		int numPackages = (int)Math.ceil((double)getFrameCount()/framesPerPackage);
		ArrayList<Integer> spotsSplitPoints = new ArrayList<>();
		ArrayList<Integer> countsSplitPoints = new ArrayList<>();
		// because I only need the split points I start a packageNr 1.
		for(int packageNr=1; packageNr<numPackages; packageNr++) {
			// max not necessary as I only need the start of the last package (==end of previous)
//			int firstFrame = packageNr*framesPerPackage; 
//			int lastFrame = Math.max(firstFrame+framesPerPackage, getFrameCount()-1);
			int firstFrame = packageNr*framesPerPackage;
			
			countsSplitPoints.add(firstFrame);
			
			int spotSplitPoint = getCuCounts().getBuffer().get(firstFrame);
//					*fitDimension*varsPerDimension;
			spotsSplitPoints.add(spotSplitPoint);
		}
		return new SplitPoints(spotsSplitPoints, countsSplitPoints, numPackages);
	}
	
	public ArrayList<BufferSpotCollection> splitByFrames(int framesPerPackage) {
		SplitPoints splitPoints = getSplitPoints(framesPerPackage);
		
		ArrayList<BufferHolder<ShortBuffer>> countsBuffers = 
				counts.split(splitPoints.framesSplitPoints);
		
		ArrayList<BufferHolder<FloatBuffer>> spotsBuffers = 
				spots.split(MathTools.multiplyAllElements(
						splitPoints.spotsSplitPoints, fitDimension*varsPerDimension));
		
		ArrayList<BufferSpotCollection> spotCollections = new ArrayList<>();
		for(int packageNr=0; packageNr<splitPoints.numPackages; packageNr++) {
			spotCollections.add(
				new BufferSpotCollection(
						countsBuffers.get(packageNr).getBuffer(),
						spotsBuffers.get(packageNr).getBuffer()));
		}
		
		return spotCollections;
	}
	
	public BufferSpotCollection(int initialSpotCapacity, int initialFrameCapacity) {
		// TODO replace 2 with var (varsPerDimension)
		spots.setBuffer(Buffers.newDirectFloatBuffer(initialSpotCapacity*fitDimension
				*varsPerDimension));
		spots.getBuffer().limit(0);
		counts.setBuffer(Buffers.newDirectShortBuffer(initialFrameCapacity));
		counts.getBuffer().limit(0);
	}
	
	public BufferSpotCollection(ShortBuffer counts, FloatBuffer spots) {
		counts.rewind();
		spots.rewind();
		this.counts.setBuffer(counts.isDirect()?counts:Buffers.copyShortBuffer(counts));
		this.spots.setBuffer(spots.isDirect()?spots:Buffers.copyFloatBuffer(spots));
	}
		
	public BufferSpotCollection(FloatBuffer spots, IntBuffer cuCounts) {
		cuCounts.rewind();
		spots.rewind();
		this.cuCounts.setBuffer(cuCounts.isDirect()?cuCounts:
			Buffers.copyIntBuffer(cuCounts));
		this.spots.setBuffer(spots.isDirect()?spots:Buffers.copyFloatBuffer(spots));
	}
	
	public BufferSpotCollection(ShortBuffer counts, FloatBuffer spots, IntBuffer cuCounts) {
		this(counts,spots);
		cuCounts.rewind();
		this.cuCounts.setBuffer(cuCounts.isDirect()?cuCounts:
			Buffers.copyIntBuffer(cuCounts));
	}
	

	public BufferSpotCollection(BufferSpotCollection bsc) {
//		bufferSpotCollection.rewindAllBuffers();
//		spots.setBuffer(
//				Buffers.copyFloatBuffer(bufferSpotCollection.getSpots().getBuffer()));
//		counts.setBuffer(
//				Buffers.copyShortBuffer(bufferSpotCollection.getCounts().getBuffer()));
//		cuCounts.setBuffer(
//				Buffers.copyIntBuffer(bufferSpotCollection.getCuCounts().getBuffer()));
//		frameNrs.setBuffer(
//				Buffers.copyIntBuffer(bufferSpotCollection.getFrameNrs().getBuffer()));
		this();
		
		spots = new BufferHolder<>(bsc.getSpots());
		counts = new BufferHolder<>(bsc.getCounts());
		// TODO remove those two lines: ?
		cuCounts = new BufferHolder<>(bsc.getCuCounts());
		frameNrs = new BufferHolder<>(bsc.getFrameNrs());
		
	}
	
	public void rewindAllBuffers() {
		for(BufferHolder<?> bufferHolder:getBuffers()) {
			if(bufferHolder.getBuffer()!=null) {
				bufferHolder.getBuffer().rewind();
			}
		}
	}
	
	public void appendSpotCollection(BufferSpotCollection spotCollection) {
		
		spots.append(spotCollection.getSpots());
		counts.append(spotCollection.getCounts());
		
		// these buffers are no longer valid. Will be recreated if needed.
		frameNrs.removeBuffer();
		cuCounts.removeBuffer();
		return;
	}

	@Override
	public Spot getSpot(int frameNr, int localSpotNr) {
		int index=getSpotIndex(frameNr,localSpotNr);
		int spotNr=getSpotNr(frameNr,localSpotNr);
		float[] values = new float[fitDimension];
		float[] uncertainties = new float[fitDimension];
		spots.getBuffer().position(index);
		spots.getBuffer().get(values, 0, fitDimension);
		spots.getBuffer().get(uncertainties, 0, fitDimension);
		return new SimpleSpot(values, uncertainties, frameNr, spotNr);
	}
	
	public float[] getSpotArray(int spotNr) {
		// TODO replace 2 by config lookup (vars per dim)?
		int floatsPerSpot = fitDimension*varsPerDimension;
		float[] spotArray = new float[floatsPerSpot];
		spots.getBuffer().position(floatsPerSpot*spotNr);
		spots.getBuffer().get(spotArray);
		return spotArray;
	}
	
	public int getSpotCount() {
		if(getSpots().holdsBuffer()) {
			return getSpots().getBuffer().limit()/(fitDimension*varsPerDimension);			
		}
		else if(getFrameCount()>0) {
			return getCuCounts().getBuffer().get(getFrameCount()-1);
		}
		else {
			return 0;
		}
		
		// TODO below is old implementation, think about new one!
//		return getAggregatedCounts().get(getFrameCount()-1);
	}
	@Override
	public int getSpotCount(int frameNr) {
		return getCounts().getBuffer().get(frameNr);
	}
	@Override
	public int getFrameCount() {
		if(counts.getBuffer()!=null) {
			return counts.getBuffer().limit();
		}
		else if(cuCounts.getBuffer()!=null) {
			return cuCounts.getBuffer().limit();
		}
		else {
			return 0;
		}
	}
	
	public int getSpotIndex(int frameNr, int spotNrInFrame) {
//		//TODO *2 because two entries per dimension (value, uncertainty)
//		int numSpotsBeforeFrame = frameNr>0?getCuCounts().getBuffer().get(frameNr-1):0;
//		// TODO could be replaced with try catch OutOfBounds for not checking all the time!
//		return (numSpotsBeforeFrame+spotNrInFrame)*fitDimension*2;
		return getSpotNr(frameNr, spotNrInFrame)*fitDimension*varsPerDimension;
	}
	
	public int getSpotNr(int frameNr, int spotNrInFrame) {
		int numSpotsBeforeFrame = frameNr>0?getCuCounts().getBuffer().get(frameNr-1):0;
		return (numSpotsBeforeFrame+spotNrInFrame);
	}


	@Override
	public Collection<String> toStringCollection() {
		// x,sx,y,sy,z,sz,I,sI,B,sB,frameNr
		ArrayList<String> outputStringCollection = new  ArrayList<String>();
		outputStringCollection.add("x");
		outputStringCollection.add("sx");
		outputStringCollection.add("y");
		outputStringCollection.add("sy");
		outputStringCollection.add("z");
		outputStringCollection.add("sz");
		outputStringCollection.add("I");
		outputStringCollection.add("sI");
		outputStringCollection.add("B");
		outputStringCollection.add("sB");
		outputStringCollection.add("frameNr");
		return outputStringCollection;
	}
	
	private void updateCounts() {
		counts.getBuffer().put(0, (short)cuCounts.getBuffer().get(0));
		int frameCount=getFrameCount();
		for(int frameNr=1;frameNr<frameCount;frameNr++) {
			counts.getBuffer().put(frameNr,
					(short) ((cuCounts.getBuffer().get(frameNr) 
							- cuCounts.getBuffer().get(frameNr-1))));
		}
	}
	
	private void updateAggregatedCounts() {
		cuCounts.getBuffer().put(0,counts.getBuffer().get(0));
		int frameCount=getFrameCount();
		for(int frameNr=1;frameNr<frameCount;frameNr++) {
			cuCounts.getBuffer().put(frameNr,
					cuCounts.getBuffer().get(frameNr-1) + counts.getBuffer().get(frameNr));
		}
	}
	
	public static IntBuffer calculateAggregatedCounts(ShortBuffer counts) {
		int frameCount=counts.limit();
		IntBuffer aggregatedCounts = IntBuffer.allocate(frameCount);
		aggregatedCounts.put(0,counts.get(0));
		for(int frameNr=1;frameNr<frameCount;frameNr++) {
			aggregatedCounts.put(frameNr,
					aggregatedCounts.get(frameNr-1) + counts.get(frameNr));
		}
		return aggregatedCounts;
	}

//	private void updateSpotsFrameNrs() {
//		ShortBuffer counts = this.getCounts();
//		int frameCount=counts.limit();
//		spotsFrameNrs = IntBuffer(frameCount);
//		for(int frameNr=1;frameNr<frameCount;frameNr++) {
//			
//		}
//	}
	
	public BufferHolder<FloatBuffer> getSpots() {
		return spots;
	}

	public BufferHolder<ShortBuffer> getCounts() {
		if(!counts.holdsBuffer()) {
			if(cuCounts.holdsBuffer()) {
				counts.setBuffer(Buffers.newDirectShortBuffer(getFrameCount()));
				updateCounts();				
			}
		}
		return counts;
	}

	public BufferHolder<IntBuffer> getCuCounts() {
		if(!cuCounts.holdsBuffer()) {
			cuCounts.setBuffer(Buffers.newDirectIntBuffer(getFrameCount()));
			updateAggregatedCounts();
		}
		return cuCounts;
	}
	
	public BufferHolder<IntBuffer> getFrameNrs() {
		if(!frameNrs.holdsBuffer()) {
			frameNrs.setBuffer(Buffers.newDirectIntBuffer(getSpotCount()));
			updateFrameNrs();
		}
		return frameNrs;
	}

	protected void updateFrameNrs() {
		frameNrs.getBuffer().rewind();
		for(int frameNr=0;frameNr<getFrameCount();frameNr++) {
			for(int localSpotNr=0;
				localSpotNr<getCounts().getBuffer().get(frameNr);localSpotNr++) {
				frameNrs.getBuffer().put(frameNr);
			}
		}
		frameNrs.getBuffer().rewind();
	}
	
	public void free() {
		for(BufferHolder<?> bufferHolder:getBuffers()) {
			bufferHolder.free();
		}
	}

	public void lock() {
		lock.lock();
		for(BufferHolder<?> bufferHolder:getBuffers()) {
			bufferHolder.lock();
		}
	}
	
	public void unlock() {
		lock.unlock();
		for(BufferHolder<?> bufferHolder:getBuffers()) {
			bufferHolder.unlock();
		}
	}
	

}
