#!/bin/bash

DEBUG="false"

while getopts 'd' OPT;
do
  case $OPT in
    d)
      DEBUG="true"
      ;;
    ?)
      echo "Usage: `basename $0` [-d]"
      exit 1
  esac
done

./gradlew clean :library:publishToMavenLocal 

if [ "$DEBUG" = "true" ]; then
  ./gradlew :app:soSourceForDebug --no-daemon -Dorg.gradle.debug=true
else
  ./gradlew :app:soSourceForDebug --info
fi
