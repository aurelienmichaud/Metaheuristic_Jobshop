


package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Arrays;

/* Shortest Processing Time */
public class GreedySolver implements Solver {

	private Instance instance;

	private int[] datePerMachine;

	private ArrayList<Task> pendingOperations;

	private GreedyBinaryRelation gbr;	

	public GreedySolver(GreedyBinaryRelation gbr) {
		this.gbr = gbr;
	}

	private void init(Instance instance) {
		this.instance = instance;
		this.datePerMachine = new int[this.instance.numMachines];
		Arrays.fill(this.datePerMachine, 0);

		this.pendingOperations = new ArrayList<Task>();
		for (int task = 0; task < this.instance.numTasks; task++) {
			for (int job = 0; job < this.instance.numJobs; job++) {
				this.pendingOperations.add(new Task(job, task));	
			}
		}
	}

	@Override
	public Result solve(Instance instance, long deadline) {

		this.init(instance);

		JobNumbers sol = new JobNumbers(instance);

		while (!this.pendingOperations.isEmpty()) {
			int earliestTime = this.getEarliestTime();
			for (Task op: this.sortTupleArray(this.expandOperations(earliestTime))) {

				sol.jobs[sol.nextToSet++] = op.job;
				this.datePerMachine[this.instance.machine(op.job, op.task)] += this.instance.duration(op.job, op.task);
			}
		}

		return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
	}

	private int getEarliestTime() {
		int minDate = this.datePerMachine[0];
		for (int date: this.datePerMachine) {
			if (date < minDate) { minDate = date; }
		}
		return minDate;
	}

	private ArrayList<Task> expandOperations(int earliestTime) {

		ArrayList<Task> arr = new ArrayList<Task>();

		for (Task t: this.pendingOperations) {
			if (this.datePerMachine[this.instance.machine(t.job, t.task)] <= earliestTime) {
				arr.add(t);
			}
		}

		/* We remove the tasks found possible from the pending operations */
		for (Task t: arr) {
				this.pendingOperations.remove(this.pendingOperations.indexOf(t));
		}

		return arr;
	}

	private ArrayList<Task> sortTupleArray(ArrayList<Task> arr) {
		ArrayList<Task> sorted = new ArrayList<Task>();
		int length = arr.size();
		for (int i = 0; i < length; i++) {
			Task optimum = arr.get(0);
			for (int j = 0; j < arr.size(); j++) {

				/* Needed for SRPT & LRPT */
				Task currentTask = arr.get(j);
				int remainingTime = 0;
				int optimumRemainingTime = 0;

				switch (this.gbr) {
					case SPT:
						if (this.instance.duration(arr.get(j).job, arr.get(j).task) < this.instance.duration(optimum.job, optimum.task)) {
							optimum = arr.get(j);
						}	
						break;

					case LPT:
						if (this.instance.duration(arr.get(j).job, arr.get(j).task) > this.instance.duration(optimum.job, optimum.task)) {
							optimum = arr.get(j);
						}	
						break;

					case SRPT:
						remainingTime = 0;
						for (int task = currentTask.task; task < this.instance.numTasks; task++) {
							remainingTime += this.instance.duration(currentTask.job, task);
						}

						optimumRemainingTime = 0;
						for (int task = optimum.task; task < this.instance.numTasks; task++) {
							optimumRemainingTime += this.instance.duration(currentTask.job, task);
						}

						if (remainingTime < optimumRemainingTime) {

							optimum = arr.get(j);
						}	
						break;

					case LRPT:
						remainingTime = 0;
						for (int task = currentTask.task; task < this.instance.numTasks; task++) {
							remainingTime += this.instance.duration(currentTask.job, task);
						}

						optimumRemainingTime = 0;
						for (int task = optimum.task; task < this.instance.numTasks; task++) {
							optimumRemainingTime += this.instance.duration(currentTask.job, task);
						}

						if (remainingTime > optimumRemainingTime) {

							optimum = arr.get(j);
						}	
						break;

					default:break;
				}
			}

			sorted.add(optimum);
			arr.remove(arr.indexOf(optimum));
		}

		return sorted;
	}
}

