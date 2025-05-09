/**

We are inserting 6 test rows with order_hash_code from values 1-6 and 3 different varids.
varid1 has 1 row
varid2 has 2 rows
varid3 has 3 rows

 */
insert into order_commit_status (var_id , program_id , user_id, order_hash_code, insert_time)
values('varid1','default','user1', 1, '2020-04-17 22:02:17.597');

insert into order_commit_status (var_id , program_id , user_id, order_hash_code, insert_time)
values('varid2','default','user1', 2, '2020-04-17 22:02:17.597');

insert into order_commit_status (var_id , program_id , user_id, order_hash_code, insert_time)
values('varid2','default','user1', 3, '2020-04-17 22:02:17.597');

insert into order_commit_status (var_id , program_id , user_id, order_hash_code, insert_time)
values('varid3','default','user1', 4, '2020-04-17 22:02:17.597');

insert into order_commit_status (var_id , program_id , user_id, order_hash_code, insert_time)
values('varid3','default','user1', 5, '2020-04-17 22:02:17.597');

insert into order_commit_status (var_id , program_id , user_id, order_hash_code, insert_time)
values('varid3','default','user1', 6, '2020-04-17 22:02:17.597');
