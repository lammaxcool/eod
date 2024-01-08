#!/bin/sh

REPLICA_TIME=15

for i in "$@"; do
  case $i in
    -e|--eos|--exactly-once)
      eos=true
      ;;
    -a|--alo|--at-least-once)
      alo=true
      ;;
  esac
done

if [[ "$eos" == true ]]; then
  docker compose up pgadmin spring-consumer-eos -d
  sleep "$REPLICA_TIME"
  docker compose up spring-consumer-eos -d --scale spring-consumer-eos=2
elif [[ "$alo" == true ]]; then
  docker compose up pgadmin redis spring-consumer-redis -d
  sleep "$REPLICA_TIME"
  docker compose up spring-consumer-redis -d --scale spring-consumer-redis=2
fi