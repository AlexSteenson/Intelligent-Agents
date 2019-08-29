package uk.ac.nott.cs.g53dia.multidemo;

import java.util.Random;

import uk.ac.nott.cs.g53dia.multilibrary.Action;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.DisposeWasteAction;
import uk.ac.nott.cs.g53dia.multilibrary.FuelPump;
import uk.ac.nott.cs.g53dia.multilibrary.LoadWasteAction;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;
import uk.ac.nott.cs.g53dia.multilibrary.RefuelAction;
import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

public class MainTankerReactive extends Tanker {

	private final int NORTH = 0, SOUTH = 1, EAST = 2, WEST = 3, NORTHEAST = 4, NORTHWEST = 5, SOUTHEAST = 6,
			SOUTHWEST = 7;

	ModelLayer model;
	public PositionTuple tankerPos;
	private int directions[][] = { { 7, 5, 4, 6 }, { 5, 4, 6, 7 }, { 4, 6, 7, 5 }, { 6, 7, 5, 4 } };
	protected int finalDir = 8;
	private int steps = 20;
	private int side = 0;
	private int dir = 0;
	private int round = 0;

	public MainTankerReactive(DemoFleet fleet) {
		this.r = new Random();
		model = fleet.model;
		tankerPos = new PositionTuple(0, 0);
	}

	@Override
	public Action senseAndAct(Cell[][] view, long timestep) {

		if (!this.actionFailed && finalDir < 8) {
			tankerPos = updatePos(finalDir, tankerPos);
		}

		model.updateLocations(view, tankerPos);
		
		PositionTuple fp = model.findClosestFuelPump(tankerPos);
		int distance = model.calculateDistance(fp, tankerPos);

		if (getFuelLevel() < distance + 5) {
			if (getCurrentCell(view) instanceof FuelPump) {
				finalDir = 8;
				return new RefuelAction();
			}
			finalDir = model.locationDirection(fp, tankerPos);
			return new MoveAction(finalDir);
		} else {
			if (model.findTask(tankerPos) == null) {

				// haven't reach the number of steps to turn
				if (round <= steps) {
					round++;
					finalDir = directions[dir][side];
					return new MoveAction(finalDir);
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
					return new MoveAction(finalDir);
				} // end else

			} else {
				TaskPosition task = model.findTask(tankerPos);

				try {
				if (task.task.getWasteAmount() + this.getWasteLevel() <= MAX_WASTE) {
					if (tankerPos.x == task.pos.x && tankerPos.y == task.pos.y) {
						task.completed = true;
						finalDir = 8;
						return new LoadWasteAction(task.task);
					} else {
						finalDir = model.locationDirection(task.pos, tankerPos);
						return new MoveAction(finalDir);
					}
				} else {
					PositionTuple well = model.findClosestWell(tankerPos);
					if (tankerPos.x == well.x && tankerPos.y == well.y) {
						finalDir = 8;
						return new DisposeWasteAction();
					} else {
						finalDir = model.locationDirection(well, tankerPos);
						return new MoveAction(finalDir);
					}
				}
				}catch(NullPointerException e) {
					return null;
				}

			}
		}

		//return new MoveAction(directions[dir][side]);
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
