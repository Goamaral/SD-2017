package iVotas.actions;

import Core.Faculty;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class FacultyAction extends ActionSupport {
    public String getEditFacultyForm() {
        return SUCCESS;
    }

    public String getRemoveFacultyForm() {
        return SUCCESS;
    }

    public String editFaculty() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.updateFaculty((Faculty) registry.get("Faculty"), (Faculty) registry.get("NewFaculty"));

        return SUCCESS;
    }

    public String createFaculty() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        Faculty faculty = (Faculty) registry.get("Faculty");

        registry.registry.createFaculty(faculty.name);

        return SUCCESS;
    }

    public String removeFaculty() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        Faculty faculty = (Faculty) registry.get("Faculty");

        registry.registry.removeFaculty(faculty.name);

        return SUCCESS;
    }

    public ArrayList<String> getFaculties() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        ArrayList<String> facultyNames = registry.getFaculties();

        registry.save("Faculty", new Faculty(facultyNames.get(0)));

        return facultyNames;
    }

    public Faculty getFaculty() throws NotBoundException, RemoteException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (Faculty) registry.get("Faculty");
    }

    public void setFaculty(Faculty faculty) throws NotBoundException, RemoteException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("Faculty", faculty);
    }

    public Faculty getNewFaculty() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (Faculty) registry.get("NewFaculty");
    }

    public void setNewFaculty(Faculty newFaculty) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("NewFaculty", newFaculty);
    }
}
