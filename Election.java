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

  public Election(String name, String start, Department department, String end, String type) {
    this.name = name;
    this.description = description;
    this.department = department;
    this.type = type;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    try {
      this.start = dateFormat.parse(start);
      this.end = dateFormat.parse(end);
    } catch(ParseException e) {
      System.out.println("BAD DATE FORMAT: ccExpire is not following the \"yyyy-MM-dd\" date format" );
    }
  }

}
