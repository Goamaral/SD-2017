import java.io.Serializable;

public class Faculty implements Serializable {
	public String name;
	
	public static final long serialVersionUID = -3974198952227657032L;

	public Faculty(String name) {
	  this.name = name;
	}
}
