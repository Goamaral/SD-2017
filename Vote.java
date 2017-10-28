class Vote {
	Election election;
	int terminalID;
	int voteNumber;
	String list;

	public Vote(Election election, int terminalID, String list) {
		this.election = election;
		this.terminalID = terminalID;
		this.list = list;
		this.voteNumber = -1;
	}
}
