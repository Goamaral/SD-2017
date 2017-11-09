

import java.util.*;

public class Vote {
	public Election election;
	public int terminalID;
	public int voteNumber;
	public String list;
	public Date date;

	public Vote(Election election, int terminalID, String list, Date date) {
		this.election = election;
		this.terminalID = terminalID;
		this.list = list;
		this.voteNumber = -1;
		this.date = date;
	}
}
