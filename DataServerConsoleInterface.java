import java.rmi.*;

public interface DataServerConsoleInterface extends Remote {
  public void createPerson(Person person) throws RemoteException;
  public void createZone(Zone zone) throws RemoteException;
  public void updateZone(Zone zone, Zone newZone) throws RemoteException;
  public void removeZone(Zone zone) throws RemoteException;
  public Zone[] listZones(String type) throws RemoteException;
  public void createElection(Election election) throws RemoteException;
  public Election[] listElections(String type) throws RemoteException;
  public void createList(List list) throws RemoteException;
  public List[] listLists(Election type) throws RemoteException;
  public void removeList(List list) throws RemoteException;
  public Person[] listCandidates(List list) throws RemoteException;
  public void addCandidate(List list, Person person) throws RemoteException;
  public void removeCandidate(List list, Person person) throws RemoteException;
  public void createVotingTable(VotingTable votingTable) throws RemoteException;
  public void removeVotingTable(VotingTable votingTable) throws RemoteException;
}
