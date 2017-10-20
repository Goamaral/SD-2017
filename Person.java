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
    int phone, String address, int cc, String ccExpire
  ) {
    this.type = type;
    this.name = name;
    this.id = id;
    this.password = password;
    this.department = department;
    this.phone = phone;
    this.address = address;
    this.cc = cc;
    this.list = null;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      this.ccExpire = dateFormat.parse(ccExpire);
    } catch(ParseException e) {
      System.out.println("BAD DATE FORMAT: ccExpire is not following the \"yyyy-MM-dd\" date format" );
    }
  }

  public Person(
    String type, String name, int id, String password, Department department,
    int phone, String address, int cc, String ccExpire, List list
  ) {
    this.type = type;
    this.name = name;
    this.id = id;
    this.password = password;
    this.department = department;
    this.phone = phone;
    this.address = address;
    this.cc = cc;
    this.list = list;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      this.ccExpire = dateFormat.parse(ccExpire);
    } catch(ParseException e) {
      System.out.println("BAD DATE FORMAT: ccExpire is not following the \"yyyy-MM-dd\" date format" );
    }
  }
}
