import java.rmi.*;

public interface DataServerConsoleInterface extends Remote {
  public boolean createPerson(Person person) throws RemoteException;
  public boolean createZone(Zone zone) throws RemoteException;
  public boolean updateZone(Zone zone, Zone newZone) throws RemoteException;
  public boolean removeZone(Zone zone) throws RemoteException;
  public Zone[] listZones(String type) throws RemoteException;
  public boolean createElection(Election election) throws RemoteException;
  public Election[] listElections(String type) throws RemoteException;
  public boolean createList(List list) throws RemoteException;
  public List[] listLists(Election type) throws RemoteException;
  public boolean removeList(List list) throws RemoteException;
  public Person[] listCandidates(List list) throws RemoteException;
  public boolean addCandidate(List list, Person person) throws RemoteException;
  public boolean removeCandidate(List list, Person person) throws RemoteException;
  public boolean createVotingTable(VotingTable votingTable) throws RemoteException;
}
