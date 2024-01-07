SELECT o.order_id, COUNT(*)
FROM public.orders o
GROUP BY o.order_id
HAVING COUNT(*) > 1;

SELECT *
FROM public.orders o
ORDER BY o.order_id DESC;

SELECT *
FROM public.orders o
WHERE o.order_id = 5224;

SELECT o.order_timestamp, o.processed_at, EXTRACT(EPOCH FROM (o.processed_at - o.order_timestamp)) AS seconds_difference
FROM public.orders o
ORDER BY seconds_difference ASC;