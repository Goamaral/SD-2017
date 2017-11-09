import java.rmi.RemoteException;
import java.util.LinkedList;

class ConsoleWorker extends Thread {
  LinkedList<Job> jobs;
  Object lock = new Object();
  boolean end = false;
  DataServerInterface registry;

  public void run() {
		Job job;

    while (true) {
      synchronized (lock) {
        if (!end) {
          synchronized (this.jobs) {
            if (this.jobs.size() == 0) {
              try {
                this.jobs.wait();
              } catch(InterruptedException e1) {
                System.out.println(e1);
                return;
              }
            } else {
            	job = this.jobs.removeLast();

            	try {
            		System.out.println("WORKING ON " + job.instruction);
		            if (job.instruction.equals("Person Register Student")
		            	|| job.instruction.equals("Person Register Teacher")
		            	|| job.instruction.equals("Person Register Employee")
		    		) {
		                this.registry.createPerson((Person)job.data1);
		            }
		            else if (job.instruction.equals("Zone Faculty Add")) {
		                this.registry.createZone((Faculty)job.data1);
		          	}
		        	else if (job.instruction.equals("Zone Faculty Edit")) {
		                this.registry.updateZone((Faculty)job.data1, (Faculty)job.data2);
		        	}
		        	else if (job.instruction.equals("Zone Faculty Remove")) {
		        		this.registry.removeZone((Faculty)job.data1);
					}
		        	else if (job.instruction.equals("Zone Department Add")) {
		        		this.registry.createZone((Department)job.data1);
					}
		        	else if (job.instruction.equals("Zone Department Edit")) {
		                this.registry.updateZone((Department)job.data1, (Department)job.data2);
					}
		        	else if (job.instruction.equals("Zone Department Remove")) {
		        		this.registry.removeZone((Department)job.data1);
					}
		        	else if (job.instruction.equals("Election General Student-Election Add")
		        		|| job.instruction.equals("Election General Teacher-Election Add")
		        		|| job.instruction.equals("Election General Employee-Election Add")
		        		|| job.instruction.equals("Election Nucleus Add")
		        	) {
		        		this.registry.createElection((Election)job.data1);
					}
		        	else if (job.instruction.equals("Election General Student-Election List Candidate Add")
		        		|| job.instruction.equals("Election General Teacher-Election List Candidate Add")
		        		|| job.instruction.equals("Election General Employee-Election List Candidate Add")
		        		|| job.instruction.equals("Election Nucleus Candidate Add")
		        	) {
		        		this.registry.addCandidate((List)job.data1, (Person)job.data2);
					}
		        	else if (job.instruction.equals("Election General Student-Election List Candidate Remove")
		        		|| job.instruction.equals("Election General Teacher-Election List Candidate Remove")
		        		|| job.instruction.equals("Election General Employee-Election List Candidate Remove")
		        		|| job.instruction.equals("Election Nucleus Candidate Remove")
					) {
		        		this.registry.removeCandidate((List)job.data1, (Person)job.data2);
					}
		        	else if (job.instruction.equals("Election General Student-Election VotingTable Add")
		    			|| job.instruction.equals("Election General Teacher-Election VotingTable Add")
		    			|| job.instruction.equals("Election General Employee-Election VotingTable Add")
		    			|| job.instruction.equals("Election Nucleus VotingTable Add")
					) {
		        		this.registry.createVotingTable((VotingTable)job.data1);
					}
		        	else if (job.instruction.equals("Election General Student-Election VotingTable Remove")
		        		|| job.instruction.equals("Election General Teacher-Election VotingTable Remove")
		        		|| job.instruction.equals("Election General Employee-Election VotingTable Remove")
		        		|| job.instruction.equals("Election Nucleus VotingTable Remove")
					) {
		        		this.registry.removeVotingTable((VotingTable)job.data1);
					}
              } catch (RemoteException e) {
                System.out.println("WORKER FAILED " + e);
                synchronized (this.jobs) {
                  this.jobs.addLast(job);
                }
              }
            }
          }
        } else return;
      }
    }
  }

  public void terminate() {
    synchronized (lock) {
      end = true;
    }
  }

  public ConsoleWorker(LinkedList<Job> jobs, DataServerInterface registry) {
    this.jobs = jobs;
    this.registry = registry;

    this.start();
  }
}
