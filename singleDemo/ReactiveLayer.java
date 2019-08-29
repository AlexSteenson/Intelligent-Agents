package uk.ac.nott.cs.g53dia.demo;

import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.FuelPump;

/**
 * The reactive layer uses a subsumption architecture to select what action to
 * output The lowest level is to forage and look around for tasks and wells The
 * next stage is to find a fuel station to refuel if needed.
 * 
 * @return
 */
public class ReactiveLayer {
	
	private final int REFUEL = 8;

	private boolean refuel = false;
	
	private int directions[][] = { { 7, 5, 4, 6 }, { 5, 4, 6, 7 }, { 4, 6, 7, 5 }, { 6, 7, 5, 4 } };
	protected int finalDir;
	private int steps = 20;
	private int side = 0;
	private int dir = 0;
	private int round = 0;

	// Constructor
	public ReactiveLayer() {

	}

	/**
	 * Determine if the tanker needs to refuel
	 * 
	 * @param tanker
	 */
	public void refuel(Cell view) {

		//If the tanker has less than half fuel or currently on a fuel pump go refuel
		if ((DemoSimulator.tank.getFuelLevel() <= MainTanker.MAX_FUEL / 2)
				|| view instanceof FuelPump && DemoSimulator.tank.getFuelLevel() < MainTanker.MAX_FUEL) {
			refuel = true;
		} else {
			refuel = false;
		}
	}

	public void resetForage() {
		round = 0;
		side = 0;
	}

	/**
	 * This function determines what direction to go in when wondering It uses a fan
	 * pattern to cover the most area with the least amount of fuel
	 */
	public void forage() {

		// haven't reach the number of steps to turn
		if (round <= steps) {
			finalDir = directions[dir][side];
			round++;
		} else {

			if (side != 3) {
				side++;
			} else {
				side = 0;
				if (dir != 3) {
					dir++;
				} else {
					dir = 0;
				}
			}

			round = 1;
			finalDir = directions[dir][side];
		} // end else
	}

	/**
	 * This function runs all the functions in the reactive layer and
	 * decides what action it should output
	 */
	public int getAction(Cell currentView) {

		// Run layers
		refuel(currentView);
		forage();

		// If the tanker needs to refuel
		if (refuel == true) {
			return REFUEL;

		}else {
			return finalDir;
		}

	}
}
