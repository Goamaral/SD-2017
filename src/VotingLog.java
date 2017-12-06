import java.util.*;


public class VotingLog {
	public Election election;
	public int cc;
	public int votingTableID;
	public Date date;

	public VotingLog(Election election, int cc, int votingTableID) {
		this.election = election;
		this.cc = cc;
		this.votingTableID = votingTableID;
	}
}
