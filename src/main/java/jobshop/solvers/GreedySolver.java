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

/* Greedy solver implementing SPT, LPT, SRPT and LRPT prioritizing methods */
public class GreedySolver implements Solver {

	private Instance instance;
	/* The time at which each machine (resource) is available */
	private int[] datePerMachine;
	/* Needed for XRPT binary relations */
	private int[][] remainingProcessingTimes;
	/* All the currently pending operations (tasks), updated at each loop of the greedy algorithm */
	private ArrayList<Task> pendingOperations;
	/* The binary relation which defines how the operations (tasks)
	 * will be sorted and prioritized */
	private GreedyBinaryRelation gbr;	

	/**
	 * Instantiate a Greedy Solver.
	 * @param gbr	The binary relation which defines how the operations (tasks)
	 *		will be sorted and prioritezed 
	 * @return	A new instance of GreedySolver
	 * @see		jobshop.solvers.GreedyBinaryRelation
	 */
	public GreedySolver(GreedyBinaryRelation gbr) { this.gbr = gbr; }

	/*
	 * Init the basic elements arrays, should be called inside this.solve(Instance) function,
	 * since it is the starting point at which we know the instance which we will work on
	 */
	private void init(Instance instance) {
		this.instance = instance;
		this.datePerMachine = new int[this.instance.numMachines];
		Arrays.fill(this.datePerMachine, 0);

		/* XRPT binary relations look for the remaining processing time of each task
		 * We instantiate the table with (-1) values and they will be calculated only once,
		 * while the algorithm is running */
		if (this.gbr == GreedyBinaryRelation.SRPT
			|| this.gbr == GreedyBinaryRelation.LRPT
			|| this.gbr == GreedyBinaryRelation.EST_SRPT
			|| this.gbr == GreedyBinaryRelation.EST_LRPT) {

			this.remainingProcessingTimes = new int[this.instance.numJobs][this.instance.numTasks];
			for (int job = 0; job < this.instance.numJobs; job++) {
				Arrays.fill(this.remainingProcessingTimes[job], -1);
			}
		}

		this.pendingOperations = new ArrayList<Task>();
		/* Initially only the first task of each job can be started */
		for (int job = 0; job < this.instance.numJobs; job++) {
			this.pendingOperations.add(new Task(job, 0));	
		}
	}

	/**
	 * Solve the instance problem.
	 * @param instance	The instance of the problem which needs be solved
	 * @param deadline	The ultimate time at which all the tasks should be finished
	 * @return		A Result infered by the built schedule
	 * @see			jobshop.Result
	 */
	@Override
	public Result solve(Instance instance, long deadline) {

		this.init(instance);

		JobNumbers sol = new JobNumbers(instance);

		while (!this.pendingOperations.isEmpty()) {
			Task op = this.getOptimalTask(this.pendingOperations);

			sol.jobs[sol.nextToSet++] = op.job;
			/* this date per machine table is needed for any EST_* binary relations */
			this.datePerMachine[this.instance.machine(op.job, op.task)] += this.instance.duration(op.job, op.task);
			/* Update the pending operations table by remove the chosen task */
			this.updatePendingOperations(op);
		}

		return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
	}

	/*
	 * Update the pending operations table by remove the chosen task 
	 * @param chosen	The task which will be removed and which the search for
	 *			new pending tasks will be based on	
	 */
	private void updatePendingOperations(Task chosen) {
		this.pendingOperations.remove(this.pendingOperations.indexOf(chosen));
		/* Maybe the task's job is now finished */
		if (chosen.task < this.instance.numTasks - 1) {
			/* The next job's task is pending */
			this.pendingOperations.add(new Task(chosen.job, chosen.task+1));
		}
	}

	/*
	 * Sorting depends on the chosen Greedy Binary Relation.
	 * @param arr	Task array to be sorted
	 * @return	The best task sorted according to the comparisonFunction binary relation
	 */
	private Task getOptimalTask(ArrayList<Task> arr) {
		switch (this.gbr) {
			case SPT: 	return this.getOptimalTask_SPT(arr);
			case LPT: 	return this.getOptimalTask_LPT(arr);
			case SRPT: 	return this.getOptimalTask_SRPT(arr);
			case LRPT:	return this.getOptimalTask_LRPT(arr);

			case EST_SPT: 	return this.getOptimalTask_EST_SPT(arr);
			case EST_LPT: 	return this.getOptimalTask_EST_LPT(arr);
			case EST_SRPT: 	return this.getOptimalTask_EST_SRPT(arr);
			case EST_LRPT:	return this.getOptimalTask_EST_LRPT(arr);
			/* Should never happen */
			default:	return (arr.isEmpty() ? null : arr.get(0));
		}
	}

	/*
	 * SPT, LPT, EST_SPT and EST_LPT general implementation.
	 * @param arr			Task array to be sorted
	 * @param comparisonFunction	Binary relation function which will compare
	 *				the current observed task with the optimum one 
	 * @return			The best task sorted according to the comparisonFunction binary relation
	 * @see				java.util.function.Bifunction
	 */
	private Task getOptimalTask_XPT(ArrayList<Task> arr,
					BiFunction<Task, Task, Boolean> comparisonFunction) {

		Task optimum = arr.get(0);

		for (int j = 1; j < arr.size(); j++) {
			Task currentTask = arr.get(j);

			if (comparisonFunction.apply(currentTask, optimum).booleanValue()) {
				optimum = arr.get(j);
			}	
		}
		return optimum;
	}

	/*
	 * SRPT, LRPT, EST_SRPT and EST_LRPT general implementation.
	 * @param arr			Task array to be sorted
	 * @param comparisonFunction	Binary relation function which will compare
	 *				the current observed task with the optimum one 
	 * @return			The best task sorted according to the comparisonFunction binary relation
	 * @see				java.util.function.Bifunction
	 */
	private Task getOptimalTask_XRPT(ArrayList<Task> arr,
					BiFunction<Task, Task, Boolean> comparisonFunction) {

		Task optimum = arr.get(0);

		for (int j = 1; j < arr.size(); j++) {
			Task currentTask = arr.get(j);
			/* The remaining processing time of that task has not been calculated yet */
			if (this.remainingProcessingTimes[currentTask.job][currentTask.task] == -1) {
				this.remainingProcessingTimes[currentTask.job][currentTask.task] = 0;
				for (int task = currentTask.task; task < this.instance.numTasks; task++) {
					this.remainingProcessingTimes[currentTask.job][currentTask.task] += this.instance.duration(currentTask.job, task);
				}
			}

			if (comparisonFunction.apply(currentTask, optimum).booleanValue()) {
				optimum = arr.get(j);
			}	
		}

		return optimum;
	}

	/*
	 * SPT implementation.
	 * @param arr			Task array to be sorted
	 * @return			The best task sorted according to the comparisonFunction binary relation
	 */
	private Task getOptimalTask_SPT(ArrayList<Task> arr) {
		return this.getOptimalTask_XPT(arr,
				(Task current, Task optimum)
				-> new Boolean(this.instance.duration(current.job, current.task) < this.instance.duration(optimum.job, optimum.task)));
	}

	/*
	 * LPT implementation.
	 * @param arr			Task array to be sorted
	 * @return			The best task sorted according to the comparisonFunction binary relation
	 */
	private Task getOptimalTask_LPT(ArrayList<Task> arr) {
		return this.getOptimalTask_XPT(arr,
				(Task current, Task optimum)
				-> new Boolean(this.instance.duration(current.job, current.task) > this.instance.duration(optimum.job, optimum.task)));
	}

	/*
	 * SRPT implementation.
	 * @param arr			Task array to be sorted
	 * @return			The best task sorted according to the comparisonFunction binary relation
	 */
	private Task getOptimalTask_SRPT(ArrayList<Task> arr) {
		return this.getOptimalTask_XRPT(arr,
				(Task current, Task optimum)
				-> new Boolean(this.remainingProcessingTimes[current.job][current.task] < this.remainingProcessingTimes[optimum.job][optimum.task]));
	}

	/*
	 * LRPT implementation.
	 * @param arr			Task array to be sorted
	 * @return			The best task sorted according to the comparisonFunction binary relation
	 */
	private Task getOptimalTask_LRPT(ArrayList<Task> arr) {
		return this.getOptimalTask_XRPT(arr,
				(Task current, Task optimum)
				-> new Boolean(this.remainingProcessingTimes[current.job][current.task] > this.remainingProcessingTimes[optimum.job][optimum.task]));
	}

	/*
	 * EST_SPT implementation.
	 * @param arr			Task array to be sorted
	 * @return			The best task sorted according to the comparisonFunction binary relation
	 */
	private Task getOptimalTask_EST_SPT(ArrayList<Task> arr) {
		return this.getOptimalTask_XPT(arr,
				(Task current, Task optimum)
				-> new Boolean(
					this.datePerMachine[this.instance.machine(current.job, current.task)] < this.datePerMachine[this.instance.machine(optimum.job, optimum.task)]
					&& this.instance.duration(current.job, current.task) < this.instance.duration(optimum.job, optimum.task)));
	}

	/*
	 * EST_LPT implementation.
	 * @param arr			Task array to be sorted
	 * @return			The best task sorted according to the comparisonFunction binary relation
	 */
	private Task getOptimalTask_EST_LPT(ArrayList<Task> arr) {
		return this.getOptimalTask_XPT(arr,
				(Task current, Task optimum)
				-> new Boolean(
					this.datePerMachine[this.instance.machine(current.job, current.task)] < this.datePerMachine[this.instance.machine(optimum.job, optimum.task)]
					&& this.instance.duration(current.job, current.task) > this.instance.duration(optimum.job, optimum.task)));
	}

	/*
	 * EST_SRPT implementation.
	 * @param arr			Task array to be sorted
	 * @return			The best task sorted according to the comparisonFunction binary relation
	 */
	private Task getOptimalTask_EST_SRPT(ArrayList<Task> arr) {
		return this.getOptimalTask_XRPT(arr,
				(Task current, Task optimum)
				-> new Boolean(this.datePerMachine[this.instance.machine(current.job, current.task)] < this.datePerMachine[this.instance.machine(optimum.job, optimum.task)]
				&& this.remainingProcessingTimes[current.job][current.task] < this.remainingProcessingTimes[optimum.job][optimum.task]));
	}

	/*
	 * EST_LRPT implementation.
	 * @param arr			Task array to be sorted
	 * @return			The best task sorted according to the comparisonFunction binary relation
	 */
	private Task getOptimalTask_EST_LRPT(ArrayList<Task> arr) {
		return this.getOptimalTask_XRPT(arr,
				(Task current, Task optimum)
				-> new Boolean(this.datePerMachine[this.instance.machine(current.job, current.task)] < this.datePerMachine[this.instance.machine(optimum.job, optimum.task)]
				&& this.remainingProcessingTimes[current.job][current.task] > this.remainingProcessingTimes[optimum.job][optimum.task]));
	}
}

