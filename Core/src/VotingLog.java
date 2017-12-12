import java.io.Serializable;
import java.util.*;

public class VotingLog implements Serializable {
	private static final long serialVersionUID = -7810083636809588197L;
	public Election election;
	public int cc;
	public int votingTableID;
	public Date date;

	VotingLog(Election election, int cc, int votingTableID) {
		this.election = election;
		this.cc = cc;
		this.votingTableID = votingTableID;
	}
}
