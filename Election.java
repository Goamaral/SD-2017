import java.io.Serializable;
import java.util.Date;
import java.text.*;

class Election implements Serializable {
  String name;
  String description;
  String type;
  Date start;
  Date end;
  Department department;

  public Election(String name, Department department, Date start, Date end, String type) {
    this.name = name;
    this.description = description;
    this.department = department;
    this.type = type;
    this.start = start;
    this.end = end;
  }

}
