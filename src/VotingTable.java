

import java.io.Serializable;


public class VotingTable implements Serializable {
	public int id;
	public int electionID;
	public String departmentName;

	public static final long serialVersionUID = 8381774809865881000L;

	public VotingTable(int id, int electionID, String departmentName) {
		this.id = id;
		this.electionID = electionID;
		this.departmentName = departmentName;
	}
	
	public VotingTable(int electionID, String departmentName) {
		this.electionID = electionID;
		this.departmentName = departmentName;
	}
}
