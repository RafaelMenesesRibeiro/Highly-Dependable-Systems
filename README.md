# Highly-Dependable-Systems

# HDS Notary



## 1 - Introduction
Create a Highly Dependable Notary application.  
The main goal of HDSNotary is to certify the transfer of ownership of goods between users.  

## 2 - Setup for testing
Notice: the guide that follows is meant to be followed on a Windows 10 operating system. The current project works
similarly well on Linux distributions, but it's not guaranteed that the setup steps are the same, as such, if 
you prefer to test this project under another operating system, it's up to you to correctly setup everything that
might be different from the present guide;

#### 2.2 Java (JDK 1.8)
* It is assumed that you already have jdk 1.8 installed on your system with the respective environment variables configured. If you don't follow this guide https://www3.ntu.edu.sg/home/ehchua/programming/howto/JDK_Howto.html

### 2.2 Databases
#### 2.2.1 Downloading and installing both PostgreSQL and PGAdmin4
* Go ahead to https://www.enterprisedb.com/downloads/postgres-postgresql-downloads.
* Download PostgreSQL Version 11.2 Windows x86-64 or whichever version fits you.
* Execute the item you just downloaded.
* Leave all items selected except StackBuilder. In this guide we'll use PGAdmin4 to create the databases, but if you feel comfortable using
a command line interface uncheck the PGAdmin4 checkbox.
* Specify installation folder, choose your own or keep the suggested default.
* Choose the name for the database's superuser and password. Don't forget these.
* Enter a port for PostgreSQL Server. Make sure that no other applications are using this port and if you are unsure just leave the default port.
* Choose a locale for the database
* Wait until installation is complete.
 
#### 2.2.2 Verifying your installation
* Open your terminal (windows cmd) and type in  
  
      psql --version
  
* If you followed the previous steps correctly you should see the following message appearing on your cmd window:  
  
      psql (PostgreSQL) 11.2
  
#### 2.2.3 Setting up your postgreSQL Server
* Open the newly installed application PGAdmin4. This will launch a new browser window.
* On the left panel of your screen, right click 'Servers', choose 'Create' > 'Server'.
* A small window will pop up with a few fields. Enter any name you desire on the 'Name' field, for example 'hds-db'.
* Under the 'Connection' tab of that same window insert '127.0.0.1' on 'Host name/address' field.
* Also fill in the 'Port' field with the port used in step 2.1.1.
* Fill in the name of 'Maintenance Database', it should be 'postgresql' by default.
* Fill in 'Username' and 'Password' to match the ones in step 2.1.1.
* Click 'Save' and connect to the server. After saving it's likely that the connection is done automatically, but if doesn't double click the database server you just created in the left panel.

#### 2.2.4 Setting up your postgreSQL Database (used in this project)
* On the top menu click 'Object' > 'Create' > 'Database' and give it a 'Name' for example 'notary-db', leave the remaining
fields as default.
* Click 'Save' then double click the database you just created in the left panel to connect to it.

#### 2.2.5 Setting up your database schemas
* Now, on the top menu, click 'Tools' > 'Query Tool' or alternatively just click the thundering symbol near 'File'.
* Head to our project's folder. Under ~/docs/psql/ you'll find two important .sql files named 'schemas.sql' and 'populate.sql'.
* Copy & Paste the contents of schemas.sql into your query tool and press 'F5' on your keyboard, or the thunder that appears
on the query tool menu, the one that is somewhat on the middle of the screen, not the one near the 'File' option.
* Clear the query tool window and then Copy & Paste the text inside populate.sql and then press 'F5' again.
* You should now have a database with some entries to test the system. You can verify this by right clicking anything on the left - the server or database you created - and then choosing 'refresh'. 
* If you did everything right, you can right click the database you created, then open schemas, then tables (all of them on the left panel) to see the tables there. You can use the query tool to see the data inside any one table, by issuing the following SQL
command inside the query tool:
        
        SELECT * FROM <tablename>
  
    In PGAdmin4 you do not need the semicolon after any command unless you intend to piggyback/chain multiple commands.

#### 2.2.6 Finishing up the setup
* Go to our project's folders and find the file 'application.properties.template'. Rename it to 'application.properties'.
* Change the following fields according to the previous steps, ignore the lines preceded by $ in this snippet:

      $ server.port=8000
      $ spring.datasource.driverClassName=org.postgresql.Driver
      $ spring.datasource.platform=postgres
       spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/notary-db
       spring.datasource.username=postgresql
       spring.datasource.password=rootroot

The database can now be used by the HDS Project

#### Extra notes regarding the database schemas adopted by this project.
* For simplicity, each user in the system is identified by the port where their REST Endpoints are located.
* The notary endpoint is always 8000, you should not try to change this behaviour. That's why the server.port property is set to 8000.
* If you want to add new users to the system to test loading capacity, identify them with a port that's bigger than 8000.

### 2.3 Maven
#### 2.3.1 Downloading Maven
* Head over to https://maven.apache.org/download.cgi and download the newest Maven version in Binary Zip Archive format. At the time of writing that would be: 
        
        apache-maven-3.6.0-bin.zip
 
#### 2.3.2 Installing Maven
* Go the folder where you've downloaded the item and extract it's contents to a folder where you wish maven to be installed.
* Now, open your windows task bar and search for 'environment variables' and open the appropriate result. A small window will open.
* On that window, on the bottom right corner you'll find a button saying 'environment variables', click it.
* Under system path, select the variable 'Path' and click 'Edit' > 'New' and type in the path where you installed maven followed by \bin, for example like so:

        C:\Program Files\apache-maven-3.6.0\bin
        
#### 2.3.3 Verifying your installation
* Open your terminal (windows cmd) and type in

        mvn --version
      
* If you have followed the previous steps correctly you should see the following message appearing on your cmd window:

        Apache Maven 3.6.0 (97c98ec64a1fdfee7767ce5ffb20918da4f719f3; 2018-10-24T19:41:47+01:00)
        Maven home: C:\Program Files\apache-maven-3.6.0\bin\..
        Java version: 1.8.0_191, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk1.8.0_191\jre
        Default locale: en_US, platform encoding: Cp1252
        OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"

### 2.4 Executing

#### 2.4.1 Installing dependencies and compiling the project
* Open your IDE terminal or your Windows CMD terminal.
* Head over to the root of the Maven project, it should be something like '~/Highly-Dependable-Systems/hds'.
* You now have to install the inter-dependencies between the various project modules:  

        mvn clean install -DskipTests
        
* If no errors are displayed, you are now ready to extract the authentication certificate from Citizen Card.
 
#### 2.4.2 Extract authentication certificate from Citizen Card
* You now have to extract the authentiaction certificate from the the Citizen Card
* Plug in the card reader/keyboard with the Citizen Card
* In the same terminal as the previous step type:

         mkdir client/src/main/resources/certs
         
         java -Djava.library.path=/usr/local/lib -cp ./security/target/classes:./security/src/main/resources/pteidlibj.jar hds.security.AuthCertExtractor ./client/src/main/resources/certs/server.pem        

* If no errors are displayed, you are now ready to test the project.
 
#### 2.4.3 Running client and server programs
* To run a server issue the following command two commands (launch only one of these):
        
        cd server
        
        mvn spring-boot:run -Dspring-boot.run.arguments=8010 -Pwindows
  
     The first argument is the userId with the highest port in the system, assuming that no client is dead, 
     unreachable and that clients will be created contiguously, without gaps between their ports. If you are running
     this project on Linux Distribution use -Plinux instead.
     
* To run a client, first go back in your folder structures and then into the client folder like so:
 
        cd ../client
        
        
* Now you can launch as many clients as you want issue the following command repeatedly:
 
        mvn spring-boot:run -Dspring-boot.run.arguments=8001,8010
        
     Where the first argument is the port (userId) of the running client and the second argument is the the number of 
     clients you will run in total plus eight-thousand, that is the highest userId in the system assuming the same as in 
     the server launch command.
 
 ## 3 - Documentation
 The main documentation are in the /docs directory at the root of the project
 
* DESIGNDECISIONS.md - for a concise description and justification of the design decisions.
* THREATANALYSIS.md - for an analysis of the possible threats and corresponding protection mechanisms.

 
