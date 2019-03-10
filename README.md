# Highly-Dependable-Systems

# HDS Notary



## 1 - Introduction

Create a Highly Dependable Notary application.  
The main goal of HDSNotary is to certify the transfer os ownership of goods between users.  



## 2 - Goals

The HSDNotary maintains information regarding the ownership of goods.  
This information needs to be updated in a dependable way to reflect the execution of transactions that transfer goods between exactly two users.

An initial set of users, the set of goods they own (represented by a tuple (goodID, userID)) and the notary identity are assumed to exist when the application first starts.  
The initial set of users and goods is immutable and known by every user.  

The notary should be contacted once the seller of a goods has agreed to transfer ownership of their good to a buyer.  
The role of the notary is to certify that the transfer is valid:

1. the good to be transfered is owned by the seller
2. both the seller and the buyer agree to the transaction


Users can perform the following operations:

1. obtain the status of any good: its owner and whether it's on sale
2. express their intention to sell any of their goods or buy any good cuurently on sale
3. submit their transaction requests to the notary


The system doesn't regard the monetary aspects of the transactions, and only needs to validate the transaction according to the rules specified above.

The Notary is equipped with a Portuguese Citizen Card, which is used to certify its identity as well as provide strong cryptographic guarantees on the transactions it validates.



## 3 - Workflow of transactions

1. Seller expresses their intention to sell a good they own, by invoking the method intentionToSell() on the Notary. The notary replies acknowledging or rejecting the operation depending on whether the seller is or isn't the owner of the good
2. The buyer obtains the state (current owner id and if it's on sale) of a given good by invoking the getStateOfGood() method
3. The buyer expresses their intention to buy a given good form a seller by invoking the buyGood() method on the seller
4. Upon receiving the first expression of interest to buy one of their goods, they request the validation of the transaction by invoking the transferGood() method on the Notary
5. The seller informs the buyer of the outcome of the transaction


Upon receiving a TransactionRequest the Notary must validate it, as per Section 2.  
In case the transaction is deemed valid, the Notary must:

1. alter the internal state of the mapping Goods -> Users
2. send back a certification attesting the validity of the transaction



## 4 - Requirements


### 4.1 - Dependability requirements

The Notary is assumed to be a centralized (single server) source and trustworthy, meaning if will follow the protocol specified above.  
The server can crash and later recover. The implementation of the Notary service should guarantee no loss or corruption of its internal state.  
The Notary must hold a Portuguese Citizen Card.  

Users are identified through a Public-Key infrastructure (PKI). For simplicity, this should be done using self-generated asymetric keys. The set of users and their Public Keys can be assumed to be static and known by all the users and the Notary.

Users cannot be trusted and might try to attack the system for their own benefit.

All the buy/sell requests, as well as the certification emmited by the Notary should be non-repudiable.

For transparency purposes all the communication should take place with no confidentiality, clear text. An attacker can interfere with the messages being exchanged in the network, including message drops, manipulations and duplications.

As such, it is necessary to design application level protection mechanisms to cope with potential threats (ex: man-in-the-middle, replay or Sybil attacks).


### 4.2 - Design and Implementation requirements

The design of the HDS Notary system consists of two main parts:

1. a library that is linked with the application and provides the API specified above
2. the server that is responsible for keeping the information associated between users and goods

The library is a client of the server.  
The system must be implemented in Java using the Java Crypto API.

The type of communication technology, including message passing technology has no restrictions, only the one stated beneath.
However, HDS Notary operated under the assumption that the communication channels are not secured. In particular, solution relying on secure channel technologies such as TLS are not allowed.



## 5 - Implementation Steps

1. As a preliminary step, before starting any implementation effort, make sure to have carefully analyzed and reasoned about security and dependability issues that may arise and the required counter-measures.
2. Simple server implementation without dependability and security guarantess. Design, implement and test the server with a trivial test client with the interface above that ignores the crypto parameters (signatures, public keys, etc)
3. Implement the Citizen Card usage by the Notary
4. Develop the client library and complete the server - Implement the client library and finalize the server supporting the specified crypto operations
5. Dependability and security - Extend the system to support the dependability and security guarantess specified above

