/* active */
INSERT INTO var_program_domain_user_restriction (user_id,is_active,var_id,program_id,login_type,auth_type)
VALUES ('user1@gmail.com','Y','var1','EPP','otp','blacklist');

/* not active */
INSERT INTO var_program_domain_user_restriction (user_id,is_active,var_id,program_id,login_type,auth_type)
VALUES ('user2@gmail.com','N','var2','EPP','otp','blacklist');
