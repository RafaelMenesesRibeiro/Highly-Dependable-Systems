#!/bin/bash

echo off
setlocal enabledelayedexpansion

call mvn clean compile install -DskipTests

FOR /L %%N IN (1, 1, 3) DO (
   set number=%%N
   set /A s=!number!+9000
   echo s=!s!
   start cmd /c "mvn spring-boot:run -Dspring-boot.run.arguments=!s!,8010 && pause"
)
