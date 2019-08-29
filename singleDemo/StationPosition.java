package uk.ac.nott.cs.g53dia.demo;

import uk.ac.nott.cs.g53dia.library.Station;

/**
 * This class stores the position and a Station object
 * the station objects need to be checked every iteration
 * for a newly generated task
 */

public class StationPosition{

	public int x;
	public int y;
	public Station station;

	public StationPosition(PositionTuple pos, Station station) {
			this.x = pos.x;
			this.y = pos.y;
			this.station = station;
	}
}
