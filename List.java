import java.io.Serializable;

class List implements Serializable {
  String name;
  Election election;

	public static final long serialVersionUID = 780428918543205496L;

  public List(Election election, String name) {
    this.election = election;
    this.name = name;
  }
}
