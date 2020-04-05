


package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;

import java.lang.Integer;
import java.lang.Boolean;

/* Shortest Processing Time */
public class GreedySolver implements Solver {

	private Instance instance;

	private int[] datePerMachine;
	/* Needed for XRPT binary relations */
	private int[][] remainingProcessingTimes;

	private ArrayList<Task> pendingOperations;

	private GreedyBinaryRelation gbr;	

	public GreedySolver(GreedyBinaryRelation gbr) {
		this.gbr = gbr;
	}

	private void init(Instance instance) {
		this.instance = instance;
		this.datePerMachine = new int[this.instance.numMachines];
		Arrays.fill(this.datePerMachine, 0);

		if (this.gbr == GreedyBinaryRelation.SRPT || this.gbr == GreedyBinaryRelation.LRPT) {
			/* XRPT binary relations look for the remaining processing time of each task
			 * We instantiate the table with (-1) values and they will be calculated only once,
			 * while the algorithm is running */
			this.remainingProcessingTimes = new int[this.instance.numJobs][this.instance.numTasks];
			for (int job = 0; job < this.instance.numJobs; job++) {
				Arrays.fill(this.remainingProcessingTimes[job], -1);
			}
		}

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
			for (Task op: this.sortTaskArray(this.expandOperations(earliestTime))) {

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

	private ArrayList<Task> sortTaskArray(ArrayList<Task> arr) {

		switch (this.gbr) {
			case SPT:
				return this.sortSPT(arr);
			case LPT:
				return this.sortLPT(arr);
			case SRPT:
				return this.sortSRPT(arr);
			case LRPT:
				return this.sortLRPT(arr);
			default:return null;
		}
	}
	
	private ArrayList<Task> sortXPT(ArrayList<Task> arr,
					BiFunction<Task, Task, Boolean> comparisonFunction) {

		ArrayList<Task> sorted = new ArrayList<Task>();
		int length = arr.size();

		for (int i = 0; i < length; i++) {
			Task optimum = arr.get(0);

			for (int j = 0; j < arr.size(); j++) {
				Task currentTask = arr.get(j);

				if (comparisonFunction.apply(currentTask, optimum).booleanValue()) {
					optimum = arr.get(j);
				}	
			}
			sorted.add(optimum);
			arr.remove(arr.indexOf(optimum));
		}
		return sorted;
	}

	private ArrayList<Task> sortXRPT(ArrayList<Task> arr,
					BiFunction<Integer, Integer, Boolean> comparisonFunction) {

		ArrayList<Task> sorted = new ArrayList<Task>();
		int length = arr.size();

		for (int i = 0; i < length; i++) {
			Task optimum = arr.get(0);

			for (int j = 0; j < arr.size(); j++) {
				Task currentTask = arr.get(j);
				/* The remaining processing time of that task has not been calculated yet */
				if (this.remainingProcessingTimes[currentTask.job][currentTask.task] == -1) {
					this.remainingProcessingTimes[currentTask.job][currentTask.task] = 0;
					for (int task = currentTask.task; task < this.instance.numTasks; task++) {
						this.remainingProcessingTimes[currentTask.job][currentTask.task] += this.instance.duration(currentTask.job, task);
					}
				}

				if (comparisonFunction.apply(new Integer(this.remainingProcessingTimes[currentTask.job][currentTask.task]),
								new Integer(this.remainingProcessingTimes[optimum.job][optimum.task])).booleanValue()) {
					optimum = arr.get(j);
				}	
			}

			sorted.add(optimum);
			arr.remove(arr.indexOf(optimum));
		}
		return sorted;
	}


	private ArrayList<Task> sortSPT(ArrayList<Task> arr) {
		return this.sortXPT(arr,
				(Task current, Task optimum)
				-> new Boolean(this.instance.duration(current.job, current.task) < this.instance.duration(optimum.job, optimum.task)));
	}

	private ArrayList<Task> sortLPT(ArrayList<Task> arr) {
		return this.sortXPT(arr,
				(Task current, Task optimum)
				-> new Boolean(this.instance.duration(current.job, current.task) > this.instance.duration(optimum.job, optimum.task)));
	}

	private ArrayList<Task> sortSRPT(ArrayList<Task> arr) {
		return this.sortXRPT(arr,
				(Integer currentRemainingTime, Integer optimumRemainingTime)
				-> new Boolean(currentRemainingTime.intValue() < optimumRemainingTime.intValue()));
	}

	private ArrayList<Task> sortLRPT(ArrayList<Task> arr) {
		return this.sortXRPT(arr,
				(Integer currentRemainingTime, Integer optimumRemainingTime)
				-> new Boolean(currentRemainingTime.intValue() > optimumRemainingTime.intValue()));
	}
}

