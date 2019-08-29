package uk.ac.nott.cs.g53dia.demo;

import java.util.LinkedList;

public class DeliberativeLayer {

	LinkedList<PositionTuple> plan = new LinkedList<PositionTuple>();
	private int tankerFuel = DemoSimulator.tank.fuelLevel();
	private int wasteLevel = DemoSimulator.tank.getWasteLevel();
	int fuelUsed = 0;
	int step = 0;
	int taskValue = 0;

	public DeliberativeLayer() {

	}

	public LinkedList<PositionTuple> backwardSearch(PositionTuple currentLocation) {


		PositionTuple nextLocation = currentLocation;

		TaskPosition biggestTask = DemoSimulator.model.findBiggestTaskPlanning(currentLocation);

		if (biggestTask == null) {
			return plan;
		}

		taskValue = biggestTask.task.getWasteAmount();

		// Distance between current location and task
		int distance = DemoSimulator.model.calculateDistance(currentLocation, biggestTask.pos);
		// Closest fuel pump to task and distance
		PositionTuple fp = DemoSimulator.model.findClosestFuelPump(biggestTask.pos);
		int fp_distance = DemoSimulator.model.calculateDistance(biggestTask.pos, fp);
		// The chance of a move failing and using a fuel
		int failChance = (int) Math.ceil(MainTanker.MAX_FUEL * 0.0200);

		// If the distance to task, then to the closest fuel pump is greater than fuel
		if (distance + fp_distance + failChance > tankerFuel) {

			// Find closest fuel pump and distance
			PositionTuple closest_fp = DemoSimulator.model.findClosestFuelPump(currentLocation);
			int closest_fp_dist = DemoSimulator.model.calculateDistance(closest_fp, currentLocation);
			int closest_fp_task_dist = DemoSimulator.model.calculateDistance(closest_fp, biggestTask.pos);

			// distance to closest fuel pump, to task then to closest fuel pump to task >
			// max fuel, it can't get there
			if (fp_distance + closest_fp_task_dist + failChance > MainTanker.MAX_FUEL || tankerFuel == MainTanker.MAX_FUEL) {
				// Set visited to true so it isn't visited again
				biggestTask.planningCompleted = true;
				//if(step != 0)
				//	return plan;
			} else { // Can get there

				// Place tanker at closest fuel pump and increase fuel used
				fuelUsed += closest_fp_dist + failChance;
				// Set fuel level to max to simulate refuelling
				tankerFuel = MainTanker.MAX_FUEL;
				// Add fuel pump to plan
				plan.add(closest_fp);
				// Update next location
				nextLocation = closest_fp;
			}
			// If can't hold waste dump
		} else if (wasteLevel + biggestTask.task.getWasteAmount() > MainTanker.MAX_WASTE) {

			// Find closest well to current position and task
			PositionTuple closest_well = DemoSimulator.model.findClosestWell(currentLocation, biggestTask.pos);
			PositionTuple closest_fp_to_well = DemoSimulator.model.findClosestFuelPump(closest_well);
			// Calculate distances
			int wellDistance = DemoSimulator.model.calculateDistance(currentLocation, closest_well);
			int fpDistance_well = DemoSimulator.model.calculateDistance(closest_fp_to_well, closest_well);
			int fpDistance_cl = DemoSimulator.model.calculateDistance(closest_fp_to_well, currentLocation);

			// If distance to well and fuel pump is greater than fuel, refuel
			if (wellDistance + fpDistance_well + failChance > tankerFuel) {
				if (fpDistance_cl + failChance > tankerFuel) {
					PositionTuple closestFp = DemoSimulator.model.findClosestFuelPump(currentLocation);
					plan.add(closestFp);
					nextLocation = closestFp;
					tankerFuel = MainTanker.MAX_FUEL;
					fuelUsed += DemoSimulator.model.calculateDistance(currentLocation, closestFp) + failChance;
				}else {
					plan.add(closest_fp_to_well);
					nextLocation = closest_fp_to_well;
					tankerFuel = MainTanker.MAX_FUEL;
					fuelUsed += fpDistance_well + failChance;
				}
			} else {
				plan.add(closest_well);
				nextLocation = closest_well;
				tankerFuel = tankerFuel - wellDistance - failChance;
				fuelUsed += wellDistance + failChance;
				wasteLevel = 0;
			}
		} else {
			plan.add(biggestTask.pos);
			fuelUsed += distance + failChance;
			
			return plan;
		}


		try {
			backwardSearch(nextLocation);
		} catch (StackOverflowError e) {

		}

		return plan;

	}
}
