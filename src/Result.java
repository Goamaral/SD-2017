import java.io.Serializable;

public class Result implements Serializable {

  public static final long serialVersionUID = -3673921374947814917L;
  public String votingListName;
  public int votes;
  
  public Result (String votingListName, int votes) {
	this.votingListName = votingListName;
	this.votes = votes;
  }
}
