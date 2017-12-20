package iVotas.actions;

import Core.Election;
import Core.Faculty;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class ElectionAction extends ActionSupport {
    public String getElectionSubTypeForm() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (String)registry.get("ElectionType");
    }

    public String getCreateElectionForm() { return SUCCESS; }

    public String createElection() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.createElection((Election)registry.get("Election"));

        return SUCCESS;
    }

    public String getMainMenu() { return SUCCESS; }



    public ArrayList<String> getFaculties() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        ArrayList<String> facultyNames = registry.getFaculties();

        registry.save("FacultyName", facultyNames.get(0));

        return facultyNames;
    }

    public ArrayList<String> getDepartments() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getDepartments((String)registry.get("FacultyName"));
    }

    public String getElectionType() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (String)registry.get("ElectionType");
    }

    public void setElectionType(String electionType) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("ElectionType", electionType);
    }

    public String getElectionSubtype() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (String)registry.get("ElectionSubtype");
    }

    public void setElectionSubtype(String electionSubtype) throws NotBoundException, RemoteException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("ElectionSubtype", electionSubtype);
    }

    public String getFacultyName() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (String)registry.get("FacultyName");
    }

    public void setFacultyName(String facultyName) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("FacultyName", facultyName);
    }

    public Election getElection() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (Election)registry.get("Election");
    }

    public void setElection(Election election) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("Election", election);
    }
}
