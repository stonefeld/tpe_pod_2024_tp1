#!/bin/bash

CLIENT=false
SERVER=false

print_error() {
  echo "$(basename $0): $1"
  exit 1
}

command -v mvn &>/dev/null || print_error "Maven is not installed"

while getopts 'cs' flag; do
  case "${flag}" in
    c) CLIENT=true;;
    s) SERVER=true;;
    *) echo "Unexpected option ${flag}";;
  esac
done

if [ "$CLIENT" = true ] ; then
  echo "Building client ..."
  mvn clean package -pl client -am -DskipTests 1>&2

  echo "Unpacking client ..."
  pushd client/target &>/dev/null
  tar -xzf tpe1-g2-client-1.0-SNAPSHOT-bin.tar.gz
  chmod +x tpe1-g2-client-1.0-SNAPSHOT/*.sh
  popd &>/dev/null

  [ -d bin ] || mkdir bin
  [ -d bin/client ] && rm -r bin/client
  mv client/target/tpe1-g2-client-1.0-SNAPSHOT bin/client
fi

if [ "$SERVER" = true ] ; then
  echo "Building server ..."
  mvn clean package -pl server -am -DskipTests 1>&2

  echo "Unpacking server ..."
  pushd server/target &>/dev/null
  tar -xzf tpe1-g2-server-1.0-SNAPSHOT-bin.tar.gz
  chmod +x tpe1-g2-server-1.0-SNAPSHOT/*.sh
  popd &>/dev/null

  [ -d bin ] || mkdir bin
  [ -d bin/server ] && rm -r bin/server
  mv server/target/tpe1-g2-server-1.0-SNAPSHOT bin/server
fi

[ "$CLIENT" = true ] && echo "Client built and unpacked in bin/client"
[ "$SERVER" = true ] && echo "Server built and unpacked in bin/server"

exit 0