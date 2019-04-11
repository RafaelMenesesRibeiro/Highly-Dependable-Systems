# Highly-Dependable-Systems

# HDS Notary



## 1 - Introduction

Create a Highly Dependable Notary application.  
The main goal of HDSNotary is to certify the transfer os ownership of goods between users.  

## 2 - Setup for testing

Notice: the guide that follows is meant to be followed on a Windows 10 operating system. The current project works
similarly well on linux distributions, but it's not guaranteed that the setup steps are exactly the same, as such, if 
you prefer to test this project under a another operating system, it's up to you to correctly setup everything that
might be different from the present guide;

### 2.1 Databases
#### 2.1.1 Installing PostgreSQL and PGAdmin4

* Go ahead to https://www.enterprisedb.com/downloads/postgres-postgresql-downloads.
* Download PostgreSQL Version 11.2 Windows x86-64 or whichever version fits you.
* Execute the item you just downloaded and leave Post
* Leave all items selected. In this guide we'll use PGAdmin4 to create the databases, but if you feel comfortable using
a command line interface, by all means, uncheck the PGAdmin4 checkbox.
* Specify installation folder, choose your own or keep the suggested default.
* Enter the password for the database superuser and password. Don't forget these.
* Enter a port for PostgreSQL Server. Make sure that no other applications are using this port and if you are unsure
just leave the default port.
* Choose a locale for the database
* Wait a few minutes until installation is complete.
 
#### 2.1.2 Verifying your installation

* Open your terminal (windows cmd) and type in 'psql --version' if you have followed the previous steps correctly you 
should see the following message appearing on your cmd window:

        psql (PostgreSQL) 11.2

#### 2.1.3 Setting up your postgreSQL Server
* Open your newly installed application PGAdmin4. This will launch a new browser window. Don't panic.
* On the left panel of your screen, right click 'Servers', choose 'Create' > 'Server'
* A small window will pop up with a few fields. Enter any name you desire on the 'Name' field for example 'hds-db'
* Under the 'Connection' tab of that same window insert '127.0.0.1' on 'Host name/address' field
* Also fill in the 'Port' field with whatever port you used in step 2.1.1
* Fill in the name of 'Maintenance Database', it should be 'postgresql' by default.
* Fill in 'Username' and 'Password' with whatever you chose during step 2.1.1 as well
* Tick 'Save Password if' you want.
* Click 'Save' and connect to the server. After saving it's likely that connection is done automatically, but if doesn't
just double click the database server you just created in the left panel.

#### 2.1.4 Setting up your postgreSQL Database (used in this project)
* On the top menu click 'Object' > 'Create' > 'Database' and give it a 'Name' for example 'notary-db', leave the remaining
fields as default.
* Click 'Save' then double click the database you just created in the left panel to connect to it.

#### 2.1.5 Setting up your database schemas
* Now, on the top menu, click 'Tools' > 'Query Tool' or alternatively just click the thundering symbol near 'File'.
* Head to our project's folder, wherever it is you placed it on under ~/docs/psql/ you'll find two important .sql files
named 'schemas.sql' and 'populate.sql', open them both with your favorite text editor, for example, SublimeText3
* Copy & Paste the contents of schemas.sql into your query tool and press 'F5' on your keyboard, or the thunder that appears
on the query tool menu, the one that is somewhat on the middle of the screen, not the one near the 'File' option.
* Clear the query tool window and then Copy & Paste the text inside populate.sql and then press 'F5' again.
* You should now have a neat database with some entries to test the system. You can verify this by right click anything
on the left the server or database you created on the previous step aand then choosing 'refresh'. If you did everything
right, you can right click the database you created, then opening schemas and then tables (all of them on the left panel)
to see the tables there. You can use the query tool to see the data inside any one table, by issuing the following SQL
command inside the query tool:
        
        SELECT * FROM <tablename>

    In PGAdmin4 you do not need the semicolon after any command unless you intend to piggyback/chain multiple commands.

#### 2.1.6 Finishing up the setup

* You reached the last step for this configuration. Head over to our project's folders and find the file 'application.properties.template'
rename it to 'application.properties' or make a copy then rename it.
* Change the following fields according to the previous steps, ignore the lines preceded by $ in this snippet:

      $ server.port=8000
      $ spring.datasource.driverClassName=org.postgresql.Driver
      $ spring.datasource.platform=postgres
       spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/notary-db
       spring.datasource.username=postgresql
       spring.datasource.password=rootroot

