package iVotas.actions;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Platform extends ActionSupport {
    private Registry registry;
    public boolean loggedAsAdmin;

    public String index() throws RemoteException, NotBoundException {
        this.registry = new Registry(ActionContext.getContext().getSession());

        if (this.registry.isLoggedIn()) {
            this.loggedAsAdmin = this.registry.isLoggedAsAdmin();
            return SUCCESS;
        } else {
            return "home";
        }
    }
}
