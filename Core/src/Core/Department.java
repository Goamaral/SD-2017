package Core;

import java.io.Serializable;

public class Department implements Serializable {
	public String name;
	public String facultyName;

	public static final long serialVersionUID = -8451177630646018087L;

	public Department(String name, String facultyName) {
		this.name = name;
		this.facultyName = facultyName;
	}
}
