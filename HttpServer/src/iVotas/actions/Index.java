package iVotas.actions;

import com.opensymphony.xwork2.ActionSupport;
import Core.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Index extends ActionSupport {
    int cc;
    String username;
    String password;

    // Controllers
    public String index() {
        return SUCCESS;
    }

    // Login
    public String login() throws Exception{
        DataServerInterface registry;
        System.out.println(cc + " " + username + " " + password);

        registry = (DataServerInterface) Naming.lookup("iVotas");
        Credential credential = registry.getCredentials(cc);
        if (credential.username == username && credential.password == password) {
            return SUCCESS;
        }

        return ERROR;
    }

    public String getUsername() {
        return username;
    }

    // Getters and setters
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = Integer.parseInt(cc);
    }
}
