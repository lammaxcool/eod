#!/bin/sh

cd $(dirname $0)/../..

./network.sh

KSQLDB_DOCKER_COMPOSE_FILE="ksqlDB/docker-compose.yml"

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
  sed -I .bak -E "s/KSQL_KSQL_STREAMS_PROCESSING_GUARANTEE: (.*)/KSQL_KSQL_STREAMS_PROCESSING_GUARANTEE: exactly_once_v2/" ${KSQLDB_DOCKER_COMPOSE_FILE}
  echo ""
  echo "docker-compose.yml updated for $CONTAINER, processing guarantee set to exactly_once_v2"
  echo ""
elif [[ "$alo" == true ]]; then
  sed -I .bak -E "s/KSQL_KSQL_STREAMS_PROCESSING_GUARANTEE: (.*)/KSQL_KSQL_STREAMS_PROCESSING_GUARANTEE: at_least_once/" ${KSQLDB_DOCKER_COMPOSE_FILE}
  echo ""
  echo "docker-compose.yml updated for $CONTAINER, processing guarantee set to at_least_once"
  echo ""
fi
rm -f "${KSQLDB_DOCKER_COMPOSE_FILE}.bak"

(cd cp-kafka-tools; docker compose up -d)

ACTIVE_BROKERS=$(sed -n -E -e "s/^  (broker-[0-9]):/\1 /p" ./kafka/docker-compose.yml | tr -d '\n')
(cd kafka; docker compose up -d zookeeper ${ACTIVE_BROKERS})

(cd connect; docker compose up --build -d)

(cd druid; docker compose up --build -d)

(cd ksqlDB; docker compose up --build -d)

(cd monitoring; docker compose up --build -d prometheus grafana)
