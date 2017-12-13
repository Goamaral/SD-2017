package iVotas.actions;

import com.opensymphony.xwork2.ActionSupport;

public class Index extends ActionSupport {
    int cc;
    int username;
    String password;

    // Controllers
    public String index() {
        return SUCCESS;
    }

    // Login
    public String login() {
        return SUCCESS;
    }

    public int getUsername() {
        return username;
    }

    // Getters and setters
    public void setUsername(int username) {
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

    public void setCc(int cc) {
        this.cc = cc;
    }
}
