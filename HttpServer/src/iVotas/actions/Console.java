package iVotas.actions;

import Core.Department;
import Core.Faculty;
import Core.Person;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Console extends ActionSupport {
    private String personType;
    private String facultyName;
    private Person person;

    public String index() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        if (registry.isLoggedIn()) {
            if (registry.isLoggedAsAdmin()) return SUCCESS;
            return "platform";
        } else {
            return "home";
        }
    }

    public String getRegisterMemberForm() {
        return SUCCESS;
    }

    public String listDepartments() {
        return SUCCESS;
    }

    public String registerMember() {
        System.out.println(this.person);

        return SUCCESS;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Person getPerson(Person person) {
        return this.person;
    }

    public ArrayList<String> getFaculties() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        ArrayList<Faculty> faculties = registry.registry.listFaculties();

        ArrayList<String> facultiesNames = new ArrayList<>();

        for(Faculty faculty : faculties) {
            facultiesNames.add(faculty.name);
        }

        this.facultyName = faculties.get(0).name;

        return facultiesNames;
    }

    public ArrayList<String> getDepartments() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        ArrayList<Department> departments = registry.registry.listDepartments(this.facultyName);

        ArrayList<String> departmentsNames = new ArrayList<>();

        for(Department department : departments) {
            departmentsNames.add(department.name);
        }

        return departmentsNames;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getPersonType() {
        return this.personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }
}
