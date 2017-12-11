CREATE USER bd IDENTIFIED BY bd;

GRANT "CONNECT" TO bd;
GRANT "RESOURCE" TO bd;
GRANT UNLIMITED TABLESPACE TO bd;
GRANT ALTER ANY CLUSTER TO bd;
GRANT ALTER SESSION TO bd;
GRANT CREATE PROCEDURE TO bd;
GRANT CREATE SEQUENCE TO bd;
GRANT CREATE TABLE TO bd;
GRANT CREATE TRIGGER TO bd;
GRANT CREATE VIEW TO bd;
ALTER USER bd DEFAULT ROLE ALL;

COMMIT;

DROP TABLE voting_list_member; 
DROP TABLE voting_log; 
DROP TABLE person; 
DROP TABLE voting_table; 
DROP TABLE department; 
DROP TABLE faculty; 
DROP TABLE voting_list; 
DROP TABLE election; 

DROP SEQUENCE election_seq;
DROP SEQUENCE voting_list_seq;
DROP SEQUENCE voting_table_seq;

CREATE TABLE election 
  ( 
     id          INT PRIMARY KEY, 
     name        VARCHAR(255) NOT NULL, 
     description VARCHAR(255) NOT NULL, 
     type        VARCHAR(255) NOT NULL, 
     subtype     VARCHAR(255) NOT NULL, 
     started_at  VARCHAR(255) NOT NULL, 
     ended_at    VARCHAR(255) NOT NULL, 
     votes       INT NOT NULL, 
     blank_votes  INT NOT NULL, 
     null_votes   INT NOT NULL 
  ); 

CREATE TABLE voting_list 
  ( 
     id          INT PRIMARY KEY, 
     election_id INT NOT NULL, 
     name        VARCHAR(255) NOT NULL, 
     votes       INT NOT NULL, 
     CONSTRAINT  fk_voting_list_election FOREIGN KEY (election_id) REFERENCES election(id) 
  ); 

CREATE TABLE faculty 
  ( 
     name VARCHAR(255) PRIMARY KEY 
  ); 

CREATE TABLE department 
  ( 
     name         VARCHAR(255) PRIMARY KEY, 
     faculty_name VARCHAR(255) NOT NULL, 
     CONSTRAINT fk_department_faculty FOREIGN KEY (faculty_name) REFERENCES faculty(name) 
  ); 

CREATE TABLE voting_table 
  ( 
     id              INT PRIMARY KEY, 
     status          INT NOT NULL,
     election_id     INT NOT NULL, 
     department_name VARCHAR(255) NOT NULL, 
     CONSTRAINT      fk_voting_table_election FOREIGN KEY (election_id) REFERENCES election(id), 
     CONSTRAINT      fk_voting_table_department FOREIGN KEY (department_name) REFERENCES department(name) 
  ); 

CREATE TABLE person 
  ( 
     cc              INT PRIMARY KEY, 
     type            VARCHAR(255) NOT NULL, 
     name            VARCHAR(255) NOT NULL, 
     password        VARCHAR(255) NOT NULL, 
     address         VARCHAR(255) NOT NULL, 
     num             INT NOT NULL, 
     phone           INT NOT NULL, 
     cc_expire       VARCHAR(255) NOT NULL, 
     department_name VARCHAR(255) NOT NULL, 
     CONSTRAINT      fk_person_department FOREIGN KEY (department_name) REFERENCES department(name) 
  ); 

CREATE TABLE voting_log 
  ( 
     election_id    INT NOT NULL, 
     person_cc      INT NOT NULL, 
     voting_table_id INT NOT NULL, 
     created_at     VARCHAR(255) NOT NULL, 
     CONSTRAINT     pk_voting_log PRIMARY KEY (election_id, person_cc), 
     CONSTRAINT     fk_voting_log_election FOREIGN KEY (election_id) REFERENCES election(id), 
     CONSTRAINT     fk_voting_log_voting_table FOREIGN KEY (voting_table_id) REFERENCES voting_table(id), 
     CONSTRAINT     fk_voting_log_person FOREIGN KEY (person_cc) REFERENCES person(cc) 
  ); 

CREATE TABLE voting_list_member 
  ( 
     person_cc     INT NOT NULL, 
     voting_list_id INT NOT NULL, 
     CONSTRAINT pk_voting_list_members PRIMARY KEY (person_cc, voting_list_id), 
     CONSTRAINT fk_voting_list_member_person FOREIGN KEY (person_cc) REFERENCES person(cc), 
     CONSTRAINT fk_votinglistmember_votinglist FOREIGN KEY (voting_list_id) REFERENCES voting_list(id) 
  ); 
  
CREATE SEQUENCE election_seq MINVALUE 0;
CREATE SEQUENCE voting_list_seq MINVALUE 0;
CREATE SEQUENCE voting_table_seq MINVALUE 0;

COMMIT;