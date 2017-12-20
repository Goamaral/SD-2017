package iVotas.actions;

import Core.Department;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.Normalizer;
import java.util.ArrayList;

public class DepartmentAction extends ActionSupport {
    public String createDepartment() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.createDepartment((Department)registry.get("Department"));

        return SUCCESS;
    }

    public String getEditDepartmentForm() { return SUCCESS; }

    public String editDepartment() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.updateDepartment((Department)registry.get("Department"), (Department)registry.get("NewDepartment"));

        return SUCCESS;
    }

    public String getRemoveDepartmentForm() { return SUCCESS; }

    public String removeDepartment() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        Department department = (Department)registry.get("Department");

        registry.registry.removeDepartment(department.name);

        return SUCCESS;
    }

    public String getCreateDepartmentForm() { return SUCCESS; }

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

    public Department getDepartment() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (Department)registry.get("Department");
    }

    public void setDepartment(Department department) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("Department", department);
    }

    public Department getNewDepartment() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (Department)registry.get("NewDepartment");
    }

    public void setNewDepartment(Department newDepartment) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("NewDepartment", newDepartment);
    }
}
