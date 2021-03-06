package jobshop;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jobshop.solvers.*;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


public class Main {

	private static final int DEFAULT_RANDOMNESS_LEVEL = 2;

	/** All solvers available in this program */
	private static HashMap<String, Solver> solvers;
	static {
		solvers = new HashMap<>();

		solvers.put("basic",			new BasicSolver());
		solvers.put("random",			new RandomSolver());

		/* GREEDY */
		solvers.put("greedyspt",		new GreedySolver(GreedyBinaryRelation.SPT));
		solvers.put("greedylpt",		new GreedySolver(GreedyBinaryRelation.LPT));
		solvers.put("greedysrpt",		new GreedySolver(GreedyBinaryRelation.SRPT));
		solvers.put("greedylrpt",		new GreedySolver(GreedyBinaryRelation.LRPT));

		solvers.put("greedyestspt",		new GreedySolver(GreedyBinaryRelation.EST_SPT));
		solvers.put("greedyestlpt",		new GreedySolver(GreedyBinaryRelation.EST_LPT));
		solvers.put("greedyestsrpt",		new GreedySolver(GreedyBinaryRelation.EST_SRPT));
		solvers.put("greedyestlrpt",		new GreedySolver(GreedyBinaryRelation.EST_LRPT));

		solvers.put("greedyspt_random", 	new GreedySolver(GreedyBinaryRelation.SPT, DEFAULT_RANDOMNESS_LEVEL));
		solvers.put("greedylpt_random", 	new GreedySolver(GreedyBinaryRelation.LPT, DEFAULT_RANDOMNESS_LEVEL));
		solvers.put("greedysrpt_random",	new GreedySolver(GreedyBinaryRelation.SRPT, DEFAULT_RANDOMNESS_LEVEL));
		solvers.put("greedylrpt_random", 	new GreedySolver(GreedyBinaryRelation.LRPT, DEFAULT_RANDOMNESS_LEVEL));

		solvers.put("greedyestspt_random", 	new GreedySolver(GreedyBinaryRelation.EST_SPT, DEFAULT_RANDOMNESS_LEVEL));
		solvers.put("greedyestlpt_random", 	new GreedySolver(GreedyBinaryRelation.EST_LPT, DEFAULT_RANDOMNESS_LEVEL));
		solvers.put("greedyestsrpt_random",	new GreedySolver(GreedyBinaryRelation.EST_SRPT, DEFAULT_RANDOMNESS_LEVEL));
		solvers.put("greedyestlrpt_random", 	new GreedySolver(GreedyBinaryRelation.EST_LRPT, DEFAULT_RANDOMNESS_LEVEL));

		/* DESCENT */
		solvers.put("descent",			new DescentSolver());

		solvers.put("descentspt",		new DescentSolver(new GreedySolver(GreedyBinaryRelation.SPT)));
		solvers.put("descentlpt",		new DescentSolver(new GreedySolver(GreedyBinaryRelation.LPT)));
		solvers.put("descentsrpt",		new DescentSolver(new GreedySolver(GreedyBinaryRelation.SRPT)));
		solvers.put("descentlrpt",		new DescentSolver(new GreedySolver(GreedyBinaryRelation.LRPT)));

		solvers.put("descentestspt",		new DescentSolver(new GreedySolver(GreedyBinaryRelation.EST_SPT)));
		solvers.put("descentestlpt",		new DescentSolver(new GreedySolver(GreedyBinaryRelation.EST_LPT)));
		solvers.put("descentestsrpt",		new DescentSolver(new GreedySolver(GreedyBinaryRelation.EST_SRPT)));
		solvers.put("descentestlrpt",		new DescentSolver(new GreedySolver(GreedyBinaryRelation.EST_LRPT)));

		solvers.put("descentspt_random", 	new DescentSolver(new GreedySolver(GreedyBinaryRelation.SPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("descentlpt_random", 	new DescentSolver(new GreedySolver(GreedyBinaryRelation.LPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("descentsrpt_random",	new DescentSolver(new GreedySolver(GreedyBinaryRelation.SRPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("descentlrpt_random", 	new DescentSolver(new GreedySolver(GreedyBinaryRelation.LRPT, DEFAULT_RANDOMNESS_LEVEL)));

		solvers.put("descentestspt_random", 	new DescentSolver(new GreedySolver(GreedyBinaryRelation.EST_SPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("descentestlpt_random", 	new DescentSolver(new GreedySolver(GreedyBinaryRelation.EST_LPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("descentestsrpt_random",	new DescentSolver(new GreedySolver(GreedyBinaryRelation.EST_SRPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("descentestlrpt_random", 	new DescentSolver(new GreedySolver(GreedyBinaryRelation.EST_LRPT, DEFAULT_RANDOMNESS_LEVEL)));

		/* TABOO */
		solvers.put("taboo",			new TabooSolver());

		solvers.put("taboospt",			new TabooSolver(new GreedySolver(GreedyBinaryRelation.SPT)));
		solvers.put("taboolpt",			new TabooSolver(new GreedySolver(GreedyBinaryRelation.LPT)));
		solvers.put("taboosrpt",		new TabooSolver(new GreedySolver(GreedyBinaryRelation.SRPT)));
		solvers.put("taboolrpt",		new TabooSolver(new GreedySolver(GreedyBinaryRelation.LRPT)));

		solvers.put("tabooestspt",		new TabooSolver(new GreedySolver(GreedyBinaryRelation.EST_SPT)));
		solvers.put("tabooestlpt",		new TabooSolver(new GreedySolver(GreedyBinaryRelation.EST_LPT)));
		solvers.put("tabooestsrpt",		new TabooSolver(new GreedySolver(GreedyBinaryRelation.EST_SRPT)));
		solvers.put("tabooestlrpt",		new TabooSolver(new GreedySolver(GreedyBinaryRelation.EST_LRPT)));

		solvers.put("taboospt_random",		new TabooSolver(new GreedySolver(GreedyBinaryRelation.SPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("taboolpt_random",		new TabooSolver(new GreedySolver(GreedyBinaryRelation.LPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("taboosrpt_random",		new TabooSolver(new GreedySolver(GreedyBinaryRelation.SRPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("taboolrpt_random", 	new TabooSolver(new GreedySolver(GreedyBinaryRelation.LRPT, DEFAULT_RANDOMNESS_LEVEL)));

		solvers.put("tabooestspt_random", 	new TabooSolver(new GreedySolver(GreedyBinaryRelation.EST_SPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("tabooestlpt_random", 	new TabooSolver(new GreedySolver(GreedyBinaryRelation.EST_LPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("tabooestsrpt_random",	new TabooSolver(new GreedySolver(GreedyBinaryRelation.EST_SRPT, DEFAULT_RANDOMNESS_LEVEL)));
		solvers.put("tabooestlrpt_random", 	new TabooSolver(new GreedySolver(GreedyBinaryRelation.EST_LRPT, DEFAULT_RANDOMNESS_LEVEL)));

		// add new solvers here
	}


	public static void main(String[] args) {
		ArgumentParser parser = ArgumentParsers.newFor("jsp-solver").build()
			.defaultHelp(true)
			.description("Solves jobshop problems.");

		parser.addArgument("-t", "--timeout")
			.setDefault(1L)
			.type(Long.class)
			.help("Solver timeout in seconds for each instance");
		parser.addArgument("--solver")
			.nargs("+")
			.required(true)
			.help("Solver(s) to use (space separated if more than one)");

		parser.addArgument("--instance")
			.nargs("+")
			.required(true)
			.help("Instance(s) to solve (space separated if more than one)");

		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

		PrintStream output = System.out;

		long solveTimeMs = ns.getLong("timeout") * 1000;

		List<String> solversToTest = ns.getList("solver");
		for(String solverName : solversToTest) {
			if(!solvers.containsKey(solverName)) {
				System.err.println("ERROR: Solver \"" + solverName + "\" is not avalaible.");
				System.err.println("       Available solvers: " + solvers.keySet().toString());
				System.err.println("       You can provide your own solvers by adding them to the `Main.solvers` HashMap.");
				System.exit(1);
			}
		}
		List<String> instancePrefixes = ns.getList("instance");
		List<String> instances = new ArrayList<>();
		for (String instancePrefix : instancePrefixes) {
			List<String> matches = BestKnownResult.instancesMatching(instancePrefix);
			if (matches.isEmpty()) {
				System.err.println("ERROR: instance prefix \"" + instancePrefix + "\" does not match any instance.");
				System.err.println("       available instances: " + Arrays.toString(BestKnownResult.instances));
				System.exit(1);
			}
			instances.addAll(matches);
		}

		float[] runtimes = new float[solversToTest.size()];
		float[] distances = new float[solversToTest.size()];

		try {
			output.print(  "                         ");
			for(String s : solversToTest)
				output.printf("%-30s", s);
			output.println();
			output.print("instance size  best      ");
			for(String s : solversToTest) {
				output.print("runtime makespan ecart        ");
			}
			output.println();


			for(String instanceName : instances) {
				int bestKnown = BestKnownResult.of(instanceName);


				Path path = Paths.get("instances/", instanceName);
				Instance instance = Instance.fromFile(path);

				output.printf("%-8s %-5s %4d      ",instanceName, instance.numJobs +"x"+instance.numTasks, bestKnown);

				for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
					String solverName = solversToTest.get(solverId);
					Solver solver = solvers.get(solverName);
					long start = System.currentTimeMillis();
					long deadline = System.currentTimeMillis() + solveTimeMs;
					Result result = solver.solve(instance, deadline);
					long runtime = System.currentTimeMillis() - start;

					if(!result.schedule.isValid()) {
						System.err.println("ERROR: solver returned an invalid schedule");
						System.exit(1);
					}

					assert result.schedule.isValid();
					int makespan = result.schedule.makespan();
					float dist = 100f * (makespan - bestKnown) / (float) bestKnown;
					runtimes[solverId] += (float) runtime / (float) instances.size();
					distances[solverId] += dist / (float) instances.size();

					output.printf("%7d %8s %5.1f        ", runtime, makespan, dist);
					output.flush();
				}
				output.println();

			}


			output.printf("%-8s %-5s %4s      ", "AVG", "-", "-");
			for(int solverId = 0 ; solverId < solversToTest.size() ; solverId++) {
				output.printf("%7.1f %8s %5.1f        ", runtimes[solverId], "-", distances[solverId]);
			}
			System.out.println();



		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
