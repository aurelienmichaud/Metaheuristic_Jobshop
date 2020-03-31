package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.util.Arrays;

public class ResourceOrder extends Encoding {

	private Task[][] resourceOrderMatrix; /* ROM */

	public ResourceOrder(Instance instance) {
		super(instance);

		this.resourceOrderMatrix = new Task[this.instance.numMachines][this.instance.numJobs];
	}

	public ResourceOrder(Instance instance, Schedule sc) {
		super(instance);

		this.resourceOrderMatrix = new Task[this.instance.numMachines][this.instance.numJobs];

		this.fromSchedule(sc);
	}

	public Task[][] getResourceOrderMatrix() {
		return this.resourceOrderMatrix;
	}

	public Schedule toSchedule() {

		int[] jobs = new int[this.instance.numJobs * this.instance.numMachines];
		Arrays.fill(jobs, -1);

		for (int i = 0, machine = 0; machine < this.instance.numMachines; machine++) {
			for (int job = 0; job < this.instance.numJobs; job++, i++) {
				jobs[i] = this.resourceOrderMatrix[machine][job].job;
			}
		}

		int[] nextFreeTimeResource = new int[this.instance.numMachines];

		int[] nextTask = new int[this.instance.numJobs];

		int[][] startTimes = new int[this.instance.numJobs][this.instance.numTasks];

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

	private int getStartTime(int machine, Task task) {

		int timeCounter = 0;

		System.out.println("=== NEW TASK " + task + " ===");

		for (Task t : this.resourceOrderMatrix[machine]) {

			if (task.equals(t)) {
				return timeCounter;
			}

			timeCounter += 	this.instance.duration(t.job, t.task);

			System.out.println("timeCounter = " + timeCounter);
		}

		return -1;
	}

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

		this.sortROMFromSchedule(sc);
	}

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
				this.resourceOrderMatrix[machine][column] 	= resourceOrderMatrix[machine][min_index];
				this.resourceOrderMatrix[machine][min_index] = tmp;
			}
		}
		
	}
}

