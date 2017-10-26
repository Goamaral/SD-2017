import java.io.Serializable;
import java.util.Date;
import java.text.*;

class Election implements Serializable {
  String name;
  String description;
  String type;
  String subtype;
  Date start;
  Date end;
  Department department;

	public static final long serialVersionUID = 2848095557101780926L;

  public Election(String name, Department department, Date start, Date end, String type, String subtype) {
    this.name = name;
    this.description = description;
    this.department = department;
    this.type = type;
    this.subtype = subtype;
    this.start = start;
    this.end = end;
  }

}
