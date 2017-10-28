import java.util.*;

class Vote {
	Election election;
	int terminalID;
	int voteNumber;
	String list;
	Date date;

	public Vote(Election election, int terminalID, String list, Date date) {
		this.election = election;
		this.terminalID = terminalID;
		this.list = list;
		this.voteNumber = -1;
		this.date = date;
	}
}
