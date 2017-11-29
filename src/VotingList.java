

import java.io.Serializable;

public class VotingList implements Serializable {
  public int id;
  public int electionID;
  public String name;

  public static final long serialVersionUID = 780428918543205496L;

  public VotingList(int id, int electionID, String name) {
	this.id = id;
    this.electionID = electionID;
    this.name = name;
  }
  
  public VotingList(int electionID, String name) {
    this.electionID = electionID;
    this.name = name;
  }
}
