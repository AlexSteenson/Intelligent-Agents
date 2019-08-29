package uk.ac.nott.cs.g53dia.demo;

import java.util.LinkedList;

import uk.ac.nott.cs.g53dia.library.Cell;
import uk.ac.nott.cs.g53dia.library.FuelPump;
import uk.ac.nott.cs.g53dia.library.Station;
import uk.ac.nott.cs.g53dia.library.Well;

/**
 * The control system decides what action returned from the deliberative layer and the reactive layer should be executed
 * It uses a set of rules to determine what action to output
 * It also compared the newly generated plan to the current plan
 *
 */
public class ControlSystem {
	
	private final int REFUEL = 8;
	private final int TASK = 9;
	private final int DUMP = 10;
	PlanModel currentPlan = new PlanModel(new LinkedList<PositionTuple>(), 0, 0);
	
	ReactiveLayer react = new ReactiveLayer();

	public ControlSystem() {

	}

	public int decideAction(Cell currentView) {

		int reactAction = react.getAction(currentView);
		
		DeliberativeLayer fs = new DeliberativeLayer();
		PositionTuple tankerPos = DemoSimulator.tank.tankerPos;

		fs.backwardSearch(tankerPos);
		
		PlanModel candidatePlan = new PlanModel(fs.plan, fs.fuelUsed, fs.taskValue);
		comparePlan(candidatePlan);
		
		//Reset the planning complete for the tasks
		for (TaskPosition tsk : DemoSimulator.model.task) {
			tsk.planningCompleted = false;
		}
		
		PositionTuple closestFp = DemoSimulator.model.findClosestFuelPump(tankerPos);
		int fpDistance = DemoSimulator.model.calculateDistance(tankerPos, closestFp);
		
		//Refuel if needed
		if(fpDistance >= DemoSimulator.tank.fuelLevel() + 2) {
			return reactAction;
		}

		//If there's a plan get the first position
		if (!currentPlan.plan.isEmpty()) {
			if (currentPlan.plan.getFirst().equals(tankerPos)) { //If the tanker is at the position
				currentPlan.plan.removeFirst();
				if (currentView instanceof Station) { //At station return task
					currentPlan.taskSize = 0;
					return TASK;
				} else if (currentView instanceof Well) { //At well return dump
					return DUMP;
				} else if (currentView instanceof FuelPump) { //At fuel pump return refuel
					react.resetForage();
					return REFUEL;
				}

			} else { //There is no plan and the reactive layer should control
				currentPlan.distance--;
				return DemoSimulator.model.locationDirection(currentPlan.plan.getFirst());
			}
		}

		return reactAction;
	}
	
	/**
	 * Compared the plans by giving the current plan and the candidate plan a score and setting the
	 * highest scoring plan as the current plan.
	 * It calculates the score based on how much waste the agent collects per time step
	 * @param candidatePlan
	 */
	public void comparePlan(PlanModel candidatePlan) {
		
		if(candidatePlan.plan.isEmpty()) { //Keep current plan
			return;
		}else if(currentPlan.plan.isEmpty()){ //Take candidate plan
			currentPlan = candidatePlan;
			return;
		}
		
		//Stops dividing by 0
		if(candidatePlan.distance == 0) {
			candidatePlan.distance = 1;
		}
		
		if(currentPlan.distance == 0) {
			currentPlan.distance = 1;
		}
		
		//Calculate scores and take the highest
		if(candidatePlan.taskSize / candidatePlan.distance > currentPlan.taskSize/ currentPlan.distance) {
			currentPlan = candidatePlan;
		}
	
	}

}
