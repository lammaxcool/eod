#!/bin/bash

DIR=$(dirname $0)

cd $DIR
echo "dir {$DIR}"

BIN=$DIR/../../bin

CONNECT=$BIN/connect
DRUID=$BIN/druid
KSQL=$BIN/ksql-shell


alias kt='kafka-topics --bootstrap-server localhost:19092,localhost:29092,localhost:39092'


$CONNECT available
[ $? -ne 0 ] && echo "connector RESTful API is not yet available, aborting script. wait until connector is ready to run this script." && exit 1
echo "connector RESTful API AVAILABLE "

# also done in build.sh
cp -r data/* ../../connect/data
echo "copying data executed"


kt --create --replication-factor 3 --partitions 4 --topic orders
echo "create topic orders executed"
kt --create --replication-factor 3 --partitions 4 --topic ORDERS_ENRICHED
echo "create topic ORDERS_ENRICHED"
kt --create --replication-factor 3 --partitions 4 --topic users
echo "create topic users executed"
kt --create --replication-factor 3 --partitions 4 --topic stores
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

$KSQL ./ksql/users.ksql
echo "ksql users.ksql executed"
$KSQL ./ksql/stores.ksql
echo "ksql stores.ksql executed"
$KSQL ./ksql/orders.ksql
echo "ksql orders.ksql executed"

echo "sleeping 2 seconds to make sure the tables for users and stores are hydrated before orders enrichment stream is created"
sleep 2
$KSQL ./ksql/orders_enriched.ksql
echo "ksql orders_enriched.ksql executed"

$DRUID load ./druid/order.json
echo "druid load order.json executed"
