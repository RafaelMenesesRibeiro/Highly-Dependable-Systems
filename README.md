# Project Design Decisions

Multiple nodes must communicate with each other with dependability guarantees. Nodes aren't trustworthy.
Clients and Servers talk using JSON over HTTP.
The nodes implement a (1,N) Atomic Byzantine Regular Registers with authenticated links and logical write timestamps obtained by majority before each write.

# Usage:

You need Java JDK 1.8, Maven 3.5, PSQL and Spring Boot installed to run this project.
If you need more keys use our KeyPairGeneratorScript.java, then put place the output on resources/keys folder

Server, 9000 <= svPort  < 10000 for regular sv and >= 10000 for smart card sv:

	mvn spring-boot:run -Dspring-boot.run.arguments=<#svPort>,<#clients>,<#regularSv>,<#smartCardSv>

Client, 8001 <= cliPort < 9000

	mvn spring-boot:run -Dspring-boot.run.arguments=<#cliPort>,<#servers>,<#regularSv>,<#smartCardSv>

Use our JMockits to test dependability. Discover tests within 'tests' folder of each module. Run with:

	mvn test -Dtest=classname

For more testing options check: https://www.mkyong.com/maven/how-to-run-unit-test-with-maven/

# Threat Analysis

This is a notary system, messages are meant to be visible. No confidentiality allowed.

## Message Tampering
Messages are signed node to node
## Message Replay
Physical timestamps and nonces.
## Message Stealing
Notaries validate 'to', 'from', 'seller', 'owner' and other signed fields before acting;
## SQL Injection
All messages are sanitized with jsoup library before they reach our Spring Controllers. JDBC Prepared Statements are used to interact with databases.
## Escalation of Privilege
For simplicity nodes load keys from the filesystem. To retrived them user id's are validated against a regex to prevent path traversing.
## Denial Of Service
Notary servers are protected with rate limiting computational challenge that must be answered before transfer good is effectuated. This mechanism wasn't applyed to any other node or operation for simplicity. No defense mechanisms put in place. There are no proxy servers in this project. Some requests are responded faster using cached messages.
## Sybil Attacks
Unless #maxFailures+1 occur nodes can't forge transaction documents (SaleCertResponses). However byzantine clients/servers may emit write timestamps that are much higher than the actual highest timestamp on the system, effectively moving the logical clock of write operations into the future. If a node emits MAX_INT the notaries can't recover.
## Double Spending Attacks
Prevented programatically by veryfing ownership of items before taking actions and by using the highest transactional level in access to the databases;
This delivery uses some locking and synchronized mechanisms.
## Other byzantine problems
Clients may emit valid, yet different, write messages to all notaries. They don't detect the situation.
Clients are no longer allowed to buy items from themselves, meaning they can no longer consume, slowly, yet effectively, the replicas storage space by making infinite buys.

# Dependability and Security Guarantees

## Availability
If a server fails even if temporarely cached messages are lost, but ownership and membership is still persisted in databases.
Service availability is ensured up to #maxFailures by having multiple instances of notary servers online. Notary servers don't talk with each other.
## Reliability
As long as the server is alive, due to protections mechanisms mentioned in (1) the services provided are correct. The system ensures linearizability.
## Safety
Non-idempotent operations are properly validated before being executed. Users can't advertise sale of goods owned by others and can't force other users to sell them goods.
## Integrity
Any altered message passing through the network or within a rogue host, is rejected by any receiving nodes due to the use of signatures.
## Maintainability
As long as a server is in maintenance, if other servers are alive and #maxFailures haven't been reach, the system will still be useable.