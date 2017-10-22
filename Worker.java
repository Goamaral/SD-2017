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
                jobs.wait();
              } catch(InterruptedException e1) {
                return;
              }
            } else {
              job = jobs.removeLast();

              try {
                switch (job.instruction) {
                  case "createPerson":
                    this.registry.createPerson((Person)job.data);
                    break;
                }
              } catch (RemoteException e) {
                synchronized (jobs) {
                  jobs.addLast(job);
                }

                synchronized (lock) {
                  if (end) {
                    return;
                  }
                  try {
                    this.sleep(1000);
                  } catch (InterruptedException e2) {
                    return;
                  }
                }
              }
            }
          }
        }

        return;
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
