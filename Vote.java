class Vote {
	Election election;
	VotingTable votingTable;
	int terminalID;
	int numberVote;

	public Vote(Election election, VotingTable votingTable, int terminalID, int numberVote) {
		this.election = election;
		this.votingTable = votingTable;
		this.terminalID = terminalID;
		this.numberVote = numberVote;
	}
}
