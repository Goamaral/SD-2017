package iVotas.actions;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class CandidateAction extends ActionSupport {
    public String getAddCandidateForm() { return SUCCESS; }

    public String getRemoveCandidateForm() { return SUCCESS; }

    public String listCandidates() { return SUCCESS; }

    public ArrayList<Integer> getVotingListsIDs() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getVotingListsIDs();
    }

    public void setVotingListID(String votingListID) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("VotingListID", Integer.parseInt(votingListID));
    }

    public ArrayList<Integer> getElectionsIDs() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getElectionsIDs();
    }

    public ArrayList<String> getElections() throws NotBoundException, RemoteException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getElections();
    }

    public ArrayList<String> getVotingLists() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getVotingLists();
    }

    public void setPersonCC(String personCC) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("PersonCC", personCC);
    }

    public ArrayList<String> getPeople() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getPeople();
    }

    public ArrayList<Integer> getPeopleCCs() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getPeopleCCs();
    }
}
