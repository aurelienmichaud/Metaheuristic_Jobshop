# Heuristic methods for JobShop scheduling

This repository contains the starter code for the assignment.


## Run the program

```
Usage:
./run main SOLVER INSTANCE							: Basically doing 'java -jar build/libs/JSP.jar --solver SOLVER --instance INSTANCE'
./run main -b SOLVER INSTANCE							: 'gradle build' and 'gradle jar' is performed before running the program
./run main --solver SOLVER [SOLVER2...] --instance INSTANCE [INSTANCE2...]	: Running each solver SOLVERs on INSTANCEs.
./run main -b --solver SOLVER [SOLVER2...] --instance INSTANCE [INSTANCE2...]	: Compiling & Running each solver SOLVERs on INSTANCEs.
```

e.g.

```
❯ ./run main basic ft06  # Run (and thus compile before if not already compiled) the basic algorithm on the instance ft06
```
The command line above indicates that we want to solve the instance named`ft06` with the `basic` solver. It should give an output like the following :

Forcing the compilation and rebuild is also possible. 
```
❯ ./run main -b basic ft06  # Compile and then run the greedy spt algorithm on the instance ft06
```

```
                         basic
instance size  best      runtime makespan ecart
ft06     6x6     55            1       60   9.1
AVG      -        -          1.0        -   9.1
```

Fields in the result view are the following :
- `instance`: name of the instance
- `size`: size of the instance `{nom-jobs}x{num-tasks}`
- `best`: best known resultfor this instance
- `runtime`: time taken by the solver in milliseconds (rounded)
- `makespan`: makespan of the solution
- `ecart`: normalized distance to the best result: `100 * (makespan - best) / best` 

One can also specify multiple solvers (below `basic` and `random`) and instances (below `ft06`, `ft10` and `ft20`) for simultaneous testing:

```
❯ java -jar build/libs/JSP.jar --solver basic random --instance ft06 ft10 ft20

                         basic                         random
instance size  best      runtime makespan ecart        runtime makespan ecart
ft06     6x6     55            1       60   9.1            999       55   0.0
ft10     10x10  930            0     1319  41.8            999     1209  30.0
ft20     20x5  1165            0     1672  43.5            999     1529  31.2
AVG      -        -          0.3        -  31.5          999.0        -  20.4
```
Here the last line give the average `runtime` and `ecart` for each solver.

```
usage: jsp-solver [-h]  [-t TIMEOUT] --solver SOLVER [SOLVER ...]
                  --instance INSTANCE [INSTANCE ...]

Solves jobshop problems.

named arguments:
  -h, --help             show this help message and exit
  --solver SOLVER [SOLVER ...]
                         Solver(s) to use  (space  separated  if  more than
                         one)
  -t TIMEOUT, --timeout TIMEOUT
                         Solver  timeout  in  seconds   for  each  instance
                         (default: 1)
  --instance INSTANCE [INSTANCE ...]
                         Instance(s) to  solve  (space  separated  if  more
                         than one)


```

## Run script

The `run` script comes with several commands and features of the form `./run COMMAND [ARGS...]`. Before using a command please do `./run COMMAND` without any arguments to get more information about how to use it. Do not worry though, these commands are straightforward.

```
❯ ./run
Usage:
./run main [--solver] SOLVER [--instance] INSTANCE		: Run SOLVER algorithm on instance INSTANCE. Please run './run main' for more information
./run other JAVA_CLASS						: Run the class JAVA_CLASS's main method
./run test Your.Package.TestClass				: Run JUnit test class
./run stats SOLVER						: Run stats script to get the stats of the algorithm running on ALL instances
./run clean							: gradle clean
./run graph							: Generate a html/js line graph comparing all the algorithms stats and open that graph in firefox
./run draw PATH_TO_INSTANCE					: Generate a svg graph file representing the instance problem and open svg file in firefox
```

## Get statistics from the algorithms

You can get statistics from the alrogithms running on all the instances located in `./instances`.
The project can be executed directly with `gradle` by specifying the arguments like so :

```
❯ ./run stats greedyestsrpt
```

This will create a file in `./stats` called `SOLVER_NAME.stats` with the results of each instances parsed with the solver `SOLVER_NAME`


## Graph visualization

It is possible to have a great comparison against all the algorithms once the `./runs stats SOLVER_NAME` is done.
The following command will basically gather all the stats files in `./stats` directory and produce a HTML/JavaScript graph with the ChartJS library. This same command will open that graph with firefox

```
❯ ./run stats greedyspt
❯ ./run stats greedylrpt
❯ ./run stats greedyestspt
...
❯ ./run graph
```

## JUnit Tests

One can execute the JUnit tests with the following command.

```
❯ ./run test jobshop.test.encodings.EncodingTests # Replace this class by the JUnit class you like
```

## Instances SVG drawing

The `run` script allows you to easily visualize an instance by drawing it in a SVG format and displaying it with firefox.

```
❯ ./run draw
Usage: ./run draw PATH_TO_INSTANCE
e.g. ./run draw ./instances/ft06
```

