package eu.brede.graspj.datatypes.spot;

import java.util.Collection;

public class TrailSpot extends SimpleSpot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int trailNr;
	private String acqName;

	public TrailSpot(float[] spotArray, int frameNr, int trailNr,
			String acqName, int spotNr) {
		super(spotArray, frameNr, spotNr);
		this.trailNr = trailNr;
		this.acqName = acqName;
	}

	public TrailSpot(TrailSpot spot) {
		super(spot);
		this.trailNr = spot.getTrailNr();
		this.acqName = spot.getAcqName();
	}

	public int getTrailNr() {
		return trailNr;
	}

	public String getAcqName() {
		return acqName;
	}

	@Override
	public Collection<String> toStringCollection() {
		// Collection<String> strCollection = new ArrayList<String>();
		//
		// strCollection.add(String.valueOf(getTrailNr()));
		//
		// strCollection.addAll(super.toStringCollection());

		Collection<String> strCollection = super.toStringCollection();
		strCollection.add(String.valueOf(getTrailNr()));
		strCollection.add(getAcqName());

		return strCollection;
	}

}
