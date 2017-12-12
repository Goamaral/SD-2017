
import java.rmi.*;
import java.util.*;


public interface DataServerInterface extends Remote {
	// Person
	int createPerson(Person person) throws RemoteException;
	ArrayList<Person> listStudentsFromDepartment(String departmentName) throws RemoteException;
	ArrayList<Person> listPeopleOfType(String type) throws RemoteException;
	
	// Faculty
	String createFaculty(String name) throws RemoteException;
	void removeFaculty(String name) throws RemoteException;
	String updateFaculty(Faculty faculty, Faculty newFaculty) throws RemoteException;
	ArrayList<Faculty> listFaculties() throws RemoteException;
	
	// Department
	String createDepartment(Department department) throws RemoteException;
	void removeDepartment(String name) throws RemoteException;
	void updateDepartment(Department department, Department newDepartment) throws RemoteException;
	ArrayList<Department> listDepartments(String facultyName) throws RemoteException;
	
	// Election
	int createElection(Election election) throws RemoteException;
	Election getElection(int id) throws RemoteException;
	ArrayList<Election> listElections(String type, String subtype) throws RemoteException;
	
	// VotingList
	int createVotingList(VotingList votingList) throws RemoteException;
	void removeVotingList(int id) throws RemoteException;
	ArrayList < VotingList > listVotingLists(int electionID) throws RemoteException;
	
	// VotingListMember
	int addCandidate(int votingListID, int personCC) throws RemoteException;
	void removeCandidate(int votingListID, int personCC) throws RemoteException;
	ArrayList < Person > listCandidates(int votingListID) throws RemoteException;
	
	// VotingTable
	int createVotingTable(VotingTable votingTable) throws RemoteException;
	void removeVotingTable(int id) throws RemoteException;
	ArrayList < VotingTable > listVotingTables(int electionID) throws RemoteException;
	ArrayList<VotingTable> getVotingTables(String departmentName, int cc) throws RemoteException;
	
	// VotingLog
	void sendLog(VotingLog log) throws RemoteException;
	
	// Vote
	void sendVote(int electionID, String votingList) throws RemoteException;
	
	// Credential
	Credential getCredentials(int cc) throws RemoteException;
	
	// Result
	ArrayList<Result> getResults(int electionID) throws RemoteException;
}
