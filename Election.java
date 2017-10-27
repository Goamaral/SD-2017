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

  public Election(String name, Date start, Date end, String type, String subtype) {
    this.name = name;
    this.description = description;
    this.type = type;
    this.subtype = subtype;
    this.start = start;
    this.end = end;
  }

}
