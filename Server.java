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
	static String referenceRMI = "iVotasServer";
	static int portTCP = 7001;
	static boolean debug = true;
	static ArrayList<String> terminalIDs = new ArrayList<String>();
	static ArrayList<TerminalConnection> terminals = new ArrayList<TerminalConnection>();
	static VotingTableAutentication auth = null;
	static VoteSender voteSender = null;
	static Department location;

	public static void main(String args[]) {
		TerminalConnection terminalConnection;
		int terminalID;
		Socket terminalSocket;
		String terminalReference;

		setSecurityPolicies();
		getOptions(args);
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
			voteSender.terminate();
			voteSender = new VoteSender(terminalIDs, registry);
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
		return registry.listDepartments(faculty);
	}

	public static ArrayList<Faculty> listFaculties() {
		return registry.listFaculties();
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
					System.out.println("java Server -rp 7000 -rr iVotas -tp 7001");
					System.exit(0);
					break;
			}
		}
	}

	public static void lookupRegistry(int port, String reference) {
		try {
			registry = (DataServerInterface)LocateRegistry.getRegistry(port).lookup(reference);
		} catch (Exception e1) {
			System.out.println("Remote failure. Trying to reconnect...");
			try {
				Thread.sleep(1000);
			} catch (Exception e2) {
				System.exit(0);
			}
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

	public void run() {
		Scanner scanner = new Scanner(System.in);
		String line;
		int cc = -1;
		boolean pass = false;
		Credential credentials;
		Election election;

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
				elections = this.getElections(cc);

				// NOTE selector election

				synchronized (this.terminals) {
					for (TerminalConnection terminal : this.terminals) {
						if (terminal.getState() == Thread.State.WAITING) {
							System.out.println("Terminal " + terminal.terminalID + " desbloqueado");
							terminal.credentials = credentials;
							terminal.election = election;
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

	public ArrayList<Election> getElections(int cc) {
		// NOTE based on cc and location
	}

	public Credential getCredentials(int cc) {
		try {
			return this.registry.getCredentials(cc);
		} catch (Exception e) {
			return this.getCredentials(cc);
		}
	}

	public void terminate() {
		synchronized (lock) {
			end = true;
		}
	}

	public VotingTableAutentication(
		ArrayList<String> terminalIDs,
		ArrayList<TerminalConnection> terminals,
		DataServerInterface registry,
		Department location
	) {
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
	VoteSender voteSender;
	DataServerInterface registry;

	public void run() {
		HashMap<String, String> response;
		ArrayList<List> lists;
		String query;
		List list;
		Vote vote;

		while (true) {
			synchronized (this.lock) {
				if (end) {
					this.closeSocket();
					return;
				}
			}

			if (!this.authorized) this.logout();

			if (this.inAvailable()) {
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
						vote = new Vote(this.election, this.terminalID, response.get("list"));
						synchronized (this.voteSender.votes) {
							this.voteSender.votes.addFirst(vote);
						}
						this.logout();
					} else if (this.authorized && response.get("type").equals("request")) {
						if (response.get("datatype").equals("list")) {
							lists = this.listLists();
							query = "type|item_list;datatype|list;item_count|" + lists.size();
							for (int i = 0; i<lists.size(); ++i) {
								list = lists.get(i);
								query = new String(query + ";item_" + i + "|" + list.name);
							}

							this.writeSocket(query);
						}
					}
				}
			}
		}
	}

	// NOTE code timeouts
	public ArrayList<List> listLists() {
		try {
			synchronized (this.election) {
				return this.registry.listLists(this.election);
			}
		} catch (RemoteException re) {
			this.nap();
			return this.listLists();
		}
	}

	public void logout() {
		synchronized (this.terminalLock) {
			this.lock();
			this.unlock();
		}

		this.writeSocket("type|status;login|required");
	}

	public boolean inAvailable() {
		try {
			return this.in.available() != 0;
		} catch (IOException ioe) {
			System.out.println("Falha na utilizacao da socket");
			synchronized (this.lock) {
				this.end = true;
				return false;
			}
		}
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

	public void nap() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
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

		this.start();
	}
}

class VoteSender extends Thread {
	boolean end = false;
	Object lock = new Object();
	ArrayList<String> terminals;
	ArrayList<Integer> voteNumbers;
	LinkedList<Vote> votes = new LinkedList<Vote>();
	int id;
	DataServerInterface registry;

	public void run() {
		Vote vote;
		int delta;

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
				if (this.end) {
					synchronized (this.votes) {
						if (this.votes.size() > 0) {
							System.out.println("Ainda existem votos por enviar");
						} else return;
					}
				}
			}

			synchronized (this.votes) {
				if (this.votes.size() > 0) {
					vote = this.votes.removeLast();
					id = vote.terminalID;

					this.voteNumbers.set(id, this.voteNumbers.get(id) + 1);
					vote.voteNumber = this.voteNumbers.get(id);
					try {
						this.registry.sendVote(vote);
					} catch (RemoteException re) {
						vote.voteNumber = -1;
						this.voteNumbers.set(id, this.voteNumbers.get(id) - 1);
						this.votes.addLast(vote);
					}
				}
			}
		}
	}

	public void terminate() {
		synchronized (lock) {
			this.end = true;
		}
	}

	public VoteSender(ArrayList<String> terminals, DataServerInterface registry) {
		this.terminals = terminals;
		this.registry = registry;

		this.setDaemon(true);
		this.start();
	}
}
