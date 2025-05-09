/*
    S-10015 TechDebt Ability to be able to add and remove categories from PS without code change
    var_program_catalog_config table

*/
insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('apple-us-en', 'PNC', 'Onyx', '1', 'excludeCategory', 'PhotographyapplePNCOnyx', 'dsheth');

insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('apple-us-en', 'PNC', 'default', '1', 'excludeCategory', 'PhotographyapplePNC-1', 'dsheth');

insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('apple-us-en', '-1', 'default', '1', 'excludeCategory', 'Photographyapple-1-1', 'dsheth');

insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('-1', '-1', 'default', '1', 'excludeCategory', 'Photography-1-1-1', 'dsheth');

/** inactive rows for excludeCategory **/
insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('-1', '-1', 'default', '0', 'excludeCategory', 'Photography-1-1-1Inactive', 'dsheth');

insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('apple-us-en', '-1', 'default', '0', 'excludeCategory', 'Photographyapple-1-1Inactive', 'dsheth');

insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('apple-us-en', 'PNC', 'default', '0', 'excludeCategory', 'PhotographyapplePNC-1Inactive', 'dsheth');

insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('apple-us-en', 'PNC', 'Onyx', '0', 'excludeCategory', 'PhotographyapplePNCOnyxInactive', 'dsheth');


insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('-1', '-1', 'default', '1', 'someName', 'LaptopAccessories-1-1-1', 'dsheth');

insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('apple-us-en', '-1', 'default', '1', 'someName', 'LaptopAccessoriesapple-1-1', 'dsheth');

insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('apple-us-en', 'PNC', 'default', '1', 'someName', 'LaptopAccessories-applePNC-1', 'dsheth');

insert into `var_program_catalog_config`(`catalog_id` , `var_id` , `program_id` ,  `active_ind`, `name`, `value`, `lastupdate_user`)
values ('apple-us-en', 'PNC', 'Onyx', '1', 'someName', 'LaptopAccessories-applePNCOnyx', 'dsheth');
