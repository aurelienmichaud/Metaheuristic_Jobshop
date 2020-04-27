package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.Schedule;

import jobshop.encodings.ResourceOrder;

import java.util.List;

public class DescentSolver extends NeighborExplorationSolver implements Solver {

	public DescentSolver() {
		super();
	}

	/* The solver used to get the initial solution we will base our neighbour search on */
	public DescentSolver(Solver initialSolver) {
		super(initialSolver);
	}

	@Override
	public Result solve(Instance instance, long deadline) {

		List<Block> blocks;

		ResourceOrder bestSolution = new ResourceOrder(this.initialSolver.solve(instance, deadline).schedule);
		int bestSolutionMakespan = bestSolution.toSchedule().makespan();


		/* stuck means we have not found any better neighbour */
		boolean stuck = false;

		while (!stuck && System.currentTimeMillis() < deadline) {

			ResourceOrder bestTmpSolution	= null;
			int bestTmpSolutionMakespan	= -1;

			stuck = true;

			for (Block block : this.blocksOfCriticalPath(bestSolution)) {

				for (Swap s : this.neighbors(block)) {
					ResourceOrder test = bestSolution.copy();
					int testMakespan;

					s.applyOn(test);
					
					Schedule testSchedule = test.toSchedule();	

					if (testSchedule != null) {
						testMakespan = testSchedule.makespan();

						if (bestTmpSolution == null) {
							bestTmpSolution = test.copy();
							bestTmpSolutionMakespan = testMakespan;
						}
						/* 'test' happens to be better than the current bestSolution
						 * -> we update bestSolution and keep going */
						if (testMakespan < bestTmpSolutionMakespan) {
							bestTmpSolution = test.copy();
							bestTmpSolutionMakespan = testMakespan; 
							stuck = false;
						}
					}
				}
			}

			if (!stuck) {
				bestSolution = bestTmpSolution.copy();
				bestSolutionMakespan = bestTmpSolutionMakespan;
			}
		}

		return new Result(instance, bestSolution.toSchedule(), Result.ExitCause.Blocked);
	}
}

