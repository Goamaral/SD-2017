import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
