CREATE TABLE merchant(
	id                  bigint          NOT NULL AUTO_INCREMENT,
	supplier_id         bigint          NOT NULL,
	merchant_id         bigint          NOT NULL,
	name                varchar(100)    NULL,
	instore_tax_rate    bigint          NULL,
	simple_name         varchar(50)     NULL,
  PRIMARY KEY (`id`)
);

/****** Object:  Table dbo.category_configuration    Script Date: 03-09-2019 PM 08:27:24 ******/

CREATE TABLE `category_configuration` (
	`id` int AUTO_INCREMENT PRIMARY KEY,
	`category_name` varchar(128) NULL,
	`template` varchar(128) NULL,
	`default_template` varchar(128) NULL,
	`is_configurable` varchar NOT NULL,
	`order_by` int NOT NULL,
	`image_url` varchar(512) NULL,
	`is_multiline_engravable` varchar NOT NULL,
	`summary_image_icon_url` varchar(512) NULL,
	`default_product_image` varchar(512) NULL,
	`brief_description` varchar(256) NULL,
	`engrave_bg_image_url` varchar(512) NULL,
	`supported_locale` varchar(128) NULL,
	`supported_var_id` varchar(100) NULL,
	`supported_program_id` varchar(100) NULL,
	`deep_link_url` varchar(200) NULL,
	`is_new` varchar NULL,
	`psid` varchar(50) NULL,
	`is_engravable` varchar NULL,
	`is_active` varchar NOT NULL);


CREATE TABLE `orders` (
	`order_id` int AUTO_INCREMENT NOT NULL PRIMARY KEY,
	`supplier_id` varchar(50) NULL,
	`var_id` varchar(50) NULL,
	`program_id` varchar(50) NULL,
	`order_date` date NULL,
	`user_id` varchar(255) NULL,
	`firstname` varchar(100) NULL,
	`lastname` varchar(100) NULL,
	`addr1` varchar(100) NULL,
	`addr2` varchar(100) NULL,
	`city` varchar(60) NULL,
	`state` varchar(50) NULL,
	`zip` varchar(20) NULL,
	`country` varchar(50) NULL,
	`phone` varchar(20) NULL,
	`email` varchar(100) NULL,
	`last_update` date NULL,
	`user_points` int NULL,
	`is_apply_super_saver_shipping` varchar(80) NULL,
	`gift_message` varchar(900) NULL,
	`ship_desc` varchar(200) NULL,
	`var_order_id` varchar(100) NULL,
	`order_source` varchar(20) NULL,
	`notification_type` varchar(20) NULL,
	`app_version` int NULL,
	`PROXY_USER` varchar(100) NULL,
	`language_code` varchar(2) NULL,
	`country_code` varchar(2) NULL,
	`currency_code` char(3) NULL,
	`business_name` varchar(125) NULL,
	`phone_alternate` varchar(30) NULL,
	`addr3` varchar(200) NULL,
	`ip_address` varchar(50) NULL,
	`email_changed` varchar(1) NULL,
	`address_changed` varchar(1) NULL,
	`gst_amount` float NULL,
	`earned_points` int NULL,
	`establishment_fees_points` int NULL,
	`establishment_fees_price` float NULL);


CREATE TABLE `order_line`(
	`order_id` int NULL,
	`line_num` int NULL,
	`supplier_id` varchar(50) not NULL,
	`var_id` varchar(50) NULL,
	`program_id` varchar(50) NULL,
	`category` varchar(100) NULL,
	`supplier_order_id` varchar(100) NULL,
	`item_id` varchar(200) NULL,
	`name` varchar(500) NULL,
	`image_url` varchar(500) NULL,
	`color` varchar(500) NULL,
	`weight` int NULL,
	`size` varchar(100) NULL,
	`attr1` varchar(100) NULL,
	`attr2` varchar(100) NULL,
	`attr3` varchar(100) NULL,
	`is_eligible_for_super_saver_shipping` varchar(1) NULL,
	`quantity` int NULL,
	`supplier_item_price` int NULL,
	`supplier_tax_price` int NULL,
	`supplier_per_shipment_price` int NULL,
	`supplier_shipping_unit` varchar(10) NULL,
	`supplier_shipping_unit_price` int NULL,
	`supplier_single_item_shipping_price` int NULL,
	`supplier_shipping_price` int NULL,
	`conv_rate` float NULL,
	`tax_rate` int NULL,
	`b2s_item_margin` float NULL,
	`var_item_margin` float NULL,
	`b2s_shipping_margin` float NULL,
	`var_shipping_margin` float NULL,
	`item_points` float NULL,
	`tax_points` float NULL,
	`shipping_points` float NULL,
	`order_line_points` int NULL,
	`b2s_item_profit_price` int NULL,
	`b2s_tax_profit_price` int NULL,
	`b2s_shipping_profit_price` int NULL,
	`var_item_profit_price` int NULL,
	`var_tax_profit_price` int NULL,
	`var_shipping_profit_price` int NULL,
	`var_order_line_price` int NULL,
	`comment` varchar(5000) NULL,
	`create_date` date NULL,
	`order_status` int NULL,
	`order_line_type` varchar(100) NULL,
	`merchant_id` varchar(100) NULL,
	`manufacturer` varchar(100) NULL,
	`brand` varchar(100) NULL,
	`category_path` varchar(500) NULL,
	`sku` varchar(100) NULL,
	`shipping_method` varchar(100) NULL,
	`seller_id` varchar(100) NULL,
	`listing_id` varchar(100) NULL,
	`store_id` varchar(20) NULL,
	`order_source` varchar(100) NULL,
	`b2s_tax_price` int NULL,
	`b2s_tax_rate` int NULL,
	`b2s_tax_points` float NULL,
	`original_partner_order_number` varchar(50) NULL,
	`resend_partner_order_number` varchar(50) NULL,
	`gateway_order_number` varchar(255) NULL,
	`bundle_id` varchar(50) NULL,
	`notification_id` int NULL,
	`policy_id` varchar(32) NULL,
	`travel_start_date` date NULL,
	`order_line_num` int NULL,
	`order_line_undiscounted_unit_fee` int NULL,
	`b2s_offline_fee` int NULL,
	`b2s_online_fee` int NULL,
	`booking_quantity` int NULL,
	`travel_end_date` date NULL,
	`fx_rate` float NULL,
	`fx_file_id` varchar(10) NULL,
	`global_carrier_fee_id` varchar(125) NULL,
	`cash_buy_in_points` float NULL,
	`cash_buy_in_price` int NULL,
	`global_carrier_tracking_url` varchar(255) NULL,
	 order_delay varchar(20) NULL,
	 points_rounding_increment int NULL,
	 eff_conv_rate float NULL,
	 var_order_line_id varchar(100) NULL,
	 discounted_supplier_item_price int NULL,
	 discounted_var_order_line_price int NULL,
	 discounted_taxes int NULL,
	 discounted_fees int NULL
);


CREATE TABLE ORDER_LINE_FEE(
	ORDER_ID int NULL,
	ORDER_LINE int NULL,
	NAME varchar(50) NULL,
	AMOUNT int NULL,
	POINTS int NULL,
	CREATE_TIME date NULL);

CREATE TABLE ORDER_LINE_TAX(
	ORDER_ID int NULL,
	ORDER_LINE int NULL,
	NAME varchar(50) NULL,
	AMOUNT int NULL,
	POINTS int NULL,
	CREATE_TIME date NULL);


CREATE TABLE orders_additional_info(
	ORDER_ID int  NULL,
	client_bill_pay int  NULL,
	attr1 varchar(50)  NULL,
	attr2 varchar(50) NULL,
	attr3 varchar(50)  NULL);


CREATE TABLE ORDER_DIAGNOSTIC_INFO(
	id int NOT NULL AUTO_INCREMENT,
	order_id int  NULL,
	hostname int  NULL,
	ipaddress varchar(50) NULL,
	fraud_order varchar(50) NULL,
	updated_by varchar(50)  NULL,
	updated_time date  NULL);

CREATE TABLE var_program(
	varid varchar(50) NOT NULL,
	programid varchar(50) NOT NULL,
	name varchar(100) NULL,
	active_ind varchar(3) NULL,
	imageurl varchar(300) NULL,
	conv_rate float NULL,
	tax_rate bigint NULL,
	point_name varchar(45) NULL,
	super_saver_ind varchar(3) NULL,
	exp_shipping_ind varchar(3) NULL,
	demo_ind varchar(3) NULL,
	program_color varchar(1500) NULL,
	page_url varchar(100) NULL,
	wish_list varchar(3) NULL,
	wish_lsit_external varchar(3) NULL,
	max_catalog_price int NULL,
	catalog_justify varchar(45) NULL,
	catalog_width bigint NULL,
	catagory_title_color varchar(45) NULL,
	return_url varchar(300) NULL,
	return_url_name nvarchar(50) NULL,
	contact_us varchar(2) NULL,
	view_orders varchar(2) NULL,
	faqs varchar(2) NULL,
	terms_conditions varchar(2) NULL,
	lastupdate_user varchar(40) NULL,
	lastupdate_time datetime NULL,
	wsdl_url varchar(1000) NULL,
	var_program_KEY int AUTO_INCREMENT NOT NULL,
	var_KEY bigint NULL,
	workstation_id varchar(50) NULL,
	wrap_url varchar(255) NULL,
	admin_issues bit NOT NULL,
	participant_issues bit NOT NULL,
	mobile_activation_key varchar(100) NULL,
	mobile_application_header_url varchar(512) NULL,
	mobile_application_authenication_type char(3) NULL,
	b2s_tax_rate bigint NULL,
	platformid varchar(50) NULL,
	New_Menu bit NOT NULL,
	showgcdelivery bit NOT NULL,
	src varchar(20) NOT NULL,
	extend_src varchar(20) NOT NULL,
	payment_option varchar(25) NOT NULL,
	points_round_type varchar(20) NOT NULL,
	insurance_phone_number varchar(20) NULL,
	point_format varchar(50) NULL,
	legacy_ind bit NOT NULL,
    is_ack_terms_cond varchar(2) NULL,
    CONSTRAINT unique_var_program_vp_1 UNIQUE (varid, programid)

	);

CREATE TABLE var_program_credit_adds_filter(
	var_id varchar(50) NOT NULL,
	program_id varchar(50) NOT NULL,
	filter varchar(30) NOT NULL,
	lastupdate_user varchar(40) NULL,
	lastupdate_time datetime NULL,
	workstation_id varchar(50) NULL,
	row_id int  NOT NULL AUTO_INCREMENT
);


CREATE TABLE `var_program_config`(
	`id` bigint  NOT NULL AUTO_INCREMENT,
	`var_id`  varchar(50) NOT NULL,
	`program_id`  varchar(50) NOT NULL,
	`active_ind` char(1) NULL,
	`name` varchar(50) NOT NULL,
	`value` varchar(max) NULL);

CREATE TABLE `order_attribute`(
	`id` bigint NOT NULL AUTO_INCREMENT,
	`order_id` bigint NULL,
	`name` varchar(30) NULL,
	`value` varchar(max) NULL);

insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values (
83439,	'BSWIFT',	'EPP',	'Y',	'catalog_id',	'apple-us-en');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83440,	'BSWIFT',	'EPP',	'Y',	'SingleItemPurchase',	'true');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83441,	'BSWIFT',	'EPP',	'Y',	'showDollars',	'true');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83442,	'BSWIFT',	'EPP',	'Y',	'landing_page_url',	'redirect:/merchandise/wellness/landing.jsp#/?state=home&redirect=/home/');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83443,	'BSWIFT',	'EPP',	'Y',	'options.name.displayOrderBy',	'caseSize,caseColor,bandType,bandColor,bandSize,communication');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83444,	'BSWIFT',	'EPP',	'Y',	'showFromPrice',	'true');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83445,	'BSWIFT',	'EPP',	'Y',	'estimatedMaxTaxRate',	'10');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83446,	'BSWIFT',	'EPP',	'Y',	'shop_name',	'bswift-us-en');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83447,	'BSWIFT',	'EPP',	'Y',	'airlineDatafeed',	'');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83448,	'BSWIFT',	'EPP',	'Y',	'showCaseSizeFromPrice',	'true');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83449,	'BSWIFT',	'EPP',	'Y',	'payPeriods',	'0');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83450,	'BSWIFT',	'EPP',	'Y',	'epp',	'true');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83451,	'BSWIFT',	'EPP',	'Y',	'payerId',	'1103500');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83452,	'BSWIFT',	'EPP',	'Y',	'isEligibleForDiscount',	'true');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83453,	'BSWIFT',	'EPP',	'Y',	'blacklist',	'true');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83676,	'BSWIFT',	'EPP',	'Y',	'postBackType',	'api');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(83798,	'BSWIFT',	'EPP',	'Y',	'email.include.employerInfo',	'true');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(142178,	'BSWIFT',	'EPP',	'N',	'analytics',	'matomo');
insert into `var_program_config` (`id`,	`var_id` ,	`program_id`, 	`active_ind`,	`name`,	`value`) values
(143262,	'BSWIFT',	'EPP',	'N',	'activeWebShops', '');
/* S-10711 - Finance Option foundational work */

INSERT INTO var_program (varid, programid, name, active_ind, imageurl, conv_rate, tax_rate, point_name, super_saver_ind, exp_shipping_ind, demo_ind, program_color, page_url, wish_list, wish_lsit_external, max_catalog_price, catalog_justify, catalog_width, catagory_title_color, return_url, return_url_name, contact_us, view_orders, faqs, terms_conditions, lastupdate_user, lastupdate_time, wsdl_url, var_program_key, var_KEY, workstation_id, wrap_url, admin_issues, participant_issues, mobile_activation_key, mobile_application_header_url, mobile_application_authenication_type, b2s_tax_rate, platformid, New_Menu, showgcdelivery, src, extend_src, payment_option, points_round_type, insurance_phone_number, point_format, legacy_ind)
values ('Amex', 'b2s_qa_only', 'b2s_qa_only', 'Y', '/apple-gr/vars/amex/dls-logo-bluebox-solid.svg', 0, 0, 'amex.cash', NULL, 'N', 'N', '', '/default', 'Y', 'N', -1, 'Y', 0, 'N', '', '', 'N', 'N', 'N', 'Y', user(), CURRENT_TIMESTAMP(), '', 78250, NULL, 'admin2', '', 0, 0, NULL, NULL, NULL, 0, NULL, 0, 1, '', '', 'CASH', 'NO_ROUNDING' , NULL, NULL, 1);

INSERT INTO var_program (varid, programid, name, active_ind, imageurl, conv_rate, tax_rate, point_name, super_saver_ind, exp_shipping_ind, demo_ind, program_color, page_url, wish_list, wish_lsit_external, max_catalog_price, catalog_justify, catalog_width, catagory_title_color, return_url, return_url_name, contact_us, view_orders, faqs, terms_conditions, lastupdate_user, lastupdate_time, wsdl_url, var_program_key, var_KEY, workstation_id, wrap_url, admin_issues, participant_issues, mobile_activation_key, mobile_application_header_url, mobile_application_authenication_type, b2s_tax_rate, platformid, New_Menu, showgcdelivery, src, extend_src, payment_option, points_round_type, insurance_phone_number, point_format, legacy_ind)
values ('AmexAU', 'default', 'default', 'Y', '/apple-gr/vars/amex/dls-logo-bluebox-solid.svg', 0, 0, 'amex.cash', NULL, 'N', 'N', '', '/default', 'Y', 'N', -1, 'Y', 0, 'N', '', '', 'N', 'N', 'N', 'Y', user(), CURRENT_TIMESTAMP(), '', 78251, NULL, 'admin2', '', 0, 0, NULL, NULL, NULL, 0, NULL, 0, 1, '', '', 'CASH', 'NO_ROUNDING' , NULL, NULL, 1);

CREATE TABLE var_program_redemption_option
(
    id int AUTO_INCREMENT PRIMARY KEY,
	var_id varchar(50) NOT NULL,
	program_id varchar(50) NOT NULL,
	payment_option varchar(50) NOT NULL,
	limit_type varchar(25) NOT NULL,
	payment_min_limit int default 0,
	payment_max_limit int default 0,
	order_by int NOT NULL,
	is_active bit NOT NULL,
	payment_provider varchar(50) NULL,
	last_updated_by varchar(100) NOT NULL,
	last_updated_date datetime NOT NULL,
	CONSTRAINT FK_var_program_redemption_option FOREIGN KEY (var_id, program_id) REFERENCES var_program (varid, programid),
	CONSTRAINT unique_var_program_payment_limit UNIQUE (var_id, program_id, payment_option,limit_type)
);

CREATE TABLE var_program_finance_option
(
	id                 int AUTO_INCREMENT    NOT NULL PRIMARY KEY,
	var_id             varchar(50)        NOT NULL,
	program_id         varchar(50)        NOT NULL,
	installment        int                NOT NULL,
	installment_period varchar(50)        NOT NULL,
	message_code       nvarchar(500)      NULL,
	order_by           int                NOT NULL,
	is_active          bit                NOT NULL,
	lastupdate_user    varchar(40)        NULL,
	lastupdate_time    datetime           NULL,
	CONSTRAINT FK_var_program_finance_option FOREIGN KEY (var_id, program_id) REFERENCES var_program (varid, programid)
);

ALTER TABLE var_program_finance_option ADD establishment_fee_type varchar(30);

ALTER TABLE var_program_finance_option ADD establishment_fee_rate float;

CREATE TABLE `var_program_catalog_config`
(
	`id`             int AUTO_INCREMENT NOT NULL PRIMARY KEY,
	`catalog_id`      varchar(50)  NOT NULL,
	`var_id`          varchar(50)  NOT NULL,
	`program_id`      varchar(50)  NOT NULL,
	`active_ind`      char(1)      NOT NULL,
	`name`           varchar(50)  NOT NULL,
	`value`           varchar(max) NULL,
	`lastupdate_user` varchar(40) default 'admin',
	`lastupdate_time` datetime    default null
);

CREATE TABLE quick_link
(
	id                    int AUTO_INCREMENT    NOT NULL PRIMARY KEY,
	locale                varchar(50)           NOT NULL,
	var_id                varchar(50)           NOT NULL,
	program_id            varchar(50)           NOT NULL,
	link_code             varchar(50)           NOT NULL,
	link_text             nvarchar(1024)        NOT NULL,
	link_url              varchar(1024)         NOT NULL,
	priority              int                   NOT NULL,
	show_unauthenticated  bit                   NOT NULL,
	display               bit                   NOT NULL
);

CREATE TABLE naughty_word
(
	id               int AUTO_INCREMENT NOT NULL PRIMARY KEY,
	locale           varchar(50) NOT NULL,
	word             varchar(50) NOT NULL,
	pattern          varchar(50) NOT NULL,
	language         varchar(50) NOT NULL,
	match_whole_word int         NOT NULL
);

CREATE TABLE order_commit_status
(
	order_commit_status_id int AUTO_INCREMENT NOT NULL PRIMARY KEY,
	var_id                 varchar(50)   NOT NULL,
	program_id             varchar(50)   NOT NULL,
	user_id                varchar(255)  NOT NULL,
	order_hash_code        int           NOT NULL,
	order_description      varchar(8000) NULL,
	insert_time            datetime      NOT NULL,
	attr1                  varchar(500)  NULL,
	attr2                  varchar(500)  NULL,
	workstation_id         varchar(50)   NULL
);

CREATE TABLE merc_search_filter
(
    search_filter_id  int AUTO_INCREMENT NOT NULL PRIMARY KEY,
    var_id           varchar(50)   NULL,
    program_id       varchar(50)   NULL,
    filter_type      varchar(10)   NULL,
    filter_scope     varchar(200)  NULL,
    filter_name      varchar(20)   NULL,
    filter_value     varchar(100)  NOT NULL,
    is_active        varchar(1)    NULL,
    added_date       datetime      NOT NULL,
    added_by         nvarchar(600) NULL,
    comment          nvarchar(600) NULL,
    CONSTRAINT merc_search_filter_unique UNIQUE (var_id, program_id, filter_type, filter_name, filter_value)
);


CREATE TABLE `var_program_notification`
(
	`id`              int AUTO_INCREMENT NOT NULL PRIMARY KEY,
	`var_id`          varchar(50)        NOT NULL,
	`program_id`      varchar(50)        NOT NULL,
	`type`            varchar(50)        NOT NULL,
	`name`            varchar(50)        NOT NULL,
	`template_id`     varchar(50)        NOT NULL,
	`locale`          varchar(10)        NOT NULL,
	`subject`        varchar(200)       NULL,
	`is_active`       bit                NOT NULL,
	`lastupdate_user` varchar(40)        NOT NULL,
	`lastupdate_time` datetime           NOT NULL);

CREATE TABLE demo_user(
	varid       varchar(50)     NOT NULL,
	programid   varchar(50)     NOT NULL,
	userid      varchar(255)    NOT NULL,
	password    varchar(50)     NULL,
	activeind   varchar(3)      NULL,
	points      bigint          NULL,
	firstname   varchar(100)    NULL,
	lastname    varchar(100)    NULL,
	addr1       varchar(100)    NULL,
	addr2       varchar(80)     NULL,
	city        varchar(80)     NULL,
	state       varchar(80)     NULL,
	zip         varchar(80)     NULL,
	country     varchar(80)     NULL,
	phone       varchar(80)     NULL,
	email       varchar(80)     NULL,
	accept_date datetime        NULL
);

CREATE TABLE domain_var_mapping(
	id              bigint AUTO_INCREMENT NOT NULL,
	domain          varchar(255)            NOT NULL,
	var_id          nvarchar(50)            NOT NULL,
	program_id      nvarchar(150)           NULL,
	created_date    datetime                NOT NULL,
	is_active       varchar(1)              NULL,
	login_type      char(15)                NULL
);

CREATE TABLE engrave_configuration(
	id                  int AUTO_INCREMENT   NOT NULL,
	category_slug_id    int                 NULL,
	model               varchar(500)        NULL,
	country             varchar(500)        NULL,
	locale              varchar(50)         NULL,
	font                varchar(400)        NULL,
	font_code           varchar(200)        NULL,
	max_chars_per_Line  varchar(500)        NULL,
	width_dimension     varchar(500)        NULL,
	no_of_lines         int                 NULL,
	template_class      varchar(100)        NULL,
	is_active           bit                 NULL,
	is_preview          bit                 NULL,
	preview_url         varchar(500)        NULL,
	is_default_preview_enabled  bit         NULL,
	is_upper_case_enabled bit               NULL
);

CREATE TABLE engrave_font_configuration
(
    id                  int AUTO_INCREMENT PRIMARY KEY,
    engrave_config_id   int                NOT NULL,
    char_length_from    int                NOT NULL,
    char_length_to      int                NOT NULL,
    font_code           nvarchar(200)      NOT NULL
);

CREATE TABLE otp(
	id             bigint AUTO_INCREMENT NOT NULL,
	email_id        nvarchar(max)   NOT NULL,
	otp             varchar(255)    NOT NULL,
	created_date    datetime        NOT NULL,
	used            varchar(1)      NOT NULL,
	usage_date      datetime        NULL
);

CREATE TABLE var_program_payment_option(
	id 									int 		AUTO_INCREMENT NOT NULL,
	var_id 								varchar(50) NOT NULL,
	program_id 							varchar(50) NOT NULL,
	payment_option 						varchar(50) NOT NULL,
	payment_template 					varchar(50) NULL,
	is_active 							bit 		NOT NULL,
	order_by 							int 		NOT NULL,
	payment_min_limit  					int 		NULL,
	payment_max_limit 					int 		NULL,
	supplementary_payment_type 			varchar(50) NULL,
	supplementary_payment_limit_type 	varchar(1) 	NULL,
	supplementary_payment_min_limit 	int 		NULL,
	supplementary_payment_max_limit 	int 		NULL,
	lastupdate_user 					varchar(40) NOT NULL,
	lastupdate_time 					datetime 	NOT NULL,
	payment_provider 					varchar(50) NULL
);

CREATE TABLE var_program_template(
	id          bigint AUTO_INCREMENT NOT NULL,
	var_id      varchar(50)     NOT NULL,
	program_id  varchar(50)     NULL,
	config_data nvarchar(max)   NOT NULL,
	is_active   bit             NOT NULL
);


CREATE TABLE order_line_shipment_notification(
	id                      bigint AUTO_INCREMENT NOT NULL,
	b2s_order_id            bigint          NULL,
	delivery_date           datetime        NULL,
	line_number             bigint          NULL,
	order_date              datetime        NULL,
	partner_order_number    varchar(100)    NULL,
	quantity                int             NULL,
	shipment_date           datetime        NULL,
	shipping_carrier        varchar(100)    NULL,
	shipping_method         varchar(100)    NULL,
	sku                     varchar(100)    NULL,
	tracking_number         varchar(100)    NULL,
	tracking_url            varchar(max)    NULL
);

CREATE TABLE Status_Change_Queue(
	QueueID             bigint AUTO_INCREMENT NOT NULL,
	Order_ID            bigint                  NOT NULL,
	line_num            bigint                  NOT NULL,
	QueueDateTime       datetime                NOT NULL,
	Order_Status        bigint                  NOT NULL,
	Attempts            int                     NOT NULL,
	Update_User_Name    varchar(50)             NULL,
	Update_Machine_Name varchar(50)             NULL,
	process_status      varchar(15)             NULL,
	process_description varchar(255)            NULL,
	process_date        datetime                NULL
);

CREATE TABLE order_line_status_history(
	order_id    bigint      NOT NULL,
	line_num    bigint      NOT NULL,
	status_id   bigint      NOT NULL,
	date_time   datetime    NOT NULL
 );

 CREATE TABLE var_program_domain_user_restriction(
	id          bigint AUTO_INCREMENT NOT NULL,
	user_id     varchar(255)            NOT NULL,
	is_active   varchar(1)              NOT NULL,
	var_id      varchar(50)             NULL,
	program_id  varchar(50)             NOT NULL,
	login_type  varchar(50)             NOT NULL,
	auth_type   varchar(20)             NOT NULL
);

CREATE TABLE whitelist_word(
	id                  int AUTO_INCREMENT NOT NULL,
	locale              nvarchar(50)    NOT NULL,
	word                nvarchar(100)   NOT NULL,
	pattern             nvarchar(500)   NOT NULL,
	match_whole_word    int             NOT NULL,
	language            varchar(50)     NOT NULL
);

create table `order_line_attribute`
(
	`id`       bigint AUTO_INCREMENT NOT NULL,
	`order_id` bigint,
	`line_num` bigint,
	`name`    varchar(30),
	`value`   nvarchar(255)
);

create table order_status
(
	status_id bigint default 0 not null
		constraint PK_order_status
			primary key,
	"desc"    varchar(40)      not null
);


CREATE TABLE shopping_cart
(
	id              int AUTO_INCREMENT NOT NULL PRIMARY KEY,
	var_id          varchar(50)        NOT NULL,
	program_id      varchar(50)        NOT NULL,
	user_id         varchar(50)        NOT NULL
);

CREATE TABLE `banner_template`
(
	`id`              int AUTO_INCREMENT NOT NULL PRIMARY KEY,
	`var_id`         varchar(50)        NOT NULL,
	`program_id`      varchar(50)        NOT NULL,
	`locale`          varchar(50)        NOT NULL,
	`name`            varchar(50)        NOT NULL,
	`value`           varchar(max)       NOT NULL,
	`active_ind`      varchar(10)        NOT NULL,
	`description`     varchar(200)       NULL
);

CREATE TABLE pricing_model_configuration
(
	id                      int AUTO_INCREMENT NOT NULL PRIMARY KEY,
	var_id                  varchar(50)        NOT NULL,
	program_id              varchar(50)        NOT NULL,
	discount_tier1          float              NULL,
	discount_tier2          float              NULL,
	discount_tier3          float              NULL,
	delta                   float              NULL,
	price_type              varchar(100)       NOT NULL,
	price_key               varchar(100)       NOT NULL,
	payment_value           float              NOT NULL,
	repayment_term          int                NULL,
	months_subsidized       int                NULL,
	payment_value_points    int                NULL
);

CREATE TABLE product_attribute_configuration
(
	id                      int AUTO_INCREMENT NOT NULL PRIMARY KEY,
	category_id             int                NULL,
	attribute_type          varchar(128)       NULL,
	attribute_name          varchar(128)       NULL,
	available_for_search    bit                NULL,
	available_for_detail    bit                NULL,
    lastupdate_user         varchar(40)        NULL,
	lastupdate_time         datetime           NULL,
	order_by                int                NULL
);

CREATE TABLE `search_redirect`
(
	`id`                    int AUTO_INCREMENT    NOT NULL PRIMARY KEY,
	`var_id`                varchar(50)           NOT NULL,
	`program_id`            varchar(50)           NOT NULL,
	`catalog_id`            varchar(50)           NOT NULL,
	`search_keyword`        nvarchar(100)         NOT NULL,
	`action_type`           varchar(50)           NOT NULL,
	`value`                 nvarchar(255)         NOT NULL,
	`active`                bit                   NOT NULL
);

CREATE TABLE var_program_gift_promo
(
    id               int AUTO_INCREMENT    NOT NULL PRIMARY KEY,
    locale           varchar(50)           NOT NULL,
    var_id           varchar(50)           NOT NULL,
    program_id       varchar(50)           NOT NULL,
    qualifying_psid  varchar(50)           NOT NULL,
    gift_item_psid   varchar(50)           NOT NULL,
    start_date       DATETIME              NOT NULL,
    end_date         DATETIME              NOT NULL,
    active           bit                   NOT NULL,
    discount         int                   NOT NULL,
    discount_type    varchar(20)           NOT NULL
);
