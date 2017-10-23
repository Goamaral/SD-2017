import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.rmi.registry.*;
import java.sql.*;
import java.util.*;

public class DataServer extends UnicastRemoteObject implements DataServerConsoleInterface {
	static Registry registry;
	static DataServer server;
	static String reference;
	static int port;


	public static void run(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			System.exit(0);
		}

		try {
			registry = (Registry) createAndBindRegistry();
			System.out.println("Server ready Port: " + port + " Reference: " + reference);
		} catch (RemoteException e) {
			System.out.println("Remote failure. Trying to reconnect...");
			run(1000);
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

		port = getPort(args);
		reference = getReference(args);
		run(0);

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
