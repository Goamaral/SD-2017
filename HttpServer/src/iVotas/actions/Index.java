package iVotas.actions;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import Core.*;
import iVotas.models.Registry;

public class Index extends ActionSupport {
    private int cc;
    private String username;
    private String password;

    public String index() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        if (registry.isLoggedIn()) {
            return "platform";
        } else return SUCCESS;
    }

    public String login() throws RemoteException, NotBoundException {
        Registry registry = new Registry(ActionContext.getContext().getSession());

        if (this.username.equals("admin") && this.password.equals("admin")) {
            registry.loginUser("admin");
            return "console";
        }

        Credential credential = registry.registry.getCredentials(this.cc);

        if (credential == null) return "home";

        if (credential.username.equals(this.username) && credential.password.equals(this.password)) {
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCc(String cc) {
        if (cc.equals("")) {
            this.cc = -1;
        } else this.cc = Integer.parseInt(cc);
    }
}