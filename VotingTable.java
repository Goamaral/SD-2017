import java.io.Serializable;

class VotingTable implements Serializable {
  Zone zone;
  Election election;

  public VotingTable(Election election, Zone zone) {
    this.zone = zone;
    this.election = election;
  }
}
