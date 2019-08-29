package uk.ac.nott.cs.g53dia.multidemo;

/**
 * This class stores a position coordinate pair
 *
 */
public class PositionTuple {

	public int x;
	public int y;
	
	public PositionTuple() {
		
	}

	public PositionTuple(int x, int y) {
			this.x = x;
			this.y = y;
	}
	
	public boolean equals(PositionTuple pos) {
		
		if(this.x == pos.x && this.y == pos.y) {
			return true;
		}
		
		return false;
	}

}
