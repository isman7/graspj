package eu.brede.graspj.datatypes.spotcollection;

import java.util.ArrayList;
import java.util.Collection;

import eu.brede.common.io.TxtExportable;
import eu.brede.graspj.datatypes.spot.TrailSpot;

public class TrailSpotTable extends ArrayList<TrailSpot> implements TxtExportable<TrailSpot> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Collection<String> toStringCollection() {
		// x,sx,y,sy,z,sz,I,sI,B,sB,frameNr,trailNr
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
		outputStringCollection.add("trailNr");
		outputStringCollection.add("acqName");
		return outputStringCollection;
	}

}
