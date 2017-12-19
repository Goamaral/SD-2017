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

	public Department() {};

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFacultyName() {
		return facultyName;
	}

	public void setFacultyName(String facultyName) {
		this.facultyName = facultyName;
	}
}
