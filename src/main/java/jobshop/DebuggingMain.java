package jobshop;

import jobshop.encodings.JobNumbers;

import jobshop.encodings.ResourceOrder;

import jobshop.encodings.Task;

import java.io.IOException;
import java.nio.file.Paths;

public class DebuggingMain {

	public static void main(String[] args) {
		try {
			// load the aaa1 instance
			Instance instance = Instance.fromFile(Paths.get("../../../../instances/aaa1"));

			// construit une solution dans la représentation par
			// numéro de jobs : [0 1 1 0 0 1]
			// Note : cette solution a aussi été vue dans les exercices (section 3.3)
			//        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
			JobNumbers enc = new JobNumbers(instance);
			enc.jobs[enc.nextToSet++] = 0;
			enc.jobs[enc.nextToSet++] = 1;
			enc.jobs[enc.nextToSet++] = 1;
			enc.jobs[enc.nextToSet++] = 0;
			enc.jobs[enc.nextToSet++] = 0;
			enc.jobs[enc.nextToSet++] = 1;

			System.out.println("\nENCODING: " + enc);

			Schedule sched = enc.toSchedule();
			// TODO: make it print something meaningful
			// by implementing the toString() method
			System.out.println("SCHEDULE: " + sched);
			System.out.println("VALID: " + sched.isValid());
			System.out.println("MAKESPAN: " + sched.makespan());

			ResourceOrder ro = new ResourceOrder(instance);
			ro.fromSchedule(sched);

			Task[][] t = ro.getResourceOrderMatrix();

			for (int i = 0; i < t.length; i++) {
				for (int j = 0; j < t[i].length; j++) {
					System.out.print(t[i][j]);
				}

				System.out.println();
			}

			System.out.println("reformed sched : " + ro.toSchedule());

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
}
