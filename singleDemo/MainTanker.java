package uk.ac.nott.cs.g53dia.demo;

import java.util.Random;

import uk.ac.nott.cs.g53dia.library.Action;
import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.DisposeWasteAction;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.LoadWasteAction;
import uk.ac.nott.cs.g53dia.library.MoveAction;
import uk.ac.nott.cs.g53dia.library.RefuelAction;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Tanker;

/**
 * 
 * This class is the tanker object. It detects if its on an object and
 * what action it should do based on that
 *
 */
public class MainTanker extends Tanker {

	public PositionTuple tankerPos;
	public int dir;
	private final int REFUEL = 8;
	private final int TASK = 9;
	private final int DUMP = 10;
	public int closest;

	private final int NORTH = 0, SOUTH = 1, EAST = 2, WEST = 3, NORTHEAST = 4, NORTHWEST = 5, SOUTHEAST = 6,
			SOUTHWEST = 7;

	public MainTanker(Random r) {
		tankerPos = new PositionTuple(0, 0);
		this.r = r;
		this.closest = -1;
	}

	/**
	 * Gets the tankers fuel level
	 * @return fuel level
	 */
	public int fuelLevel() {
		return this.getFuelLevel();

	}

	/**
	 * The tanker receives an action and senses what object it is currently on and what action it should do
	 * e.g. If the tanker is on a fuel pump it should refuel
	 */
	@Override
	public Action senseAndAct(Cell[][] view, long timestep) {
		
		Cell currentCell = getCurrentCell(view);
		int action = DemoSimulator.controlSystem.decideAction(currentCell);

		if (action == REFUEL) {
			if (currentCell instanceof FuelPump) {
				dir = REFUEL;
				return new RefuelAction();
			}
			PositionTuple closest_fp = DemoSimulator.model.findClosestFuelPump(tankerPos);
			dir = DemoSimulator.model.locationDirection(closest_fp);
			return new MoveAction(dir);

		} else if (action == DUMP) {
			dir = DUMP;
			return new DisposeWasteAction();

		} else if (action == TASK) {
			dir = TASK;
			Station station = (Station) currentCell;

			for (TaskPosition tsk : DemoSimulator.model.task) {
				if (tsk.task.equals(station.getTask())) {
					int amount = station.getTask().getWasteAmount();
					if (amount <= MainTanker.MAX_WASTE)
						tsk.completed = true;
					
					break;
				}
			}
			DemoSimulator.numTasks++;
			return new LoadWasteAction(station.getTask());
		} else {
			dir = action;
			return new MoveAction(dir);
		}

	}

	public PositionTuple updatePos(int movement, PositionTuple pos) {

		switch (movement) {
		case NORTHEAST:
			pos.x++;
			pos.y++;
			break;
		case NORTHWEST:
			pos.x--;
			pos.y++;
			break;
		case SOUTHEAST:
			pos.x++;
			pos.y--;
			break;
		case SOUTHWEST:
			pos.x--;
			pos.y--;
			break;
		case SOUTH:
			pos.y--;
			break;
		case WEST:
			pos.x--;
			break;
		case NORTH:
			pos.y++;
			break;
		case EAST:
			pos.x++;
			break;
		}

		return pos;

	}
}
