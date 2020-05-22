package jobshop.solvers;

import jobshop.Solver;

import jobshop.encodings.Task;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.JobNumbers;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class NeighborExplorationSolver {

	protected Solver initialSolver;

	public NeighborExplorationSolver() {
		this.initialSolver = new GreedySolver(GreedyBinaryRelation.SPT);
	}

	public NeighborExplorationSolver(Solver initialSolver) {
		this.initialSolver = initialSolver;
	}

	/** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
	 * This class identifies a block in a ResourceOrder representation.
	 *
	 * Consider the solution in ResourceOrder representation
	 * machine 0 : (0,1) (1,2) (2,2)
	 * machine 1 : (0,2) (2,1) (1,1)
	 * machine 2 : ...
	 *
	 * The block with : machine = 1, firstTask= 0 and lastTask = 1
	 * Represent the task sequence : [(0,2) (2,1)]
	 *
	 * */
	static class Block {
		/** machine on which the block is identified */
		final int machine;
		/** index of the first task of the block */
		final int firstTask;
		/** index of the last task of the block */
		final int lastTask;

		Block(int machine, int firstTask, int lastTask) {
			this.machine = machine;
			this.firstTask = firstTask;
			this.lastTask = lastTask;
		}
	}

	/**
	 * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
	 *
	 * Consider the solution in ResourceOrder representation
	 * machine 0 : (0,1) (1,2) (2,2)
	 * machine 1 : (0,2) (2,1) (1,1)
	 * machine 2 : ...
	 *
	 * The swap with : machine = 1, t1= 0 and t2 = 1
	 * Represent inversion of the two tasks : (0,2) and (2,1)
	 * Applying this swap on the above resource order should result in the following one :
	 * machine 0 : (0,1) (1,2) (2,2)
	 * machine 1 : (2,1) (0,2) (1,1)
	 * machine 2 : ...
	 */
	static class Swap {
		// machine on which to perform the swap
		final int machine;
		// index of one task to be swapped
		final int t1;
		// index of the other task to be swapped
		final int t2;

		Swap(int machine, int t1, int t2) {
			this.machine = machine;
			this.t1 = t1;
			this.t2 = t2;
		}

		/** Apply this swap on the given resource order, transforming it into a new solution. */
		public void applyOn(ResourceOrder order) {

			int tmp_job 	= order.tasksByMachine[this.machine][this.t1].job;
			int tmp_task 	= order.tasksByMachine[this.machine][this.t1].task;
			Task tmp = new Task(tmp_job, tmp_task);

			order.tasksByMachine[this.machine][this.t1] = new Task(order.tasksByMachine[this.machine][this.t2].job,
										order.tasksByMachine[this.machine][this.t2].task);

			order.tasksByMachine[this.machine][this.t2] = tmp;
		}
	}

	private int getTaskIndex(Task t, ResourceOrder order) {
		int currentMachine = order.instance.machine(t.job, t.task);
		int taskIndex = -1;
		/* let's find the task's index in the resource order table */
		for (int i = 0; i < order.tasksByMachine[currentMachine].length; i++) {
			if (order.tasksByMachine[currentMachine][i].equals(t)) {
				taskIndex = i;	
				break;
			}
		}
		return taskIndex;
	}

	/** Returns a list of all blocks of the critical path. */
	protected List<Block> blocksOfCriticalPath(ResourceOrder order) {

		ArrayList<Block> blocks = new ArrayList<Block>();

		int currentMachine = -1;
		int firstTaskIndex = -1;
		int lastTaskIndex = -1;
		boolean atLeastTwo = false;
		
		for (Task t : order.toSchedule().criticalPath()) {
			int machine = order.instance.machine(t);

			if (currentMachine == -1) {
				currentMachine = machine;	
				firstTaskIndex = this.getTaskIndex(t, order);
				continue;
			}

			if (machine == currentMachine) {
				lastTaskIndex = this.getTaskIndex(t, order);
				atLeastTwo = true;
			} else {
				if (atLeastTwo == true) {
					blocks.add(new Block(currentMachine, firstTaskIndex, lastTaskIndex));
				}
				currentMachine = machine;	
				firstTaskIndex = this.getTaskIndex(t, order);
				atLeastTwo = false;
			}
		}

		return blocks;
	}

	/** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
	protected List<Swap> neighbors(Block block) {

		List<Swap> neighbours = new ArrayList<Swap>();

		neighbours.add(new Swap(block.machine, block.firstTask,	block.firstTask + 1));

		if (block.firstTask != block.lastTask - 1) {
			neighbours.add(new Swap(block.machine, block.lastTask - 1, block.lastTask));
		}
	
		return neighbours;
	}

	static class BlockJobNumbers {
		/** machine on which the block is identified */
		final int machine;
		/** index of the first job of the block */
		final int firstJob;
		final int secondFirstJob;

		final int secondLastJob;
		/** index of the last job of the block */
		final int lastJob;

		BlockJobNumbers(int machine, int firstJob, int secondFirstJob, int secondLastJob, int lastJob) {
			this.machine = machine;
			this.firstJob = firstJob;
			this.secondFirstJob = secondFirstJob;
			this.secondLastJob = secondLastJob;
			this.lastJob = lastJob;
		}
	}

	static class SwapJobNumbers {
		/** machine on which the block is identified */
		final int machine;
		// index of one job to be swapped
		final int j1;
		// index of the other job to be swapped
		final int j2;

		SwapJobNumbers(int machine, int j1, int j2) {
			this.machine = machine;
			this.j1 = j1;
			this.j2 = j2;
		}

		/** Apply this swap on the given resource order, transforming it into a new solution. */
		public void applyOn(JobNumbers order) {
			int tmp = order.jobs[this.j1];

			order.jobs[this.j1] = order.jobs[this.j2];
			order.jobs[this.j2] = tmp;
		}
	}

	private int getTaskIndex(Task t, JobNumbers order) {
		int taskIndex = -1;
		/* let's find the task's index in the job numbers table */
		int jobCounter = -1;
		for (int i = 0; i < order.jobs.length; i++) {
			if (order.jobs[i] == t.job) {
				jobCounter++;
			}

			if (jobCounter == t.task) {
				taskIndex = i;
				break;
			}
		}
		return taskIndex;
	}

	/** Returns a list of all blocks of the critical path. */
	protected List<BlockJobNumbers> blocksOfCriticalPath(JobNumbers order) {

		ArrayList<BlockJobNumbers> blocks = new ArrayList<BlockJobNumbers>();

		int currentMachine = -1;

		int firstTaskIndex 		= -1;
		int secondFirstTaskIndex 	= -1;
		int secondLastTaskIndex 	= -1;
		int lastTaskIndex 		= -1;

		boolean atLeastTwo = false;
		
		for (Task t : order.toSchedule().criticalPath()) {
			int machine = order.instance.machine(t);

			if (currentMachine == -1) {
				currentMachine = machine;	
				firstTaskIndex = this.getTaskIndex(t, order);
				continue;
			}

			if (machine == currentMachine) {
				if (secondFirstTaskIndex == -1) {
					secondFirstTaskIndex = this.getTaskIndex(t, order);
				} else {
					secondLastTaskIndex = lastTaskIndex;
					lastTaskIndex = this.getTaskIndex(t, order);
				}
				atLeastTwo = true;
			} else {
				if (atLeastTwo == true) {
					blocks.add(new BlockJobNumbers(currentMachine, firstTaskIndex, secondFirstTaskIndex, secondLastTaskIndex, lastTaskIndex));
				}

				currentMachine = machine;	

				firstTaskIndex 		= this.getTaskIndex(t, order);
				secondFirstTaskIndex 	= -1;
				secondLastTaskIndex 	= -1;
				lastTaskIndex 		= -1;

				atLeastTwo = false;
			}
		}

		return blocks;
	}

	/** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
	protected List<SwapJobNumbers> neighbors(BlockJobNumbers block) {

		List<SwapJobNumbers> neighbors = new ArrayList<SwapJobNumbers>();

		neighbors.add(new SwapJobNumbers(block.machine, block.firstJob, block.secondFirstJob));

		if (block.lastJob != -1) {
			if (block.secondLastJob != -1) {
				neighbors.add(new SwapJobNumbers(block.machine, block.secondLastJob, block.lastJob));
			} else {
				neighbors.add(new SwapJobNumbers(block.machine, block.secondFirstJob, block.lastJob));
			}
		}
	
		return neighbors;
	}
}

