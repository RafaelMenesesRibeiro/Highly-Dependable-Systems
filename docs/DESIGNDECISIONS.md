## Project Design Decisions

This project requires multiple nodes to communicate with each other. The objective is to provide guarantees of integrity and availability. It's assumed that the notary server is a trusted entity but that remaining nodes, clients are not. The system is meant to be implemented with Java.

### 1. Databases:

#### 1.1 PostgreSQL

We chose to use PostgreSQL over MySQL (or any other Structured Query Language) mainly due to the fact that everyone in the group as experienced it before. Not only in University projects but also in internships or personal projects. MySQL, in particular, is slower than PSQL. Furthermore PSQL is opensource and is ACID compliant.

### 2. Used Frameworks:

#### 2.1 Spring Boot

We were free to create communication channels anyway we desired. We could use Sockets, Java RMI, WebServices with SOAP-XML or HTTP-REST.
Communicating via HTTP is usually easier than explicitly programming with Sockets and REST is more flexibile and quicker to implement when compared to SOAP, for those reasons, it seemed natural to the group to adopt powerful, proven frameworks like Spring Boot. Since we do not need graphical interfaces, we chose not use Spring MVC or similar additive plugins even when considering that the group already had experience with them.

Our notary server runs a Spring Boot application. A simple HTTP Server that receives and replies to requests defined by JSON bodies.

Clients also run a Sprint Boot application, which allows clients to communicate with each other, therefore allowing them to trade goods with each other, assuming those trades are approved by the Notary at a later stage. Before launching their own server, the Clients make up a thread that works as a command line interface, allowing them to input commands that generate requests to other clients or the notary itself.

For more information refer about the RESTful APIs used by the notary server you can read more on:

		~/Highly-Dependable-Systems/hds/server/README.md

You can also learn more about the client RESTful APIs on the following document:

		~/Highly-Dependable-Systems/hds/client/README.md

### 3. Tools

#### 3.1 Maven

We are using Maven to perform the following tasks:

* Gather requirements
* Analyse requirements
* Develop (code) solution
* Test solution
* Deploy solution
* Maintain solution

Maven basically allowed us to easily manage module dependencies, wether, they were local or remote, such has the ones obtained from Maven Central.

#### 3.2 Postman

We do not teach how to setup postman, as we only used this tool to test our server and client RESTful APIs before implementing signatures.
It allowed us to quickly test our endpoints and find initial errors.