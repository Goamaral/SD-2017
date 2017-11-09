import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.util.*;
import java.lang.Thread.State;
import java.io.*;

class Server {
	static DataServerInterface registry;
	static ServerSocket listenSocket;
	static int portRMI = 7000;
	static String ipRMI = "localhost";
	static String referenceRMI = "iVotas";
	static int portTCP = 7001;
	static boolean debug = true;
	static ArrayList<String> terminalIDs = new ArrayList<String>();
	static ArrayList<TerminalConnection> terminals = new ArrayList<TerminalConnection>();
	static VotingTableAutentication auth = null;
	static VoteSender voteSender = null;
	static Department location;
	static RmiNapper rmiNapper;

	public static void main(String args[]) {
		TerminalConnection terminalConnection;
		int terminalID;
		Socket terminalSocket;
		String terminalReference;
		LinkedList<Vote> votes;
		LinkedList<Log> logs;

		setSecurityPolicies();
		getOptions(args);
		if (rmiNapper == null) rmiNapper = new RmiNapper();
		lookupRegistry(portRMI, referenceRMI);
		createServerSocket();
		location = getLocation();

		System.out.println("Mesa de voto");
		if (debug) {
			System.out.println("RMI => localhost:" + portRMI + "\\" + referenceRMI);
			System.out.println("TCP => localhost:" + portTCP);
		}

		for (TerminalConnection terminal : terminals) {
			terminal.terminate();
		}

		if (voteSender != null) {
			synchronized (voteSender.votes) {
				votes = voteSender.votes;
			}

			synchronized (voteSender.logs) {
				logs = voteSender.logs;
			}

			voteSender.terminate();
			voteSender = new VoteSender(terminalIDs, registry);
			voteSender.votes = votes;
			voteSender.logs = logs;
		} else voteSender = new VoteSender(terminalIDs, registry);

		if (auth != null) {
			auth.terminate();
			auth = new VotingTableAutentication(terminalIDs, terminals, registry, location);
		} else auth = new VotingTableAutentication(terminalIDs, terminals, registry, location);

		while(true) {
			terminalSocket = waitForRequest();
			terminalReference = new String(
				terminalSocket.getInetAddress() + ":" + terminalSocket.getPort()
			);

			synchronized (terminalIDs) {
				terminalID = terminalIDs.indexOf(terminalReference);
				if (terminalID == -1) {
					terminalID = terminalIDs.size();
					terminalIDs.add(terminalReference);
				}

				synchronized (terminals) {
					if (terminalID < terminals.size()) {
						terminalConnection = terminals.get(terminalID);

						terminalConnection.terminate();

						terminals.set(
							terminalID,
							new TerminalConnection(terminalID, terminalSocket, voteSender, registry)
						);

						if (debug)
							System.out.println(
								"TerminalConnection created at " + terminalID + " "
								+ terminalIDs.get(terminalID)
							);
					} else {
						terminals.add(
							terminalID,
							new TerminalConnection(terminalID, terminalSocket, voteSender, registry)
							);

						if (debug)
							System.out.println(
								"TerminalConnection created at " + terminalID + " "
								+ terminalIDs.get(terminalID)
							);
					}
				}
			}
		}
	}

	public static void nap() {
		try {
			Thread.sleep(1000);
			rmiNapper.nap();
		} catch (InterruptedException ie) {
			System.exit(0);
		}
	}

	public static Department getLocation() {
		ArrayList<Faculty> faculties = listFaculties();
		ArrayList<Department> departments;
		ArrayList<String> list = new ArrayList<String>();
		int opcao;
		Faculty faculty;
		Department department;

		for (Faculty auxFaculty : faculties) {
			list.add(auxFaculty.name);
		}

		opcao = selector(list, "Selecione a faculdade onde se situa");

		faculty = faculties.get(opcao);

		departments = listDepartments(faculty);

		list.clear();

		for (Department auxDepartment : departments) {
			list.add(auxDepartment.name);
		}

		opcao = selector(list, "Selecione o departmento onde se situa");

		return departments.get(opcao);

	}

	public static ArrayList<Department> listDepartments(Faculty faculty) {
		ArrayList<Department> ret;
		try {
			ret = registry.listDepartments(faculty);
			rmiNapper.awake();
			return ret;
		} catch (RemoteException re) {
			nap();
			return listDepartments(faculty);
		}
	}

	public static ArrayList<Faculty> listFaculties() {
		ArrayList<Faculty> ret;
		try {
			ret = registry.listFaculties();
			rmiNapper.awake();
			return ret;
		} catch (RemoteException re) {
			nap();
			return listFaculties();
		}
	}

	public static int selector(ArrayList<String> list, String title) {
		int i = 0;
		int opcao;
		Scanner scanner = new Scanner(System.in);
		String line;

		System.out.println("--------------------");
		System.out.println(title);
		System.out.println("--------------------");

		for (String item : list) {
			System.out.println("[" + i + "] " + item);
			++i;
		}

		System.out.print("Opcao: ");
		line = scanner.nextLine();

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

	public static Socket waitForRequest() {
		try {
			return listenSocket.accept();
		} catch (IOException ioe) {
			return waitForRequest();
		}
	}

	public static void createServerSocket() {
		try {
			listenSocket = new ServerSocket(portTCP);
		} catch (IOException ioe) {
			System.out.println("Falha na criacao da socket de servidor");
		}
	}

	public static void getOptions(String[] args) {
		for (int i=0; i<args.length; ++i) {
			switch (args[i]) {
				case "-rp":
				case "--rmiport":
					try {
						portRMI = Integer.parseInt(args[i+1]);
					} catch (Exception e) {
						System.out.println("No RMI port provided, using default: 7000");
					}
					break;
				case "-ri":
				case "--rmiip":
					try {
						ipRMI = args[i+1];
					} catch (Exception e) {
						System.out.println("No RMI ip provided, using default: localhost");
					}
					break;
				case "-rr":
				case "--rmireference":
					try {
						referenceRMI = args[i+1];
					} catch (Exception e1) {
						System.out.println("No valid RMI reference provided, using default: iVotas");
					}
					break;
				case "-tp":
				case "--tcpport":
					try {
						portTCP = Integer.parseInt(args[i+1]);
					} catch (Exception e2) {
						System.out.println("No valid TCP port provided, using default: 7001");
					}
					break;
				case "-h":
				case "--help":
					System.out.println("java Server -rp 7000 -ri localhost -rr iVotas -tp 7001");
					System.exit(0);
					break;
			}
		}
	}

	public static void lookupRegistry(int port, String reference) {
		try {
			registry = (DataServerInterface)LocateRegistry.getRegistry(port).lookup(reference);
			rmiNapper.awake();
			return;
		} catch (RemoteException re) {
			nap();
			lookupRegistry(port, reference);
		} catch (NotBoundException nbe) {
			nap();
			lookupRegistry(port, reference);
		}
	}

	public static void setSecurityPolicies() {
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new SecurityManager());
	}
}

class VotingTableAutentication extends Thread {
	boolean end = false;
	Object lock = new Object();
	ArrayList<String> terminalIDs;
	ArrayList<TerminalConnection> terminals;
	DataServerInterface registry;
	boolean debug = true;
	ArrayList<Election> elections;
	Department location;
	RmiNapper rmiNapper;

	public void run() {
		Scanner scanner = new Scanner(System.in);
		String line;
		int cc = -1;
		boolean pass = false;
		Credential credentials;
		Election election;
		int opcao;
		ArrayList<String> list =  new ArrayList<String>();

		while (true) {
			synchronized (this.lock) {
				if (end) {
					break;
				}
			}

			do {
				System.out.print("Cartao Cidadao: ");
				line = scanner.nextLine();
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

			if (credentials != null) {
				this.elections = this.getElections(cc);
				list.clear();

				for (Election electionAux : this.elections) {
					list.add(electionAux.name);
				}

				opcao = this.selector(list, "Escolha uma eleicao");
				election = this.elections.get(opcao);

				synchronized (this.terminals) {
					for (TerminalConnection terminal : this.terminals) {
						if (terminal.getState() == Thread.State.WAITING) {
							System.out.println("Terminal " + terminal.terminalID + " desbloqueado");
							terminal.credentials = credentials;
							terminal.election = election;
							terminal.lists = listLists(election);
							terminal.log = new Log(location, election, cc);
							synchronized (terminal.terminalLock) {
								terminal.terminalLock.notify();
							}
						}
					}
				}
			} else {
				System.out.println("Membro nao resgistado");
			}
		}
	}

	public ArrayList<List> listLists(Election election) {
		ArrayList<List> ret;

		try {
			ret = this.registry.listLists(election);
			this.rmiNapper.awake();
			return ret;
		} catch (RemoteException re) {
			this.nap();
			return this.listLists(election);
		}
	}

	public int selector(ArrayList<String> list, String title) {
		int i = 0;
		int opcao;
		Scanner scanner = new Scanner(System.in);
		String line;

		System.out.println("--------------------");
		System.out.println(title);
		System.out.println("--------------------");

		for (String item : list) {
			System.out.println("[" + i + "] " + item);
			++i;
		}

		System.out.print("Opcao: ");
		line = scanner.nextLine();

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

	public ArrayList<Election> getElections(int cc) {
		ArrayList<Election> ret;

		try {
			ret = registry.listElections(location, cc);
			this.rmiNapper.awake();
			return ret;
		} catch (RemoteException re) {
			this.nap();
			return getElections(cc);
		}
	}

	public void nap() {
		try {
			this.wait(1000);
			this.rmiNapper.nap();
		} catch (InterruptedException ie) {
			this.end = true;
		}
	}

	public Credential getCredentials(int cc) {
		Credential ret;

		try {
			ret = this.registry.getCredentials(cc);
			this.rmiNapper.awake();
			return ret;
		} catch (Exception e) {
			this.nap();
			return this.getCredentials(cc);
		}
	}

	public void terminate() {
		synchronized (lock) {
			end = true;
		}
	}

	public VotingTableAutentication(
		ArrayList<String> terminalIDs, ArrayList<TerminalConnection> terminals,
		DataServerInterface registry, Department location) {
			this.terminalIDs = terminalIDs;
			this.terminals = terminals;
			this.registry = registry;
			this.location = location;

			this.start();
		}
}

class TerminalConnection extends Thread {
	boolean end = false;
	Object lock = new Object();
	Object terminalLock = new Object();
	int terminalID;
	Credential credentials;
	DataInputStream in;
	DataOutputStream out;
	Socket terminalSocket;
	boolean locked = true;
	boolean debug = true;
	boolean authorized = false;
	Election election;
	ArrayList<List> lists;
	VoteSender voteSender;
	DataServerInterface registry;
	Log log;
	TerminalWatcher terminalWatcher;
	boolean watcherTimedout = false;
	Object watcherLock = new Object();

	public void run() {
		HashMap<String, String> response;
		String query;
		List list;
		Vote vote;
		Date date;

		while (true) {
			synchronized (this.lock) {
				if (end) {
					this.closeSocket();
					return;
				}
			}

			if (!this.authorized) this.logout();

			if (this.waitForRequest()) {
				response = this.parseResponse(this.readSocket());
				if (response != null) {
					if (response.get("type").equals("login")) {
						this.authorized = response.get("username").equals(credentials.username)
													&& response.get("password").equals(credentials.password);

						if (this.authorized) {
							this.writeSocket("type|status;login|sucessful");
						} else {
							this.writeSocket("type|status;login|failed");
						}
					} else if (this.authorized && response.get("type").equals("vote")) {
						date = new Date();
						vote = new Vote(this.election, this.terminalID, response.get("list"), date);
						this.log.date = date;
						synchronized (this.voteSender.votes) {
							this.voteSender.votes.addFirst(vote);
							this.voteSender.logs.addFirst(this.log);
						}
						this.logout();
					} else if (this.authorized && response.get("type").equals("request")) {
						if (response.get("datatype").equals("list")) {
							query = "type|item_list;datatype|list;item_count|" + this.lists.size();
							for (int i = 0; i<this.lists.size(); ++i) {
								list = this.lists.get(i);
								query = new String(query + ";item_" + i + "|" + list.name);
							}

							this.writeSocket(query);
						}
					}
				}
			}
		}
	}

	public void logout() {
		synchronized (this.terminalLock) {
			this.lock();
			this.unlock();
		}

		this.writeSocket("type|status;login|required");
	}

	public boolean waitForRequest() {
		boolean ret = false;
		this.terminalWatcher.start();
		try {
			ret = this.in.available() != 0;
			this.terminalWatcher.awake();
			synchronized (this.watcherLock) {
				if (this.watcherTimedout) {
					this.logout();
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

	public void unlock() {
		synchronized (this.lock) {
			if (this.locked) {
				synchronized (this.terminalLock) {
					this.terminalLock.notify();
				}
				this.locked = false;
			}
		}
	}

	public void lock() {
		try {
			synchronized (this.lock) {
				if (!this.locked) {
					synchronized (this.terminalLock) {
						this.terminalLock.wait();
					}
					this.locked = true;
				}
			}
		} catch (InterruptedException ie) {
			this.unlock();
		}
	}

	public void writeSocket(String query) {
		try {
			this.out.writeUTF(query);
		} catch (IOException ioe) {
			if (debug) System.out.println("Falha na utilizacao da socket");
			this.end = true;
		}
	}

	public String readSocket() {
		try {
			return this.in.readUTF();
		} catch (IOException e) {
			if (debug) System.out.println("Falha na utilizacao da socket");
			this.end = true;
			return null;
		}
	}

	public HashMap<String, String> parseResponse(String response) {
		HashMap<String, String> out = new HashMap<String, String>();
		String[] pairs = response.split(";");
		String[] pairParts;

		if (response == null) return null;

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
		int terminalID, Socket terminalSocket, VoteSender voteSender,
		DataServerInterface registry
	) {
		this.terminalID = terminalID;
		this.terminalSocket = terminalSocket;
		this.voteSender = voteSender;

		try {
			this.in = new DataInputStream(terminalSocket.getInputStream());
			this.out = new DataOutputStream(terminalSocket.getOutputStream());
		} catch (IOException ioe) {
			return;
		}

		this.terminalWatcher = new TerminalWatcher(this.watcherTimedout, this.watcherLock);

		this.start();
	}
}

class TerminalWatcher extends Thread {
	int timeout = 120;
	Object timeoutLock = new Object();
	boolean watcherTimedout;
	Object watcherLock;

	public void run() {
		while (true);
	}

	public void start() {
		while (true) {
			synchronized (this.watcherLock) {
				if (this.watcherTimedout) {
					this.watcherTimedout = false;
				}
			}
			try {
				this.wait(1000);
			} catch (InterruptedException e) {
				System.exit(0);
			}
			synchronized (this.timeoutLock) {
				this.timeout = this.timeout - 1;
				if (this.timeout == 0) {
					synchronized (this.watcherLock) {
						this.watcherTimedout = true;
						return;
					}
				}
			}
		}
	}

	public void awake() {
		synchronized (this.watcherLock) {
			this.watcherTimedout = false;
		}
		synchronized (this.timeoutLock) {
			this.timeout = 120;
		}
	}

	public TerminalWatcher(boolean watcherTimedout, Object watcherLock) {
		this.watcherTimedout = watcherTimedout;
		this.watcherLock = watcherLock;
		this.start();
	}
}

class VoteSender extends Thread {
	boolean end = false;
	Object lock = new Object();
	ArrayList<String> terminals;
	ArrayList<Integer> voteNumbers;
	LinkedList<Vote> votes = new LinkedList<Vote>();
	LinkedList<Log> logs = new LinkedList<Log>();
	int id;
	DataServerInterface registry;
	RmiNapper rmiNapper;
	boolean terminator = false;

	public void run() {
		Vote vote;
		int delta;
		Log log;

		while (true) {
			synchronized (this.terminals) {
				delta = this.terminals.size() - this.voteNumbers.size();
				if (delta > 0) {
					for (int i=0; i<delta; ++i) {
						this.voteNumbers.add(this.voteNumbers.size()+i, 0);
					}
				}
			}

			synchronized (lock) {
				if (this.terminator) {
					return;
				}

				if (this.end) {
					synchronized (this.votes) {
						synchronized (this.logs) {
							if (this.votes.size() > 0 || this.logs.size() > 0) {
								System.out.println("Ainda existem votos por enviar");
							} else return;
						}
					}
				}
			}

			synchronized (this.votes) {
				if (this.votes.size() > 0) {
					vote = this.votes.removeLast();
					id = vote.terminalID;

					this.voteNumbers.set(id, this.voteNumbers.get(id) + 1);
					vote.voteNumber = this.voteNumbers.get(id);
					if (vote.date.after(vote.election.start) && vote.date.before(vote.election.end)) {
						try {
							this.registry.sendVote(vote);
							this.rmiNapper.awake();
						} catch (RemoteException re) {
							vote.voteNumber = -1;
							this.voteNumbers.set(id, this.voteNumbers.get(id) - 1);
							this.votes.addLast(vote);
							this.nap();
						}
					}
				}
			}

			synchronized (this.logs) {
				if (this.logs.size() > 0) {
					log = this.logs.removeLast();

					if (log.date.after(log.election.start) && log.date.before(log.election.end)) {
						try {
							this.registry.sendLog(log);
							this.rmiNapper.awake();
						} catch (RemoteException re) {
							this.logs.addLast(log);
							this.nap();
						}
					}
				}
			}
		}
	}

	public void nap() {
		try {
			this.wait(1000);
			this.rmiNapper.nap();
		} catch (InterruptedException ie) {
			System.exit(0);
		}
	}

	public void terminate() {
		synchronized (lock) {
			this.end = true;
			this.terminator = true;
		}
	}

	public VoteSender(ArrayList<String> terminals, DataServerInterface registry) {
		this.terminals = terminals;
		this.registry = registry;

		this.setDaemon(true);
		this.rmiNapper = new RmiNapper();
		this.start();
	}
}

class RmiNapper extends Thread {
	int timeout = 30;
	int tries;
	boolean end = false;
	Object lock = new Object();
	boolean debug = true;

	public void run() {
		while(true) {
			synchronized (this.lock) {
				if (this.end) {
					return;
				}
			}
		}
	}

	public void nap() {
		this.tries = this.tries + 1;
		if (this.debug) System.out.println("Trying to connect to RMI server, " + this.tries);
		if (this.tries == this.timeout) {
			System.out.println("Ligacao com o servidor principal nao pode ser estabelecida");
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

	public RmiNapper() {
		this.start();
	}
}
