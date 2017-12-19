package Core;

import java.io.Serializable;

public class Election implements Serializable {
  public int id;
  public String name;
  public String description;
  public String type;
  public String subtype;
  public String start;
  public String end;
  
  public static final long serialVersionUID = 2848095557101780926L;

  public Election() {}

  public Election(int id, String name, String description, String type, String subtype, String start, String end) {
    this.id = id;
	this.name = name;
    this.description = description;
    this.type = type;
    this.subtype = subtype;
    this.start = start;
    this.end = end;
  }

  public Election(String name, String description, String type, String subtype, String start, String end) {
	this.name = name;
    this.description = description;
    this.type = type;
    this.subtype = subtype;
    this.start = start;
    this.end = end;
  }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
