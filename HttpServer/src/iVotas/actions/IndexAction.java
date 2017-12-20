package iVotas.actions;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import Core.*;
import iVotas.models.Registry;

public class IndexAction extends ActionSupport {
    public String execute() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        if (registry.isLoggedIn()) {
            return "platform";
        } else return SUCCESS;
    }

    public String login() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        String username = (String) registry.get("Username");
        String password = (String) registry.get("Password");

        if (username.equals("admin") && password.equals("admin")) {
            registry.loginUser("admin");
            return "console";
        }

        int cc = (Integer) registry.get("CC");

        Credential credential = registry.registry.getCredentials(cc);

        if (credential == null) return "home";

        if (credential.username.equals(username) && credential.password.equals(password)) {
            registry.loginUser(username);
            return "platform";
        }

        return "home";
    }

    public String logout() throws RemoteException, NotBoundException{
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.logoutUser();

        return SUCCESS;
    }

    public void setUsername(String username) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("Username", username);
    }

    public void setPassword(String password) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        registry.save("Password", password);
    }

    public void setCc(String cc) throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        if (!cc.equals("")) {
            registry.save("CC", Integer.parseInt(cc));
        } else registry.save("CC", -1);
    }
}