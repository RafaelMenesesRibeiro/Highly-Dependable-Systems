drop table if exists ownership cascade;
drop table if exists certificates cascade;
drop table if exists users cascade;
drop table if exists goods cascade;

----------------------------------------
-- Table Creation
----------------------------------------

CREATE TABLE users (
	userId varchar(50),
	CONSTRAINT pk_users PRIMARY KEY(userId)
);

CREATE TABLE certificates (
	userId varchar(50),
	serialNumber varchar(30),
	certVersion varchar(10),
	signatureAlgorithmId varchar(50),
	issuerName varchar(50),
	notBefore varchar(100),
	notAfter varchar(100),
	subjectName varchar(50),
	publicKeyAlgorithm varchar(25),
	subjectPublicKey varchar(512) NOT NULL UNIQUE,
	issuerUniqueId varchar(50),
	subjectUniqueId varchar(50),
	certificateSignatureAlgorithm varchar(25),
	certificateSignature varchar(1024) NOT NULL,
	CONSTRAINT pk_certificates PRIMARY KEY(userId),
	CONSTRAINT fk_users FOREIGN KEY(userId) REFERENCES users(userId)
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
