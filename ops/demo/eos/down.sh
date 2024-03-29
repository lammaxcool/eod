#!/bin/sh

cd $(dirname $0)/../..

(cd cp-kafka-tools; docker compose down -v)
(cd monitoring; docker compose down -v)
(cd connect; docker compose down -v)
(cd druid; docker compose down -v)
(cd ksqlDB; docker compose down -v)
(cd kafka; docker compose down -v)
