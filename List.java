import java.io.Serializable;

class List implements Serializable {
  String name;
  Election election;

  public List(Election election, String name) {
    this.election = election;
    this.name = name;
  }
}
