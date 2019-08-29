package uk.ac.nott.cs.g53dia.multidemo;

import java.util.Random;

import uk.ac.nott.cs.g53dia.multilibrary.Action;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.DisposeWasteAction;
import uk.ac.nott.cs.g53dia.multilibrary.FuelPump;
import uk.ac.nott.cs.g53dia.multilibrary.LoadWasteAction;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;
import uk.ac.nott.cs.g53dia.multilibrary.RefuelAction;
import uk.ac.nott.cs.g53dia.multilibrary.Station;
import uk.ac.nott.cs.g53dia.multilibrary.Tanker;



/**
 * 
 * This class is the tanker object. It detects if its on an object and
 * what action it should do based on that
 *
 */
public class MainTanker extends Tanker {

	public ModelLayer model;
	public ControlSystem controlSystem = new ControlSystem();
	public DemoFleet fleet;
	
	private int tankerID;
	public PositionTuple tankerPos;
	public int dir = -1;
	private int action;
	
	public int reactDir = 0;
	
	private final int REFUEL = 8;
	private final int TASK = 9;
	private final int DUMP = 10;
	public int closest;

	private final int NORTH = 0, SOUTH = 1, EAST = 2, WEST = 3, NORTHEAST = 4, NORTHWEST = 5, SOUTHEAST = 6,
			SOUTHWEST = 7;

	public MainTanker(int ID, DemoFleet fleet) {
		this(new Random());
		tankerPos = new PositionTuple(0, 0);
		this.closest = -1;
		this.tankerID = ID;
		this.fleet = fleet;
		this.model = fleet.model;
		
	}

	public MainTanker(Random random) {
		this.r = random;
		tankerPos = new PositionTuple(0, 0);
		this.closest = -1;
	}

	/**
	 * Gets the tankers fuel level
	 * @return fuel level
	 */
	public int fuelLevel() {
		return this.getFuelLevel();
	}
	
	public void setTankerID(int ID) {
		this.tankerID = ID;
	}
	
	public int getTankerID() {
		return this.tankerID;
	}

	/**
	 * The tanker receives an action and senses what object it is currently on and what action it should do
	 * e.g. If the tanker is on a fuel pump it should refuel
	 */
	@Override
	public Action senseAndAct(Cell[][] view, long timestep) {
		
		if(!this.actionFailed && dir != -1) {
			tankerPos = updatePos(dir, tankerPos);
		}
		
		model.updateLocations(view, tankerPos);
		
		Cell currentCell = getCurrentCell(view);
		action = controlSystem.decideAction(view, this);

		if (action == REFUEL) {
			if (currentCell instanceof FuelPump) {
				dir = REFUEL;
				return new RefuelAction();
			}
			PositionTuple closest_fp = model.findClosestFuelPump(tankerPos);
			dir = model.locationDirection(closest_fp, tankerPos);
			return new MoveAction(dir);

		} else if (action == DUMP) {
			dir = DUMP;
			return new DisposeWasteAction();

		} else if (action == TASK) {
			dir = TASK;
			Station station = (Station) currentCell;

			for (TaskPosition tsk : model.task) {
				if (tsk.task.equals(station.getTask())) {
					int amount = station.getTask().getWasteAmount();
					if (amount <= MainTanker.MAX_WASTE)
						tsk.completed = true;
					
					break;
				}
			}
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
