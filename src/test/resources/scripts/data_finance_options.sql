/* S-10711 - Finance Option foundational work */

INSERT INTO var_program_redemption_option (var_id, program_id, payment_option, limit_type, payment_min_limit, payment_max_limit, order_by, is_active, payment_provider, last_updated_by, last_updated_date)
VALUES ('Amex','b2s_qa_only','finance','dollar',0,100,2,1,'AMEX',
        'Appl_user',CURRENT_TIMESTAMP());

INSERT INTO var_program_redemption_option (var_id, program_id, payment_option, limit_type, payment_min_limit, payment_max_limit, order_by, is_active, payment_provider, last_updated_by, last_updated_date)
VALUES ('AmexAU','default','finance','dollar',0,100,2,1,'AMEX',
        'Appl_user',CURRENT_TIMESTAMP());

--AMEX Singapore b2s_qa_only
INSERT INTO var_program_finance_option (var_id, program_id, installment, installment_period, message_code, is_active, order_by, lastupdate_user, lastupdate_time)
values ('Amex', 'b2s_qa_only', 6, 'month', N'finance.month.6.text', 1, 3, user(), CURRENT_TIMESTAMP());

INSERT INTO var_program_finance_option (var_id, program_id, installment, installment_period, message_code, is_active, order_by, lastupdate_user, lastupdate_time)
values ('Amex', 'b2s_qa_only', 9, 'month', N'finance.month.9.text', 1, 2, user(), CURRENT_TIMESTAMP());

INSERT INTO var_program_finance_option (var_id, program_id, installment, installment_period, message_code, is_active, order_by, lastupdate_user, lastupdate_time)
values ('Amex', 'b2s_qa_only', 12, 'month', N'finance.month.12.text', 1, 1, user(), CURRENT_TIMESTAMP());

--AMEX Australia default
INSERT INTO var_program_finance_option (var_id, program_id, installment, installment_period, message_code, is_active, order_by, lastupdate_user, lastupdate_time)
values ('AmexAU', 'default', 2, 'month', N'finance.month.2.text', 1, 5, user(), CURRENT_TIMESTAMP());

INSERT INTO var_program_finance_option (var_id, program_id, installment, installment_period, message_code, is_active, order_by, lastupdate_user, lastupdate_time)
values ('AmexAU', 'default', 4, 'month', N'finance.month.4.text', 1, 4, user(), CURRENT_TIMESTAMP());

INSERT INTO var_program_finance_option (var_id, program_id, installment, installment_period, message_code, is_active, order_by, lastupdate_user, lastupdate_time)
values ('AmexAU', 'default', 6, 'month', N'finance.month.6.text', 1, 3, user(), CURRENT_TIMESTAMP());

INSERT INTO var_program_finance_option (var_id, program_id, installment, installment_period, message_code, is_active, order_by, lastupdate_user, lastupdate_time)
values ('AmexAU', 'default', 8, 'month', N'finance.month.8.text', 1, 2, user(), CURRENT_TIMESTAMP());

INSERT INTO var_program_finance_option (var_id, program_id, installment, installment_period, message_code, is_active, order_by, lastupdate_user, lastupdate_time)
values ('AmexAU', 'default', 10, 'month', N'finance.month.10.text', 1, 1, user(), CURRENT_TIMESTAMP());

-- Updating table with additional fields
--Amex
UPDATE var_program_finance_option SET establishment_fee_type = 'percentage', establishment_fee_rate = 1
WHERE var_id ='Amex' AND program_id='b2s_qa_only' AND installment = 3 AND installment_period ='month';

UPDATE var_program_finance_option SET establishment_fee_type = 'percentage', establishment_fee_rate = 2
WHERE var_id ='Amex' AND program_id='b2s_qa_only' AND installment = 6 AND installment_period ='month';

UPDATE var_program_finance_option SET establishment_fee_type = 'percentage', establishment_fee_rate = 3
WHERE var_id ='Amex' AND program_id='b2s_qa_only' AND installment = 9 AND installment_period ='month';

UPDATE var_program_finance_option SET establishment_fee_type = 'percentage', establishment_fee_rate = 4
WHERE var_id ='Amex' AND program_id='b2s_qa_only' AND installment = 12 AND installment_period ='month';

--AmexAU
UPDATE var_program_finance_option SET establishment_fee_type = 'percentage', establishment_fee_rate = 1
WHERE var_id ='AmexAU' AND program_id='default' AND installment = 2 AND installment_period ='month';

UPDATE var_program_finance_option SET establishment_fee_type = 'percentage', establishment_fee_rate = 2
WHERE var_id ='AmexAU' AND program_id='default' AND installment = 4 AND installment_period ='month';

UPDATE var_program_finance_option SET establishment_fee_type = 'percentage', establishment_fee_rate = 3
WHERE var_id ='AmexAU' AND program_id='default' AND installment = 6 AND installment_period ='month';

UPDATE var_program_finance_option SET establishment_fee_type = 'percentage', establishment_fee_rate = 4
WHERE var_id ='AmexAU' AND program_id='default' AND installment = 8 AND installment_period ='month';

UPDATE var_program_finance_option SET establishment_fee_type = 'percentage', establishment_fee_rate = 5
WHERE var_id ='AmexAU' AND program_id='default' AND installment = 10 AND installment_period ='month';