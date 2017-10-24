import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.rmi.registry.*;
import java.sql.*;
import java.util.*;

public class DataServer extends UnicastRemoteObject implements DataServerConsoleInterface {
	static DataServerConsoleInterface backupRegistry;
	static Registry serverRegistry;
	static DataServer server;
	static String reference;
	static int port;
	static int socketPort = 7002;


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
/*
	public static String listen() {
		byte[] buf = new byte[256];
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.exit(0);
			}
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try{
            	MulticastSocket socket = new MulticastSocket(socketPort);
				System.out.println("Listenting");
            	socket.receive(packet);
            	socket.close();

	            String received = new String(packet.getData(), 0, packet.getLength());
	            System.out.println(received);
	            return received;
            }catch(Exception e) {
            	System.out.println(e);
            	return null;
            } 
        }
	}
*/
	public static void runBackupServer(int delay){
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			System.exit(0);
		}

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


				try{
					byte[] buf = new byte[256];
					String msg = "ping";
        			buf = msg.getBytes(); 
					InetAddress sendAddress = InetAddress.getByName("localhost");
					DatagramPacket packet = new DatagramPacket(buf, buf.length, sendAddress, socketPort);

					DatagramSocket socket = new DatagramSocket(socketPort+1);

					System.out.println("Sending: " + msg);
		        	socket.send(packet);

		        	socket.receive(packet);
	            	InetAddress returnAddress = packet.getAddress();
	            	int returnPort = packet.getPort();
		            String received = new String(packet.getData(), 0, packet.getLength());
		            System.out.println("Recieved: " + received);

		        	socket.close();
				}catch(Exception e){
					System.out.println(e);
				}


				tries--;
			}

		} catch (Exception e) {
			System.out.println("Remote failure: " + e + "\nTrying to reconnect...");
			runBackupServer(1500);
		}
	}
/*
	public static void sendMessage(int sendPort, String msg) {
		byte[] buf = new byte[256];
        buf = msg.getBytes();
		try{
			InetAddress sendAddress = InetAddress.getByName("localhost");
			DatagramPacket packet = new DatagramPacket(buf, buf.length, sendAddress, sendPort);

			MulticastSocket socket = new MulticastSocket(port);
			System.out.println("Sending: " + msg);
        	socket.send(packet);

        	socket.close();
		}catch(Exception e){
			System.out.println(e);
		}
    }
*/
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
		return;
	}

  public ArrayList<Faculty> listFaculties() throws RemoteException {
		// Vês se o type é "Faculty" ou "Department" e devolves a lista de
		// departamentos / faculdades de acordo
		ArrayList<Faculty> faculties = new ArrayList<Faculty>();
		faculties.add(new Faculty("FCTUC"));

		Faculty[] test = new Faculty[faculties.size()];
		test = faculties.toArray(test);
		System.out.println(Arrays.toString(test));
		return faculties;
	}

	public ArrayList<Department> listDepartments(Faculty faculty) throws RemoteException {
		ArrayList<Department> departments = new ArrayList<Department>();
		departments.add(new Department(faculty, "DEI"));

		Department[] test = new Department[departments.size()];
		test = departments.toArray(test);
		System.out.println(Arrays.toString(test));
		return departments;
	}

  public void createElection(Election election) throws RemoteException {
		return;
	}

  public ArrayList<Election> listElections(String type) throws RemoteException {
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


	public static void main(String args[]) {
		try {
			server = new DataServer();
		} catch (RemoteException e) {
			System.out.println(e);
			System.exit(-1);
		}
		setSecurityPolicies();
		port = getPort(args);
		reference = getReference(args);

		runServer(0);

		try{
			OracleCon database = new OracleCon("bd","bd");
			ResultSet faculties = database.query("select * from faculty");

			while(faculties.next()){
				System.out.println(faculties.getString(1));
			}

			//database.closeConnection();



		}catch (Exception e) {System.out.println(e);}

		return;
	}

}
