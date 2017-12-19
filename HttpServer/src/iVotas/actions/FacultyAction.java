package iVotas.actions;

import Core.Faculty;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class FacultyAction extends ActionSupport {
    private Faculty faculty;
    private Faculty newFaculty;

    public String getEditFacultyForm() {
        return SUCCESS;
    }

    public String getRemoveFacultyForm() {
        return SUCCESS;
    }

    public String editFaculty() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.updateFaculty(this.faculty, this.newFaculty);

        return SUCCESS;
    }

    public String createFaculty() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.createFaculty(this.faculty.name);

        return SUCCESS;
    }

    public String removeFaculty() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.removeFaculty(this.faculty.name);

        return SUCCESS;
    }

    public ArrayList<String> getFaculties() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        ArrayList<String> facultyNames = registry.getFaculties();

        this.faculty = new Faculty(facultyNames.get(0));

        return facultyNames;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public Faculty getNewFaculty() {
        return newFaculty;
    }

    public void setNewFaculty(Faculty newFaculty) {
        this.newFaculty = newFaculty;
    }
}
