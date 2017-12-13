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
}
