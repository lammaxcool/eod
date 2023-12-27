#!/bin/sh

cd $(dirname $0)/../..

./network.sh

ACTIVE_BROKERS=$(sed -n -E -e "s/^  (broker-[0-9]):/\1 /p" ./kafka/docker-compose.yml | tr -d '\n')
(cd kafka; docker compose up -d zookeeper ${ACTIVE_BROKERS})

(cd connect; docker compose up --build -d)

(cd druid; docker compose up --build -d)

(cd ksqlDB; docker compose up --build -d)

(cd monitoring; docker compose up --build -d prometheus grafana)
