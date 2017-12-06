
import java.rmi.*;
import java.util.*;


public interface DataServerInterface extends Remote {
	// Person
	public int createPerson(Person person) throws RemoteException;
	public ArrayList<Person> listStudentsFromDepartment(String departmentName) throws RemoteException;
	public ArrayList<Person> listPeopleOfType(String type) throws RemoteException;
	
	// Faculty
	public String createFaculty(String name) throws RemoteException;
	public void removeFaculty(String name) throws RemoteException;
	public String updateFaculty(Faculty faculty, Faculty newFaculty) throws RemoteException;
	public ArrayList<Faculty> listFaculties() throws RemoteException;
	
	// Department
	public String createDepartment(Department department) throws RemoteException;
	public void removeDepartment(String name) throws RemoteException;
	public String updateDepartment(Department department, Department newDepartment) throws RemoteException;
	public ArrayList<Department> listDepartments(String facultyName) throws RemoteException;
	
	// Election
	public int createElection(Election election) throws RemoteException;
	public Election getElection(int id) throws RemoteException;
	public ArrayList<Election> listElections(String type, String subtype) throws RemoteException;
	
	// VotingList
	public int createVotingList(VotingList votingList) throws RemoteException;
	public void removeVotingList(int id) throws RemoteException;
	public ArrayList < VotingList > listVotingLists(int electionID) throws RemoteException;
	
	// VotingListMember
	public int addCandidate(int votingListID, int personCC) throws RemoteException;
	public void removeCandidate(int votingListID, int personCC) throws RemoteException;
	public ArrayList < Person > listCandidates(int votingListID) throws RemoteException;
	
	// VotingTable
	public int createVotingTable(VotingTable votingTable) throws RemoteException;
	public void removeVotingTable(int id) throws RemoteException;
	public ArrayList < VotingTable > listVotingTables(int electionID) throws RemoteException;
	public ArrayList<VotingTable> getVotingTables(String departmentName, int cc) throws RemoteException;
	
	// VotingLog
	public boolean sendLog(VotingLog log) throws RemoteException;
	
	// Vote
	public void sendVote(int electionID, String votingList) throws RemoteException;
	
	// Credential
	public Credential getCredentials(int cc) throws RemoteException;
	
	// Result
	public ArrayList<Result> getResults(int electionID) throws RemoteException;
}
