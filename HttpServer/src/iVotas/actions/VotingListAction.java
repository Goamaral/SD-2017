package iVotas.actions;

import Core.Election;
import Core.VotingList;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class VotingListAction extends ActionSupport {
    public String getCreateVotingListForm() { return SUCCESS; }

    public String createVotingList() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.createVotingList((VotingList) registry.get("VotingList"));

        return SUCCESS;
    }

    public String getRemoveVotingListForm() { return SUCCESS; }

    public String listVotingLists() { return SUCCESS; }

    public String removeVotingList() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        int votingListID = (Integer) registry.get("VotingListID");

        registry.registry.removeVotingList(votingListID);

        return SUCCESS;
    }


    public ArrayList<String> getElections() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getElections();
    }

    public ArrayList<Integer> getElectionsIDs() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getElectionsIDs();
    }

    public void setVotingList(VotingList votingList) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("VotingList", votingList);
    }

    public void setElectionID(String electionID) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("ElectionID", Integer.parseInt(electionID));
    }

    public ArrayList<String> getVotingLists() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getVotingLists();
    }

    public ArrayList<Integer> getVotingListsIDs() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return registry.getVotingListsIDs();
    }

    public void setVotingListID(int votingListID) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("VotingListID", votingListID);
    }
}
