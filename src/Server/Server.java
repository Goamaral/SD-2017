import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.util.*;
import java.io.*;

class Server {
	static int portRMI = 7000;
	static String ipRMI = "localhost";
	static String referenceRMI = "iVotas";
	static int portTCP = 7001;
	
	public static void main(String args[]) {
		setSecurityPolicies();
		getOptions(args);
		
		new ServerListener(portRMI, referenceRMI, ipRMI, portTCP);
	}
	
	public static void setSecurityPolicies() {
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new SecurityManager());
	}

	public static void getOptions(String[] args) {
		for (int i=0; i<args.length; ++i) {
			if (args[i].equals("-rp")
				|| args[i].equals("--rmiport")
			) {
				try {
					portRMI = Integer.parseInt(args[i+1]);
				} catch (Exception e) {
					System.out.println("No RMI port provided, using default: 7000");
				}
			}
			else if (args[i].equals("-ri")
				|| args[i].equals("-rmiip")
			) {
				try {
					ipRMI = args[i+1];
				} catch (Exception e) {
					System.out.println("No RMI ip provided, using default: localhost");
				}
			}
			else if (args[i].equals("-rr")
				|| args[i].equals("--rmireference")
			) {
				try {
					referenceRMI = args[i+1];
				} catch (Exception e1) {
					System.out.println("No valid RMI reference provided, using default: iVotas");
				}
			}
			else if (args[i].equals("-tp")
				|| args[i].equals("-tcpport")
			) {
				try {
					portTCP = Integer.parseInt(args[i+1]);
				} catch (Exception e2) {
					System.out.println("No valid TCP port provided, using default: 7001");
				}
			}
			else if (args[i].equals("-h")
				|| args[i].equals("--help")
			) {
				System.out.println("java Server -rp 7000 -ri localhost -rr iVotas -tp 7001");
				System.exit(0);
			}
		}
	}

}

class ServerListener extends Thread {
	DataServerInterface registry;
	ServerSocket listenSocket;
	int portRMI;
	String ipRMI;
	String referenceRMI;
	int portTCP;
	ArrayList<TerminalConnection> terminals = new ArrayList<TerminalConnection>();
	VotingTableAutentication auth;
	String departmentName;
	RmiNapper rmiNapper;

	public ServerListener(int portRMI, String referenceRMI, String ipRMI, int portTCP) {
		this.portRMI = portRMI;
		this.referenceRMI = referenceRMI;
		this.ipRMI = ipRMI;
		this.portTCP = portTCP;
		this.rmiNapper = new RmiNapper(this);
		
		this.start();
	}
		
	public void run() {
		int terminalID;
		Socket terminalSocket;		
		lookupRegistry();
		createServerSocket();
		
		departmentName = getLocation();

		System.out.println("Mesa de voto");
		System.out.println("RMI => localhost:" + portRMI + "\\" + referenceRMI);
		System.out.println("TCP => localhost:" + portTCP);
		
		auth = new VotingTableAutentication(this.rmiNapper, this);

		while(true) {
			terminalSocket = waitForRequest();

			synchronized (terminals) {
			  terminalID = terminals.size();
			  terminals.add(new TerminalConnection(this, terminalID, terminalSocket));
			  System.out.println("Novo terminal (" + terminalID + ") conectado");
			}
		}
	}

	public String getLocation() {
		ArrayList<Faculty> faculties = listFaculties();
		ArrayList<Department> departments;
		ArrayList<String> list = new ArrayList<String>();
		int opcao;
		String facultyName;
		
		if (faculties == null) {
			System.out.println("Nao exitem faculdades registadas para"
				+ " indicar a localizacao da mesa de voto");
			System.exit(0);
		}

		for (Faculty auxFaculty : faculties) {
			list.add(auxFaculty.name);
		}

		opcao = selector(list, "Selecione a faculdade onde se situa");

		facultyName = faculties.get(opcao).name;

		departments = listDepartments(facultyName);

		list.clear();

		for (Department auxDepartment : departments) {
			list.add(auxDepartment.name);
		}

		opcao = selector(list, "Selecione o departmento onde se situa");

		return departments.get(opcao).name;

	}

	public ArrayList<Department> listDepartments(String facultyName) {
		try {
			return registry.listDepartments(facultyName);
		} catch (RemoteException remoteException) {
			System.out.println("Falha na obtencao de departamentos. A tentar novamente...");
			this.rmiNapper.nap();
		}
		
		return listDepartments(facultyName);
	}

	public ArrayList<Faculty> listFaculties() {
		try {
			return registry.listFaculties();
		} catch (RemoteException remoteException) {
			System.out.println("Falha na obtencao de faculdades. A tentar novamente...");
			this.rmiNapper.nap();
		} 
		
		return listFaculties();
	}

	public int selector(ArrayList<String> list, String title) {
		int i = 0;
		int opcao;
		String line;

		System.out.println("--------------------");
		System.out.println(title);
		System.out.println("--------------------");

		for (String item : list) {
			System.out.println("[" + i + "] " + item);
			++i;
		}

		System.out.print("Opcao: ");
		line = System.console().readLine();
		
		try {
			opcao = Integer.parseInt(line);
		} catch (Exception e) {
			System.out.println("Opcao invalida");
			return selector(list, title);
		}

		if (opcao >= i && opcao < 0) {
			System.out.println("Opcao invalida");
			return selector(list, title);
		}

		return opcao;
	}

	public Socket waitForRequest() {
		try {
			return listenSocket.accept();
		} catch (IOException ioe) {
			return waitForRequest();
		}
	}

	public void createServerSocket() {
		try {
			listenSocket = new ServerSocket(portTCP);
		} catch (IOException ioe) {
			System.out.println("Falha na criacao da socket de servidor");
		}
	}

	public void lookupRegistry() {
		rmiNapper.nap();		
	}
}

class VotingTableAutentication extends Thread {
	ArrayList<VotingTable> votingTables;
	RmiNapper rmiNapper;
	ServerListener serverListener;

	public void run() {		
		while(true) {
			this.cycle();
		}
	}
	
	public void cycle() {
		String line;
		int cc = -1, opcao;
		Election election = null;
		VotingTable votingTable;
		boolean pass = false;
		Credential credentials = null;
		ArrayList<String> list = new ArrayList<String>();
		TerminalConnection terminal = null;
		Boolean waitingForTerminal = false;
		
		// Auth
		while (credentials == null) {
			do {
				System.out.print("Cartao Cidadao: ");
				line = System.console().readLine();
				try {
					cc = Integer.parseInt(line);
					if (cc == -1) {
						pass = false;
					} else {
						pass = true;
					}
				} catch (Exception e) {
					pass = false;
				}
			} while (!pass);
			
			credentials = this.getCredentials(cc);
			
			if (credentials == null) System.out.println("Membro nao registado ou credenciais invalidas");
		}
		
		while (terminal == null) {
			synchronized (this.serverListener.terminals) {
				for (TerminalConnection terminalConnection : this.serverListener.terminals) {
					if (terminalConnection.getState() == Thread.State.WAITING) {
						terminal = terminalConnection;
					}
				}
			}
			
			if (terminal == null && !waitingForTerminal) {
				System.out.println("Nao existem terminais disponiveis de momento, aguarde...");
				waitingForTerminal = true;
			}	
		}
		
		this.votingTables = this.getVotingTables(cc);
		list.clear();
		
		if (this.votingTables.size() != 0) {
			for (VotingTable aux : this.votingTables) {
				election = getElection(aux.electionID);
				list.add(election.name);
			}

			opcao = this.selector(list, "Escolha uma eleicao");
			votingTable = this.votingTables.get(opcao);
			
			election = getElection(votingTable.electionID);
			
			synchronized (this.serverListener.terminals) {
				System.out.println("Terminal " + terminal.terminalID + " desbloqueado");
				terminal.credentials = credentials;
				terminal.election = election;
				terminal.votingLists = listVotingLists(election.id);
				terminal.log = new VotingLog(election, cc, votingTable.id);
				synchronized (terminal.terminalLock) {
					terminal.terminalLock.notify();
				}
				credentials = null;
				election = null;
			}
		} else {
			System.out.println("Nao existem eleicoes disponiveis");
		}

	}

	private Election getElection(int electionID) {
		try {
			return this.serverListener.registry.getElection(electionID);
		} catch (RemoteException remoteException) {
			System.out.println("Obtencao de eleicao falhada. A tentar novamente...");
			this.rmiNapper.nap();
		}
		
		return getElection(electionID);
	}

	public ArrayList<VotingList> listVotingLists(int electionID) {
		try {
			return this.serverListener.registry.listVotingLists(electionID);
		} catch (RemoteException re) {
			System.out.println("Obtencao de listas falhada. A tentar novamente...");
			this.rmiNapper.nap();
		}
		
		return this.listVotingLists(electionID);
	}

	public int selector(ArrayList<String> list, String title) {
		int i = 0;
		int opcao;
		String line;

		System.out.println("--------------------");
		System.out.println(title);
		System.out.println("--------------------");

		for (String item : list) {
			System.out.println("[" + i + "] " + item);
			++i;
		}

		System.out.print("Opcao: ");
		line = System.console().readLine();
		
		try {
			opcao = Integer.parseInt(line);
		} catch (Exception e) {
			System.out.println("Opcao invalida");
			return selector(list, title);
		}

		if (opcao >= i && opcao < 0) {
			System.out.println("Opcao invalida");
			return selector(list, title);
		}

		return opcao;
	}

	public ArrayList<VotingTable> getVotingTables(int cc) {
		try {
			return this.serverListener.registry.getVotingTables(this.serverListener.departmentName, cc);
		} catch (RemoteException remoteException) {
			System.out.println("Falha na obtencao de eleicoes disponiveis. A tentar novamente...");
			this.rmiNapper.nap();
		}
		
		return getVotingTables(cc);
	}

	public Credential getCredentials(int cc) {
		try {
			return this.serverListener.registry.getCredentials(cc);
		} catch (RemoteException remoteException) {
			System.out.println("Falha na obtencao de credenciais. A tentar novamente...");
			this.rmiNapper.nap();
		}
		
		return this.getCredentials(cc);
	}

	public VotingTableAutentication(RmiNapper rmiNapper, ServerListener serverListener) {
			this.rmiNapper = rmiNapper;
			this.serverListener = serverListener;

			this.start();
		}
}

class TerminalConnection extends Thread {
	boolean end = false;
	Object lock = new Object();
	Object terminalLock = new Object();
	Credential credentials;
	DataInputStream in;
	DataOutputStream out;
	boolean locked = true;
	Election election;
	ArrayList<VotingList> votingLists;
	VotingLog log;
	boolean watcherTimedout;
	Object watcherLock = new Object();
	TerminalWatcher terminalWatcher = null;
	int terminalID;
	ServerListener serverListener;
	Socket terminalSocket;

	public void run() {
		while(true) {
			synchronized (this.lock) {
				if (end) {
					this.closeSocket();
					return;
				}
			}
			
			this.cycle();
			// TODO Write logout
			System.out.println("Terminal " + terminalID + " bloqueado");
		}
	}
	
	public void cycle() {
		HashMap<String, String> response = null;
		String query;
		VotingList votingList;
		Date date;
		Boolean authorized = false;
		
		synchronized (this.terminalLock) {
			try {
				this.terminalLock.wait();
			} catch (InterruptedException e) {
				this.terminate();
			}
		}
		
		this.watcherTimedout = false;

		// Auth
		while (response == null) {
			synchronized (this.watcherLock) {
				if (this.watcherTimedout) {
					return;
				}
			}
			
			this.writeSocket("type|status;login|required");
			
			response = this.waitForRequest();
			
			if (response != null && response.get("type").equals("login")) {
				authorized = response.get("username").equals(credentials.username)
								  && response.get("password").equals(credentials.password);
				
				if (!authorized) {
					this.writeSocket("type|status;login|failed");
					response = null;
				} else {
					this.writeSocket("type|status;login|sucessful");
				}
			} else response = null;
		}
		
		response = null;
		
		// Get Elections
		while (response == null) {
			synchronized (this.watcherLock) {
				if (this.watcherTimedout) {
					return;
				}
			}
			
			response = this.waitForRequest();
			
			if (response != null && response.get("type").equals("request")) {
				if (response.get("datatype").equals("list")) {
					query = "type|item_list;datatype|list;item_count|" + (this.votingLists.size() + 2);
					query = query + ";item_0|Nulo;item_1|Branco";
					for (int i = 0; i < this.votingLists.size(); ++i) {
						votingList = this.votingLists.get(i);
						query = new String(query + ";item_" + (i + 2) + "|" + votingList.name);
					}

					this.writeSocket(query);
				} else response = null;
			} else response = null;
		}
		
		response = null;
		
		// Vote
		while (response == null) {
			synchronized (this.watcherLock) {
				if (this.watcherTimedout) {
					return;
				}
			}
			
			response = this.waitForRequest();
			
			
			if (response != null && response.get("type").equals("vote")) {
				date = new Date();
				this.log.date = date;
				this.sendLog(log);
				this.sendVote(election.id, response.get("list"));
			} else response = null;
		}
	}
		
	private void sendLog(VotingLog log) {
		try {
			this.serverListener.registry.sendLog(log);
			return;
		} catch (RemoteException remoteException) {
			this.serverListener.rmiNapper.nap();
		}
		
		sendLog(log);
	}

	private void sendVote(int electionID, String VotingList) {
		try {
			this.serverListener.registry.sendVote(electionID, VotingList);
			return;
		} catch (RemoteException remoteException) {
			this.serverListener.rmiNapper.nap();			
		}
		
		sendVote(electionID, VotingList);
	}

	public HashMap<String, String> waitForRequest() {
		try {
			while(this.in.available() == 0) {
				if (this.terminalWatcher == null) {
					this.terminalWatcher = new TerminalWatcher(this, this.watcherLock);
				}
			}
			
			this.terminalWatcher.terminate();
			this.terminalWatcher = null;
			
			synchronized (this.watcherLock) {
				if (this.watcherTimedout) {
					return null;
				}
			}
			
			return this.parseResponse(this.readSocket());

		} catch (IOException ioe) {
			System.out.println("Falha na utilizacao da socket");
			this.terminate();
		}


		return null;
	}

	public void closeSocket() {
		try {
			this.terminalSocket.close();
		} catch (IOException e) {
			return;
		}
	}

	public void writeSocket(String query) {
		try {
			this.out.writeUTF(query);
		} catch (IOException ioe) {
			System.out.println("Falha na escrita na socket");
			this.end = true;
		}
	}

	public String readSocket() {
		try {
			return this.in.readUTF();
		} catch (IOException e) {
			System.out.println("Falha na leitura na socket");
			this.end = true;
			return null;
		}
	}

	public HashMap<String, String> parseResponse(String response) {
		HashMap<String, String> out = new HashMap<String, String>();
		String[] pairs = response.split(";");
		String[] pairParts;

		for (String pair : pairs) {
			pairParts = pair.split("\\|");
			out.put(pairParts[0], pairParts[1]);
		}

		return out;
	}

	public void terminate() {
		synchronized (lock) {
			this.end = true;
		}
	}

	public TerminalConnection(ServerListener serverListener, int terminalID, Socket terminalSocket) {
		this.terminalID = terminalID;
		this.serverListener = serverListener;
		this.terminalSocket = terminalSocket;

		try {
			this.in = new DataInputStream(terminalSocket.getInputStream());
			this.out = new DataOutputStream(terminalSocket.getOutputStream());
		} catch (IOException ioe) {
			return;
		}

		this.start();
	}
}

class TerminalWatcher extends Thread {
	int timeout = 30;
	Object timeoutLock = new Object();
	TerminalConnection terminalConnection;
	Object watcherLock;
	Object lock = new Object();
	Boolean end = false;

	public void run() {
		while (true) {
			synchronized (this.lock) {
				if(this.end) return;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.exit(0);
			}
			
			synchronized (this.timeoutLock) {
				this.timeout = this.timeout - 1;
				
				if (this.timeout == 0) {
					synchronized (this.watcherLock) {
						this.terminalConnection.watcherTimedout = true;
						return;
					}
				}
			}
		}

	}

	public void terminate() {
		synchronized (this.lock) {
			this.end = true;
		}
	}

	public TerminalWatcher(TerminalConnection terminalConnection, Object watcherLock) {
		this.terminalConnection = terminalConnection;
		this.watcherLock = watcherLock;
		
		this.start();
	}
}

class RmiNapper extends Thread {
	int timeout = 30;
	int tries;
	boolean end = false;
	Object lock = new Object();
	boolean debug = true;
	ServerListener serverListener;

	public void run() {
		while(true) {
			synchronized (this.lock) {
				if (this.end) {
					return;
				}
			}
		}
	}

	synchronized public void nap() {
		
		try {
			this.serverListener.registry = (DataServerInterface)LocateRegistry
					.getRegistry(this.serverListener.portRMI)
					.lookup(this.serverListener.referenceRMI);
			return;
		} catch (AccessException e) {
			String error = e.getStackTrace().toString();
			System.out.println(error);
		} catch (RemoteException e) {
			String error = e.getStackTrace().toString();
			System.out.println(error);
		} catch (NotBoundException e) {
			String error = e.getStackTrace().toString();
			System.out.println(error);
		}
		
		this.tries = this.tries + 1;
		System.out.println("Trying to connect to RMI server, " + this.tries);
		
		if (this.tries == this.timeout) {
			System.out.println("Ligacao com o servidor principal nao pode ser estabelecida");
			
			//TODO connect to secondary rmi server
			
			synchronized (this.lock) {
				this.end = true;
			}
		}
		
		try {
			this.wait(1000);
		} catch (InterruptedException interruptedException) {
			System.exit(0);
		}
	}

	public void awake() {
		this.tries = 0;
	}

	public void terminate() {
		synchronized (this.lock) {
			this.end = true;
		}
	}

	public RmiNapper(ServerListener serverListener) {
		this.serverListener = serverListener;
		
		this.start();
	}
}
