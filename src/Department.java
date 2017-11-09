
import java.io.Serializable;




public class Department extends Zone implements Serializable {
	public Faculty faculty;

	public static final long serialVersionUID = -8451177630646018087L;

	public Department(Faculty faculty, String name) {
		super(name);
		this.faculty = faculty;
	}
}
