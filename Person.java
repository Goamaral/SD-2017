import java.util.Date;

class Person {
  String tipo;
  String name;
  int id;
  String password;
  int department;
  int phone;
  String morada;
  int cc;
  Date ccExpire;

  public Person(
    String tipo, String name, int id, String password, int department,
    int phone, String morada, int cc, Date ccExpire
  ) {
    this.tipo = tipo;
    this.name = name;
    this.id = id;
    this.password = password;
    this.department = department;
    this.phone = phone;
    this.morada = morada;
    this.cc = cc;
    this.ccExpire = ccExpire;
  }
}
