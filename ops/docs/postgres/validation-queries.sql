SELECT o.order_id, COUNT(*)
FROM eos.orders_eos o
GROUP BY o.order_id
HAVING COUNT(*) > 1;

WITH diff AS (SELECT o.order_timestamp, o.processed_at, EXTRACT(EPOCH FROM (o.processed_at - o.order_timestamp)) AS seconds_difference
              FROM eos.orders_eos o)
SELECT MIN(d.seconds_difference), MAX(d.seconds_difference), AVG(d.seconds_difference)
FROM diff d;

SELECT o.order_id, COUNT(*)
FROM redis.orders_redis o
GROUP BY o.order_id
HAVING COUNT(*) > 1;

WITH diff AS (SELECT o.order_timestamp, o.processed_at, EXTRACT(EPOCH FROM (o.processed_at - o.order_timestamp)) AS seconds_difference
              FROM redis.orders_redis o)
SELECT MIN(d.seconds_difference), MAX(d.seconds_difference), AVG(d.seconds_difference)
FROM diff d;