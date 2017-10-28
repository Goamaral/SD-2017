import java.io.Serializable;
import java.rmi.*;

class Faculty extends Zone implements Serializable {
	public static final long serialVersionUID = -3974198952227657032L;

  public Faculty(String name) {
    super(name);
  }
}
