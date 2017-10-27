class Vote {
	VotingTable votingTable;
	int terminalID;
	int voteNumber;
	String list;

	public Vote(VotingTable votingTable, int terminalID, String list) {
		this.votingTable = votingTable;
		this.terminalID = terminalID;
		this.list = list;
		this.voteNumber = -1;
	}
}
