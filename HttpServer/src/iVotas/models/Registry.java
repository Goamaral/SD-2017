package iVotas.models;

import Core.*;
import com.opensymphony.xwork2.ActionContext;
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

        ArrayList<String> facultiesNames = new ArrayList<>();

        for(Core.Faculty faculty : faculties) {
            facultiesNames.add(faculty.name);
        }

        return facultiesNames;
    }

    public ArrayList<String> getDepartments(String facultyName) throws RemoteException {
        ArrayList<Department> departments = this.registry.listDepartments(facultyName);

        ArrayList<String> departmentsNames = new ArrayList<>();

        for(Department department : departments) {
            departmentsNames.add(department.name);
        }

        return departmentsNames;
    }

    public void fetchElections() throws RemoteException {
        String type = (String) this.get("ElectionType");
        String subtype = (String) this.get("ElectionSubtype");

        ArrayList<Election> elections = this.registry.listElections(type, subtype);

        ArrayList<String> electionsNames = new ArrayList<>();
        ArrayList<Integer> electionsIDs = new ArrayList<>();

        for (Election election : elections) {
            electionsNames.add(election.name);
            electionsIDs.add(election.id);
        }

        this.save("ElectionsNamesList", electionsNames);
        this.save("ElectionsIDsList", electionsIDs);

        this.save("ElectionName", electionsNames.get(0));
        this.save("ElectionID", electionsIDs.get(0));
    }

    public ArrayList<String> getElections() throws RemoteException {
        return (ArrayList<String>) this.get("ElectionsNamesList");
    }

    public ArrayList<Integer> getElectionsIDs() throws RemoteException {
        return (ArrayList<Integer>) this.get("ElectionsIDsList");
    }

    public void save(String key, Object value) {
        this.session.put(key, value);
    }

    public Object get(String key) {
        return this.session.get(key);
    }
}
