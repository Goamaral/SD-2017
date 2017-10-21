drop table person;
drop table election;
drop table list;
drop table department;
drop table faculty;
 
/* 
Creates table "person", which is identified by its cc number.
Password is encrypted
Each "person" line represents one person (voter)
*/
CREATE TABLE person
(type           VARCHAR(15)         NOT NULL,
 name           VARCHAR(255)        NOT NULL,
 id             int                 NOT NULL,
 password       VARCHAR(30)         NOT NULL,
 department     VARCHAR(100)        NOT NULL,
 phone          int,
 address        VARCHAR(255),
 cc             int                 PRIMARY KEY,
 ccExpire       DATE,
 listName       VARCHAR(15)
);
 
/*
Creates table "election", which has a unique id for referencing.
Each "election" line represents an election of a list (from a pool of lists).
*/
CREATE TABLE election
(id             int                 PRIMARY KEY,
 name           VARCHAR(255)        NOT NULL,
 description    VARCHAR(255),
 type           VARCHAR(6)          NOT NULL,
 start          DATE                NOT NULL,
 end            DATE                NOT NULL,   
);

/*
Creates table "list", which has a unique id for referencing.
Each "list" line represents a voting list (a group of people who want to get elected)
*/
CREATE TABLE list
(id             int                 NOT NULL        PRIMARY KEY,
 name           VARCHAR(30)         NOT NULL,
 election       int                 FOREIGN KEY REFERENCES election(id)
);
 
/*
Creates table "department", with a unique name
Each "department" line represents a building of the university dealing with a specific area of activity.
*/
CREATE TABLE department
(name           VARCHAR(255)        PRIMARY KEY,
 faculty        VARCHAR(255)        FOREIGN KEY REFERENCES faculty(name)
);

/*
Creates table "faculty", which has a unique name
Each "faculty" line represents a group of university departments concerned with a major division of knowledge. 
*/
CREATE TABLE faculty
(name           VARCHAR(255)        PRIMARY KEY,
);


COMMIT;
