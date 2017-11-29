import java.rmi.*;
import java.rmi.server.*;
import java.io.IOException;
import java.net.*;
import java.rmi.registry.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.text.*;

import com.sun.org.apache.regexp.internal.REUtil;

public class DataServer extends UnicastRemoteObject implements DataServerInterface {
  private static final long serialVersionUID = -977374215791991679L;
  static DataServerInterface backupRegistry;
  static Registry serverRegistry;
  static DataServer server;
  static String reference;
  static int port;
  static int socketPort = 7002;
  static OracleCon database;
  static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
  static SimpleDateFormat electionDateFormat = new SimpleDateFormat("dd-MM-yyyy k:m");

  public static void main(String args[]) {
    // create RMI Server
    try {
      server = new DataServer();
    } catch (RemoteException e) {
      System.out.println(e);
      System.exit(-1);
    }
    // set security policies
    setSecurityPolicies();

    // set port and reference for RMI Server
    port = getPort(args);
    reference = getReference(args);

    // connect to oracle database
    database = new OracleCon("bd", "bd", true);

    // run RMI Server
    runServer(0);

    return;
  }

  public static void runServer(int delay) {
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      System.exit(0);
    }

    try {
      serverRegistry = createAndBindRegistry();
      System.out.println("Server ready Port: " + port + " Reference: " + reference);

      while (true) {
        try {
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
        } catch (Exception e) {
          System.out.println(e);
        }
      }
    } catch (RemoteException e) {
      System.out.println("Remote failure:\n" + e);
      runBackupServer(0);
    }
  }

  public static void runBackupServer(int delay) {
    try {
      backupRegistry = (DataServerInterface) lookupRegistry(port, reference);
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
          byte[] buf = new byte[256];
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
        runServer(0);
      }
    } catch (SocketException se) {
      System.out.println("Socket failure\nTrying to reconnect...");
      runBackupServer(1500);
    } catch (RemoteException re) {
      System.out.println("Remote failure\nTrying to reconnect...");
      runBackupServer(1500);
    } catch (NotBoundException nbe) {
      System.out.println("Not bound\nTrying to reconnect...");
      runBackupServer(1500);
    }
  }

  // DataServer Console Interface Methods
  public Election getElection(int id) throws RemoteException {
    ResultSet resultSet = this.query("SELECT name, description, type, subtype, start, end"
      + " FROM Election WHERE id = " + id
    );
    
    try {
	   return new Election(
	      id,
	      resultSet.getString("name"),
	      resultSet.getString("description"),
	      resultSet.getString("type"),
	      resultSet.getString("subtype"),
	      resultSet.getString("start"),
	      resultSet.getString("end")
	    );
    } catch(SQLException sqlException) {
    	System.out.println("Error getting election:\n" + sqlException);
    }
  }
  
  public int createPerson(Person person) throws RemoteException {	  
    this.query("INSERT INTO Person VALUES ("
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
    
    return person.cc;
  }

  public String createFaculty(Faculty faculty) throws RemoteException {
    this.query("INSERT INTO Faculty VALUES ("
      + "'" + faculty.name + "'"
      + ")"
    );
    
    return faculty.name;
  }
  
  public String createDepartment(Department department) throws RemoteException {
    this.query("INSERT INTO Department VALUES ("
      + "'" + department.name + "'"
      + ", '" + department.facultyName + "'"
      + ")"
    );
    
    return department.name;
  }

  public void updateFaculty(Faculty faculty, Faculty newFaculty) throws RemoteException {
    this.query("UPDATE Faculty"
  	  + " SET name = '" + newFaculty.name + "'"
      + " WHERE name = '" + faculty.name + "'"
    );
  }
  
  public void updateDepartment(Department department, Department newDepartment) {
    this.query("UPDATE Department SET"
      + " name = '" + newDepartment.name + "'"
      + ", facultyName = '" + newDepartment.facultyName + "'"
      + " WHERE name = '" + department.name + "'"
    );
  }

  public void removeFaculty(String name) throws RemoteException {  
    this.query("DELETE FROM Faculty WHERE name = '" + name + "'");
  }
  
  public void removeDepartment(String name) throws RemoteException {
    this.query("DELETE FROM Department WHERE name = '" + name + "'");
  }

  public ArrayList < Faculty > listFaculties() throws RemoteException {
    ArrayList < Faculty > faculties = new ArrayList < Faculty > ();
    ResultSet resultSet = this.query("SELECT name FROM Faculty");
    
    try {
      while (resultSet.next()) {
        faculties.add(
          new Faculty(resultSet.getString("name"))
        );
      }
    } catch (SQLException e) {
      System.out.println("Error on listFaculties(): " + e);
      return new ArrayList<Faculty>();
    }

    return faculties;
  }

  public ArrayList < Department > listDepartments(Faculty faculty) throws RemoteException {
    ArrayList < Department > departments = new ArrayList < Department > ();    
    
    ResultSet resultSet = this.query(
	  "SELECT name, facultyName"
	  + " FROM Department "
	  + " WHERE faculty = '" + faculty.name + "'"
    );
    
    try {
      while (resultSet.next()) {
        departments.add(
          new Department(
            resultSet.getString("name"),
            resultSet.getString("facultyName")
          )
        );
      }
    } catch (SQLException e) {
      System.out.println("Error on listDepartments(): " + e);
      return new ArrayList<Department>();
    }

    return departments;
  }

  public int createElection(Election election) throws RemoteException {
    ResultSet resultSet = this.query("INSERT INTO Election VALUES ("
      + " (SELECT MAX(id)+1 as newID from Election)"
      + ", '" + election.name + "'"
      + ", '" + election.description + "'"
      + ", '" + election.type + "'"
      + ", '" + election.subtype + "'"
      + ", '" + electionDateFormat.format(election.start) + "'"
      + ", '" + electionDateFormat.format(election.end) + "'"
      + ", 0" // blank votes
      + ", 0" // null votes
      + "') RETURNING newID"
     );
    
    try {
    	return resultSet.getInt("newID");
    } catch(SQLException sqlException) {
    	System.out.println("Failed to return electionID");
    	return -1;
    }
  }

  public ArrayList < Election > listElections(String type, String subtype) throws RemoteException {
    ArrayList < Election > elections = new ArrayList < Election > ();
    
    ResultSet resultSet = this.query(
      "SELECT id, name, description, start, end"
      + " FROM Election"
      + " WHERE electionType = '" + type + "'"
      + " AND electionSubType = '" + subtype + "'"
    );
    
    try {
      while (resultSet.next()) {
        //Date eventEndDate = dateFormat.parse(resultSet.getString("electionEnd"));
        //if (eventEndDate.compareTo(new Date()) >= 0) { // if still running
          elections.add(
            new Election(
              resultSet.getInt("id"),
              resultSet.getString("name"),
              resultSet.getString("description"),
              type,
              subtype,
              electionDateFormat.parse(resultSet.getString("start")),
              electionDateFormat.parse(resultSet.getString("end"))
            )
          );
        }
      //}
    } catch (SQLException sqlException) {
      System.out.println("SQL failure on listElections()");
      return new ArrayList<Election>();
    } catch (ParseException parseException) {
      System.out.println("Error parsing date on listElections()");
      return new ArrayList<Election>();
    }

    return elections;
  }

  public ArrayList < Election > listElections(String departmentName, int cc) throws RemoteException {
    
  }

  public int createVotingList(VotingList votingList) throws RemoteException {
    ResultSet resultSet = this.query("INSERT INTO VotingList VALUES ("
      + " (SELECT MAX(id)+1 as newID from VotingList)"
      + ", '" + votingList.name + "'"
      + ", " + votingList.electionID
      + ") RETURNING newID"
    );
    
    try {
    	return resultSet.getInt("newID");
    } catch(SQLException sqlException) {
    	System.out.println("Failed to return votingListID");
    	return -1;
    }
  }

  public ArrayList < VotingList > listVotingLists(int electionID) throws RemoteException {
    ArrayList < VotingList > votingLists = new ArrayList < VotingList > ();
        
    ResultSet resultSet = this.query("SELECT id, name FROM VotingList"
      + " WHERE electionID = " + electionID
    );
    
    try {
      while (resultSet.next()) {
    	  votingLists.add(
    	    new VotingList(
    	      resultSet.getInt("id"),
    	      electionID,
              resultSet.getString("name")
            )
    	  );
      }
    } catch (Exception e) {
      System.out.println("Error on listLists(): " + e);
      return null;
    }

    return votingLists;
  }

  public void removeVotingList(int id) throws RemoteException {	
	  this.query("DELETE FROM VotingList WHERE listID = " + id + ")");
  }

  public ArrayList < Person > listCandidates(int votingListID) throws RemoteException {
    ArrayList < Person > candidates = new ArrayList < Person > ();

    ResultSet resultSet = this.query(
      "SELECT cc, type, name, password, address, number, phone, ccExpire, departmentName FROM Person"
      + " WHERE cc in ( SELECT personCC FROM VotingListMember"
        + " WHERE votingListID = " + votingListID + " )"
    );

    try {
      while (resultSet.next()) {
        candidates.add(
          new Person(
        	resultSet.getInt("cc"),
        	resultSet.getString("type"),
        	resultSet.getString("name"),
        	resultSet.getString("password"),
        	resultSet.getString("address"),
            resultSet.getInt("number"),
            resultSet.getInt("phone"),
            resultSet.getString("ccExpire"),
            resultSet.getString("departmentName")  
          )
        );
      }
    } catch (Exception e) {
      System.out.println("Error on listCandidates(): " + e);
    }

    return candidates;
  }

  public void addCandidate(int votingListID, int personCC) throws RemoteException {
    this.query("INSERT INTO VotingListMembers VALUES (" + personCC + ", " + votingListID + ")");
  }

  public void removeCandidate(int votingListID, int personCC) throws RemoteException {
	this.query("DELETE FROM VotingListMembers"
      + " WHERE listID = " + votingListID
      + " AND personCC = " + personCC
      + ")"
    );
  }

  public int createVotingTable(VotingTable votingTable) throws RemoteException {
    ResultSet resultSet = this.query("INSERT INTO votingTable VALUES ("
      + " ( SELECT MAX(id)+1 as newID FROM VotingTable )"
      + ", " + votingTable.electionID
      + ", '" + votingTable.departmentName + "'"
      + ") RETURNING newID"
    );
    
    try {
    	return resultSet.getInt("newID");
    } catch(SQLException sqlException) {
    	System.out.println("Failed to return votingTableID");
    	return -1;
    }
  }

  public void removeVotingTable(int id) throws RemoteException {	  
    this.query("DELETE FROM VotingTable"
      + " WHERE id = " + id
      + ")"
    );
  }

  public ArrayList < VotingTable > listVotingTables(int electionID) throws RemoteException {
    ArrayList < VotingTable > votingTables = new ArrayList < VotingTable > ();
    
    ResultSet resultSet = this.query("SELECT id, departmentName FROM VotingTable"
      + " WHERE electionID = " + electionID
    );
    
    try {
      while (resultSet.next()) {
    	votingTables.add(
    	  new VotingTable(
    	    resultSet.getInt("id"), 
    		electionID,
    		resultSet.getString("departmentName")
    	  )
    	);
      }
    } catch (Exception e) {
      System.out.println("Error on listLists(): " + e);
      new ArrayList < VotingTable > ();
    }

    return votingTables;

  }

  public void sendVote(int electionID, String votingList) throws RemoteException {
	boolean success = false;
	int type = -1;
	boolean isNull = votingList.equals("Nulo");
	boolean isBlank = votingList.equals("Branco");
	
    if (!isNull && !isBlank) {
    	type = 2;
    	success = this.voteVotingList(electionID, votingList);
    } else if (isNull) {
    	type = 1;
    	success = true;
	} else if (isBlank) {
		type = 0;
		success = true;
	} else {
		success = false;
	}
    
    if (success) this.voteElection(electionID, type);
    
    return;
  }
  
  public void voteElection(int electionID, int type) {
	if (type == 0) {
		this.query("UPDATE Election"
		  + " SET blankVotes = ( SELECT blankVotes FROM Election"
		    + " WHERE id = " + electionID + " ) + 1"
		  + " WHERE id = " + electionID
		);
	} else if (type == 1) {
		this.query("UPDATE Election"
		  + " SET nullVotes = ( SELECT nullVotes FROM Election"
		    + " WHERE id = " + electionID + " ) + 1"
		  + " WHERE id = " + electionID
		);
	} else if (type == 2) {
		this.query("UDPATE Election"
		  + "SET votes = ( SELECT votes FROM Election"
		   + " WHERE id = " + electionID + " ) + 1"
		  + " WHERE id = " + electionID
		);
	}
  }
  
  public boolean voteVotingList(int electionID, String votingList) {
    ResultSet resultSet = this.query("UPDATE VotingList"
      + "SET votes = ( SELECT votes FROM VotingList"
        + " WHERE name = " + votingList
        + " AND electionID = " + electionID
        + " ) + 1"
      + " WHERE name = " + votingList
      + " AND electionID = " + electionID
    );

    return resultSet != null;
  }

  public void sendLog(VotingLog votingLog) throws RemoteException {
    this.query("INSERT INTO VotingLog VALUES ("
      + " " + votingLog.electionID 
      + ", " + votingLog.cc
      + ", " + votingLog.votingTableID
      + ", " + electionDateFormat.format(votingLog.date)
      + " )"
    );
    
    return;
  }

  public ArrayList<Result> getResults(int electionID) throws RemoteException {  
	ArrayList<Result> results = new ArrayList<Result>();
	  
    ResultSet electionNonVotingListsVotes = this.query(
      "SELECT votes, blankVotes, nullVotes FROM Election"
      + " WHERE id = " + electionID
    );
    
    try {
	    results.add(
	      new Result("Branco", electionNonVotingListsVotes.getInt("blankVotes"))
	    );
	    
	    results.add(
  	      new Result("Nulo", electionNonVotingListsVotes.getInt("nullVotes"))
  	    );
    } catch (SQLException sqlException) {
    	return results;
    }
    
    ResultSet electionVotingListsVotes = this.query("SELECT name, votes FROM VotingList"
      + " WHERE electionID = " + electionID
    );
   
    try {
      while (electionVotingListsVotes.next()) {
        results.add(
          new Result(
            electionVotingListsVotes.getString("name"),
            electionVotingListsVotes.getInt("votes")
          )
        );
      }
    } catch (Exception e) {
      System.out.println("Error in getResults(" + electionID + "): " + e);
    }
    
    return results;
  }

  public Credential getCredentials(int cc) throws RemoteException {
    ResultSet resultSet = this.query("SELECT number, password FROM Person WHERE cc = " + cc);
    
    try {
      if (resultSet.next())
        return new Credential(
          resultSet.getInt("number"),
          resultSet.getString("password")
        );
    } catch (Exception e) {
      System.out.println("Error on getCredentials(" + cc + "): " + e);
    }
    
    return null;
  }

  public ResultSet query(String query) {
    try {
      return database.query(query);
    } catch (Exception exception) {
      System.out.println("Error on query(" + query + "): " + exception);
      return null;
    }
  }

  public static int getPort(String args[]) {
    try {
      return Integer.parseInt(args[0]);
    } catch (ArrayIndexOutOfBoundsException e) {
      return 7000;
    } catch (NumberFormatException e) {
      return 7000;
    }
  }

  public static String getReference(String args[]) {
    try {
      return args[1].toString();
    } catch (ArrayIndexOutOfBoundsException e) {
      return "iVotas";
    }
  }

  public static Registry createAndBindRegistry() throws RemoteException {
    Registry reg = LocateRegistry.createRegistry(port);
    reg.rebind(reference, server);
    return reg;
  }

  public static Remote lookupRegistry(int port, String reference) throws RemoteException, NotBoundException {
    return LocateRegistry.getRegistry(port).lookup(reference);
  }

  public static void setSecurityPolicies() {
    System.getProperties().put("java.security.policy", "policy.all");
    System.setSecurityManager(new SecurityManager());
  }

  public DataServer() throws RemoteException {
    super();
  }
}
