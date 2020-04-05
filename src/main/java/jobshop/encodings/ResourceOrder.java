package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.util.Arrays;

public class ResourceOrder extends Encoding {

	private Task[][] resourceOrderMatrix; /* ROM */

	/**
	 * ResourceOrder constructor.
	 * @param instance	The instance problem
	 */
	public ResourceOrder(Instance instance) {
		super(instance);

		this.resourceOrderMatrix = new Task[this.instance.numMachines][this.instance.numJobs];
	}

	/**
	 * ResourceOrder constructor.
	 * @param instance	The instance problem
	 * @param sc		The shedule which we infer the resource order matrix from
	 */
	public ResourceOrder(Instance instance, Schedule sc) {
		super(instance);

		this.resourceOrderMatrix = new Task[this.instance.numMachines][this.instance.numJobs];

		this.fromSchedule(sc);
	}

	public Task[][] getResourceOrderMatrix() {
		return this.resourceOrderMatrix;
	}

	/**
	 * Translate the resource order matrix into a schedule
	 * @return	Schedule built from the resource order matrix
	 */
	public Schedule toSchedule() {
		/* For each machine, it stores the time at which the resource is available */
		int[] nextFreeTimeResource = new int[this.instance.numMachines];
		/* For each job, it stores the pending task */
		int[] nextTask = new int[this.instance.numJobs];
		/* For each task, it stores the time at which they will be started */
		int[][] startTimes = new int[this.instance.numJobs][this.instance.numTasks];
		/* For each machine, all the consecutive jobs which have a task that require that machine */
		int[] jobs = new int[this.instance.numJobs * this.instance.numMachines];

		for (int i = 0, machine = 0; machine < this.instance.numMachines; machine++) {
			for (int job = 0; job < this.instance.numJobs; job++, i++) {
				jobs[i] = this.resourceOrderMatrix[machine][job].job;
			}
		}

		for (int job : jobs) {
			int task = nextTask[job];
			int machine = this.instance.machine(job, task);

			int est = (task == 0) ? 0 : startTimes[job][task-1] + this.instance.duration(job, task-1);

			est = Math.max(est, nextFreeTimeResource[machine]);
	
			startTimes[job][task] = est;
			nextFreeTimeResource[machine] = est + this.instance.duration(job, task);
			nextTask[job] = task + 1;
		}	

		return new Schedule(this.instance, startTimes);
	}

	/**
	 * Translate a schedule into the resource order matrix
	 * @param sc	Schedule translated into the resource order matrix
	 */
	public void fromSchedule(Schedule sc) {

		if (sc == null) { return; }
		if (!sc.pb.equals(this.instance)) {
			System.out.println("[jobshop.encodings.ResourceOrder.fromSchedule(Schedule sc)] - ERROR: parameter Schedule does not have the same instance as ResourceOrder object.");
			return;
		}


		for (int machine = 0; machine < this.instance.numMachines; machine++) {
			for (int job = 0; job < this.instance.numJobs; job++) {
				this.resourceOrderMatrix[machine][job] = new Task(job, this.instance.task_with_machine(job, machine));
			}
		}

		/* We now need to get our matrix in order and sort it according
		 * to the ascending task starting time for each machine */
		this.sortROMFromSchedule(sc);
	}

	/*
	 * Sort the resource order matrix in ascending task starting time for each machine
	 * @param sc	Base schedule which is used to get the starting time of each task
	 */
	private void sortROMFromSchedule(Schedule sc) {

		if (sc == null) { return; }

		for (int machine = 0; machine < this.instance.numMachines; machine++) {
			for (int column = 0; column < this.instance.numJobs; column++) {

				int min_index = column;

				for (int i = column + 1; i < this.instance.numJobs; i++) {

					Task min 	= this.resourceOrderMatrix[machine][min_index];
					Task current 	= this.resourceOrderMatrix[machine][i];

					if (sc.startTime(current.job, current.task) < sc.startTime(min.job, min.task)) {
						min_index = i;
					}
				}

				Task tmp = this.resourceOrderMatrix[machine][column];
				this.resourceOrderMatrix[machine][column] 	= this.resourceOrderMatrix[machine][min_index];
				this.resourceOrderMatrix[machine][min_index] = tmp;
			}
		}
		
	}
}

