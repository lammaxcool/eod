select *
from (select ORDER_ID, count("COUNT") "COUNT", sum("COUNT") "SUM_COUNT"
      from orders
      where __time >= current_timestamp - interval '1' day
      group by 1
      order by ORDER_ID desc)
where "COUNT" > 1
order by "ORDER_ID" desc