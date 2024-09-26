#!/bin/bash

CLIENT=false
SERVER=false
CLEAN=${CLEAN:-false}

log_file="tpe_builder.log"

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

if [ "$CLIENT" = false ] && [ "$SERVER" = false ]; then
  print_error "You must specify at least one of the following options: -c, --client, -s, --server"
fi

if [ "$CLIENT" = true ]; then
  echo "===== BUILDING CLIENT ====="
  echo "Creating client folders ..."
  [ -d bin/client ] && rm -rf bin/client
  mkdir -p bin/client

  echo "Building client ..."
  [ "$CLEAN" = true ] && mvn clean -pl client -am 1>&2
  mvn package -pl client -am &>$log_file

  [ $? -ne 0 ] && print_error "Error building client. Check ${log_file} for more information"

  printf "Unpacking client ...\n\n"
  tar -xzf client/target/tpe1-g2-client-1.0-SNAPSHOT-bin.tar.gz -C bin/client --strip-components=1
  chmod +x bin/client/*.sh
fi

if [ "$SERVER" = true ]; then
  echo "===== BUILDING SERVER ====="
  echo "Creating server folders ..."
  [ -d bin/server ] && rm -rf bin/server
  mkdir -p bin/server

  echo "Building server ..."
  [ "$CLEAN" = true ] && mvn clean -pl server -am 1>&2
  mvn package -pl server -am &>$log_file

  [ $? -ne 0 ] && print_error "Error building server. Check ${log_file} for more information"

  printf "Unpacking server ...\n\n"
  tar -xzf server/target/tpe1-g2-server-1.0-SNAPSHOT-bin.tar.gz -C bin/server --strip-components=1
  chmod +x bin/server/*.sh
fi

echo "===== BUILDING FINISHED ====="
[ "$CLIENT" = true ] && echo "Client built and unpacked inside bin/client"
[ "$SERVER" = true ] && echo "Server built and unpacked inside bin/server"

exit 0
