import java.util.Date;
import java.text.*;

class Person {
  String tipo;
  String name;
  int id;
  String password;
  Department department;
  int phone;
  String morada;
  int cc;
  Date ccExpire;

  public Person(
    String tipo, String name, int id, String password, Department department,
    int phone, String morada, int cc, String ccExpire
  ) {
    this.tipo = tipo;
    this.name = name;
    this.id = id;
    this.password = password;
    this.department = department;
    this.phone = phone;
    this.morada = morada;
    this.cc = cc;

    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      this.ccExpire = dateFormat.parse(ccExpire);
    } catch(ParseException e) {
      System.out.println("BAD DATE FORMAT: ccExpire is not following the \"yyyy-MM-dd\" date format" );
    }
  }
}
