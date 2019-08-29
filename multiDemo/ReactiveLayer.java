package uk.ac.nott.cs.g53dia.multidemo;

import java.util.ArrayList;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.FuelPump;
import uk.ac.nott.cs.g53dia.multilibrary.Well;

/**
 * The reactive layer uses a subsumption architecture to select what action to
 * output The lowest level is to forage and look around for tasks and wells The
 * next stage is to find a fuel station to refuel if needed.
 * 
 * @return
 */
public class ReactiveLayer {

	private final int REFUEL = 8;
	private final int DUMP = 10;

	private boolean refuel = false;

	private int forageDir = -1;
	protected int finalDir;

	// Constructor
	public ReactiveLayer() {

	}

	/**
	 * Determine if the tanker needs to refuel
	 * 
	 * @param tanker
	 */
	public void refuel(Cell view, MainTanker tank) {

		// If the tanker has less than half fuel or currently on a fuel pump go refuel
		if ((tank.getFuelLevel() <= MainTanker.MAX_FUEL / 2 + 4)
				|| view instanceof FuelPump && tank.getFuelLevel() < MainTanker.MAX_FUEL) {
			forageDir = -1;
			refuel = true;
		} else {
			refuel = false;
		}
	}

	/**
	 * This function determines what direction to go in when wondering It uses a fan
	 * pattern to cover the most area with the least amount of fuel
	 */
	public void forage() {

		if(forageDir == -1) {
			forageDir = (int) (Math.random() * 7 + 1);
			finalDir = forageDir;
		}else {
			finalDir = forageDir;
		}
		

	}

	/**
	 * This function runs all the functions in the reactive layer and decides what
	 * action it should output
	 */
	public int getAction(Cell currentView, MainTanker tank) {

		// Run layers
		refuel(currentView, tank);
		forage();

		// If the tanker needs to refuel
		if (refuel == true) {
			return REFUEL;

		} else if (tank.getWasteLevel() > 0) {

			if (currentView instanceof Well) {
				return DUMP;
			} else {
				return tank.model.locationDirection(tank.model.findClosestWell(tank.tankerPos), tank.tankerPos);
			}

		} else {
			return finalDir;
		}

	}
}
