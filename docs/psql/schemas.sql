drop table if exists ownership cascade;
drop table if exists users cascade;
drop table if exists goods cascade;

----------------------------------------
-- Table Creation
----------------------------------------

CREATE TABLE users (
	userId varchar(50),
	CONSTRAINT pk_users PRIMARY KEY(userId)
);

CREATE TABLE goods (
	goodId varchar(50),
	onSale boolean,
	CONSTRAINT pk_goods PRIMARY KEY (goodId)
);

CREATE TABLE ownership (
	goodId varchar(50),
	userId varchar(50),
	CONSTRAINT pk_ownership PRIMARY KEY (goodId),
	CONSTRAINT fk_ownership_goodId FOREIGN KEY (goodId) REFERENCES goods(goodId),
	CONSTRAINT fk_ownership_userID FOREIGN KEY (userId) REFERENCES users(userId)
);
