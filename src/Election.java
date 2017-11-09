
import java.io.Serializable;
import java.util.Date;

public class Election implements Serializable {
  public String name;
  public String description;
  public String type;
  public String subtype;
  public Date start;
  public Date end;

  public static final long serialVersionUID = 2848095557101780926L;

  public Election(String name, String description, Date start, Date end, String type, String subtype) {
    this.name = name;
    this.description = description;
    this.type = type;
    this.subtype = subtype;
    this.start = start;
    this.end = end;
  }

}
