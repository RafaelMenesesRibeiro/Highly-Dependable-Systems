@ECHO off
setlocal EnableDelayedExpansion


ECHO.
ECHO.
ECHO *****************************************************************
ECHO * Make sure you install PSQL Server before running this script  *
ECHO * Also ensure you've setted the env. variable of PSQL-CMD       *
ECHO * ... For example C:\PostgreSQL\pg11\bin                        *
ECHO *****************************************************************
ECHO * This script creates a database for HDS_Server and it's tables *
ECHO *****************************************************************
ECHO.
ECHO.
SET /p DUMMY=Hit ENTER to continue...
ECHO.
ECHO.
:: psql -d <dbname> -U <host>
:: IF YOU USED ANYTHING OTHER THAN POSTGRES DEFAUTLS DURING INSTALLATION CHANGE dbname
:: CREATE DATABASE <name>
:: IF YOU ALTER ANY DEFAUTLS REMEMBER TO EDIT ..Highly-Dependable-Systems/hds/server/src/main/resources/application.properties
SET DB_NAME="postgres"
SET DB_USER="postgres"
SET DB_HOST="localhost"
psql -d %DB_NAME% -U %DB_USER% -f batSetup.sql
ECHO.
ECHO.
SET /p DUMMY=Hit ENTER to quit...