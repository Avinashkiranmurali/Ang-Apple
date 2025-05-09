/*
    S-13750 var_program_gift_promo table
    Validate locale, var & program combination
    Validate Start & End promo combination
    Active & Not Active combination

    Timestamp format 'YYYY-MM-DD HH:MM:SS.millis --> 2020-07-10 12:50:00.000000'
*/
INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('-1', '-1', 'default', '30001MWYK2ZM/A', '30001MVH22LL/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');

INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('-1', '-1', 'default', '30001MWYK2ZM/A', '30001MWP22AM/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');

-- catalog, var & program combination
INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('-1', '-1', 'default', '30001MWV72LL/A', '30001MWP22AM/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');

INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('en_US', '-1', 'default', '30001MWV72LL/A', '30001MVH22LL/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');

INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('en_US', 'Delta', 'default', '30001MWV72LL/A', '30001MU8F2AM/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');

INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('en_US', 'Delta', 'b2s_qa_only', '30001MWV72LL/A', '30001MWT92LL/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');

INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('en_US', 'Chase', 'default', '30001MWV72LL/A', '30001MVH52LL/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');


INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('-1', '-1', 'default', '30001MWV72LL/A', '30001MWTK2LL/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');

INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('en_US', '-1', 'default', '30001MWV72LL/A', '30001MWTJ2LL/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');

INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('en_US', 'Delta', 'default', '30001MWV72LL/A', '30001MWT82LL/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');

INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('en_US', 'Delta', 'b2s_qa_only', '30001MWV72LL/A', '30001MY252LL/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');

INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('en_US', 'Chase', 'default', '30001MWV72LL/A', '30001MWP42LL/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2120-08-08 12:12:00.000000'}, 1, 100, 'Percentage');

-- Start & End promo configuration
INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('-1', '-1', 'default', '30001MWV72LL/A', '30001MY252LL/A', {ts '2120-08-08 12:12:00.000000'}, {ts '2120-09-09 12:12:00.000000'}, 1, 100, 'Percentage');

INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('-1', '-1', 'default', '30001MWV72LL/A', '30001MXK62LL/A', {ts '2020-06-06 12:12:00.000000'}, {ts '2020-07-07 12:12:00.000000'}, 1, 100, 'Percentage');

--	In-Active configuration
INSERT INTO var_program_gift_promo
    (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active, discount, discount_type) VALUES
	('-1', '-1', 'default', '30001MWV72LL/A', '30001MWTK2LL/A', {ts '2020-07-07 12:12:00.000000'}, {ts '2020-08-08 12:12:00.000000'}, 0, 100, 'Percentage');


-- insert time format in DB 'hh:mm MM:DD:YYYY' --> '12:30 08/14/2020'
--INSERT INTO var_program_gift_promo
--  (locale, var_id, program_id, qualifying_psid, gift_item_psid, start_date, end_date, active) VALUES
--	('en_US', '-1', 'default', '30001MWV72LL/A', '30001MVH22LL/A', getdate(), '12:30 08/14/2020', 1);