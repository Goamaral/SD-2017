/* 
   Fazer copy-paste deste ficheiro
   para um terminal de SQL e executar.
*/
 
drop table person;
drop table election;
drop table faculty;
drop table department;
drop table list;
 
 
/* Creates person table
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
 */
CREATE TABLE person
(type           VARCHAR(15),
 name           VARCHAR(255),
 id             NUMERIC(10),
 password       VARCHAR(30),
 department     VARCHAR(100),
 phone          NUMERIC(9),
 address        VARCHAR(255),
 cc             NUMERIC(8),
 ccExpire       DATE,
 listName       VARCHAR(15)
);
 
 
 
/* Creates election table
  String name;
  String description;
  String type;
  Date start;
  Date end;
  Department department;
  List[] lists;
 */
CREATE TABLE election
(type           VARCHAR(15),
 name           VARCHAR(255),
 id             NUMERIC(10),
 password       VARCHAR(30),
 department     VARCHAR(100),
 phone          NUMERIC(9),
 address        VARCHAR(255),
 cc             NUMERIC(8),
 ccExpire       DATE,
 listName       VARCHAR(15)
);


/* Creates person table
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
 */
CREATE TABLE person
(type           VARCHAR(15),
 name           VARCHAR(255),
 id             NUMERIC(10),
 password       VARCHAR(30),
 department     VARCHAR(100),
 phone          NUMERIC(9),
 address        VARCHAR(255),
 cc             NUMERIC(8),
 ccExpire       DATE,
 listName       VARCHAR(15)
);
 
 
COMMIT;
