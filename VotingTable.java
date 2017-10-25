import java.io.Serializable;

class VotingTable implements Serializable {
  Department department;
  Election election;

  public VotingTable(Election election, Department department) {
    this.department = department;
    this.election = election;
  }
}
