package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Task;

/**
 * This class is used to store the position of tasks, the task its self
 * and if the task is complete or not. 
 * The completed boolean represents if the task has been completed
 * planningCompleted boolean represents if the task has been completed when
 * simulating a run when planning
 */
public class TaskPosition {

	PositionTuple pos;
	public Task task;
	boolean completed;
	boolean planningCompleted;

	public TaskPosition(PositionTuple pos, Task task) {
			this.pos = pos;
			this.task = task;
			this.completed = false;
			this.planningCompleted = false;
	}
}
