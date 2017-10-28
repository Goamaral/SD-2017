import java.io.Serializable;

class Department extends Zone implements Serializable {
  Faculty faculty;

	public static final long serialVersionUID = -8451177630646018087L;

  public Department(Faculty faculty, String name) {
    super(name);
    this.faculty = faculty;
  }
}
