import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.util.*;

public class Server {
	static DataServerServerInterface registry;
	static ServerSocket listenSocket;
	static int portRMI = 7000;
	static String referenceRMI = "iVotasServer";
	static int portTCP = 7001;
	static boolean debug = true;
	static ArrayList<String> terminalIDs = new ArrayList<String>();
	static ArrayList<TerminalConnection> terminals = new ArrayList<TerminalConnection>();

	public static void main(String args[]) {
		TerminalConnection terminalConnection;
		setSecurityPolicies();
		getOptions(args);
		//lookupRegistry(portRMI, referenceRMI);

		try {
			listenSocket = new ServerSocket(portTCP);
			System.out.println("Mesa de voto");
			if (debug) {
				System.out.println("RMI => localhost:" + portRMI + "\\" + referenceRMI);
				System.out.println("TCP => localhost:" + portTCP);
			}

			while(true) {
				Socket terminalSocket = listenSocket.accept();
				String terminalReference = new String(
					terminalSocket.getInetAddress() + ":" + terminalSocket.getPort()
				);

				if (debug) System.out.println("New socket connected: " + terminalReference);

				if (terminalID = terminalIDs.indexOf(terminalReference) == -1) {
					terminalID = terminalIDs.size();
					terminalIDs.add(terminalReference);
				}

				if (terminals.size() < terminalID) {
					terminalConnection = terminals.get(terminalID);
					terminalConnection.terminate();
					terminal = new TerminalConnection(terminalID, terminalSocket);
					terminals.add(terminalID, terminalConnection);
				}
			}

		} catch (Exception e) {
			try {
				Thread.sleep(1000);
				main(args);
			} catch (Exception e1) {
				System.exit(0);
			}
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
			registry = (DataServerServerInterface)LocateRegistry.getRegistry(port).lookup(reference);
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

class TerminalConnection extends Thread {
	static boolean end = false;
	static Object lock = new Object();
	int terminalID;
	TerminalConnection terminalConnection;

	public void run() {
		while (!end) {

		}
	}

	public void terminate() {
    synchronized (lock) {
      end = true;
    }
  }

	public TerminalConnection(int terminalID, TerminalConnection terminalConnection) {
    this.terminalID = terminalID;
		this.terminalConnection = terminalConnection;

    this.start();
  }
}
