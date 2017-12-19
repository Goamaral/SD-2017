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
    private String electionType;
    private String electionSubtype;
    private String facultyName;
    private Election election;

    public String getElectionSubTypeForm() { return this.electionType; }

    public String getCreateElectionForm() { return SUCCESS; }

    public String createElection() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.createElection(this.election);

        return SUCCESS;
    }

    public ArrayList<String> getFaculties() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        ArrayList<String> facultyNames = registry.getFaculties();

        this.facultyName = facultyNames.get(0);

        return facultyNames;
    }

    public ArrayList<String> getDepartments() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getDepartments(this.facultyName);
    }

    public String getElectionType() {
        return electionType;
    }

    public void setElectionType(String electionType) {
        this.electionType = electionType;
    }

    public String getElectionSubtype() {
        return electionSubtype;
    }

    public void setElectionSubtype(String electionSubtype) {
        this.electionSubtype = electionSubtype;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public Election getElection() {
        return election;
    }

    public void setElection(Election election) {
        this.election = election;
    }
}
