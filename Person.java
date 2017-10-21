import java.util.Date;
import java.text.*;

class Person {
  String type;
  String name;
  int id;
  String password;
  Department department;
  int phone;
  String address;
  int cc;
  Date ccExpire;
  List list;

  public Person(
    String type, String name, int id, String password, Department department,
    int phone, String address, int cc, Date ccExpire
  ) {
    this.type = type;
    this.name = name;
    this.id = id;
    this.password = password;
    this.department = department;
    this.phone = phone;
    this.address = address;
    this.cc = cc;
    this.ccExpire = ccExpire;
    this.list = null;
  }

  public Person(
    String type, String name, int id, String password, Department department,
    int phone, String address, int cc, Date ccExpire, List list
  ) {
    this.type = type;
    this.name = name;
    this.id = id;
    this.password = password;
    this.department = department;
    this.phone = phone;
    this.address = address;
    this.cc = cc;
    this.ccExpire = ccExpire;
    this.list = list;
  }
}
