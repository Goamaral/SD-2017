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
                  case "Person Register Student":
            			case "Person Register Teacher":
            			case "Person Register Employee":
                    this.registry.createPerson((Person)job.data1);
            				break;

            			case "Zone Faculty Add":
                    this.registry.createZone((Faculty)job.data1);
            				break;
            			case "Zone Faculty Edit":
                    this.registry.updateZone((Faculty)job.data1, (Faculty)job.data2);
            				break;
            			case "Zone Faculty Remove":
                    this.registry.removeZone((Faculty)job.data1);
            				break;

            			case "Zone Department Add":
                    this.registry.createZone((Department)job.data1);
            				break;
            			case "Zone Department Edit":
                    this.registry.updateZone((Department)job.data1, (Department)job.data2);
            				break;
            			case "Zone Department Remove":
                    this.registry.removeZone((Department)job.data1);
            				break;

            			case "Election General Student-Election Add":
            			case "Election General Teacher-Election Add":
            			case "Election General Employee-Election Add":
            			case "Election Nucleus Add":
                    this.registry.createElection((Election)job.data1);
            				break;
            			case "Election General Student-Election List Candidate Add":
            			case "Election General Teacher-Election List Candidate Add":
            			case "Election General Employee-Election List Candidate Add":
            			case "Election Nucleus Candidate Add":
                    this.registry.addCandidate((List)job.data1, (Person)job.data2);
            				break;
                  case "Election General Student-Election List Candidate Remove":
            			case "Election General Teacher-Election List Candidate Remove":
            			case "Election General Employee-Election List Candidate Remove":
                  case "Election Nucleus Candidate Remove":
                    this.registry.removeCandidate((List)job.data1, (Person)job.data2);
                    break;
            			case "Election General Student-Election VotingTable Add":
            			case "Election General Teacher-Election VotingTable Add":
            			case "Election General Employee-Election VotingTable Add":
            			case "Election Nucleus VotingTable Add":
                    this.registry.createVotingTable((VotingTable)job.data1);
            				break;
            			case "Election General Student-Election VotingTable Remove":
            			case "Election General Teacher-Election VotingTable Remove":
            			case "Election General Employee-Election VotingTable Remove":
            			case "Election Nucleus VotingTable Remove":
                    this.registry.removeVotingTable((VotingTable)job.data1);
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
