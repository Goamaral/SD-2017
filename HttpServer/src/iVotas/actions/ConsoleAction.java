package iVotas.actions;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import iVotas.models.Registry;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ConsoleAction extends ActionSupport {
    public String execute() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        if (registry.isLoggedIn()) {
            if (registry.isLoggedAsAdmin()) return SUCCESS;
            return "platform";
        } else {
            return "home";
        }
    }
}
