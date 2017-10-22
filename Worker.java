import java.util.*;
import java.rmi.*;

class Worker extends Thread {
  static LinkedList<Job> jobs;
  static Job job;
  static Object lock = new Object();
  static boolean end = false;
  DataServerConsoleInterface registry;

  public void run() {
    while (true) {
      synchronized (lock) {
        if (!end) {
          synchronized (jobs) {
            if (jobs.size() == 0) {
              try {
                System.out.println("WORKER WAITING");
                jobs.wait();
                System.out.println("WORKER STARTED");
              } catch(InterruptedException e1) {
                System.out.println(e1);
                return;
              }
            } else {
              job = jobs.removeLast();

              try {
                if (job.instruction.equals("createPerson")) {
                  this.registry.createPerson((Person)job.data);
                }
                System.out.println("TASK " + job.instruction + " DONE BOSS");
              } catch (RemoteException e) {
                System.out.println("WORKER FAILED " + e);
                synchronized (jobs) {
                  jobs.addLast(job);
                }

                synchronized (lock) {
                  if (end) {
                    System.out.println("WORK DONE BOSS");
                    return;
                  }
                  try {
                    this.sleep(1000);
                    System.out.println("WORKER RETRYING...");
                  } catch (InterruptedException e2) {
                    System.out.println(e2);
                    return;
                  }
                }
              }
            }
          }
        } else {
          return;
        }
      }
    }
  }

  public void terminate() {
    synchronized (lock) {
      end = true;
    }
  }

  public Worker(LinkedList<Job> jobs, DataServerConsoleInterface registry) {
    this.jobs = jobs;
    this.registry = registry;

    this.start();
  }
}
