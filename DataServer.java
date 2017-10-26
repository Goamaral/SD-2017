import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.rmi.registry.*;
import java.sql.*;
import java.util.*;

public class DataServer
	extends UnicastRemoteObject
	implements DataServerConsoleInterface, DataServerServerInterface
{
	static DataServerConsoleInterface backupRegistry;
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
		database = new OracleCon("bd","bd");

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
			serverRegistry = (Registry) createAndBindRegistry();
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
	            }catch(Exception e) {
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
			backupRegistry = (DataServerConsoleInterface) lookupRegistry(port, reference);
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
				}catch (SocketTimeoutException e) {
	                // timeout exception.
	                System.out.println("Timeout reached!!! " + tries);
	                tries--;
	            }catch (Exception e){}
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
		// Person -> Department -> Faculty
		// Pegas na person, tiras o departamento, tiras a faculdade
		// Consegues agora inserir a pessoa na base de dados
		System.out.println(person.toString());
		return;
	}

  public void createZone(Zone zone) throws RemoteException {
		// Usas o instanceof para ver se é faculdade ou departamento
		// Se for faculdade inseres na tabela se ainda nao existir
		// Se for departamento, pegas na faculdade do departamento,
		// adicionas se ainda nao existir e depois adicionas o departamento se ja nao existir

		return;
	}

  	public void updateZone(Zone zone, Zone newZone) throws RemoteException {
		// Identificas se é faculdade ou departamento e atualizas com os novos dados
		// o departamento ou faculdade

		return;
	}

  	public void removeZone(Zone zone) throws RemoteException {
		// Identificas se é faculdade ou departamento e removes
		String message;
		if(zone instanceof Faculty)
			message = "DELETE FROM faculty WHERE facName = '" + zone.name + "'";
		else
			message = "DELETE FROM department WHERE depName = '" + zone.name + "'";

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
					resultSet.getString(1)	// facName
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
		String message = "SELECT * FROM department WHERE facName = '" + faculty.name + "'";
  		ResultSet resultSet = fetchData(message);
  		try{
			while(resultSet.next()){
				departments.add(new Department(
					new Faculty(resultSet.getString(2)),	// Faculty
					resultSet.getString(1)	// depName
					));
			}
		}catch(SQLException e) {
			System.out.println("Error on listFaculties(): " + e);
			return null;
		}

		return departments;
	}

  public void createElection(Election election) throws RemoteException {
		return;
	}

  public ArrayList<Election> listElections(String type, String subtype) throws RemoteException {
		return null;
	}

  public void createList(List list) throws RemoteException {
		return;
	}

  public ArrayList<List> listLists(Election type) throws RemoteException {
		return null;
	}

  public void removeList(List list) throws RemoteException {
		return;
	}

  public ArrayList<Person> listCandidates(List list) throws RemoteException {
		return null;
	}

  public void addCandidate(List list, Person person) throws RemoteException {
		return;
	}

  public void removeCandidate(List list, Person person) throws RemoteException {
		return;
	}

  public void createVotingTable(VotingTable votingTable) throws RemoteException {
		return;
	}

  public void removeVotingTable(VotingTable votingTable) throws RemoteException {
		return;
	}

	public ArrayList<VotingTable> listVotingTables(Election election) throws RemoteException {
		return null;
	}


	public void changeData(String message){
		try{
			database.insert(message);
			System.out.println(message);
		}catch(Exception e){
			System.out.println("Error in changeData(" + message + "): " + e);
		}
	}

	public ResultSet fetchData(String message){
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
