import Core.*;

import java.rmi.*;
import java.rmi.server.*;
import java.io.IOException;
import java.net.*;
import java.rmi.registry.*;
import java.sql.*;
import java.util.*;
import java.text.*;

public class DataServer extends UnicastRemoteObject implements DataServerInterface {
  private static final long serialVersionUID = -977374215791991679L;
  private static DataServer server;
  private static String reference;
  private static int port;
  private static int socketPort = 7002;
  private static OracleCon database;
  private static SimpleDateFormat electionDateFormat = new SimpleDateFormat("dd-MM-yyyy k:m");

  public static void main(String args[]) {
    // create RMI Server
    try {
      server = new DataServer();
    } catch (RemoteException e) {
      e.printStackTrace();
    }

    // set port and reference for RMI Server
    port = getPort(args);
    reference = getReference(args);

    // connect to oracle database
    database = new OracleCon("bd", "bd", true);

    // run RMI Server
    runServer();
  }

  private static void runServer() {
    try {
      createAndBindRegistry();
      System.out.println("Server ready Port: " + port + " Reference: " + reference);

      //noinspection InfiniteLoopStatement
      while (true) {
          byte[] buf = new byte[256];
          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          DatagramSocket socket = new DatagramSocket(socketPort);
          System.out.println("Listenting on port: " + socketPort);
          socket.receive(packet);
          InetAddress returnAddress = packet.getAddress();
          int returnPort = packet.getPort();
          String received = new String(packet.getData(), 0, packet.getLength());
          System.out.println("Recieved: " + received);
          String returnMessage = "pong";
          buf = returnMessage.getBytes();
          packet = new DatagramPacket(buf, buf.length, returnAddress, returnPort);
          System.out.println("Sending: " + returnMessage);
          socket.send(packet);
          socket.close();
      }
    } catch (RemoteException e) {
      System.out.println("Remote failure:\n" + e);
      runBackupServer();
    } catch (IOException e) {
        e.printStackTrace();
    }
  }

  private static void runBackupServer() {
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
      System.exit(0);
    }

    try {
      lookupRegistry(port, reference);
      System.out.println("Backup server ready Port: " + port + " Reference: " + reference);
      int tries = 5;
      while (tries > 0) {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          System.exit(0);
        }

        DatagramSocket socket = new DatagramSocket(socketPort + 1);
        try {
          byte[] buf;
          String msg = "ping";
          buf = msg.getBytes();
          InetAddress sendAddress = InetAddress.getByName("localhost");
          DatagramPacket packet = new DatagramPacket(buf, buf.length, sendAddress, socketPort);
          System.out.println("Sending: " + msg);
          socket.send(packet);
          socket.setSoTimeout(1000);
          socket.receive(packet);
          String received = new String(packet.getData(), 0, packet.getLength());
          System.out.println("Recieved: " + received);
          tries = 5;
        } catch (SocketTimeoutException ste) {
          // timeout exception.
          System.out.println("Timeout reached!!! " + tries);
          tries--;
        } catch (IOException ioe) {
          socket.close();
        }
        runServer();
      }
    } catch (SocketException se) {
      System.out.println("Socket failure\nTrying to reconnect...");
      runBackupServer();
    } catch (RemoteException re) {
      System.out.println("Remote failure\nTrying to reconnect...");
      runBackupServer();
    } catch (NotBoundException nbe) {
      System.out.println("Not bound\nTrying to reconnect...");
      runBackupServer();
    }
  }
  
  // DataServer Console Interface Methods
  public ArrayList<Person> listStudentsFromDepartment(String departmentName) {
	  ArrayList<Person> people = new ArrayList<>();
		
	    ResultSet resultSet = query(
	      "SELECT cc, type, name, password, address, num, phone, cc_expire"
	      + " FROM person"
	      + " WHERE department_name = '" + departmentName + "' AND type = 'Student'"
	    );
	    
	    try {
            if (resultSet != null) {
                while (resultSet.next()) {
                  people.add(
                    new Person(
                      resultSet.getInt("cc"),
                      resultSet.getString("type"),
                      resultSet.getString("name"),
                      resultSet.getString("password"),
                      resultSet.getString("address"),
                      resultSet.getInt("num"),
                      resultSet.getInt("phone"),
                      resultSet.getString("cc_expire"),
                      departmentName
                    )
                  );
                }
            }
        } catch (SQLException e) {
	        System.out.println("Error on listStudentsFromDepartment(): " + e);
	        return new ArrayList<>();
	      }

	      return people;
  }
  
  public ArrayList<Person> listPeopleOfType(String type) {
	ArrayList<Person> people = new ArrayList<>();
	
    ResultSet resultSet = query(
      "SELECT cc, name, password, address, num, phone, cc_expire, department_name"
      + " FROM person"
      + " WHERE type = '" + type + "'"
    );
    
    try {
        if (resultSet != null) {
            while (resultSet.next()) {
              people.add(
                new Person(
                  resultSet.getInt("cc"),
                  type,
                  resultSet.getString("name"),
                  resultSet.getString("password"),
                  resultSet.getString("address"),
                  resultSet.getInt("num"),
                  resultSet.getInt("phone"),
                  resultSet.getString("cc_expire"),
                  resultSet.getString("department_name")
                )
              );
            }
        }
    } catch (SQLException e) {
        System.out.println("Error at listPeopleOfType(): " + e);
        return new ArrayList<>();
      }

      return people;
  }
  
  public Election getElection(int id) {
    ResultSet resultSet = query("SELECT name, description, type, subtype, started_at, ended_at"
      + " FROM election WHERE id = " + id
    );
        
    try {
        if (resultSet != null) {
            resultSet.next();
            return new Election(
                    id,
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("type"),
                    resultSet.getString("subtype"),
                    resultSet.getString("started_at"),
                    resultSet.getString("ended_at")
            );
        }
    } catch(SQLException sqlException) {
    	System.out.println("Error at getElection():\n" + sqlException);
    }
    
    return null;
  }
  
  public int createPerson(Person person) {
    ResultSet resultSet = query("INSERT INTO person VALUES ("
      + person.cc
      + ", '" + person.type + "'"
      + ", '" + person.name + "'"
      + ", '" + person.password + "'"
      + ", '" + person.address + "'" 
      + ", " + person.number
      + ", " + person.phone
      + ", '" + person.ccExpire + "'"
      + ", '" + person.departmentName + "'"
      + ")"
    );
        
    if (resultSet == null) return -1;
    return person.cc;
  }

  public String createFaculty(String name) {
    ResultSet resultSet = query("INSERT INTO faculty VALUES ("
      + "'" + name + "'"
      + ")"
    );
        
    if (resultSet == null) return null;
    return name;
  }
  
  public String createDepartment(Department department) {
    ResultSet resultSet = query("INSERT INTO department VALUES ("
      + "'" + department.name + "'"
      + ", '" + department.facultyName + "'"
      + ")"
    );
        
    if (resultSet == null) return null;
    return department.name;
  }

  public String updateFaculty(Faculty faculty, Faculty newFaculty) {
    ResultSet resultSet = query("UPDATE faculty"
  	  + " SET name = '" + newFaculty.name + "'"
      + " WHERE name = '" + faculty.name + "'"
    );
      
    if (resultSet == null) return null;
    return newFaculty.name;
  }
  
  public void updateDepartment(Department department, Department newDepartment) {
    this.query("UPDATE department SET"
      + " name = '" + newDepartment.name + "'"
      + ", faculty_name = '" + newDepartment.facultyName + "'"
      + " WHERE name = '" + department.name + "'"
    );
  }

  public void removeFaculty(String name) {
    query("DELETE FROM faculty WHERE name = '" + name + "'");
  }
  
  public void removeDepartment(String name) {
    query("DELETE FROM department WHERE name = '" + name + "'");
  }

  public ArrayList < Faculty > listFaculties() {
    ArrayList < Faculty > faculties = new ArrayList <> ();
    ResultSet resultSet = query("SELECT name FROM faculty");
    
    try {
        if (resultSet != null) {
            while (resultSet.next()) {
              faculties.add(
                new Faculty(resultSet.getString("name"))
              );
            }
        }
    } catch (SQLException e) {
      System.out.println("Error at listFaculties(): " + e);
      return new ArrayList<>();
    }

    return faculties;
  }

  public ArrayList <Department> listDepartments(String facultyName) {
    ArrayList < Department > departments = new ArrayList <> ();
    
    ResultSet resultSet = query(
	  "SELECT name, faculty_name"
	  + " FROM department "
	  + " WHERE faculty_name = '" + facultyName + "'"
    );
        
    try {
        if (resultSet != null) {
            while (resultSet.next()) {
              departments.add(
                new Department(
                  resultSet.getString("name"),
                  resultSet.getString("faculty_name")
                )
              );
            }
        }
    } catch (SQLException e) {
      System.out.println("Error at listDepartments(): " + e);
      return new ArrayList<>();
    }

    return departments;
  }

  public int createElection(Election election) {
	int id = -1;
	ResultSet resultSet = this.query("SELECT election_seq.nextval AS id from dual");
	
	try {
        if (resultSet != null) {
            resultSet.next();
            id = resultSet.getInt("id");
        }
	} catch(SQLException sqlException) {
		System.out.println("Failed to generate election_id");
		return id;
	}
			
    resultSet = this.query("INSERT INTO Election VALUES ("
		      + id
		      + ", '" + election.name + "'"
		      + ", '" + election.description + "'"
		      + ", '" + election.type + "'"
		      + ", '" + election.subtype + "'"
		      + ", '" + election.start + "'"
		      + ", '" + election.end + "'"
		      + ", 0" // votes
		      + ", 0" // blank votes
		      + ", 0" // null votes
		      + ")"
	);
        
    if (resultSet != null) return id;
    return -1;
  }

  public ArrayList < Election > listElections(String type, String subtype) {
    ArrayList <Election> elections = new ArrayList <> ();
    
	ResultSet resultSet = this.query(
      "SELECT id, name, description, started_at, ended_at FROM election"
      + " WHERE type = '" + type + "' AND subtype = '" + subtype + "'"
    );
    
    try {
        if (resultSet != null) {
            while (resultSet.next()) {
              elections.add(
                new Election(
                  resultSet.getInt("id"),
                  resultSet.getString("name"),
                  resultSet.getString("description"),
                  type,
                  subtype,
                  resultSet.getString("started_at"),
                  resultSet.getString("ended_at")
                )
              );
            }
        }
    } catch (SQLException sqlException) {
      System.out.println("Failed at listElections(): " + sqlException);
      return new ArrayList<>();
    }

    return elections;
  }

  public int createVotingList(VotingList votingList) {
	int id = -1;
	ResultSet resultSet = this.query("SELECT voting_list_seq.nextval AS id FROM dual");
	
	try {
        if (resultSet != null) {
            resultSet.next();
            id = resultSet.getInt("id");
        }
	} catch (SQLException sqlException) {
		System.out.println("Failed to generate voting_list_id");
		return id;
	}
	
    resultSet = this.query("INSERT INTO voting_list VALUES ("
      + id
      + ", " + votingList.electionID
      + ", '" + votingList.name + "'"
      + ", 0"
      + ")"
    );
                
    if (resultSet != null) return id;
    return -1;
  }

  public ArrayList < VotingList > listVotingLists(int electionID) {
    ArrayList < VotingList > votingLists = new ArrayList <> ();
        
    ResultSet resultSet = query("SELECT id, name FROM voting_list"
      + " WHERE election_id = " + electionID
    );
    
    try {
        if (resultSet != null) {
            while (resultSet.next()) {
                votingLists.add(
                  new VotingList(
                    resultSet.getInt("id"),
                    electionID,
                    resultSet.getString("name")
                  )
                );
            }
        }
    } catch (Exception e) {
      System.out.println("Falied at listLists(): " + e);
      return null;
    }

    return votingLists;
  }

  public void removeVotingList(int id) {
	  this.query("DELETE FROM voting_list WHERE id = " + id);
  }

  public ArrayList <Person> listCandidates(int votingListID) {
    ArrayList < Person > candidates = new ArrayList <> ();

    ResultSet resultSet = query(
      "SELECT cc, type, name, password, address, num, phone, cc_expire, department_name FROM person"
      + " WHERE cc in ( SELECT person_cc FROM voting_list_member"
        + " WHERE voting_list_id = " + votingListID + " )"
    );

    try {
        if (resultSet != null) {
            while (resultSet.next()) {
              candidates.add(
                new Person(
                  resultSet.getInt("cc"),
                  resultSet.getString("type"),
                  resultSet.getString("name"),
                  resultSet.getString("password"),
                  resultSet.getString("address"),
                  resultSet.getInt("num"),
                  resultSet.getInt("phone"),
                  resultSet.getString("cc_expire"),
                  resultSet.getString("department_name")
                )
              );
            }
        }
    } catch (Exception e) {
      System.out.println("Failed at listCandidates(): " + e);
    }

    return candidates;
  }

  public int addCandidate(int votingListID, int personCC) {
    ResultSet resultSet = query("INSERT INTO voting_list_member VALUES ("
      + personCC + ", " + votingListID + ")"
    );
    
    if (resultSet == null) return -1;
    return personCC;
  }

  public void removeCandidate(int votingListID, int personCC) {
	this.query("DELETE FROM voting_list_member"
      + " WHERE voting_list_id = " + votingListID
      + " AND person_cc = " + personCC
    );
  }

  public int createVotingTable(VotingTable votingTable) {
	int id = -1;
	ResultSet resultSet = this.query("SELECT voting_table_seq.nextval AS id FROM dual");
	
	try {
        if (resultSet != null) {
            resultSet.next();
            id = resultSet.getInt("id");
        }
	} catch (SQLException sqlException) {
		System.out.println("Failed to generate voting_table_id");
		return id;
	}
	  
    resultSet = this.query("INSERT INTO voting_table VALUES ("
      + id
      + ", 0"
      + ", " + votingTable.electionID
      + ", '" + votingTable.departmentName + "'"
      + ")"
    );
    
    if (resultSet != null) return id;
    return -1;
  }

  public void removeVotingTable(int id) {
    this.query("DELETE FROM voting_table WHERE id = " + id);
  }

  public ArrayList < VotingTable > listVotingTables(int electionID) {
    ArrayList < VotingTable > votingTables = new ArrayList <> ();
    
    ResultSet resultSet = this.query("SELECT id, status, department_name FROM voting_table"
      + " WHERE election_id = " + electionID
    );
    
    try {
        if (resultSet != null) {
            while (resultSet.next()) {
              votingTables.add(
                new VotingTable(
                  resultSet.getInt("id"),
                  resultSet.getInt("status"),
                  electionID,
                  resultSet.getString("department_name")
                )
              );
            }
        }
    } catch (Exception e) {
      System.out.println("Failed at listLists(): " + e);
      new ArrayList <VotingTable> ();
    }

    return votingTables;

  }

  public void sendVote(int electionID, String votingList) {
	boolean success;
	int type;
	boolean isNull = votingList.equals("Nulo");
	boolean isBlank = votingList.equals("Branco");
	
    if (!isNull && !isBlank) {
    	type = 2;
    	success = this.voteVotingList(electionID, votingList);
    } else if (isNull) {
    	type = 1;
    	success = true;
	} else {
		type = 0;
		success = true;
	}
    
    if (success) this.voteElection(electionID, type);
  }
  
  private void voteElection(int electionID, int type) {
	if (type == 0) {
		query("UPDATE election"
		  + " SET blank_votes = ( SELECT blank_votes FROM election"
		    + " WHERE id = " + electionID + " ) + 1"
		  + " WHERE id = " + electionID
		);		
	} else if (type == 1) {
		query("UPDATE election"
		  + " SET null_votes = ( SELECT null_votes FROM election"
		    + " WHERE id = " + electionID + " ) + 1"
		  + " WHERE id = " + electionID
		);
	} else if (type == 2) {
		query("UPDATE election"
		  + " SET votes = ( SELECT votes FROM election"
		   + " WHERE id = " + electionID + " ) + 1"
		  + " WHERE id = " + electionID
		);
	}
  }
  
  private boolean voteVotingList(int electionID, String votingList) {
    ResultSet resultSet = query("UPDATE voting_list"
      + " SET votes = ( SELECT votes FROM voting_list"
        + " WHERE name = '" + votingList + "'"
        + " AND election_id = " + electionID
        + " ) + 1"
      + " WHERE name = '" + votingList + "'"
      + " AND election_id = " + electionID
    );

    return resultSet != null;
  }

  public void sendLog(VotingLog votingLog) {
    this.query("INSERT INTO voting_log VALUES ("
      + " " + votingLog.election.id 
      + ", " + votingLog.cc
      + ", " + votingLog.votingTableID
      + ", '" + electionDateFormat.format(votingLog.date) + "'"
      + " )"
    );

  }

  public ArrayList<Result> getResults(int electionID) {
	ArrayList<Result> results = new ArrayList<>();
	  
    ResultSet resultSet = this.query(
      "SELECT votes, blank_votes, null_votes FROM election"
      + " WHERE id = " + electionID
    );
    
    try {
        if (resultSet != null) {
            resultSet.next();
            results.add(
                    new Result("Branco", resultSet.getInt("blank_votes"))
            );

            results.add(
                    new Result("Nulo", resultSet.getInt("null_votes"))
            );
        }
    } catch (SQLException sqlException) {
    	System.out.println("Failed to get results from election: " + sqlException);
    	return results;
    }
    
    resultSet = this.query("SELECT name, votes FROM voting_list"
      + " WHERE election_id = " + electionID
    );
    
    try {
        if (resultSet != null) {
            while (resultSet.next()) {
              results.add(
                new Result(
                    resultSet.getString("name"),
                    resultSet.getInt("votes")
                )
              );
            }
        }
    } catch (Exception e) {
      System.out.println("Failed to get results from voting lists(): " + e);
    }
    
    return results;
  }

  public Credential getCredentials(int cc) {
    ResultSet resultSet = query("SELECT num, password FROM person WHERE cc = " + cc);
    
    try {
        if (resultSet != null && resultSet.next()) return new Credential(
                resultSet.getInt("num"),
                resultSet.getString("password")
        );
    } catch (Exception e) {
      System.out.println("Failed at getCredentials(): " + e);
    }
    
    return null;
  }
	
  public ArrayList<VotingTable> getVotingTables(String departmentName, int cc) {
    ArrayList<VotingTable> votingTables = new ArrayList<>();
    
	ResultSet resultSet = this.query("SELECT id, status, election_id, department_name FROM voting_table"
	  + " WHERE election_id IN ("
        + "(SELECT id FROM election WHERE type = 'Nucleous' AND subtype ="
          + "(SELECT UNIQUE '" + departmentName +"' FROM person" 
            + " INTERSECT SELECT department_name FROM person WHERE cc = " + cc + ")" 
        + " AND SYSDATE >= to_date(started_at) AND SYSDATE <= to_date(ended_at))"
        + " UNION SELECT id FROM election WHERE type = 'General' AND subtype ="
          + "(SELECT type FROM person WHERE cc = " + cc +")"
          + " AND SYSDATE >= to_date(started_at) AND SYSDATE <= to_date(ended_at)"
        + " MINUS (SELECT election_id FROM voting_log WHERE person_cc = " + cc +")"
      + ")"
    );
    
    try {
        if (resultSet != null) {
            while(resultSet.next()) {
                votingTables.add(
                  new VotingTable(
                    resultSet.getInt("id"),
                    resultSet.getInt("status"),
                    resultSet.getInt("election_id"),
                    resultSet.getString("department_name")
                  )
                );
            }
        }
    } catch (SQLException sqlException) {
		System.out.println("Failed at getVotingTables(): " + sqlException);
		return new ArrayList<>();
	}
    
    return votingTables;
  }

  private ResultSet query(String query) {
    try {
      return database.query(query);
    } catch (Exception exception) {
      System.out.println("Failed at query(): " + exception);
      return null;
    }
  }

  private static int getPort(String args[]) {
    try {
      return Integer.parseInt(args[0]);
    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
      return 7000;
    }
  }

  private static String getReference(String args[]) {
    try {
      return args[1];
    } catch (ArrayIndexOutOfBoundsException e) {
      return "iVotas";
    }
  }

  private static void createAndBindRegistry() throws RemoteException {
    //System.setProperty("java.rmi.server.hostname","127.0.1.1");
    Registry reg = LocateRegistry.createRegistry(port);
    reg.rebind(reference, server);
  }

  private static void lookupRegistry(int port, String reference) throws RemoteException, NotBoundException {
      LocateRegistry.getRegistry(port).lookup(reference);
  }

  private DataServer() throws RemoteException {
    super();
  }
}
