package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.Random;

import java.lang.Integer;
import java.lang.Boolean;

/* Greedy solver implementing SPT, LPT, SRPT and LRPT and EST_* prioritizing methods */
public class GreedySolver implements Solver {


	/* General parameters */

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


	/* Randomness-enabled-only parameters */

	private boolean randomness;
	/* Tells us at which rate we have to invoke randomness.
	 * Basically tells us we invoke randomness at a rate of (1/randomnessLevel)
	 * The greater the randomnessLevel is, the less random the algorithm will be */
	private int randomnessLevel;


	/**
	 * Instantiate a Greedy Solver.
	 * @param gbr	The binary relation which defines how the operations (tasks)
	 *		will be sorted and prioritezed 
	 * @return	A new instance of GreedySolver
	 * @see		jobshop.solvers.GreedyBinaryRelation
	 */
	public GreedySolver(GreedyBinaryRelation gbr) { this.gbr = gbr; }
	/**
	 * Instantiate a Greedy Solver.
	 * @param gbr			The binary relation which defines how the operations (tasks)
	 *				will be sorted and prioritezed 
	 * @param randomnessLevel	Basically tells us we invoke randomness at a rate of (1/randomnessLevel).
	 *				The greater the randomnessLevel is, the less random the algorithm will be.
	 *				This value needs to be greater than 0 though. A 0 value means no randomness at all.
	 * @return			A new instance of GreedySolver
	 * @see				jobshop.solvers.GreedyBinaryRelation
	 */
	public GreedySolver(GreedyBinaryRelation gbr, int randomnessLevel) {
		this.gbr = gbr;
		this.randomnessLevel	= randomnessLevel;
		this.randomness 	= (this.randomnessLevel > 0 	? true 			: false);
	}

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
		ArrayList<Task> arg;
		/* This is where we translate randomness :
		 * -	If the array contains at least 'this.randomnessLevel' elements,
		 * 	then we randomly remove (arr.size()/this.randomnessLevel) elements
		 *	from the array 'arr'. 
		 * -	Otherwise we do not apply random operations  */
		if (this.randomness) {
			if (this.randomnessLevel == 1) {
				return arr.get(new Random().nextInt(arr.size()));
			} 

			/* Make a shallow copy of arr in order to be able to modify it */
			arg = new ArrayList<Task>(arr);
			for (int i = 0; i < (arr.size() / this.randomnessLevel); i++) {
				arg.remove(new Random().nextInt(arg.size()));
			}

		} else { arg = arr; }

		switch (this.gbr) {
			case SPT: 	return this.getOptimalTask_SPT(arg);
			case LPT: 	return this.getOptimalTask_LPT(arg);
			case SRPT: 	return this.getOptimalTask_SRPT(arg);
			case LRPT:	return this.getOptimalTask_LRPT(arg);

			case EST_SPT: 	return this.getOptimalTask_EST_SPT(arg);
			case EST_LPT: 	return this.getOptimalTask_EST_LPT(arg);
			case EST_SRPT: 	return this.getOptimalTask_EST_SRPT(arg);
			case EST_LRPT:	return this.getOptimalTask_EST_LRPT(arg);
			/* Should never happen */
			default:	return (arg.isEmpty() ? null : arg.get(0));
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

