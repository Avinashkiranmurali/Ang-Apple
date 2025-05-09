/**

We are inserting 6 test rows 
order_id = 1 has 1 row
order_id = 2 has 2 rows with duplicate name
order_id = 3 has 3 rows

 */
insert into `order_attribute` (`id`, `order_id` , `name`, `value`)
values(111, 1, 'name1', 'value1_orderid1');

insert into `order_attribute` (`id`, `order_id` , `name`, `value`)
values(112, 2, 'name2', 'value2_orderid2_duplicate_name');
insert into `order_attribute` (`id`, `order_id` , `name`, `value`)
values(113, 2, 'name2', 'value3_orderid2_duplicate_name');

insert into `order_attribute` (`id`, `order_id` , `name`, `value`)
values(114, 3, 'name4', 'value4_orderid3');
insert into `order_attribute` (`id`, `order_id` , `name`, `value`)
values(115, 3, 'name5', 'value5_orderid3');
insert into `order_attribute` (`id`, `order_id` , `name`, `value`)
values(116, 3, 'name6', 'value6_orderid3');
