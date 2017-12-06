import java.io.Serializable;

public class Credential implements Serializable {
  public static final long serialVersionUID = -3288171420864809141L;
  public String username;
  public String password;

  public Credential(int username, String password) {
    this.username = Integer.toString(username);
    this.password = password;
  }
}
