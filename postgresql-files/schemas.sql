drop table if exists users cascade;

----------------------------------------
-- Table Creation
----------------------------------------

CREATE TABLE users (
	userId varchar(50),
	publicKey varchar(460) NOT NULL UNIQUE,
	CONSTRAINT pk_users PRIMARY KEY(userId)
);