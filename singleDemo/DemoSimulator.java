package uk.ac.nott.cs.g53dia.demo;

import uk.ac.nott.cs.g53dia.library.*;
import java.util.Random;

/**
 * An example of how to simulate execution of a tanker agent in the sample
 * (task) environment.
 * <p>
 * Creates a default {@link Environment}, a {@link DemoTanker} and a GUI window
 * (a {@link TankerViewer}) and executes the Tanker for DURATION days in the
 * environment.
 * 
 * @author Julian Zappala
 */

/*
 * Copyright (c) 2005 Neil Madden. Copyright (c) 2011 Julian Zappala
 * (jxz@cs.nott.ac.uk)
 * 
 * See the file "license.terms" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */

public class DemoSimulator {

	public static MainTanker tank;
	public static ModelLayer model = new ModelLayer();
	public static ControlSystem controlSystem = new ControlSystem();
	public static int numTasks = 0;

	/**
	 * Time for which execution pauses so that GUI can update. Reducing this value
	 * causes the simulation to run faster.
	 */
	private static int DELAY = 20;

	/**
	 * Number of timesteps to execute
	 */
	private static int DURATION = 10000;

	public static void main(String[] args) {
		// Set the seed for reproducible behaviour
		Random r = new Random();
		// Create an environment
		Environment env = new Environment(Tanker.MAX_FUEL / 2, r);
		// Create a tanker
		tank = new MainTanker(r);
		// Create a GUI window to show the tanker
		TankerViewer tv = new TankerViewer(tank);
		tv.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		// Start executing the Tanker
		while (env.getTimestep() < DURATION) {
			// Advance the environment timestep
			env.tick();
			// Update the GUI
			tv.tick(env);
			// Get the current view of the tanker
			Cell[][] view = env.getView(tank.getPosition(), Tanker.VIEW_RANGE);
			// Update model
			model.updateLocations(view);
			
			// Let the tanker choose an action
			Action act = tank.senseAndAct(view, env.getTimestep());
			// Try to execute the action
			try {
				act.execute(env, tank);
				Thread.sleep(DELAY);
				if(tank.dir < 8)
					tank.tankerPos = tank.updatePos(tank.dir, tank.tankerPos);
			} catch (OutOfFuelException ofe) {
				System.err.println(ofe.getMessage());
				System.exit(-1);
			} catch (ActionFailedException afe) {
				System.err.println(afe.getMessage());
			} catch (Exception e) {
			}

		} // end while
		System.out.println("Visited " + numTasks);
	}

}
