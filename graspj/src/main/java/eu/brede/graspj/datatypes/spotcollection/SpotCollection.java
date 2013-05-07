package eu.brede.graspj.datatypes.spotcollection;

import java.io.Serializable;

import eu.brede.common.io.TxtExportable;
import eu.brede.graspj.datatypes.spot.Spot;


public interface SpotCollection extends TxtExportable<Spot>, Serializable
{
	Spot getSpot(int frameNr, int spotNr);
//	void setSpot(int frameNr, int spotNr, Spot spot);
	int getSpotCount(int frameNr);
	int getFrameCount();
}
