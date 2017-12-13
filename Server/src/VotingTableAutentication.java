import Core.*;

import java.rmi.RemoteException;
import java.util.ArrayList;

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
					if (terminalConnection.getState() == State.WAITING) {
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

			opcao = Utility.selector(list, "Escolha uma eleicao");
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
