package iVotas.actions;

import Core.Faculty;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class FacultyAction extends ActionSupport {
    private String facultyName;
    private String newFacultyName;

    public String getEditFacultyForm() {
        return SUCCESS;
    }

    public String getRemoveFacultyForm() {
        return SUCCESS;
    }

    public String editFaculty() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.updateFaculty(new Faculty(this.facultyName), new Faculty(this.newFacultyName));

        return SUCCESS;
    }

    public String createFaculty() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.createFaculty(this.facultyName);

        return SUCCESS;
    }

    public String removeFaculty() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.removeFaculty(this.facultyName);

        return SUCCESS;
    }

    public ArrayList<String> getFaculties() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        ArrayList<Faculty> faculties = registry.registry.listFaculties();

        ArrayList<String> facultiesNames = new ArrayList<>();

        for (Faculty faculty : faculties) {
            facultiesNames.add(faculty.name);
        }

        this.facultyName = facultiesNames.get(0);

        return facultiesNames;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getNewFacultyName() {
        return newFacultyName;
    }

    public void setNewFacultyName(String newFacultyName) {
        this.newFacultyName = newFacultyName;
    }
}
