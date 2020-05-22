package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.Schedule;

import jobshop.encodings.ResourceOrder;
import jobshop.encodings.JobNumbers;

import java.util.Arrays;
import java.util.List;

public class TabooSolver extends NeighborExplorationSolver implements Solver {

	private int iterationMax = 100000;
	private int swapLivingTime = 2;

	public TabooSolver() {
		super();
	}

	/* The solver used to get the initial solution we will base our neighbour search on */
	public TabooSolver(Solver initialSolver) {
		super(initialSolver);
	}

	@Override
	public Result solve(Instance instance, long deadline) {
		return this.solveResourceOrder(instance, deadline);
		//return this.solveJobNumbers(instance, deadline);
	}

	private Result solveResourceOrder(Instance instance, long deadline) {

		int iterationCounter = 0;

		int[][] solutionTaboos = new int[instance.numTasks * instance.numMachines][instance.numTasks * instance.numMachines];

		for (int i = 0; i < (instance.numTasks * instance.numMachines); i++) {
			Arrays.fill(solutionTaboos[i], 0);
		}

		ResourceOrder bestSolution = new ResourceOrder(this.initialSolver.solve(instance, deadline).schedule);
		int bestSolutionMakespan = bestSolution.toSchedule().makespan();

		ResourceOrder bestCurrentSolution 	= bestSolution;
		int bestCurrentSolutionMakespan		= bestSolutionMakespan;

		boolean stuck = false;

		while (iterationCounter < this.iterationMax && System.currentTimeMillis() < deadline) {

			stuck = true;

			iterationCounter++;

			ResourceOrder bestTmpSolution 		= null;
			int bestTmpSolutionMakespan 		= -1;

			int bestTmpSolutionMachine		= -1;
			int bestTmpSolutionTaskIndexT1		= -1;
			int bestTmpSolutionTaskIndexT2		= -1;

			for (Block block : this.blocksOfCriticalPath(bestSolution)) {

				for (Swap s : this.neighbors(block)) {

					int taskIndexT1 = bestSolution.tasksByMachine[s.machine][s.t1].task;
					int taskIndexT2 = bestSolution.tasksByMachine[s.machine][s.t2].task;

					/* Still living swaps are taboos, and we do not consider them */
					if (iterationCounter >= solutionTaboos[s.machine * instance.numTasks + taskIndexT2][s.machine * instance.numTasks + taskIndexT1]) {

						ResourceOrder test = bestCurrentSolution.copy();
						int testMakespan;
						s.applyOn(test);

						Schedule testSchedule = test.toSchedule();

						if (testSchedule != null) {
							testMakespan = test.toSchedule().makespan();

							/* 'test' happens to be better than the current bestSolution
							 * -> we update bestSolution and keep going */
							if (bestTmpSolution == null || testMakespan < bestTmpSolutionMakespan) {

								bestTmpSolution 		= test.copy();
								bestTmpSolutionMakespan 	= testMakespan;

								bestTmpSolutionMachine		= s.machine;
								bestTmpSolutionTaskIndexT1 	= taskIndexT1;
								bestTmpSolutionTaskIndexT2 	= taskIndexT2;

								stuck = false;
							}
						}

					} 
				}
			}


			if (bestTmpSolution != null) {

				solutionTaboos[bestTmpSolutionMachine * instance.numTasks + bestTmpSolutionTaskIndexT2][bestTmpSolutionMachine * instance.numTasks + bestTmpSolutionTaskIndexT1] = iterationCounter + this.swapLivingTime;

				bestCurrentSolution = bestTmpSolution.copy();
				bestCurrentSolutionMakespan = bestTmpSolutionMakespan;

				if (bestTmpSolutionMakespan < bestSolutionMakespan) {
					bestSolution = bestTmpSolution.copy();
					bestSolutionMakespan = bestTmpSolutionMakespan;
				}
			} 

		}

		return new Result(instance, bestSolution.toSchedule(), Result.ExitCause.Blocked);
	}

	private Result solveJobNumbers(Instance instance, long deadline) {

		int iterationCounter = 0;

		int[][] solutionTaboos = new int[instance.numTasks * instance.numMachines][instance.numTasks * instance.numMachines];

		for (int i = 0; i < (instance.numTasks * instance.numMachines); i++) {
			Arrays.fill(solutionTaboos[i], 0);
		}

		JobNumbers bestSolution = new JobNumbers(this.initialSolver.solve(instance, deadline).schedule);
		int bestSolutionMakespan = bestSolution.toSchedule().makespan();

		JobNumbers bestCurrentSolution 		= bestSolution;
		int bestCurrentSolutionMakespan		= bestSolutionMakespan;

		boolean stuck = false;

		while (iterationCounter < this.iterationMax && System.currentTimeMillis() < deadline) {

			stuck = true;

			iterationCounter++;

			JobNumbers bestTmpSolution 		= null;
			int bestTmpSolutionMakespan 		= -1;

			int bestTmpSolutionMachine		= -1;
			int bestTmpSolutionTaskIndexT1		= -1;
			int bestTmpSolutionTaskIndexT2		= -1;

			for (BlockJobNumbers block : this.blocksOfCriticalPath(bestSolution)) {

				for (SwapJobNumbers s : this.neighbors(block)) {

					int taskIndexT1 = -1;
					int taskIndexT2 = -1;

					int jobCounter1 = 0;
					int jobCounter2 = 0;
					for (int i = 0; i < bestSolution.jobs.length; i++) {

						if(bestSolution.jobs[s.j1] == bestSolution.jobs[i]) { jobCounter1++; }
						if(bestSolution.jobs[s.j2] == bestSolution.jobs[i]) { jobCounter2++; }

						if (i == s.j1) { taskIndexT1 = (jobCounter1 - 1); }
						if (i == s.j2) { taskIndexT2 = (jobCounter2 - 1); }

						if (taskIndexT1 >= 0 && taskIndexT2 >= 0) { break; }
					}

					/* Still living swaps are taboos, and we do not consider them */
					if (iterationCounter >= solutionTaboos[s.machine * instance.numTasks + taskIndexT2][s.machine * instance.numTasks + taskIndexT1]) {

						JobNumbers test = bestCurrentSolution.copy();
						int testMakespan;
						s.applyOn(test);

						Schedule testSchedule = test.toSchedule();

						if (testSchedule != null) {
							testMakespan = test.toSchedule().makespan();

							/* 'test' happens to be better than the current bestSolution
							 * -> we update bestSolution and keep going */
							if (bestTmpSolution == null || testMakespan < bestTmpSolutionMakespan) {
								bestTmpSolution 		= test.copy();
								bestTmpSolutionMakespan 	= testMakespan; 

								bestTmpSolutionMachine		= s.machine;
								bestTmpSolutionTaskIndexT1 	= taskIndexT1;
								bestTmpSolutionTaskIndexT2 	= taskIndexT2;

								stuck = false;
							}
						}
					} 
				}
			}

			if (bestTmpSolution != null) {

				solutionTaboos[bestTmpSolutionMachine * instance.numTasks + bestTmpSolutionTaskIndexT2][bestTmpSolutionMachine * instance.numTasks + bestTmpSolutionTaskIndexT1] = iterationCounter + this.swapLivingTime;

				bestCurrentSolution = bestTmpSolution.copy();
				bestCurrentSolutionMakespan = bestTmpSolutionMakespan;

				if (bestTmpSolutionMakespan < bestSolutionMakespan) {
					bestSolution = bestTmpSolution.copy();
					bestSolutionMakespan = bestTmpSolutionMakespan;
				}
			} 

		}

		return new Result(instance, bestSolution.toSchedule(), Result.ExitCause.Blocked);
	}
}

