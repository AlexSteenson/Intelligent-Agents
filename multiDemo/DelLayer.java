package uk.ac.nott.cs.g53dia.multidemo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

public class DelLayer {

	private int tankerFuel;
	private int wasteLevel;
	public int fuelUsed = 0;
	public int step = 0;
	public int taskValue = 0;
	public PlanCoordinator allPlans;
	int tankID;
	MainTanker tank;

	public PriorityQueue<PlanNode> nodeFP;
	public PriorityQueue<PlanNode> nodeWell;
	public LinkedList<PositionTuple> plan;
	public LinkedList<PlanModel> allPlan = new LinkedList<>();
	public int taskDist;

	public DelLayer(MainTanker tank, PlanCoordinator allPlans) {
		this.tankerFuel = tank.fuelLevel();
		this.wasteLevel = tank.getWasteLevel();
		this.tankID = tank.getTankerID();
		this.allPlans = allPlans;
		this.tank = tank;
	}

	public PlanModel findPlan() {

		ArrayList<TaskPosition> tasks = tank.model.task;

		//For all the tasks get a plan
		for (TaskPosition task : tasks) {
			//If the task isn't already completed
			if (!task.completed) {
				//If no other tanker is completing the task
				if (allPlans.checkTaskIsSafe(task, tank.getTankerID())) {
					plan = new LinkedList<>();
					tankerFuel = tank.fuelLevel();
					wasteLevel = tank.getWasteLevel();
					taskDist = 0;
					LinkedList<PositionTuple> taskPlan = backwardsSearch(tank.tankerPos, task);
					if (!taskPlan.isEmpty()) {
						PlanModel planM = new PlanModel(taskPlan, taskDist, task.task.getWasteAmount(),
								tank.getTankerID());
						allPlan.add(planM);
					}
				}
			}
		}

		if (allPlan.isEmpty()) {
			return null;
		}

		PlanModel bestPlan = CompareTasks();
		allPlans.addPlan(bestPlan);

		return bestPlan;
	}

	/**
	 * Backwards chain through pre conditions using an A* heuristic until all
	 * preconditions are met
	 * 
	 * @param currentLocation
	 * @param task
	 * @return
	 */
	public LinkedList<PositionTuple> backwardsSearch(PositionTuple currentLocation, TaskPosition task) {

		addNodes(task, currentLocation);
		boolean refuel = false;

		if (nodeFP.isEmpty()) {
			plan = new LinkedList<>();
			return plan;
		}

		//If the tanker needs to visit a well, find the best well
		if (!nodeWell.isEmpty()) {
			//Get the head of the well pq
			PositionTuple bestWell = nodeWell.poll().node;
			int wellDist = tank.model.calculateDistance(currentLocation, bestWell);
			int fp_well_dist = tank.model.calculateDistance(bestWell, nodeFP.peek().node);

			//If it can make it to the well with the current fuel level
			if (wellDist + fp_well_dist + 4 < tankerFuel) {
				//increment total distance
				taskDist += wellDist;
				plan.add(bestWell);
				//set waste to 0
				wasteLevel = 0;
				//continue searching from the new node
				backwardsSearch(bestWell, task);
				return plan;
			} else {
				refuel = true;
			}

		}

		int taskDistance = tank.fleet.model.calculateDistance(currentLocation, task.pos);
		PositionTuple closest_fp_task = tank.fleet.model.findClosestFuelPump(task.pos);
		int distance_fp_task = tank.fleet.model.calculateDistance(task.pos, closest_fp_task);

		//if the tanker needs to refuel to get to the task
		if (tankerFuel < taskDistance + distance_fp_task + 4 || refuel) {
			//get head of pq
			PositionTuple bestFP = nodeFP.poll().node;
			int fpDist = tank.model.calculateDistance(currentLocation, bestFP);

			//increment distance
			taskDist += fpDist;
			//add node to plan
			plan.add(bestFP);
			//Set fuel to max
			tankerFuel = Tanker.MAX_FUEL;
			//Continue search from the new node
			backwardsSearch(bestFP, task);
		} else {
			//all pre conditions are met and the task can be added
			plan.add(task.pos);
			taskDist += tank.fleet.model.calculateDistance(currentLocation, task.pos);
			return plan;
		}

		return plan;

	}

	/**
	 * Calculates the pre-conditions for the tanker and adds the respective nodes to
	 * the priority queue using their values to be expanded
	 * 
	 * @param task
	 * @param currentPos
	 */
	public void addNodes(TaskPosition task, PositionTuple currentPos) {

		// Create priority queues
		nodeFP = new PriorityQueue<PlanNode>(new DescComparator());
		nodeWell = new PriorityQueue<PlanNode>(new DescComparator());

		int taskDistance = tank.fleet.model.calculateDistance(currentPos, task.pos);

		// If the tanker can't make it to the task
		if (tankerFuel == Tanker.MAX_FUEL || taskDistance + 4 > Tanker.MAX_FUEL) {
			plan = new LinkedList<>();
			return;
		}
		// Add all the fuel pumps to the priority queue
		for (PositionTuple fp : tank.fleet.model.fuelPump) {
			// Calculate h(n)
			int euclideanDistance = tank.fleet.model.calculateDistance(fp, task.pos);
			// Calculate g(n)
			int nodeDistance = tank.fleet.model.calculateDistance(currentPos, fp);
			// Add to the queue with value f(n) = g(n) + h(n)
			PlanNode n = new PlanNode(fp, euclideanDistance + nodeDistance);
			nodeFP.add(n);
		}

		if (wasteLevel + task.task.getWasteAmount() > Tanker.MAX_WASTE) {
			for (PositionTuple well : tank.fleet.model.well) {
				int euclideanDistance = tank.fleet.model.calculateDistance(well, task.pos);
				int nodeDistance = tank.fleet.model.calculateDistance(currentPos, well);
				PlanNode n = new PlanNode(well, euclideanDistance + nodeDistance);
				nodeWell.add(n);
			}

		}

	}

	/**
	 * Compares the plans for all the tasks and returns the best Compares by
	 * calculating the ratio of points gained to distance travelled
	 * 
	 * @return
	 */
	public PlanModel CompareTasks() {

		double bestValue = -1;
		PlanModel bestTask = null;

		for (PlanModel pm : allPlan) {
			if (bestValue < pm.taskSize / (pm.distance + 1) && pm.plan != null) {
				bestValue = pm.taskSize / (pm.distance + 1);
				bestTask = pm;
			}
		}

		return bestTask;
	}

	// Comparator
	class DescComparator implements Comparator<PlanNode> {

		@Override
		public int compare(PlanNode o1, PlanNode o2) {
			return o1.value - o2.value;
		}

	}
}
