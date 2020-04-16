package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.Schedule;

import jobshop.encodings.Task;
import jobshop.encodings.ResourceOrder;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class DescentSolver implements Solver {

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

			Task tmp = order.tasksByMachine[this.machine][this.t1];

			order.tasksByMachine[this.machine][this.t1] = order.tasksByMachine[this.machine][this.t2];
			order.tasksByMachine[this.machine][this.t2] = tmp;
		}
	}


	@Override
	public Result solve(Instance instance, long deadline) {
		GreedySolver initialSolver = new GreedySolver(GreedyBinaryRelation.SPT);

		List<Block> blocks;

		ResourceOrder bestSolution = new ResourceOrder(initialSolver.solve(instance, deadline).schedule);
		int bestSolutionMakespan = bestSolution.toSchedule().makespan();

		boolean stuck = false;

		while (System.currentTimeMillis() > deadline || stuck) {

			stuck = true;

			for (Block block : this.blocksOfCriticalPath(bestSolution)) {

				for (Swap s : this.neighbors(block)) {
					ResourceOrder test = bestSolution.copy();
					s.applyOn(test);
		
					int testMakespan = test.toSchedule().makespan();
					if (testMakespan < bestSolutionMakespan) {
						bestSolution = test.copy();
						bestSolutionMakespan = testMakespan; 
						stuck = false;
					}
				}
			}
		}

		return new Result(instance, bestSolution.toSchedule(), Result.ExitCause.Blocked);
	}

	/** Returns a list of all blocks of the critical path. */
	List<Block> blocksOfCriticalPath(ResourceOrder order) {
		ArrayList<Block> blockList = new ArrayList<Block>();

		List<Task> criticalPath = order.toSchedule().criticalPath();

		ArrayList<Integer> indexes = new ArrayList<Integer>();
		int[][] visitedMachines = new int[order.instance.numMachines][2];

		for (int i = 0; i < order.instance.numMachines; i++) {
			Arrays.fill(visitedMachines[i], -1);
		}

		for (Task t : criticalPath) {
			int currentMachine = order.instance.machine(t.job, t.task);
			int[] current = visitedMachines[currentMachine];
			int taskIndex = -1;
			/* let's find the task's index in the resource order table */
			for (int i = 0; i < order.tasksByMachine[currentMachine].length; i++) {
				if (order.tasksByMachine[currentMachine][i] == t) {
					taskIndex = i;	
					break;
				}
			}

			if (current[0] == -1) {
				current[0] = taskIndex;
			} else {
				indexes.add(new Integer(currentMachine));
				current[1] = taskIndex;
			}
		}

		for (Integer i: indexes) {
			int[] tt = visitedMachines[i.intValue()];
			blockList.add(new Block(i, tt[0], tt[1]));
		}

		return blockList;
	}

	/** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
	List<Swap> neighbors(Block block) {
		List<Swap> neighbours = new ArrayList<Swap>();

		neighbours.add(new Swap(block.machine, block.firstTask, block.firstTask+1));
		neighbours.add(new Swap(block.machine, block.lastTask, block.lastTask-1));
	
		return neighbours;
	}
}

