package uk.ac.nott.cs.g53dia.multidemo;

import java.util.ArrayList;

public class PlanCoordinator {

	// Store all the plans for the tanks
	// Check a plan to the others plan

	private ArrayList<PlanModel> tankerPlans = new ArrayList<PlanModel>();

	/**
	 * Adds the tankers current plan to the arrayList
	 * 
	 * @param tankerID
	 * @param newPlan
	 */
	public void addPlan(PlanModel newPlan) {

		// Checks to see if the tanker already has a plan and to overwrite it
		for (int i = 0; i < tankerPlans.size(); i++) {
			PlanModel plan = tankerPlans.get(i);
			if (plan.tankerID == newPlan.tankerID) {

				tankerPlans.set(i, newPlan);
				return;

			}
		}
		// There isn't a plan for the tanker so add it
		tankerPlans.add(newPlan);
	}

	/**
	 * Checks if the task is currently being completed by another tanker
	 * @param taskCheck
	 * @param tankerID
	 * @return
	 */
	public boolean checkTaskIsSafe(TaskPosition taskCheck, int tankerID) {

		//for all the plans
		for (PlanModel plan : tankerPlans) {
			//if the plan is a different tanker
			if (plan.tankerID != tankerID) {
				if (plan.plan != null && !plan.plan.isEmpty()) {
					//If the task is a task another tanker is doing return false
					if (plan.plan.getLast().x == taskCheck.pos.x && plan.plan.getLast().y == taskCheck.pos.y) {
						return false;
					}

				}

			}
		}
		return true;
	}

}
