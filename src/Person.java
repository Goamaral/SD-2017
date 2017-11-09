


import java.io.Serializable;
import java.util.Date;


public class Person implements Serializable {
	public String type;
	public String name;
	public int number;
	public String password;
	public Department department;
	public int phone;
	public String address;
	public int cc;
	public Date ccExpire;

	public static final long serialVersionUID = -7092225612111117624L;

	public Person(
			String type, String name, int number, String password, Department department,
			int phone, String address, int cc, Date ccExpire
	) {
		this.type = type;
		this.name = name;
		this.number = number;
		this.password = password;
	this.department = department;
	this.phone = phone;
	this.address = address;
	this.cc = cc;
	this.ccExpire = ccExpire;
	}
}
