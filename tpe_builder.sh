#!/bin/bash

CLIENT=false
SERVER=false
CLEAN=${CLEAN:-false}

print_error() {
  echo "$(basename $0): $1"
  exit 1
}

print_help() {
  echo "Usage: $(basename $0) [-c|--client] [-s|--server] [-h|--help] [-C|--clean]"
  echo "  -c, --client  Build and unpack the client"
  echo "  -s, --server  Build and unpack the server"
  echo "  -C, --clean   Clean the project before building"
  echo "  -h, --help    Show this help message"
  exit 0
}

command -v mvn &>/dev/null || print_error "Maven is not installed"
[ -z "$*" ] && print_help

for i in "$@"; do
  case "${i}" in
    -c|--client) CLIENT=true;;
    -s|--server) SERVER=true;;
    -C|--clean) CLEAN=true;;
    -h|--help) print_help;;
    *) print_error "Unexpected option ${i}";;
  esac
done

if [ "$CLIENT" = true ]; then
  echo "Building client ..."
  [ "$CLEAN" = true ] && mvn clean -pl client -am 1>&2
  mvn package -pl client -am 1>&2

  echo "Unpacking client ..."
  pushd client/target &>/dev/null
  tar -xzf tpe1-g2-client-1.0-SNAPSHOT-bin.tar.gz
  chmod +x tpe1-g2-client-1.0-SNAPSHOT/*.sh
  popd &>/dev/null

  [ -d bin ] || mkdir bin
  [ -d bin/client ] && rm -r bin/client
  mv client/target/tpe1-g2-client-1.0-SNAPSHOT bin/client
fi

if [ "$SERVER" = true ]; then
  echo "Building server ..."
  [ "$CLEAN" = true ] && mvn clean -pl server -am 1>&2
  mvn package -pl server -am 1>&2

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