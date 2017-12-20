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
    private Boolean electionFetched = false;

    public String getCreateVotingListForm() { return SUCCESS; }

    public String createVotingList() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.registry.createVotingList((VotingList) registry.get("VotingList"));

        return SUCCESS;
    }


    public ArrayList<String> getElections() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        if (!this.electionFetched) {
            registry.fetchElections();
            this.electionFetched = true;
        }

        return (ArrayList<String>) registry.get("ElectionsNamesList");
    }

    public ArrayList<Integer> getElectionsIDs() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        if (!this.electionFetched) {
            registry.fetchElections();
            this.electionFetched = true;
        }

        return (ArrayList<Integer>) registry.get("ElectionsIDsList");
    }

    public VotingList getVotingList() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        return (VotingList) registry.get("VotingList");
    }

    public void setVotingList(VotingList votingList) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("VotingList", votingList);
    }
}
