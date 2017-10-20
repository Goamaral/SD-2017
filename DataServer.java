import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.rmi.registry.*;

/*
  RMI SERVER - RMI + UDP
  STATUS: WORKING
*/

public class DataServer extends UnicastRemoteObject implements DataServerConsoleInterface {
	public static void run(int port, String reference, DataServer s, int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			System.exit(0);
		}

		try {
			Registry reg = (Registry) createAndBindRegistry(port, reference, s);

			System.out.println("Server ready Port: " + port + " Reference: " + reference);
			while(true);
		} catch (RemoteException e) {
			System.out.println("Remote failure. Trying to reconnect...");
			run(port, reference, s, 1000);
		}
	}

	// DataServer Console Interface Methods
	public void createPerson(Person person) throws RemoteException {
		return;
	}

  public void createZone(Zone zone) throws RemoteException {
		return;
	}

  public void updateZone(Zone zone, Zone newZone) throws RemoteException {
		return;
	}

  public void removeZone(Zone zone) throws RemoteException {
		return;
	}

  public Zone[] listZones(String type) throws RemoteException {
		return null;
	}

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

	public static Registry createAndBindRegistry(int port, String reference, DataServer s) throws RemoteException {
		Registry reg = LocateRegistry.createRegistry(port);
		reg.rebind(reference, s);
		return reg;
	}

	public DataServer() throws RemoteException {
		super();
	}

	public static void main(String args[]) {
		DataServer s = null;

		try {
			s = new DataServer();
		} catch (RemoteException e) {
			System.out.println(e);
			System.exit(-1);
		}

		int port = getPort(args);;
		String reference = getReference(args);
		run(port, reference, s, 0);
	}

}
