#!/bin/bash

DIR=$(dirname $0)

cd $DIR
echo "dir {$DIR}"

BIN=$DIR/../../bin

CONNECT=$BIN/connect
DRUID=$BIN/druid

KT="docker exec -t cp-kafka-tools kafka-topics --bootstrap-server broker-1:9092,broker-2:9092,broker-3:9092"
KSQL="docker exec -t cp-kafka-tools ksql http://ksql-server:8088 --config-file /config/ksql.properties --file "

$CONNECT available
[ $? -ne 0 ] && echo "connector RESTful API is not yet available, aborting script. wait until connector is ready to run this script." && exit 1
echo "connector RESTful API AVAILABLE "

# also done in build.sh
cp -r data/* ../../connect/data
echo "copying data executed"

#docker exec -t cp-kafka-tools kafka-topics --bootstrap-server broker-1:9092,broker-2:9092,broker-3:9092 --create --replication-factor 3 --partitions 4 --topic orders
${KT} --create --replication-factor 3 --partitions 4 --topic orders
echo "create topic orders executed"
${KT} --create --replication-factor 3 --partitions 4 --topic ORDERS_ENRICHED
echo "create topic ORDERS_ENRICHED"
${KT} --create --replication-factor 3 --partitions 4 --topic users
echo "create topic users executed"
${KT} --create --replication-factor 3 --partitions 4 --topic stores
echo "create topic stores executed"

$CONNECT plugins
[ $? -eq 1 ] && error_msg "connect RESTful API not yet available" && exit 1
echo "connect plugins executed"

$CONNECT create ./connectors/store.json
echo "connect create connector store.json executed"
$CONNECT create ./connectors/user.json
echo "connect create connector user.json executed"
$CONNECT create ./connectors/order.json
echo "connect create connector order.json executed"

#docker exec -t cp-kafka-tools ksql http://ksql-server:8088 --config-file /config/ksql.properties --file /ksql/users.ksql
$KSQL /ksql/users.ksql
echo "ksql users.ksql executed"
$KSQL /ksql/stores.ksql
echo "ksql stores.ksql executed"
$KSQL /ksql/orders.ksql
echo "ksql orders.ksql executed"

echo "sleeping 2 seconds to make sure the tables for users and stores are hydrated before orders enrichment stream is created"
sleep 2
$KSQL /ksql/orders_enriched.ksql
echo "ksql orders_enriched.ksql executed"

$DRUID load ./druid/order.json
echo "druid load order.json executed"
