

import java.io.Serializable;


public class VotingTable implements Serializable {
	public Department department;
	public Election election;

	public static final long serialVersionUID = 8381774809865881000L;

	public VotingTable(Election election, Department department) {
		this.department = department;
		this.election = election;
	}
}
