import java.io.Serializable;
import java.rmi.*;

class Faculty extends Zone implements Serializable {
  public Faculty(String name) {
    super(name);
  }
}
