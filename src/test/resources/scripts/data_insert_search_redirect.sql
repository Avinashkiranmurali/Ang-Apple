/*
    S-12561 search redirects to be able to provide alternate search keyword or redirect url based on search result
    search_redirect table

*/
insert into `search_redirect` (`catalog_id`, `var_id`, `program_id`, `search_keyword`, `action_type`, `value`, `active`)values('apple-us-en','UA','MP','adaptor','ALTERNATE','adapter',1);

insert into `search_redirect` (`catalog_id`, `var_id`, `program_id`, `search_keyword`, `action_type`, `value`, `active`)values('apple-us-en','UA','default','adaptor','ALTERNATE','adapter all program',1);

insert into `search_redirect` (`catalog_id`, `var_id`, `program_id`, `search_keyword`, `action_type`, `value`,
`active`) values('apple-us-en','-1','default','adaptor','ALTERNATE','adapter all var',1);

insert into `search_redirect` (`catalog_id`, `var_id`, `program_id`, `search_keyword`, `action_type`, `value`,
`active`) values('apple-us-en','UA','default','airpod pro','REDIRECT','#/store/browse/music/music-airpods',1);

insert into `search_redirect` (`catalog_id`, `var_id`, `program_id`, `search_keyword`, `action_type`, `value`,
`active`) values('apple-us-en','-1','default','airpod pro','REDIRECT','#/store/browse/music/music-airpods/all_var',1);

insert into `search_redirect` (`catalog_id`, `var_id`, `program_id`, `search_keyword`, `action_type`, `value`,
`active`) values('apple-us-en','UA','default','airfly','REDIRECT_ON_NO_RESULT',
'#/store/curated/accessories/all-accessories/all-accessories-wireless-headphones/all_program',0);

insert into `search_redirect` (`catalog_id`, `var_id`, `program_id`, `search_keyword`, `action_type`, `value`,
`active`) values('apple-us-en','-1','default','airfly','REDIRECT_ON_NO_RESULT',
'#/store/curated/accessories/all-accessories/all-accessories-wireless-headphones',1);

insert into `search_redirect` (`catalog_id`, `var_id`, `program_id`, `search_keyword`, `action_type`, `value`,
`active`) values('-1','-1','default','airfly next','ALTERNATE','butterfly',1);

