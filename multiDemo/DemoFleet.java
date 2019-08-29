package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.*;

public class DemoFleet extends Fleet {

	//private static final long serialVersionUID = 2509705188519124033L;
	/**
	 * Number of tankers in the fleet
	 */
	public static int FLEET_SIZE = 3;
	public PlanCoordinator planCoord = new PlanCoordinator();
	public ModelLayer model = new ModelLayer();

	public DemoFleet() {
		// Create the tankers
		for (int i = 0; i < FLEET_SIZE; i++) {
			this.add(new MainTanker(i, this));
		}
	}
}
