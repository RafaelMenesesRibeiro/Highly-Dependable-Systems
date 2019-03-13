drop table if exists users cascade;

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
	signatureAlgorithmId varchar(50)
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
	CONSTRAINT pk_users PRIMARY KEY(userId),
	CONSTRAINT fk_users FOREIGN KEY(userId) REFERENCES users(userId)
);