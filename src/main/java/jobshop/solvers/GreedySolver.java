package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;

import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.Random;

import java.lang.Integer;
import java.lang.Boolean;

/** 
 * Greedy solver implementing SPT, LPT, SRPT and LRPT and EST_* prioritizing methods,
 * a randomness implementation for each of these binary relations can be enabled too.
 */
public class GreedySolver implements Solver {


	/* General parameters */

	private Instance instance;
	/* The date at which each machine (resource) is available */
	private int[] machineAvailabilityDate;
	/* The date at which each task is going to start */
	private int[][] taskStartingDate;
	/* Needed for XRPT binary relations */
	private int[][] remainingProcessingTimes;
	/* All the currently pending operations (tasks), updated at each loop of the greedy algorithm */
	private ArrayList<Task> pendingOperations;
	/* The binary relation which defines how the operations (tasks)
	 * will be sorted and prioritized */
	private GreedyBinaryRelation gbr;	
	/* The function which translates the chosen binary relation.
	 * Used when sorting pending tasks in order to find the optimal one.
	 * This function actually contains the binary relation and should tell us
	 * whether the the first argument (currently analyzed task) is better than
	 * the second argument (the current optimal task).
	 * Therefore it takes two tasks, and return a boolean */
	private BiFunction<Task, Task, Boolean> compareTwoTasks;


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
		this.randomness 	= (this.randomnessLevel > 0 ? true : false);
	}

	/*
	 * Initialize the basic elements arrays, should be called inside this.solve(Instance) function,
	 * since it is the starting point at which we know the instance which we will work on
	 */
	private void init(Instance instance) {
		this.instance = instance;

		if (this.gbr == GreedyBinaryRelation.EST_SPT
			|| this.gbr == GreedyBinaryRelation.EST_LPT
			|| this.gbr == GreedyBinaryRelation.EST_SRPT
			|| this.gbr == GreedyBinaryRelation.EST_LRPT) {

			this.machineAvailabilityDate = new int[this.instance.numMachines];
			Arrays.fill(this.machineAvailabilityDate, 0);

			this.taskStartingDate = new int[this.instance.numJobs][this.instance.numTasks];
		}

		/* XRPT binary relations look for the remaining processing time of each task */
		if (this.gbr == GreedyBinaryRelation.SRPT
			|| this.gbr == GreedyBinaryRelation.LRPT
			|| this.gbr == GreedyBinaryRelation.EST_SRPT
			|| this.gbr == GreedyBinaryRelation.EST_LRPT) {

			this.remainingProcessingTimes = new int[this.instance.numJobs][this.instance.numTasks];
			/* The remaining processing time of one task is basically the sum of the duration of 
			 * the remaining tasks of the same job */
			for (int job = 0; job < this.instance.numJobs; job++) {
				this.remainingProcessingTimes[job][this.instance.numTasks - 1] = this.instance.duration(job, this.instance.numTasks - 1);
				for (int task = this.instance.numTasks - 2; task >= 0; task--) {
					this.remainingProcessingTimes[job][task] = this.remainingProcessingTimes[job][task + 1] + this.instance.duration(job, task);
				}
			}
		}

		this.pendingOperations = new ArrayList<Task>();
		/* Initially only the first task of each job can be started */
		for (int job = 0; job < this.instance.numJobs; job++) {
			this.pendingOperations.add(new Task(job, 0));	
		}

		/* Setting the binary relation comparison function used when sorting the pending tasks */
		switch (this.gbr) {
			case SPT: 
				this.compareTwoTasks = (Task current, Task optimum)
					-> new Boolean(this.instance.duration(current) < this.instance.duration(optimum));
				break;
			case LPT: 
				this.compareTwoTasks = (Task current, Task optimum)
					-> new Boolean(this.instance.duration(current) > this.instance.duration(optimum));
				break;
			case SRPT: 
				this.compareTwoTasks = (Task current, Task optimum)
					-> new Boolean(this.remainingProcessingTimes[current.job][current.task] < this.remainingProcessingTimes[optimum.job][optimum.task]);
				break;
			case LRPT: 
				this.compareTwoTasks = (Task current, Task optimum)
					-> new Boolean(this.remainingProcessingTimes[current.job][current.task] > this.remainingProcessingTimes[optimum.job][optimum.task]);
				break;

			case EST_SPT: 
				this.compareTwoTasks = (Task current, Task optimum)
					->
					{ 
						int cest = this.getEarliestStartingTime(current);
						int oest = this.getEarliestStartingTime(optimum);

						return new Boolean((cest < oest) || (cest == oest && this.instance.duration(current) < this.instance.duration(optimum)));
					};
				break;
			case EST_LPT: 
				this.compareTwoTasks = (Task current, Task optimum)
					->
					{ 
						int cest = this.getEarliestStartingTime(current);
						int oest = this.getEarliestStartingTime(optimum);

						return new Boolean((cest < oest) || (cest == oest && this.instance.duration(current) > this.instance.duration(optimum)));
					};
				break;
			case EST_SRPT:
				this.compareTwoTasks = (Task current, Task optimum)
					-> 
					{ 
						int cest = this.getEarliestStartingTime(current);
						int oest = this.getEarliestStartingTime(optimum);

						return new Boolean((cest < oest) || (cest == oest && this.remainingProcessingTimes[current.job][current.task] < this.remainingProcessingTimes[optimum.job][optimum.task]));
					};
				break;
			case EST_LRPT:
				this.compareTwoTasks = (Task current, Task optimum)
					-> 
					{ 
						int cest = this.getEarliestStartingTime(current);
						int oest = this.getEarliestStartingTime(optimum);

						return new Boolean((cest < oest) || (cest == oest && this.remainingProcessingTimes[current.job][current.task] > this.remainingProcessingTimes[optimum.job][optimum.task]));
					};
				break;
			/* Should never happen */
			default:
				System.err.println("ERROR: Binary relation '" + this.gbr + "' not recognized");
				System.exit(1);
				break;
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

		ResourceOrder sol = new ResourceOrder(instance);

		while (!this.pendingOperations.isEmpty()/* && System.currentTimeMillis() < deadline*/) {

			Task op = this.getOptimalTask(this.pendingOperations);
			int op_machine = instance.machine(op);

			/* feed our resource ordered solution with the newly found optimal task */
			sol.tasksByMachine[op_machine][sol.nextFreeSlot[op_machine]++] = op;

			/* Any EST_* binary relations related processing need to keep track of 
			 * the starting date of each task and the date at which each machine is available */
			if (this.gbr == GreedyBinaryRelation.EST_SPT
				|| this.gbr == GreedyBinaryRelation.EST_LPT
				|| this.gbr == GreedyBinaryRelation.EST_SRPT
				|| this.gbr == GreedyBinaryRelation.EST_LRPT) {

				this.taskStartingDate[op.job][op.task] = this.getEarliestStartingTime(op);
				this.machineAvailabilityDate[op_machine] += this.instance.duration(op);
			}
			/* Update the pending operations table by removing the chosen task */
			this.updatePendingOperations(op);
		}

		return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
	}

	/*
	 * Update the pending operations table by removing the chosen task 
	 * @param chosen	The task which will be removed and which the search for
	 *			new pending tasks will be based on	
	 */
	private void updatePendingOperations(Task chosen) {
		this.pendingOperations.remove(this.pendingOperations.indexOf(chosen));
		/* Maybe the task's job is not finished */
		if (chosen.task < this.instance.numTasks - 1) {
			/* The next job's task is now pending */
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
		 * -	Otherwise we do not apply any random operations */
		if (this.randomness) {
			if (this.randomnessLevel == 1) {
				return arr.get(new Random().nextInt(arr.size()));
			} 

			/* Make a shallow copy of arr to be able to modify it */
			arg = new ArrayList<Task>(arr);
			for (int i = 0; i < (arr.size() / this.randomnessLevel); i++) {
				arg.remove(new Random().nextInt(arg.size()));
			}

		} else { arg = arr; }

		Task optimum = arg.get(0);

		for (int j = 1; j < arg.size(); j++) {
			Task currentTask = arg.get(j);

			if (this.compareTwoTasks.apply(currentTask, optimum).booleanValue()) {
				optimum = arg.get(j);
			}	
		}

		return optimum;
	}

	/*
	 * Only for EST_* binary relations
	 * @param t	The task we want to get the earliest starting time of
	 */
	private int getEarliestStartingTime(Task t) {
		int est;
		est = t.task == 0 ? 0 : this.taskStartingDate[t.job][t.task - 1] + this.instance.duration(t.job, t.task - 1);
		est = Math.max(est, this.machineAvailabilityDate[this.instance.machine(t)]);
		return est;
	}
}

