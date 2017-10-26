import java.io.Serializable;

class VotingTable implements Serializable {
  Department department;
  Election election;

	public static final long serialVersionUID = 8381774809865881000L;

  public VotingTable(Election election, Department department) {
    this.department = department;
    this.election = election;
  }
}
