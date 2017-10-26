import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.util.*;
import java.lang.Thread.State;
import java.io.*;

public class Server {
	static DataServerInterface registry;
	static ServerSocket listenSocket;
	static int portRMI = 7000;
	static String referenceRMI = "iVotasServer";
	static int portTCP = 7001;
	static boolean debug = true;
	static ArrayList<String> terminalIDs = new ArrayList<String>();
	static ArrayList<TerminalConnection> terminals = new ArrayList<TerminalConnection>();
	static VotingTableAutentication auth = null;

	public static void main(String args[]) {
		TerminalConnection terminalConnection;
		int terminalID;
		Socket terminalSocket;
		String terminalReference;

		setSecurityPolicies();
		getOptions(args);
		// NOTE lookupRegistry(portRMI, referenceRMI);

		createServerSocket();
		System.out.println("Mesa de voto");
		if (debug) {
			System.out.println("RMI => localhost:" + portRMI + "\\" + referenceRMI);
			System.out.println("TCP => localhost:" + portTCP);
		}

		if (auth != null) {
			auth.terminate();
			auth = new VotingTableAutentication(terminalIDs, terminals, registry);
		} else auth = new VotingTableAutentication(terminalIDs, terminals, registry);

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

						terminals.add(terminalID, new TerminalConnection(terminalID, terminalSocket));
						if (debug) System.out.println("TerminalConnection created at " + terminalID + " " + terminalIDs.get(terminalID));
					} else {
						terminals.add(terminalID, new TerminalConnection(terminalID, terminalSocket));
						if (debug) System.out.println("TerminalConnection created at " + terminalID + " " + terminalIDs.get(terminalID));
					}
				}
			}
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

	public void run() {
		Scanner scanner = new Scanner(System.in);
		String line;
		int cc = -1;
		boolean pass = false;
		Credential credentials;

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
				synchronized (this.terminals) {
					for (TerminalConnection terminal : this.terminals) {
						if (debug) System.out.println("ID: " + terminal.terminalID + " STATE: " + terminal.getState());
						if (terminal.getState() == Thread.State.WAITING) {
							terminal.credentials = credentials;
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

	public Credential getCredentials(int cc) {
		try {
			return new Credential("goa","root"); // NOTE
			//return this.registry.getCredentials(cc);
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
		DataServerInterface registry
	) {
			this.terminalIDs = terminalIDs;
			this.terminals = terminals;
			this.registry = registry;

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

	public void run() {
		HashMap<String, String> response;

		while (true) {
			synchronized (this.lock) {
				if (end) {
					this.closeSocket();
					return;
				}
			}

			if (!this.authorized) this.reset();

			if (this.inAvailable()) {
				response = this.parseResponse(this.readSocket());
				if (response != null) {
					if (response.get("type").equals("login")) {
						this.authorized = response.get("username").equals(credentials.username)
													&& response.get("password").equals(credentials.password);

						if (this.authorized) {
							this.writeSocket("type|status;login|sucessful;msg|Podes votar :D");
						} else {
							this.writeSocket("type|status;login|failed");
						}
					} else if (this.authorized && response.get("type").equals("vote")) {

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

	public TerminalConnection(int terminalID, Socket terminalSocket) {
		this.terminalID = terminalID;
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
