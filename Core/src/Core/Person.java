package Core;

import java.io.Serializable;

public class Person implements Serializable {
	public int cc;
	public String type;
	public String name;
	public String password;
	public String address;
	public int number;
	public int phone;
	public String ccExpire;
	public String departmentName;

	public static final long serialVersionUID = -7092225612111117624L;

	public Person(
			int cc, String type, String name, String password, String address,
			int number, int phone, String ccExpire, String departmentName
	) {
	  this.cc = cc;
	  this.type = type;
	  this.name = name;
	  this.password = password;
	  this.address = address;
	  this.number = number;
	  this.phone = phone;
	  this.ccExpire = ccExpire;
	  this.departmentName = departmentName;
	}
}
