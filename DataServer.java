import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.rmi.registry.*;
import java.sql.*;
import java.util.*;
import java.text.*;

public class DataServer extends UnicastRemoteObject implements DataServerInterface
{
	static DataServerInterface backupRegistry;
	static Registry serverRegistry;
	static DataServer server;
	static String reference;
	static int port;
	static int socketPort = 7002;
	static OracleCon database;

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
		database = new OracleCon("bd","bd", true);

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

			while(true){
				try{
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
				} catch(Exception e) {
						System.out.println(e);
				}
			}
		} catch (RemoteException e) {
			System.out.println("Remote failure:\n" + e );
			runBackupServer(0);
		}
	}

	public static void runBackupServer(int delay){
		try {
			backupRegistry = (DataServerInterface) lookupRegistry(port, reference);
			System.out.println("Backup server ready Port: " + port + " Reference: " + reference);
			int tries = 5;
			while(tries > 0){
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					System.exit(0);
				}

				DatagramSocket socket = new DatagramSocket(socketPort+1);
				try{
					byte[] buf = new byte[256];
					String msg = "ping";
					buf = msg.getBytes();
					InetAddress sendAddress = InetAddress.getByName("localhost");
					DatagramPacket packet = new DatagramPacket(buf, buf.length, sendAddress, socketPort);
					System.out.println("Sending: " + msg);
					socket.send(packet);
					socket.setSoTimeout(1000);
					socket.receive(packet);
					InetAddress returnAddress = packet.getAddress();
					int returnPort = packet.getPort();
					String received = new String(packet.getData(), 0, packet.getLength());
					System.out.println("Recieved: " + received);
					tries = 5;
				} catch (SocketTimeoutException e) {
					// timeout exception.
					System.out.println("Timeout reached!!! " + tries);
					tries--;
			} catch (Exception e){}
				socket.close();
			}
			runServer(0);

		} catch (Exception e) {
			System.out.println("Remote failure: " + e + "\nTrying to reconnect...");
			runBackupServer(1500);
		}
	}

	// DataServer Console Interface Methods
	public void createPerson(Person person) throws RemoteException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String message = "INSERT INTO person VALUES (" +
			"'"    + person.type +
			"', '" + person.name +
			"', "  + person.number +
			", '"  + person.password +
			"', '" + person.department.name +
			"', "  + person.phone +
			", '"  + person.address +
			"', "  + person.cc +
			", '"  + dateFormat.format(person.ccExpire) +
			"')";
		changeData(message);
	}

  public void createZone(Zone zone) throws RemoteException {
  	String message;
		if(zone instanceof Faculty){
			message = "INSERT INTO faculty VALUES ('" + zone.name + "')";
		} else if(zone instanceof Department) {
			Department department = (Department) zone;
			message = "INSERT INTO department VALUES ('" + department.name + "', " + department.faculty.name+ ")";
		} else {
			System.out.println("Error in createZone("+ zone +"): Not Department or Faculty");
			return;
		}
		changeData(message);
	}

	public void updateZone(Zone zone, Zone newZone) throws RemoteException {
		// check if zone already exists
		String message;
		if(zone instanceof Faculty && newZone instanceof Faculty) {
			message = "UPDATE faculty SET facName = '" + newZone.name +
			"' WHERE facName = '" + zone.name + "'";
		}
		else if (zone instanceof Department && newZone instanceof Department) {
			Department department = (Department)newZone;
			message = "UPDATE department SET depName = '" + department.name +
			"' , faculty = '" + department.faculty.name +
			"' WHERE depName = '" + department.name + "'";
		}
		else {
			System.out.println("Error in updateZone("+ zone +"): Not Department or Faculty");
			return;
		}
		changeData(message);
	}

	public void removeZone(Zone zone) throws RemoteException {
		String message;
		if(zone instanceof Faculty) {
			message = "DELETE FROM faculty WHERE facName = '" + zone.name + "'";
		}
		else if (zone instanceof Department) {
			message = "DELETE FROM department WHERE depName = '" + zone.name + "'";
		}
		else {
			System.out.println("Error in removeZone("+ zone +"): Not Department or Faculty");
			return;
		}
		changeData(message);
	}


	public void createFaculty(Faculty faculty) throws RemoteException{
		String message = "INSERT INTO faculty VALUES ('" + faculty.name + "')";
		changeData(message);
	}

	public ArrayList<Faculty> listFaculties() throws RemoteException {
		ArrayList<Faculty> faculties = new ArrayList<Faculty>();
		String message = "SELECT facName FROM faculty";
		ResultSet resultSet = fetchData(message);
		try{
			while(resultSet.next()){
				faculties.add(new Faculty(
					resultSet.getString("facName")
					));
			}
		}catch(SQLException e) {
			System.out.println("Error on listFaculties(): " + e);
			return null;
		}

		return faculties;
	}

	public ArrayList<Department> listDepartments(Faculty faculty) throws RemoteException {
		ArrayList<Department> departments = new ArrayList<Department>();
		String message = "SELECT faculty, depName FROM department WHERE facName = '" + faculty.name + "'";
		ResultSet resultSet = fetchData(message);
		try{
			while(resultSet.next()){
				departments.add(new Department(
					new Faculty(resultSet.getString("faculty")),
					resultSet.getString("depName")
					));
			}
		}catch(SQLException e) {
			System.out.println("Error on listDepartments(): " + e);
			return null;
		}

		return departments;
	}

	public void createElection(Election election) throws RemoteException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:m");
		String message = "INSERT INTO election VALUES (" + createElectionID(election) +
			", '" + election.name +
			"', '" + election.description +
			"', '" + election.type +
			"', '" + election.subtype +
			"', '" + dateFormat.format(election.start) +
			"', '" + dateFormat.format(election.end) +
			"')";
		changeData(message);
	}

	public ArrayList<Election> listElections(String type, String subtype) throws RemoteException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:m");
		java.util.Date dateNow = new java.util.Date();
		ArrayList<Election> elections = new ArrayList<Election>();
		String message = "SELECT electionName, electionStart, electionEnd FROM department WHERE electionType = '" + type
						+ "' AND electionSubType = '" + subtype + "'";
		ResultSet resultSet = fetchData(message);
		try {
			while(resultSet.next()){
				java.util.Date eventEndDate = dateFormat.parse(resultSet.getString("electionEnd"));
				if(eventEndDate.compareTo(dateNow) >= 0) // if still running
					elections.add(new Election( resultSet.getString("electionName"),
											dateFormat.parse(resultSet.getString("electionStart")),
											dateFormat.parse(resultSet.getString("electionEnd")),
											type,
											subtype
				));
			}
		}catch(Exception e) {
			System.out.println("Error on listElections(): " + e);
			return null;
		}

		return elections;
	}


	public ArrayList<Election> listElections(Department department, int cc) throws RemoteException{
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:m");
		ArrayList<Election> elections = new ArrayList<Election>();

		// get all election IDs from department
		String message = "SELECT electionID FROM votingTable WHERE depName = '" + department.name + "'";
		ResultSet resultSet_electionID = fetchData(message);

		// get person type and department (if applicable)
		message = "SELECT type, depName FROM person WHERE cc = " + cc;
		ResultSet resultSet_person = fetchData(message);

		try{
			String personType = resultSet_person.getString("type");
			String personDepartment = resultSet_person.getString("depName");

			while(resultSet_electionID.next()){

				// get election info
				message = "Select * FROM election WHERE electionID = " + resultSet_electionID.getInt("electionID");
				ResultSet resultSet_election = fetchData(message);

				String electionType = resultSet_election.getString("electionType");
				String electionSubType = resultSet_election.getString("electionSubType");

				switch(personType){
					case "Student":
						if (electionType.equals("General") && electionSubType.equals("Student-Election")){
							elections.add(new Election( resultSet_election.getString("electionName"), 
											dateFormat.parse(resultSet_election.getString("electionStart")), 
											dateFormat.parse(resultSet_election.getString("electionEnd")), 
											electionType, 
											electionSubType
							));
						}
 						if(electionType.equals("Nucleus") && personDepartment.equals(electionSubType)){
 							elections.add(new Election( resultSet_election.getString("electionName"), 
											dateFormat.parse(resultSet_election.getString("electionStart")), 
											dateFormat.parse(resultSet_election.getString("electionEnd")), 
											electionType, 
											electionSubType
							));
 						}
					break;
					case "Teacher":
						if(electionType.equals("General") && electionSubType.equals("Teacher-Election")){
							elections.add(new Election( resultSet_election.getString("electionName"), 
											dateFormat.parse(resultSet_election.getString("electionStart")), 
											dateFormat.parse(resultSet_election.getString("electionEnd")), 
											electionType, 
											electionSubType
							));
						}
					break;
					case "Employee":
						if(electionType.equals("General") && electionSubType.equals("Employee-Election")){
							elections.add(new Election( resultSet_election.getString("electionName"), 
											dateFormat.parse(resultSet_election.getString("electionStart")), 
											dateFormat.parse(resultSet_election.getString("electionEnd")), 
											electionType, 
											electionSubType
							));
						}
					break;
					default: 
						System.out.println("Error on listElections(): Employee type unrecognized");
					return null;
				}
			}
		}catch (Exception e){
			System.out.println("Error on listElections(): " + e);
			return null;
		}
		return elections;
	}

	public void createList(List list) throws RemoteException {
		String message = "INSERT INTO votingList VALUES (" + createListID(list) +
			", '" + list.name +
			"', " + getElectionID(list.election) +
			")";
		changeData(message);
	}

  public ArrayList<List> listLists(Election election) throws RemoteException {
		ArrayList<List> lists = new ArrayList<List>();
		String message = "SELECT listName FROM votingList WHERE electionID = " + getElectionID(election) ;
		ResultSet resultSet = fetchData(message);
		try {
			while(resultSet.next()){
				lists.add(new List( election,
									resultSet.getString(1)
				));
			}
		} catch(Exception e) {
			System.out.println("Error on listLists(): " + e);
			return null;
		}

		return lists;
	}

	public void removeList(List list) throws RemoteException {
		String message = "DELETE FROM votingList WHERE listID = " + getListID(list) + ")";
		changeData(message);
	}

	public ArrayList<Person> listCandidates(List list) throws RemoteException {
		ArrayList<Person> candidates = new ArrayList<Person>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		// get the cc column from listMembers
		int listID = getListID(list);

		String message = "SELECT personCC FROM votingListMembers WHERE listID = " + listID;
		ResultSet resultSet1 = fetchData(message);

		try{
			while(resultSet1.next()){
				message = "SELECT type, name, personID, password, depName, phone, address, cc, ccExpire " +
						  "FROM person WHERE cc = " + resultSet1.getInt(1);
				ResultSet resultSet2 = fetchData(message);
				while(resultSet2.next()){
					message = "SELECT faculty, depName FROM department WHERE depName = '" + resultSet2.getString(5) + "'";
					ResultSet resultSet3 = fetchData(message);
					candidates.add(new Person(resultSet2.getString(1),
											  resultSet2.getString(2),
											  resultSet2.getInt(3),
											  resultSet2.getString(4),
											  new Department(new Faculty(resultSet3.getString(1)), resultSet3.getString(2)),
											  resultSet2.getInt(6),
											  resultSet2.getString(7),
											  resultSet2.getInt(8),
											  dateFormat.parse(resultSet2.getString(9))
					));
				}
			}
		} catch (Exception e){
			System.out.println("Error on listCandidates(): " + e );
			return null;
		}

		return candidates;
	}

	public void addCandidate(List list, Person person) throws RemoteException {
		String message = "INSERT INTO votingListMembers VALUES (" + getListID(list) + ", " + person.cc + ")";
		changeData(message);
	}

	public void removeCandidate(List list, Person person) throws RemoteException {
		String message = "DELETE FROM votingListMembers WHERE listID = " + getListID(list) +
		" AND personCC = " + person.cc +
		")";
		changeData(message);
	}

	public void createVotingTable(VotingTable votingTable) throws RemoteException {
		String message = "INSERT INTO votingTable VALUES (" + getElectionID(votingTable.election) +
				", '" + votingTable.department.name +
				"')";
		changeData(message);
	}

	public void removeVotingTable(VotingTable votingTable) throws RemoteException {
		String message = "DELETE FROM votingTable WHERE electionID = " + getElectionID(votingTable.election) +
		"AND depName = '" + votingTable.department.name +
		"')";
		changeData(message);
	}

	public ArrayList<VotingTable> listVotingTables(Election election) throws RemoteException{
		ArrayList<VotingTable> votingTables = new ArrayList<VotingTable>();
		String message = "SELECT depName FROM votingTable WHERE electionID = " + getElectionID(election) ;
  		ResultSet resultSet_depName = fetchData(message);
  		try{
			while(resultSet_depName.next()){
				String depName = resultSet_depName.getString("depName");
				message = "SELECT faculty FROM department WHERE depName = '" + depName + "'";
				ResultSet resultSet_faculty = fetchData(message);

				votingTables.add(new VotingTable(
									election, 
									new Department(new Faculty(resultSet_faculty.getString("facName")),
										depName)
				));
			}
		}catch(Exception e) {
			System.out.println("Error on listLists(): " + e);
			return null;
		}

		return votingTables;

	}

	public void sendVote(Vote vote) throws RemoteException{
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:m");
		String message = "INSERT INTO vote VALUES ("+ getElectionID(vote.election) +
						", " + vote.terminalID +
						", " + vote.voteNumber +
						", '" + vote.list +
						"', '" + dateFormat.format(vote.date) +
						"')";
		changeData(message);
		return;
	}


	public void sendLog(Log log) throws RemoteException{
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:m");
		String message = "INSERT INTO log VALUES ('" + log.department.name +
							"', " + getElectionID(log.election) +
							", '" + dateFormat.format(log.date) +
							"', " + log.cc +
							")";
		changeData(message);
		return;
	}

	public Hashtable<String, Integer> getResults(Election election) throws RemoteException{
		Hashtable<String, Integer> elections = new Hashtable<String, Integer>();
		// get all lists of an election
		int electionID = getElectionID(election);
		String message = "SELECT listID, listName FROM votingList WHERE electionID = " + electionID;
		ResultSet resultSet1 = fetchData(message);
		try{
			while(resultSet1.next()){
				message = "SELECT COUNT(*) FROM vote WHERE electionID = " + electionID +
												   " AND votingListID = " + resultSet1.getInt("listID");
				ResultSet resultSet2 = fetchData(message);
				while(resultSet2.next()){
					elections.put(resultSet1.getString("listName"),
								  resultSet2.getInt(1));
				}
			}
			return elections;
		}catch(Exception e){
			System.out.println("Error in getResults("+ election +"): " + e );
		}
		return null;
	}

	public Credential getCredentials(int cc) throws RemoteException{
		String message = "SELECT personID, password FROM Person WHERE cc = " + cc ;
		ResultSet resultSet = fetchData(message);
		try{
			while (resultSet.next()){
				return new Credential(resultSet.getString("personID"),
									  resultSet.getString("password"));
			}
		}  catch(Exception e){
			System.out.println("Error on getCredentials("+ cc +"): " + e);
		}
		return null;
	}

	private int getListID(List list) {
		String message = "SELECT listID FROM votingList WHERE listName = '" + list.name
		+ "' AND electionID = " + getElectionID(list.election);
		ResultSet resultSet = fetchData(message);
		int listID = -1; // if an error occurs, return an invalid ID
		try {
			while(resultSet.next()){
				listID = resultSet.getInt("listID");
			}
			return listID;
		} catch(SQLException e) {
			System.out.println("Error on getListID("+ list +"): " + e);
			return -1;
		}
	}

	private int createListID(List list) {

		// check if list already exists
		if(getListID(list) == -1){
			System.out.println("List already exists: " + list);
			return -1;
		}

		// get MAX ID
		String message = "SELECT MAX(listID) FROM votingList";
		ResultSet resultSet = fetchData(message);
		int maxListID = 0; // the first listID created will have this value
		try{
			while(resultSet.next()){
				maxListID = resultSet.getInt("listID");
			}
			return maxListID;
		}catch(SQLException e) {
			System.out.println("Error on createListID("+ list +"): " + e);
			return -1;
		}
	}

	private int getElectionID(Election election) {
		String message = "SELECT electionID FROM election WHERE electionName = '" + election.name
		+ "' AND electionType = '" + election.type + "'";
		ResultSet resultSet = fetchData(message);
		int electionID = -1; // if an error occurs, return an invalid ID
		try {
			while(resultSet.next()){
				electionID = resultSet.getInt("electionID");
			}
			return electionID;
		} catch(SQLException e) {
			System.out.println("Error on getElectionID("+ election +"): " + e);
			return -1;
		}
	}

	private int createElectionID(Election election) {

		// check if election already exists
		if(getElectionID(election) == -1){
			System.out.println("Election already exists: " + election);
			return -1;
		}

		// get MAX ID
		String message = "SELECT MAX(electionID) FROM election";
		ResultSet resultSet = fetchData(message);
		int maxElectionID = 0; // the first electionID created will have this value
		try{
			while(resultSet.next()){
				maxElectionID = resultSet.getInt("electionID");
			}
			return maxElectionID;
		}catch(SQLException e) {
			System.out.println("Error on createElectionID("+ election +"): " + e);
			return -1;
		}
	}

	public void changeData(String message) {
		try{
			database.insert(message);
			System.out.println(message);
		}catch(Exception e){
			System.out.println("Error in changeData(" + message + "): " + e);
		}
	}

	public ResultSet fetchData(String message) {
		try{
			ResultSet resultQuery = database.query(message);
			System.out.println(message);
			return resultQuery;
		} catch(Exception e){
			System.out.println("Error in fetchData(" + message + "): " + e);
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
