import java.rmi.*;
import java.util.*;

public interface DataServerInterface extends Remote {
  // People
  public void createPerson(Person person) throws RemoteException;

  // Buildings
  public void createZone(Zone zone) throws RemoteException;
  public void updateZone(Zone zone, Zone newZone) throws RemoteException;
  public void removeZone(Zone zone) throws RemoteException;
  public ArrayList<Faculty> listFaculties() throws RemoteException;
  public ArrayList<Department> listDepartments(Faculty faculty) throws RemoteException;

  // Elections
  public void createElection(Election election) throws RemoteException;
  public ArrayList<Election> listElections(String type, String subtype) throws RemoteException;
	public ArrayList<Election> listElections(Department department, int cc) throws RemoteException;

  // Lists
  public void createList(List list) throws RemoteException;
  public ArrayList<List> listLists(Election election) throws RemoteException;
  public void removeList(List list) throws RemoteException;
  public ArrayList<Person> listCandidates(List list) throws RemoteException;

  // List candidates
  public void addCandidate(List list, Person person) throws RemoteException;
  public void removeCandidate(List list, Person person) throws RemoteException;

  // Voting tables
  public void createVotingTable(VotingTable votingTable) throws RemoteException;
  public void removeVotingTable(VotingTable votingTable) throws RemoteException;

	// Votes
	public void sendVote(Vote vote) throws RemoteException;
	public void sendLog(Log log) throws RemoteException;

	// Authentication
	public Credential getCredentials(int cc) throws RemoteException;
}
