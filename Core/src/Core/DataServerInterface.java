package Core;

import java.rmi.*;
import java.util.*;

public interface DataServerInterface extends Remote {
	// Core.DataServerInterface.Person
	int createPerson(Person person) throws RemoteException;
	ArrayList<Person> listStudentsFromDepartment(String departmentName) throws RemoteException;
	ArrayList<Person> listPeopleOfType(String type) throws RemoteException;
	
	// Core.Faculty
	String createFaculty(String name) throws RemoteException;
	void removeFaculty(String name) throws RemoteException;
	String updateFaculty(Faculty faculty, Faculty newFaculty) throws RemoteException;
	ArrayList<Faculty> listFaculties() throws RemoteException;
	
	// Core.Department
	String createDepartment(Department department) throws RemoteException;
	void removeDepartment(String name) throws RemoteException;
	void updateDepartment(Department department, Department newDepartment) throws RemoteException;
	ArrayList<Department> listDepartments(String facultyName) throws RemoteException;
	
	// Core.Election
	int createElection(Election election) throws RemoteException;
	Election getElection(int id) throws RemoteException;
	ArrayList<Election> listElections(String type, String subtype) throws RemoteException;
	
	// Core.VotingList
	int createVotingList(VotingList votingList) throws RemoteException;
	void removeVotingList(int id) throws RemoteException;
	ArrayList < VotingList > listVotingLists(int electionID) throws RemoteException;
	
	// VotingListMember
	int addCandidate(int votingListID, int personCC) throws RemoteException;
	void removeCandidate(int votingListID, int personCC) throws RemoteException;
	ArrayList < Person > listCandidates(int votingListID) throws RemoteException;
	
	// Core.VotingTable
	int createVotingTable(VotingTable votingTable) throws RemoteException;
	void removeVotingTable(int id) throws RemoteException;
	ArrayList < VotingTable > listVotingTables(int electionID) throws RemoteException;
	ArrayList<VotingTable> getVotingTables(String departmentName, int cc) throws RemoteException;
	
	// Core.VotingLog
	void sendLog(VotingLog log) throws RemoteException;
	
	// Vote
	void sendVote(int electionID, String votingList) throws RemoteException;
	
	// Core.Credential
	Credential getCredentials(int cc) throws RemoteException;
	
	// Core.Result
	ArrayList<Result> getResults(int electionID) throws RemoteException;
}
