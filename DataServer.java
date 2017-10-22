import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.rmi.registry.*;
import java.sql.*;
/*
  RMI SERVER - RMI + UDP
  STATUS: NOT WORKING
*/



public class DataServer extends UnicastRemoteObject implements DataServerConsoleInterface {
	static Registry registry;
	static DataServer server;
	static String reference;
	static int port;

	static boolean debug = true;

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
<<<<<<< HEAD
  public Faculty[] listFaculties() throws RemoteException{
  	return null;
  }
  public Department[] listDepartments(Faculty faculty) throws RemoteException{
  	return null;
  }
=======

  public Faculty[] listFaculties() throws RemoteException {
		// Vês se o type é "Faculty" ou "Department" e devolves a lista de
		// departamentos / faculdades de acordo
		// Faculty[] ret = new Faculty[1];
		// ret[0] = new Faculty("FCTUC");
		// return ret;
		return null;
	}

	public Department[] listDepartments(Faculty faculty) throws RemoteException {
		// Department[] ret = new Department[1];
		// ret[0] = new Department(faculty, "DEI");
		// return ret;
		return null;
	}
>>>>>>> 98d005bb2b3bd59b0cb7319aee49d743c5bd7978

  public void createElection(Election election) throws RemoteException {
		return;
	}

  public Election[] listElections(String type) throws RemoteException {
		return null;
	}

  public void createList(List list) throws RemoteException {
		return;
	}

  public List[] listLists(Election type) throws RemoteException {
		return null;
	}

  public void removeList(List list) throws RemoteException {
		return;
	}

  public Person[] listCandidates(List list) throws RemoteException {
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
			return Integer.parseInt(args[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return 7000;
		} catch (NumberFormatException e) {
			return 7000;
		}
	}

	public static String getReference(String args[]) {
		try {
			return args[2].toString();
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


	
	public static void loadDriverClass() throws Exception {
		if(debug) System.out.println("loadDriverClass()");
		try{
			//step1 load the driver class  
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}catch(Exception e){ 
			System.out.println(e); 
		}
	}

	public static Connection createConnectionObject() throws Exception {
		if(debug) System.out.println("createConnectionObject()");
		Connection con;
		try{
			//step2 create  the connection object  
			con = DriverManager.getConnection(  
			"jdbc:oracle:thin:@localhost:1521:xe","bd","bd");
			return con;
		}catch(Exception e){ 
			System.out.println(e);
			return null;
		}
	}

	public static Statement createStatementObject(Connection con) throws Exception {
		if(debug) System.out.println("createStatementObject(Connection con)");
		Statement stmt;
		try{
			//step3 create the statement object  
			stmt = con.createStatement();  
			return stmt;
		}catch(Exception e){ 
			System.out.println(e); 
			return null;
		}
	}

	public static void executeQuery(Statement stmt) throws Exception {
		if(debug) System.out.println("executeQuery(Statement stmt)");
		try{
			//step4 execute query  
			ResultSet rs=stmt.executeQuery("select * from faculty");  
			while(rs.next())  
				System.out.println(rs.getString(1));
		}catch(Exception e){ 
			System.out.println(e); 
		}
	}

	public static void closeConnectionObject(Connection con) throws Exception {
		if(debug) System.out.println("closeConnectionObject(Connection con)");
		try{
			//step5 close the connection object  
			con.close();  
		}catch(Exception e){ 
			System.out.println(e); 
		}
	}

	public static void connectOracle() throws Exception {
		if(debug) System.out.println("connectOracle()");
		try{    
			loadDriverClass();
			Connection con = createConnectionObject();
			Statement stmt = createStatementObject(con);
			executeQuery(stmt);
		}catch(Exception e){ 
			System.out.println(e); 
		}
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
			connectOracle();
		}catch(Exception e){
			System.out.println(e); 
		}
		
		return;
	}

}
