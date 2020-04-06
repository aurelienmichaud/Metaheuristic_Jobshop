# Heuristic methods for JobShop scheduling

This repository contains the starter code for the assignment.


## Run the program

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
❯ ./run graph
```

## test

One can execute the JUnit tests with the following command.

```
❯ ./run test jobshop.test.encodings.EncodingTests # Replace this class by the JUnit class you like
```

