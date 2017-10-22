import java.io.Serializable;

class Zone implements Serializable {
  String name;

  public Zone(String name) {
    this.name = name;
  }

  public String toString() {
    return new String(this.getClass().getName() + this.name);
  }
}
