drop table vote;
drop table votingListMembers;
drop table person;
drop table votingTable;
drop table votingList;
drop table department;
drop table faculty;
drop table election;

/*
Creates table "election", which has a unique id for referencing.
Each "election" line represents an election of a list (from a pool of lists).
*/
CREATE TABLE election
(electionID           int                 PRIMARY KEY,
 electionName         VARCHAR(255)        NOT NULL,
 electionDescription  VARCHAR(255),
 electionType         VARCHAR(255)        NOT NULL,
 electionSubType      VARCHAR(255),
 electionStart        VARCHAR(255)        NOT NULL,
 electionEnd          VARCHAR(255)        NOT NULL  
);

/*
Creates table "faculty", which has a unique name
Each "faculty" line represents a group of university departments concerned with a major division of knowledge. 
*/
CREATE TABLE faculty
(facName        VARCHAR(255)        PRIMARY KEY
);

/*
Creates table "department", with a unique name
Each "department" line represents a building of the university dealing with a specific area of activity.
*/
CREATE TABLE department
(depName        VARCHAR(255)        PRIMARY KEY,
 faculty        VARCHAR(255)        ,
                                    FOREIGN KEY (faculty)
                                        REFERENCES faculty(facName)
);

/*
Creates table "votingList", which has a unique id for referencing.
Each "votingList" line represents a group of people who want to get elected
*/
CREATE TABLE votingList
(listID         int                 NOT NULL        PRIMARY KEY,
 listName       VARCHAR(30)         NOT NULL,
 electionID           int, 
                                    FOREIGN KEY (electionID) 
                                        REFERENCES election(electionId)
);

CREATE TABLE votingTable
(electionID,    int                 NOT NULL,
 depName,       VARCHAR(255)        NOT NULL,
                                    FOREIGN KEY (depName) 
                                        REFERENCES department(depName)
                                    CONSTRAINT PK_votingTable PRIMARY KEY (electionID, depName)
);


/* 
Creates table "person", which is identified by its cc number.
Password is encrypted
Each "person" line represents one person (voter)
*/
CREATE TABLE person
(type           VARCHAR(15)         NOT NULL,
 name           VARCHAR(255)        NOT NULL,
 personID       int                 NOT NULL,
 password       VARCHAR(30)         NOT NULL,
 depName        VARCHAR(100)        NOT NULL, 
                                    FOREIGN KEY (depName) 
                                        REFERENCES department(depName),
 phone          int,
 address        VARCHAR(255),
 cc             int                 PRIMARY KEY,
 ccExpire       VARCHAR(255)
);
 
CREATE TABLE votingListMembers
(listID         int                 NOT NULL,
                                    FOREIGN KEY (listID)
                                        REFERENCES votingList(listID),
 personCC       int                 NOT NULL,
                                    FOREIGN KEY (personCC)
                                        REFERENCES person(cc),
                                    CONSTRAINT PK_votingListMemebers PRIMARY KEY (listID, personCC)

);

CREATE TABLE vote
(electionID     int                 NOT NULL,
                                    FOREIGN KEY (electionID)
                                        REFERENCES election(electionID)
 terminalID     int                 NOT NULL,
 votingListID   int                 NOT NULL,
                                    FOREIGN KEY (votingListID)
                                        REFERENCES votingList(votingListID),
 voteDate       VARCHAR(255)        NOT NULL
);

COMMIT;
