#!/bin/bash

ECHO OFF
SetLocal EnableDelayedExpansion

set initialClientPort=8001
set initialServerPort=9000

set /p numberOfClients="Enter number of clients to create : "
set /p maxNumberOfClients="Enter maximum number of clients in the system: "
set /p numberOfServers="Enter number of servers to create : "

set /A sum=%initialClientPort%+%maxNumberOfClients%-1
set /A maxClientPort=%sum%
echo First Client Port = %initialClientPort%
echo Final Client Port = %maxClientPort%

set /A sum=%initialServerPort%+%numberOfServers%-1
set /A maxServerPort=%sum%
echo First Server Port = %initialServerPort%
echo Final Server Port = %maxServerPort%


call mvn clean compile install -DskipTests

FOR /L %%N IN (1, 1, %numberOfServers%) DO (
   set number=%%N
   set /A s=!number!+%initialServerPort%-1
   echo s=!s!
   start cmd /c "cd server && mvn spring-boot:run -Dspring-boot.run.arguments=!s!,%maxClientPort%,%maxServerPort% && pause"
)

FOR /L %%N IN (1, 1, %numberOfClients%) DO (
   set number=%%N
   set /A s=!number!+%initialClientPort%-1
   echo s=!s!
   start cmd /c "cd client && mvn spring-boot:run -Dspring-boot.run.arguments=!s!,%maxClientPort%,%maxServerPort% && pause"
)

pause