package uk.ac.nott.cs.g53dia.multidemo;

import java.util.ArrayList;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.FuelPump;
import uk.ac.nott.cs.g53dia.multilibrary.Station;
import uk.ac.nott.cs.g53dia.multilibrary.Well;

/**
 * The model layer keeps an internal state of the environment and can perform calculations
 * to figure out locations of objects in the environment. e.g. the closest fuel pump
 * The class logs all the objects such fuel pumps, stations, wells and tasks
 * The tankers position is tracked accurately and the position of every object is calculated accurately
 * All stations are polled every time step to see if it has generated a task
 *
 */

public class ModelLayer {

	public ArrayList<PositionTuple> fuelPump = new ArrayList<PositionTuple>();
	public ArrayList<StationPosition> station = new ArrayList<StationPosition>();
	public ArrayList<PositionTuple> well = new ArrayList<PositionTuple>();
	public ArrayList<TaskPosition> task = new ArrayList<TaskPosition>();

	private final int NORTH = 0, // (0, +1)
			SOUTH = 1, // (0, -1)
			EAST = 2, // (+1, 0)
			WEST = 3, // (-1, +1)
			NORTHEAST = 4, // (+1, +1)
			NORTHWEST = 5, // (-1, +1)
			SOUTHEAST = 6, // (-1, +1)
			SOUTHWEST = 7; // (-1, -1)

	// Constructor
	public ModelLayer() {
	}

	/**
	 * Update all the objects based on that the agent can see. If a new object
	 * such as a fuel pump is discovered it will be added to the array list.
	 * All tasks are polled to see if any stations have generate new tasks
	 * @param view
	 */
	public void updateLocations(Cell[][] view, PositionTuple tankerPos) {

		//For all the cells in the agents view
		for (int i = 0; i < view[0].length; i++) {
			for (int j = 0; j < view.length; j++) {
				//Get the cell being observed
				Cell c = view[i][j];
				//If the cell is a fuel pump calculate the position
				if (c instanceof FuelPump) {
					PositionTuple fpPos = calculatePosition(i, j, tankerPos);
					boolean valid = true;

					//If the fuel pump has already been seen don't add it to the list
					for (int k = 0; k < fuelPump.size(); k++) {
						if (fuelPump.get(k).x == fpPos.x && fuelPump.get(k).y == fpPos.y)
							valid = false;
					}
					//Otherwise add it
					if (valid)
						fuelPump.add(fpPos);

					//If the cell is a Station calculate the position
				} else if (c instanceof Station) {
					StationPosition stationPos = new StationPosition(calculatePosition(i, j, tankerPos), (Station) c);
					boolean valid = true;

					for (int k = 0; k < station.size(); k++) {
						if (station.get(k).x == stationPos.x && station.get(k).y == stationPos.y) { // Same station
																									// Location
							if (station.get(k).station.getTask() != stationPos.station.getTask()) {// Different tasks add the updated station
								station.remove(k);
								station.add(stationPos);
								break;
							} else { //don't add
								valid = false;
								break;
							}
						}

					}

					//Add the new station
					if (valid) {
						station.add(stationPos);
					}

					//If the cell is a well calculate its position
				} else if (c instanceof Well) {
					PositionTuple wellPos = calculatePosition(i, j, tankerPos);
					boolean valid = true;

					//Check to see if its already in the list
					for (int k = 0; k < well.size(); k++) {
						if (well.get(k).x == wellPos.x && well.get(k).y == wellPos.y)
							valid = false;
					}
					//New wells are added
					if (valid)
						well.add(wellPos);
				}

			} // end j for
		} // end i for

		updateTasks();
	}

	/**
	 * Poll over all the stations and check if it has generated a new task
	 */
	public void updateTasks() {

		//For all stations
		for (int i = 0; i < station.size(); i++) {
			//If the station has a task check if its already in the task list
			if (station.get(i).station.getTask() != null) {
				PositionTuple taskTuple = new PositionTuple(station.get(i).x, station.get(i).y);
				TaskPosition taskPos = new TaskPosition(taskTuple, station.get(i).station.getTask());
				boolean valid = true;

				for (int j = 0; j < task.size(); j++) {
					if (task.get(j).task.equals(taskPos.task)) {
						valid = false;
					}

				}
				//If its new add it to the task list
				if (valid)
					task.add(taskPos);
			}
		}
	}

	/**
	 * Calculate the position of the given cell based off the tankers position and
	 * the cell coordinates
	 * 
	 * @param x
	 * @param y
	 */
	public PositionTuple calculatePosition(int x, int y, PositionTuple tankerPos) {

		int tanker_x = tankerPos.x;
		int tanker_y = tankerPos.y;
		int tankerViewRange = MainTanker.VIEW_RANGE;

		//Map the cells position based off takers position
		int cell_x = x - tankerViewRange + tanker_x;
		int cell_y = -(y - tankerViewRange - tanker_y);

		return new PositionTuple(cell_x, cell_y);

	}

	/**
	 * Calculates the distance between two locations
	 * @param loc
	 * @param dest
	 * @return distance
	 */
	public int calculateDistance(PositionTuple loc, PositionTuple dest) {

		PositionTuple coord = new PositionTuple(loc.x - dest.x, loc.y - dest.y);

		return Math.max(Math.abs(coord.x), Math.abs(coord.y));
	}

	/**
	 * Finds the closest fuel pump to a given position
	 * @param pos
	 * @return fuel pump position
	 */
	public PositionTuple findClosestFuelPump(PositionTuple pos) {

		if (fuelPump.isEmpty()) { // If there are no fuel pumps in location
			return null;
		} else if (fuelPump.size() == 1) { // If there's only 1
			return fuelPump.get(0);
		} else { // else find closest

			PositionTuple closest = null;
			int dist = Integer.MAX_VALUE;

			for (PositionTuple i : fuelPump) {
				PositionTuple fp_coord = new PositionTuple(pos.x - i.x, pos.y - i.y);

				if (dist > Math.max(Math.abs(fp_coord.x), Math.abs(fp_coord.y))) {
					closest = i;
					dist = Math.max(Math.abs(fp_coord.x), Math.abs(fp_coord.y));
				}
			}

			return closest;
		}

	}
	
	public PositionTuple findClosestWell(PositionTuple pos) {

		if (well.isEmpty()) { // If there are no fuel pumps in location
			return null;
		} else if (well.size() == 1) { // If there's only 1
			return well.get(0);
		} else { // else find closest

			PositionTuple closest = null;
			int dist = Integer.MAX_VALUE;

			for (PositionTuple i : well) {
				PositionTuple fp_coord = new PositionTuple(pos.x - i.x, pos.y - i.y);

				if (dist > Math.max(Math.abs(fp_coord.x), Math.abs(fp_coord.y))) {
					closest = i;
					dist = Math.max(Math.abs(fp_coord.x), Math.abs(fp_coord.y));
				}
			}

			return closest;
		}

	}


	/**
	 * Finds the biggest task per distance travelled. 
	 * The task that returns the largest value per time step is selected
	 * @param pos current location of the agent
	 * @return task
	 */
	public TaskPosition findTask(PositionTuple pos) {

		//If there are no tasks
		if (task.isEmpty()) {
			return null;
		}

		TaskPosition closest = null;
		int dist = Integer.MAX_VALUE;

		for (TaskPosition i : task) {
			if(i.completed == false) {
				PositionTuple fp_coord = new PositionTuple(pos.x - i.pos.x, pos.y - i.pos.y);

				if (dist > Math.max(Math.abs(fp_coord.x), Math.abs(fp_coord.y))) {
					closest = i;
					dist = Math.max(Math.abs(fp_coord.x), Math.abs(fp_coord.y));
				}
			}
			
		}
		return closest;
	}
	
	/**
	 * This is used to find the direction of an object based off the tankers position
	 * @param location
	 * @return
	 */
	public int locationDirection(PositionTuple location, PositionTuple tankerPos) {

		int dPos = 0;
		int x, y;
		x = tankerPos.x - location.x;
		y = tankerPos.y - location.y;

		if (x < 0 && y < 0) {
			dPos = NORTHEAST;
		}
		if (x < 0 && y > 0) {
			dPos = SOUTHEAST;
		}
		if (x < 0 && y == 0) {
			dPos = EAST;
		}

		if (x > 0 && y < 0) {
			dPos = NORTHWEST;
		}
		if (x > 0 && y > 0) {
			dPos = SOUTHWEST;
		}
		if (x > 0 && y == 0) {
			dPos = WEST;
		}

		if (x == 0 && y < 0) {
			dPos = NORTH;
		}
		if (x == 0 && y > 0) {
			dPos = SOUTH;
		}

		return dPos;
	}
}
