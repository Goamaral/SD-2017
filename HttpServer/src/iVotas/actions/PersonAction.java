package iVotas.actions;

import Core.Person;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class PersonAction extends ActionSupport {
    public String getRegisterMemberForm() {
        return SUCCESS;
    }

    public String listDepartments() {
        return SUCCESS;
    }

    public String registerMember() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.createPerson((Person) registry.get("Person"));

        return SUCCESS;
    }

    public ArrayList<String> getFaculties() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        ArrayList<String> facultyNames = registry.getFaculties();

        return facultyNames;
    }

    public ArrayList<String> getDepartments() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getDepartments((String) registry.get("FacultyName"));
    }

    public void setPerson(Person person) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("Person", person);
    }

    public Person getPerson() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (Person) registry.get("Person");
    }

    public String getPersonType() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (String) registry.get("PersonType");
    }

    public void setPersonType(String personType) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("PersonType", personType);
    }

    public void setFacultyName(String facultyName) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("FacultyName", facultyName);
    }
}
