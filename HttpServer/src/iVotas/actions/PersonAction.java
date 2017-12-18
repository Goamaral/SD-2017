package iVotas.actions;

import Core.Person;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class PersonAction extends ActionSupport {
    private Person person;
    private String personType;
    private String facultyName;

    public String getRegisterMemberForm() {
        return SUCCESS;
    }

    public String listDepartments() {
        return SUCCESS;
    }

    public String registerMember() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.createPerson(this.person);

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

    public void setPerson(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return this.person;
    }

    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }
}
