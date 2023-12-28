#!/bin/bash

NETWORK=kafka-local

#
# creates a network unique to this project that can be shared between docker compose instances
#
if [ "$(docker network inspect -f '{{.Name}}' $NETWORK 2>/dev/null)" != "$NETWORK" ]; then
  echo "creating network $NETWORK"
  (docker network create -d bridge $NETWORK >/dev/null)
fi

