import java.util.*;
import java.rmi.*;

class ConsoleWorker extends Thread {
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
                jobs.wait();
              } catch(InterruptedException e1) {
                System.out.println(e1);
                return;
              }
            } else {
              job = jobs.removeLast();

              try {
                System.out.println("WORKING ON " + job.instruction);
                switch (job.instruction) {
                  case "Person Register":
                    this.registry.createPerson((Person)job.data1);
                    break;
                  case "Zone Faculty Add":
                  case "Zone Department Add":
                    switch (job.data1.getClass().getName()) {
                      case "Faculty":
                        this.registry.createZone((Faculty)job.data1);
                        break;
                      case "Department":
                        this.registry.createZone((Department)job.data1);
                        break;
                    }
                    break;
                  case "Zone Faculty Edit":
                  case "Department Edit":
                    switch (job.data1.getClass().getName()) {
                      case "Faculty":
                        this.registry.updateZone((Faculty)job.data1, (Faculty)job.data2);
                        break;
                      case "Department":
                        this.registry.updateZone((Department)job.data1, (Department)job.data2);
                        break;
                    }
                    break;
                  case "Zone Faculty Remove":
                  case "Zone Department Remove":
                    switch (job.data1.getClass().getName()) {
                      case "Faculty":
                        this.registry.removeZone((Faculty)job.data1);
                        break;
                      case "Department":
                        this.registry.removeZone((Department)job.data1);
                        break;
                    }
                    break;
                }
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

  public ConsoleWorker(LinkedList<Job> jobs, DataServerConsoleInterface registry) {
    this.jobs = jobs;
    this.registry = registry;

    this.start();
  }
}
