

import java.io.Serializable;

public class List implements Serializable {
  public String name;
  public Election election;

	public static final long serialVersionUID = 780428918543205496L;

  public List(Election election, String name) {
    this.election = election;
    this.name = name;
  }
}
