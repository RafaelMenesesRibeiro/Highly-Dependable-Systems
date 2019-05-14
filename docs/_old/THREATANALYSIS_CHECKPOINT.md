## Threat Analysis
Since this is a notary system, messages are meant to be public. This means that not only is confidentiality not a fundamental requirement, but is also undesired as described by the project statement.

### 1. Threats an

#### 1.1 Message Tampering
Neutrilized threat with signatures

#### 1.2 Message Replay
Neutrilized threat with timestamps and nonces. These mechanisms can't be bypassed due to 1.1.

#### 1.3 Message Stealing
Is possible, but won't affect the System because the Notary cross references fields like 'to', 'from', 'seller', 'owner' and others with the signatures in the messages.

#### 1.4 SQL Injection
It's not guaranteed that all SQL injections have been prevented. However, received messages are sanitized using jsoup library before they reach our Spring Controllers. Any of the incomming data is inserted in database queries using JDBC prepared statements.

#### 1.5 Escalation of Privilege
Could be possible because of our simplified use of userIds to load public/private keys from the filesystem. However a regex is used to guarantee that such identifiers are indeed ports and not paths. Possibly preventing path traversal attacks.

#### 1.6 Denial Of Service && Distributed Denial of Service
No defense mechanisms put in place. Proxies could be put in place between the client and the notary and possibly limit requests done by each user to the webserver APIs.

Note: The notary server implements a volatile cache that maps requests to users and stores any previous response to that request, up to a limit of 128 messages, to allow users to receive previous messages should they fail to receive replies for any reason. This slightly mitigates DoS attacks because it speeds up processing by avoiding access to databases and signature verification.

#### 1.7 Sybil Attacks
It is not possible for rogue clients to take control of the network. Important operations are only considered valid if they are signed by the notary. See SaleCertificateResponse class as an example.

#### 1.8 Double Spending Attacks
Double spending / Dobule Sell attacks are prevented in two manners:

* Logically by veryfing that a good belongs to a user before taking action
* Using transactions in access to the databases

Locks and Synchronized mechanisms haven't been implemented on the code level.

### 2. Dependability and Security Guarantees

#### 2.1 Availability
In this checkpoint no availability guarantees are given by any node in the system. Namely, if the server crashes it will not come back up. All state regarding previously answered messages are lost, but ownership and membership is persisted in databases.

#### 2.2 Reliability
As long as the server is alive, due to protections mechanisms mentioned in (1) the services provided are correct.

#### 2.3 Safety
We only have two non-idempotent endpoints on the system. The one that puts a good on sale and the one that allows users to sell their items to requestors. In both situations, the Notary makes sure that users can't advertise sale of goods owned by other users and that users can't force other users to sell them goods.

#### 2.4 Integrity
Any message that is altered while passing through the network or within a rogue host, is rejected by the notary due to the use of signatures.

#### 2.5 Maintainability
If the server undergoes maintenance the service will not be available to anyone in the system. There is no replication on this delivery.