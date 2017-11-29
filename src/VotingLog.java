import java.util.*;


public class VotingLog {
	public int electionID;
	public int cc;
	public int votingTableID;
	public Date date;

	public VotingLog(int electionID, int cc, int votingTableID, Date date) {
		this.electionID = electionID;
		this.cc = cc;
		this.votingTableID = votingTableID;
		this.date = date;
	}
}
