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
		TerminalConnection terminalConnection;
		int terminalID;
		Socket terminalSocket;
		String terminalReference;
		
		lookupRegistry();
		createServerSocket();
		
		departmentName = getLocation();

		System.out.println("Mesa de voto");
		System.out.println("RMI => localhost:" + portRMI + "\\" + referenceRMI);
		System.out.println("TCP => localhost:" + portTCP);
		
		auth = new VotingTableAutentication(this.rmiNapper, this);

		while(true) {
			terminalSocket = waitForRequest();
			terminalReference = new String(
				terminalSocket.getInetAddress() + ":" + terminalSocket.getPort()
			);

			synchronized (terminals) {
				terminalID = findTerminal(terminalReference);
								
				if (terminalID != -1) {
					terminalConnection = terminals.get(terminalID);

					terminalConnection.terminate();

					terminals.set(
						terminalID,
						new TerminalConnection(terminalReference, terminalSocket, registry, this.rmiNapper)
					);

					System.out.println("Terminal " + terminalID + " reconectado (" + terminalReference + ")");
				} else {
					terminalID = terminals.size();
					
					terminals.add(
						terminalID,
						new TerminalConnection(terminalReference, terminalSocket, registry, this.rmiNapper)
				    );

				  System.out.println("Novo terminal (" + terminalID + ") conectado " + terminalReference);
				}
			}
		}
	}
	
	public int findTerminal(String terminalReference) {
		TerminalConnection terminalConnection;
		
		for (int i = 0; i < terminals.size(); ++i) {
			terminalConnection = terminals.get(i);
			if (terminalConnection.reference.equals(terminalReference)) return i;
		}
		
		return -1;
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
		} catch (RemoteException re) {
			lookupRegistry();
			return listDepartments(facultyName);
		}
	}

	public ArrayList<Faculty> listFaculties() {
		try {
			return registry.listFaculties();
		} catch (RemoteException remoteException) {
			lookupRegistry();
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
		try {
			try {
				registry = (DataServerInterface)LocateRegistry.getRegistry(portRMI).lookup(referenceRMI);
				rmiNapper.awake();
				return;
			} catch (RemoteException re) {
				Thread.sleep(1000);
				rmiNapper.nap();
			} catch (NotBoundException nbe) {
				Thread.sleep(1000);
				rmiNapper.nap();
			}
			lookupRegistry();
		} catch(InterruptedException interruptedException) {
			System.exit(0);
		}		
	}
}

class VotingTableAutentication extends Thread {
	ArrayList<VotingTable> votingTables;
	RmiNapper rmiNapper;
	ServerListener serverListener;
	boolean terminalAssigned = false;

	public void run() {
		String line;
		int cc = -1, opcao;
		Election election = null;
		VotingTable votingTable;
		boolean pass = false;
		Credential credentials = null;
		ArrayList<String> list = new ArrayList<String>();
		TerminalConnection terminal;

		while (true) { 
			if (credentials == null) {
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
			}

			if (credentials != null) {
				this.votingTables = this.getVotingTables(cc);
				list.clear();
				
				if (this.votingTables.size() != 0 && election == null) {
					System.out.println("id: " + this.votingTables.size());
					for (VotingTable aux : this.votingTables) {
						election = getElection(aux.electionID);
						list.add(election.name);
					}

					opcao = this.selector(list, "Escolha uma eleicao");
					votingTable = this.votingTables.get(opcao);
					
					election = getElection(votingTable.electionID);
					
					synchronized (this.serverListener.terminals) {
						if (this.serverListener.terminals.size() != 0) {
							for (int i = 0; i < this.serverListener.terminals.size(); ++i) {
								terminal = this.serverListener.terminals.get(i);
								System.out.println("HERE: " + terminal.getState());
								if (terminal.getState() == Thread.State.WAITING) {
									terminalAssigned = true;
									System.out.println("Terminal " + i + " desbloqueado");
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
								
								if (!terminalAssigned)
									System.out.println("Nao existem terminais disponiveis de momento, aguarde");
								
								terminalAssigned = false;
							}
						} else {
							System.out.println("Nao existem terminais disponiveis de momento, aguarde");
							try {
								Thread.sleep(500);
							} catch (InterruptedException interruptedException) {
								System.exit(0);
							}
						}
					}
				} else {
					if (election == null) {
						System.out.println("Nao existem eleicoes disponiveis");
						credentials = null;
					}
				}
			} else {
				System.out.println("Membro nao resgistado");
			}
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
		} catch (RemoteException re) {
			this.rmiNapper.nap();
		}
		return getVotingTables(cc);
	}

	public Credential getCredentials(int cc) {
		try {
			return this.serverListener.registry.getCredentials(cc);
		} catch (Exception e) {
			System.out.println("Falha na obtencao de credenciais: " + e);
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
	String reference;
	Credential credentials;
	DataInputStream in;
	DataOutputStream out;
	Socket terminalSocket;
	boolean locked = true;
	boolean debug = true;
	boolean authorized = false;
	Election election;
	ArrayList<VotingList> votingLists;
	DataServerInterface registry;
	VotingLog log;
	boolean watcherTimedout = false;
	Object watcherLock = new Object();
	RmiNapper rmiNapper;
	Boolean lockDown = false;
	TerminalWatcher terminalWatcher = null;

	public void run() {
		while(true) {
			synchronized (this.lock) {
				if (end) {
					this.closeSocket();
					return;
				}
			}
			
			this.cycle();
		}
	}
	
	public void cycle() {
		HashMap<String, String> response;
		String query;
		VotingList votingList;
		Date date;
		
		synchronized (this.terminalLock) {
			try {
				this.terminalLock.wait();
			} catch (InterruptedException e) {
				this.terminate();
			}
		}

		this.auth();
		
		while(true) {
			if (this.waitForRequest()) {
				response = this.parseResponse(this.readSocket());
				if (response != null) {
					if (response.get("type").equals("login")) {
						System.out.println("HERE: " + credentials.username + " " + credentials.password);
						this.authorized = response.get("username").equals(credentials.username)
													&& response.get("password").equals(credentials.password);
						
						if (this.authorized) {
							this.writeSocket("type|status;login|sucessful");
						} else {
							this.writeSocket("type|status;login|failed");
						}
					} else if (this.authorized && response.get("type").equals("vote")) {
						date = new Date();
						this.log.date = date;
						this.sendLog(log);
						this.sendVote(election.id, response.get("list"));
						break;
					} else if (this.authorized && response.get("type").equals("request")) {
						if (response.get("datatype").equals("list")) {
							query = "type|item_list;datatype|list;item_count|" + (this.votingLists.size() + 2);
							query = query + ";item_0|Nulo;item_1|Branco";
							for (int i = 0; i < this.votingLists.size(); ++i) {
								votingList = this.votingLists.get(i);
								query = new String(query + ";item_" + (i + 2) + "|" + votingList.name);
							}

							this.writeSocket(query);
						}
					}
				}
			} else {
				if (this.lockDown) break;
			}
		}
		
		this.lockDown = false;
		this.authorized = false;
	}
	
	private void sendLog(VotingLog log) {
		try {
			registry.sendLog(log);
		} catch (RemoteException remoteException) {
			this.rmiNapper.nap();
			sendLog(log);
		}
	}

	private void sendVote(int id, String string) {
		// TODO Auto-generated method stub
		
	}

	public void auth() {
		this.writeSocket("type|status;login|required");
	}

	public boolean waitForRequest() {
		boolean ret = false;
		try {
			ret = this.in.available() != 0;
			if (!ret) {
				if (this.terminalWatcher == null) {
					this.terminalWatcher = new TerminalWatcher(this, this.watcherLock);
				}
			} else {
				this.terminalWatcher.terminate();
				this.terminalWatcher = null;
			}

			synchronized (this.watcherLock) {
				if (this.watcherTimedout) {
					this.lockDown = true;
					return false;
				}
			}
		} catch (IOException ioe) {
			System.out.println("Falha na utilizacao da socket");
			this.terminate();
		}


		return ret;
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
			if (debug) System.out.println("Falha na escrita na socket");
			this.end = true;
		}
	}

	public String readSocket() {
		try {
			return this.in.readUTF();
		} catch (IOException e) {
			if (debug) System.out.println("Falha na leitura na socket");
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

	public TerminalConnection(
		String reference, Socket terminalSocket,
		DataServerInterface registry, RmiNapper rmiNapper
	) {
		this.reference = reference;
		this.terminalSocket = terminalSocket;
		this.rmiNapper = rmiNapper;

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
	int timeout = 10;
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
			
			synchronized (this.watcherLock) {
				if (this.terminalConnection.watcherTimedout) {
					this.terminalConnection.watcherTimedout = false;
				}
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
