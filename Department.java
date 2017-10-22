import java.io.Serializable;

class Department extends Zone implements Serializable {
  Faculty faculty;

  public Department(Faculty faculty, String name) {
    super(name);
    this.faculty = faculty;
  }
}
