drop table log;
drop table vote;
drop table votingListMembers;
drop table person;
drop table votingTable;
drop table votingList;
drop table department;
drop table faculty;
drop table election;

CREATE TABLE Election (
  id            int            PRIMARY KEY,
  name          VARCHAR(255)   NOT NULL,
  description   VARCHAR(255)   NOT NULL,
  type          VARCHAR(255)   NOT NULL,
  subtype       VARCHAR(255)   NOT NULL,
  start         VARCHAR(255)   NOT NULL,
  end           VARCHAR(255)   NOT NULL,
  votes         int            NOT NULL,
  blankVotes    int            NOT NULL,
  nullVotes     int            NOT NULL
);

CREATE TABLE Faculty (
  name   VARCHAR(255)   PRIMARY KEY
);

CREATE TABLE Department (
  name          VARCHAR(255)   PRIMARY KEY,
  facultyName   VARCHAR(255)   NOT NULL,
  CONSTRAINT    FK_Faculty     FOREIGN KEY     (facultyName)
                REFERENCES     Faculty(name)
);

CREATE TABLE VotingList (
  id          int            PRIMARY KEY,
  electionID  int            NOT NULL,
  name        VARCHAR(255)   NOT NULL,
  votes       int            NOT NULL,
  CONSTRAINT  FK_Election    FOREIGN KEY    (electionID)
              REFERENCES     Election(id)
);

CREATE TABLE VotingTable (
  id               int              PRIMARY KEY,
  electionID       int              NOT NULL,
  departmentName   VARCHAR(255)     NOT NULL,
  CONSTRAINT       FK_Election
                   FOREIGN KEY      (electionID)
                   REFERENCES       Election(id),
  CONSTRAINT       FK_Department
                   FOREIGN KEY      (depName)
                   REFERENCES       Department(name)
);

CREATE TABLE Person (
  cc               int             PRIMARY KEY,
  type             VARCHAR(255)    NOT NULL,
  name             VARCHAR(255)    NOT NULL,
  password         VARCHAR(255)    NOT NULL,
  address          VARCHAR(255)    NOT NULL,
  number           int             NOT NULL,
  phone            int             NOT NULL,
  ccExpire         VARCHAR(255)    NOT NULL,
  departmentName   VARCHAR(255)    NOT NULL,
  CONSTRAINT       FK_Department
                   FOREIGN KEY     (depName)
                   REFERENCES      Department(name)
);

CREATE TABLE VotingListMember (
  personCC       int                    NOT NULL,
  votingListID   int                    NOT NULL,
  CONSTRAINT     PK_votingListMembers
                 PRIMARY KEY            (personCC, votingListID),
  CONSTRAINT     FK_Person
                 FOREIGN KEY            (personCC)
                 REFERENCES             Person(id),
  CONSTRAINT     FK_VotingList
                 FOREIGN KEY            (votingListID)
                 REFERENCES             VotingList(id)
);

CREATE TABLE VotingLog (
  electionID      int              NOT NULL,
  personCC        int              NOT NULL,
  votingTableID   int              NOT NULL,
  date            VARCHAR(255)     NOT NULL,
  CONSTRAINT      PK_VotingLog
                  PRIMARY KEY      (electionID, personCC),
  CONSTRAINT      FK_Election
                  FOREIGN KEY      (electionID)
                  REFERENCES       Election(id)
  CONSTRAINT      FK_VotingTable
                  FOREIGN KEY      (votingTableID)
                  REFERENCES       votingTable(id),
  CONSTRAINT      FK_Person
                  FOREIGN KEY      (personCC)
                  REFERENCES       Person(cc)
);

COMMIT;
