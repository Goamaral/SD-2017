package Core;

import java.io.Serializable;

public class Person implements Serializable {
	public int cc;
	public String type;
	public String name;
	public String password;
	public String address;
	public int number;

	public String getCc() {
		return Integer.toString(this.cc);
	}

	public void setCc(String cc) {
		this.cc = Integer.parseInt(cc);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getNumber() {
		return Integer.toString(number);
	}

	public void setNumber(String number) {
		this.number = Integer.parseInt(number);
	}

	public int getPhone() {
		return phone;
	}

	public void setPhone(int phone) {
		this.phone = phone;
	}

	public String getCcExpire() {
		return ccExpire;
	}

	public void setCcExpire(String ccExpire) {
		this.ccExpire = ccExpire;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public int phone;
	public String ccExpire;
	public String departmentName;

	public static final long serialVersionUID = -7092225612111117624L;

	public Person() {

	}

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
