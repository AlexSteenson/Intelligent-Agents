package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.FuelPump;
import uk.ac.nott.cs.g53dia.multilibrary.Station;
import uk.ac.nott.cs.g53dia.multilibrary.Well;

/**
 * The control system decides what action returned from the deliberative layer
 * and the reactive layer should be executed It uses a set of rules to determine
 * what action to output It also compared the newly generated plan to the
 * current plan
 *
 */
public class ControlSystem {

	private final int REFUEL = 8;
	private final int TASK = 9;
	private final int DUMP = 10;

	PlanModel currentPlan;
	ReactiveLayer react = new ReactiveLayer();

	public ControlSystem() {

	}

	/**
	 * Gets the best plan, if it on the location of an object, return the tankers action
	 * otherwise move towards the location
	 * @param view
	 * @param tank
	 * @return
	 */
	public int decideAction(Cell[][] view, MainTanker tank) {
		
		PlanCoordinator allPlans = tank.fleet.planCoord;
		Cell currentView = tank.getCurrentCell(view);
		int reactAction = react.getAction(currentView, tank);
		PositionTuple tankerPos = tank.tankerPos;

		//get plan
		DelLayer del = new DelLayer(tank, allPlans);
		PlanModel currentPlan = del.findPlan();


		if (currentPlan != null) {
			if (currentPlan.plan != null && !currentPlan.plan.isEmpty()) {
				if (currentPlan.plan.getFirst().equals(tankerPos)) { // If the tanker is at the position
					currentPlan.plan.removeFirst();
					if (currentView instanceof Station) { // At station return task
						return TASK;
					} else if (currentView instanceof Well) { // At well return dump
						return DUMP;
					} else if (currentView instanceof FuelPump) { // At fuel pump return refuel
						return REFUEL;
					}

				} else {
					return tank.model.locationDirection(currentPlan.plan.getFirst(), tankerPos);
				}
			}

		}

		return reactAction;
	}

}
