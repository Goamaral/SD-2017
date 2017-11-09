import java.io.Serializable;

public class Zone implements Serializable {
	public String name;

	public static final long serialVersionUID = -1549622902380917128L;

	public Zone(String name) {
		this.name = name;
	}

	public String toString() {
		return new String(this.getClass().getName() + this.name);
	}
}