package iVotas.models;

import Core.*;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.struts2.dispatcher.SessionMap;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Map;


public class Registry {
    public DataServerInterface registry;
    private SessionMap<String, Object> session;

    public Registry(Map<String, Object> session) throws RemoteException, NotBoundException{
        this.session = (SessionMap<String, Object>) session;

        if(!this.session.containsKey("Registry")) {
            this.registry = (DataServerInterface) LocateRegistry.getRegistry(7000).lookup("iVotas");
        } else {
           this.registry = (DataServerInterface) this.session.get("Registry");
        }
    }

    public Object getUserLoggedIn() {
        return this.session.get("user");
    }

    public boolean isLoggedIn() {
        return this.session.get("user") != null;
    }

    public boolean isLoggedAsAdmin() {
        return this.session.get("user") == "admin";
    }

    public void loginUser(String user) {
        this.session.put("user", user);
    }

    public void logoutUser() {
        this.session.put("user", null);
    }

    public ArrayList<String> getFaculties() throws RemoteException {
        ArrayList<Core.Faculty> faculties = this.registry.listFaculties();
        ArrayList<String> facultyNames = new ArrayList<>();

        if (faculties.size() == 0) return facultyNames;

        for(Core.Faculty faculty : faculties) {
            facultyNames.add(faculty.name);
        }

        this.save("FacultyName", facultyNames.get(0));

        return facultyNames;
    }

    public ArrayList<String> getDepartments(String facultyName) throws RemoteException {
        ArrayList<Department> departments = this.registry.listDepartments(facultyName);
        ArrayList<String> departmentsNames = new ArrayList<>();

        if (departments.size() == 0) return departmentsNames;

        for(Department department : departments) {
            departmentsNames.add(department.name);
        }

        this.save("DepartmentName", departmentsNames.get(0));

        return departmentsNames;
    }

    public ArrayList<String> getElections() throws RemoteException {
        String type = (String) this.get("ElectionType");
        String subtype = (String) this.get("ElectionSubtype");

        ArrayList<Election> elections = this.registry.listElections(type, subtype);
        ArrayList<String> electionNames = new ArrayList<>();

        if (elections.size() == 0) return electionNames;

        for (Election election : elections) {
            electionNames.add(election.name);
        }

        this.save("ElectionName", electionNames.get(0));

        return electionNames;
    }

    public ArrayList<Integer> getElectionsIDs() throws RemoteException {
        String type = (String) this.get("ElectionType");
        String subtype = (String) this.get("ElectionSubtype");

        ArrayList<Election> elections = this.registry.listElections(type, subtype);
        ArrayList<Integer> electionsIDs = new ArrayList<>();

        if (elections.size() == 0) return electionsIDs;

        for (Election election : elections) {
            electionsIDs.add(election.id);
        }

        this.save("ElectionID", electionsIDs.get(0));

        return electionsIDs;
    }

    public ArrayList<String> getVotingLists() throws RemoteException {
        int electionID = (Integer) this.get("ElectionID");
        ArrayList<VotingList> votingLists = this.registry.listVotingLists(electionID);
        ArrayList<String> votingListsNames = new ArrayList<>();

        if (votingLists.size() == 0) return votingListsNames;

        for (VotingList votingList : votingLists) {
            votingListsNames.add(votingList.name);
        }

        this.save("VotingListName", votingListsNames.get(0));

        return votingListsNames;
    }

    public ArrayList<Integer> getVotingListsIDs() throws RemoteException {
        int electionID = (Integer) this.get("ElectionID");
        ArrayList<VotingList> votingLists = this.registry.listVotingLists(electionID);
        ArrayList<Integer> votingListsIDs = new ArrayList<>();

        if (votingLists.size() == 0) return  votingListsIDs;

        for (VotingList votingList : votingLists) {
            votingListsIDs.add(votingList.id);
        }

        this.save("VotingListID", votingListsIDs.get(0));

        return votingListsIDs;
    }

    public void save(String key, Object value) {
        this.session.put(key, value);
    }

    public Object get(String key) {
        return this.session.get(key);
    }
}
