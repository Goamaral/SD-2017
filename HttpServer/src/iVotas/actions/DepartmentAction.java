package iVotas.actions;

import Core.Department;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class DepartmentAction extends ActionSupport {
    private Department department;
    private Department newDepartment;
    private String facultyName;

    public String createDepartment() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.createDepartment(this.department);

        return SUCCESS;
    }

    public String getEditDepartmentForm() { return SUCCESS; }

    public String editDepartment() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.updateDepartment(this.department, this.newDepartment);

        return SUCCESS;
    }

    public String getRemoveDepartmentForm() { return SUCCESS; }

    public String removeDepartment() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.removeDepartment(this.department.name);

        return SUCCESS;
    }

    public String getCreateDepartmentForm() { return SUCCESS; }

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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Department getNewDepartment() {
        return newDepartment;
    }

    public void setNewDepartment(Department newDepartment) {
        this.newDepartment = newDepartment;
    }
}
