drop table person;
drop table votingList;
drop table department;
drop table faculty;
drop table election;

/*
Creates table "election", which has a unique id for referencing.
Each "election" line represents an election of a list (from a pool of lists).
*/
CREATE TABLE election
(electionId           int                 PRIMARY KEY,
 electionName         VARCHAR(255)        NOT NULL,
 electionDesc         VARCHAR(255),
 electionType         VARCHAR(6)          NOT NULL,
 electionStart        DATE                NOT NULL,
 electionEnd          DATE                NOT NULL  
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
(listId         int                 NOT NULL        PRIMARY KEY,
 listName       VARCHAR(30)         NOT NULL,
 electionID           int, 
                                    FOREIGN KEY (electionID) 
                                        REFERENCES election(electionId)
);


/* 
Creates table "person", which is identified by its cc number.
Password is encrypted
Each "person" line represents one person (voter)
*/
CREATE TABLE person
(type           VARCHAR(15)         NOT NULL,
 name           VARCHAR(255)        NOT NULL,
 personId       int                 NOT NULL,
 password       VARCHAR(30)         NOT NULL,
 depName        VARCHAR(100)           NOT NULL, 
                                    FOREIGN KEY (depName) 
                                        REFERENCES department(depName),
 phone          int,
 address        VARCHAR(255),
 cc             int                 PRIMARY KEY,
 ccExpire       DATE,
 listId         int,
                                    FOREIGN KEY (listId)
                                        REFERENCES votingList(listId)
);
 

COMMIT;
