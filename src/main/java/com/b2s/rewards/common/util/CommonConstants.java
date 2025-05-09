package com.b2s.rewards.common.util;

import com.b2s.shop.common.User;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;

import static com.b2s.rewards.security.util.ExternalUrlConstants.EXTERNAL_URLS;
import static org.apache.commons.lang3.StringUtils.*;

@SuppressWarnings("unchecked")
/* This class houses common constants and common util methods */
public final class CommonConstants {

    public static final String APPLICATION_PROPERTIES = "applicationProperties";
    public static final String EN_US_TAG = "en-US";

    public static final String US_CATALOG_ID = "apple-us-en";
    public static final int CURRENCY_CODE_LENGTH = 3;

    public static final String EMPLOYEE_GROUP_ID = "employeeGroupId";
    public static final String DISABLE_CART_TOTAL_MODIFIED_POP_UP = "disableCartTotalModifiedPopUp";

    public static final String VAR_CHASE = "Chase";
    public static final String VAR_DELTA = "Delta";
    public static final String VAR_VITALITY = "Vitality";
    public static final String VAR_VITALITYUS = "VitalityUS";
    public static final String VAR_VITALITYCA = "VitalityCA";
    public static final String VAR_RBC = "RBC";
    public static final String VAR_USM = "USM";
    public static final String VAR_BSWIFT = "BSWIFT";
    public static final String VAR_SCOTIA = "SCOTIA";
    public static final String VAR_CITI= "CITI";
    public static final String VAR_WELLSFARGO = "WF";
    public static final String VAR_WEIGHTWATCHERS = "WeightWatchers";
    public static final String VAR_UA = "UA";
    public static final String VAR_GRASSROOTSUK = "GrassRootsUK";
    public static final String VAR_AMEX_AU = "AmexAU";
    public static final String VAR_VIRGIN_AU = "VirginAU";
    public static final String VAR_PNC = "PNC";
    public static final String VAR_FDR = "FDR";
    public static final String VAR_FDR_PSCU = "FDR_PSCU";
    public static final String VAR_FSV = "FSV";
    public static final String PROGRAM_DRP = "DRP";
    public static final String LOCALE_EN_US = "en_US";
    public static final String LOCALE_EN_CA = "en_CA";
    public static final String LOCALE_EN_GB = "en_GB";
    public static final String LOCALE_EN_AU = "en_AU";
    public static final String LOCALE_EN_ZA = "en_ZA";
    public static final String LOCALE_RU_RU = "ru_RU";
    public static final String LOCALE_FR_CH = "fr_CH";
    public static final String LOCALE_NL_NL = "nl_NL";
    public static final String NEW_LINE_REGEX = "(\\r|\\n|\\r\\n)+";
    public static final String USER_SESSION_OBJECT = "USER";
    public static final String PROGRAM_SESSION_OBJECT = "PROGRAM";
    public static final String MAINTENANCE_MESSAGE_SESSION_OBJECT = "MAINTENANCE-MESSAGE";
    public static final String XSRF_TOKEN_SESSION_KEY = "XSRF-TOKEN";
    public static final String XSRF_TOKEN_REQUEST_HEADER_KEY = "X-XSRF-TOKEN";
    public static final String NUM_PAY_PERIODS = "NUM_PAY_PERIODS";
    public static final String EXPERIENCE_DRP = "DRP";
    public static final String INSTALLMENT_PAYMENT_TYPE = "INSTALLMENT";

    public static final String POPULATE_USER_INFO = "populateUserInfo";

    public static final String CART_COUNT_ENDPOINT = "cartCountEndpoint";
    public static final String VAR_ID = "varid";
    public static final String VAR_ID_CAMEL_CASE = "varId";
    public static final String LOCAL_VAR_ORDER_ID_PREFIX = "VAR";
    public static final String COUNTRY_CODE_CAMEL_CASE = "countryCode";
    public static final String LANGUAGE_CODE_CAMEL_CASE = "languageCode";
    public static final String JAVA_SESSION_ID = "jsessionid";
    public static final String EPP = "epp";
    public static final String PAY_PERIODS = "payPeriods";
    public static final String PAY_DURATION = "payDuration";
    public static final String PAY_FREQUENCY = "payFrequency";
    public static final String PAY_FREQUENCY_VALUE = "payFrequencyValue";
    public static final String SHOW_EXACT_PAY_PER_PERIOD = "showExactPayPerPeriod";
    public static final String AUTH_TYPE = "authType";
    // For VOM integration
    public static final String SID = "sid";
    public static final String CSID = "csid";
    public static final String SESSIONID = "sessionId";
    public static final String ACCOUNT_SESSIONID = "accountSessionId";
    public static final String HEADER_IMAGE_URL = "headerImageUrl";
    public static final String SITE = "site";
    public static final String CLIENT_CODE = "clientcode";
    public static final String CLIENT_CODE_CAMEL_CASE = "ClientCode";
    public static final String USER_ID = "userid";
    public static final String USER_PD = "pword";
    public static final String EMAIL_LOWER_CASE = "email";
    public static final String USER_ID_CAMEL_CASE = "userId";
    public static final String ORDER_DATE_CAMEL_CASE = "orderDate";
    public static final String PROGRAM_ID_NON_CAMEL_CASE = "programid";
    public static final String PROGRAM_GROUP = "programgroup";
    public static final String ACCOUNT_ID = "accountid";
    public static final String SUPPLIER_ID = "supplierid";
    public static final String SUPPLIER_NAME = "suppliername";
    public static final String SHIP_DESC = "shipdesc";
    public static final String B2S_ITEM_PROFIT_PRICE = "b2sitemprofitprice";
    public static final String VAR_ITEM_PROFIT_PRICE = "varitemprofitprice";
    public static final String B2S_SHIPPING_PROFIT_PRICE = "b2sshippingprofitprice";
    public static final String VAR_SHIPPING_PROFIT_PRICE = "varshippingprofitprice";
    public static final String VAR_ITEM_MARGIN = "varitemmargin";
    public static final String USM_VAR_ITEM_MARGIN = "varmargin";
    public static final String VIS_ADD_INFO_ECN = "ecn";
    public static final String VIS_BOOKING_CHANNEL = "bookingChannel";
    public static final String VIS_CURRENCY = "currency";
    public static final String VIS_PRODUCT_TYPE_MERCHANDISE = "MERCHANDISE";
    public static final String VIS_PRODUCT_TYPE_SERVICEPLAN = "SERVICE_PLAN";
    public static final String VIS_PRODUCT_TYPE_GIFTCARD = "GIFT_CARD";
    public static final String VIS_ALTERNATE_ADDRESS_INDICATOR = "alternateAddressIndicator";
    public static final String VIS_ACCOUNTS_SERVICE_URL = "VIS_ACCOUNTS_SERVICE_URL";
    public static final String VIS_ADDTNL_INFO_KEY_COUNTRY_CODE = "countryCode";
    public static final String VIS_ADDTNL_INFO_KEY_LANGUAGE_CODE = "languageCode";
    public static final String VIS_ADDTNL_INFO_KEY_IS_DEFAULT_ADDRESS = "isDefaultAddress";
    public static final String VIS_AUTHORIZED_USER_FIRST_NAME = "authorizedUserFirstName";
    public static final String VIS_AUTHORIZED_USER_LAST_NAME = "authorizedUserLastName";
    public static final String OWNER_TYPE = "ownerType";
    public static final String AUTHORIZED = "Authorized";
    public static final String UTF8_ENCODING = "UTF-8";
    public static final String AES = "AES";
    public static final String AES_INSTANCE = "AES/CBC/PKCS5PADDING";
    public static final String ERROR = "error";
    public static final int SUPPLIER_TYPE_USM = 0;
    public static final int SUPPLIER_TYPE_MERC = 200;
    public static final int SUPPLIER_TYPE_SERVICE_PLAN = 50000;
    public static final int SUPPLIER_TYPE_MERC_GENERAL = 200;
    public static final int SUPPLER_TYPE_TRAVEL = 20;
    public static final int SUPPLIER_TYPE_EVENT = 30;
    public static final int SUPPLIER_TYPE_ADV = 40;
    public static final int SUPPLIER_TYPE_ACT = 41;
    public static final String APEX_HEADER_PARTNERCODE = "apex.header.partnercode";
    @Deprecated
    public static final int SUPPLIER_TYPE_AGC = 50;
    public static final int SUPPLIER_TYPE_MERC_UK = 50;
    public static final int SUPPLIER_CUSTOM = 10000;
    public static final int SUPPLIER_LOCAL_GENERIC_STORE = 10100;
    public static final int SUPPLIER_TYPE_EVENT_NEW = 10030;
    public static final int SUPPLIER_TYPE_GAD = 10040;
    public static final int SUPPLIER_TYPE_CA_MERC = 10043;
    public static final int SUPPLIER_TYPE_HOTEL = 10060;
    public static final int SUPPLIER_TYPE_HOTEL1 = 10061;
    public static final int SUPPLIER_TYPE_CAR = 10065;
    public static final int SUPPLIER_TYPE_DISNEY = 10070;
    public static final int SUPPLIER_TYPE_VIATOR = 10080;
    public static final int SUPPLIER_TYPE_FLIGHT = 10086;
    public static final int SUPPLIER_TYPE_EVENT_WY = 10088;
    public static final int SUPPLIER_TYPE_GOLF = 10090;
    public static final int SUPPLIER_TYPE_MI = 12000;
    public static final int SUPPLIER_TYPE_GIFTCARD = 600;
    public static final int SUPPLIER_TYPE_CREDIT = 20000;
    public static final int SUPPLIER_TYPE_DISCOUNTCODE = 30000;
    public static final int SUPPLIER_TYPE_PAYROLLDEDUCTION = 30002;
    public static final int SUPPLIER_ID_NGC = 10095;
    public static final int SUPPLIER_CATEGORY_REWARDS_ON_SALE = 12143;
    public static final int SUPPLIER_TYPE_AMP = 40000;
    public static final String CAROUSEL_RECENTLY_VIEWED_MAX_COUNT = "carousel.recentlyViewed.maxCount";
    public static final String CAROUSEL_AFFORDABILITY_MAX_COUNT = "carousel.affordability.maxCount";
    public static final String DEVICE_EXPERIENCE = "DEVICEEXPERIENCE";

    @Deprecated
    public static final int SUPPLIER_TYPE_DIGITAL = 700;
    public static final int SUPPLIER_TYPE_FEE = 20050;
    public static final int SUPPLIER_LOCAL_MEMORABILIA_STORE = 20100;
    public static final String SUPPLIER_TYPE_USM_S = "0";
    public static final String SUPPLIER_TYPE_MERC_S = "200";
    public static final String SUPPLER_TYPE_TRAVEL_S = "20";
    public static final String SUPPLIER_TYPE_EVENT_S = "30";
    public static final String SUPPLIER_TYPE_ADV_S = "40";
    public static final String SUPPLIER_TYPE_MERC_GENERAL_S = "200";
    public static final String SUPPLIER_TYPE_GIFTCARD_S = "600";
    @Deprecated
    public static final String SUPPLIER_TYPE_DIGITAL_S = "700";
    public static final String SUPPLIER_TYPE_EVENT_NEW_S = "10030";
    public static final String SUPPLIER_TYPE_GAD_S = "10040";
    public static final String SUPPLIER_TYPE_CA_MERC_S = "10043";
    public static final String SUPPLIER_TYPE_HOTEL_S = "10060";
    public static final String SUPPLIER_TYPE_HOTEL1_S = "10061";
    public static final String SUPPLIER_TYPE_CAR_S = "10065";
    public static final String SUPPLIER_TYPE_DISNEY_S = "10070";
    public static final String SUPPLIER_TYPE_VIATOR_S = "10080";
    public static final String SUPPLIER_TYPE_FLIGHT_S = "10086";
    public static final String SUPPLIER_TYPE_GOLF_S = "10090";
    public static final String SUPPLIER_LOCAL_GENERIC_STORE_S = "10100";
    public static final String SUPPLIER_TYPE_MI_S = "12000";
    public static final String SUPPLIER_TYPE_AGC_S = "12002";
    public static final String SUPPLIER_TYPE_CREDIT_S = "20000";
    public static final String SUPPLIER_TYPE_DISCOUNTCODE_S = "30000";
    public static final String SUPPLIER_TYPE_PAYROLLDEDUCTION_S = "30002";
    public static final String SUPPLIER_TYPE_AMP_S = "40000";
    public static final String SUPPLIER_TYPE_SERVICE_PLAN_S = "50000";
    public static final String SUPPLIER_TYPE_FEE_S = "20050";
    @Deprecated
    public static final String SUPPLIER_TYPE_MERC_STR = "Amazon";
    public static final String SUPPLIER_TYPE_TRAVEL_STR = "Travel";
    public static final String SUPPLIER_TYPE_EVENT_STR = "Tickets";
    public static final String SUPPLIER_TYPE_ADV_STR = "Adventure";
    public static final String SUPPLIER_TYPE_CA_MERC_STR = "Canadian Merchandise";
    public static final String SUPPLIER_TYPE_GOLF_STR = "Golf";
    public static final String SUPPLIER_TYPE_GAD_STR = "GAD";
    public static final String SUPPLIER_TYPE_LAND_STR = "Landing";
    public static final String SUPPLIER_TYPE_DISNEY_STR = "Disney";
    public static final String SUPPLIER_TYPE_VIATOR_STR = "Activites";
    public static final String SUPPLIER_TYPE_MI_STR = "Market Innovators";
    public static final String SUPPLIER_TYPE_GIFTCARD_STR = "GiftCards";
    public static final String SUPPLIER_TYPE_GIFT_CARDS_STR = "gift-cards";
    @Deprecated
    public static final String SUPPLIER_TYPE_DIGITAL_STR = "Digital";
    public static final String CAT_CREDIT_STR = "CREDIT";
    public static final String CAT_DISCOUNTCODE_STR = "DISCOUNTCODE";
    public static final String CAT_PAYROLLDEDUCTION_STR = "PAYROLL_DEDUCTION";
    public static final String ENGRAVE_DISABLED = "engraveDisabled";
    public static final int CART_SIZE = 9;
    public static final String CUSTOM_CART = "CUSTOMCART";
    /**
     * Order Status
     */
    public static final int ORDER_STATUS_STARTED = -2;
    public static final int ORDER_STATUS_PROCESSING = 0;
    public static final int ORDER_STATUS_ORDERED = 1;
    public static final int ORDER_STATUS_SHIPPED = 2;
    public static final int ORDER_STATUS_COMPLETED = 3;
    public static final int ORDER_STATUS_RETURN_PENDING = 5;
    public static final int ORDER_STATUS_RETURNED = 6;
    public static final int ORDER_STATUS_CANCELLED = 7;
    public static final int ORDER_STATUS_PROCCESSED = 8;
    public static final int ORDER_STATUS_NLA = 9;
    public static final int ORDER_STATUS_ORDER_PENDING = 10;
    public static final int ORDER_STATUS_DEMO = 11;
    public static final int ORDER_STATUS_REPROCESS = 12;
    public static final int ORDER_STATUS_PARTIAL_REPROCESS = 16;
    public static final int ORDER_STATUS_COMPLETED_BAD_ADDR = 14;
    public static final int ORDER_STATUS_COMPLETED_REFUSED = 15;
    public static final int ORDER_STATUS_BACK_ORDERED = 18;
    public static final int ORDER_STATUS_PARTIAL_REFUND = 19;
    public static final int ORDER_STATUS_DELIVERY_DATE_UPDATED = 20;
    public static final int ORDER_LINE_ADJUSTMENT = 21;
    public static final int ORDER_STATUS_COMPLETED_UNSHIPABLE = 22;
    public static final int ORDER_STATUS_BACKORDER_REVIEW = 23;
    public static final int ORDER_STATUS_ORDERED_INSTORE_PICKUP_AVAILABLE = 24;
    public static final int ORDER_STATUS_COMPLETED_RETURN_STR_CRDT = 25;
    public static final int ORDER_STATUS_RSL_REQUESTED = 26;
    public static final int ORDER_STATUS_RSL_FORWARDED = 27;
    public static final int ORDER_STATUS_RSL_USED = 28;
    public static final int ORDER_STATUS_RSL_VENDOR_RECD_ITEM = 29;
    public static final int ORDER_STATUS_RSL_PENDING_VENDOR_RFND = 30;
    public static final int ORDER_STATUS_COMPLETED_RSL_EXPIRED = 31;
    public static final int ORDER_STATUS_COMPLETED_RTNW_CLOSED = 32;
    public static final int ORDER_STATUS_COMPLETED_CONVEN_RTN = 33;
    public static final int ORDER_STATUS_COMPLETED_NOT_RECD = 34;
    public static final int ORDER_STATUS_COMPLETED_RTN_DAMAGED = 35;
    public static final int ORDER_STATUS_COMPLETED_RTN_WRONG_ITEM = 36;
    public static final int ORDER_STATUS_COMPLETED_GC_NOT_RECD = 37;
    public static final int ORDER_STATUS_COMPLETED_GC_RESHIP_REQ = 38;
    public static final int ORDER_STATUS_COMPLETED_POFP_FWRD = 39;
    public static final int ORDER_STATUS_TICKETING = 40;  //Book fight success, ticketing.
    public static final int ORDER_STATUS_LOST_STOLEN = 44;  //LostStolen
    public static final int ORDER_STATUS_FRAUD = 98;
    public static final int ORDER_STATUS_FAILED = 99;
    public static final int ORDER_STATUS_KILLED = 97;
    public static final int ORDER_STATUS_ON_HOLD = 42;
    public static final int ORDER_STATUS_RESUBMIT = 999;
    /**
     * Order Status Descriptions
     */
    public static final String ORDER_STATUS_PROCESSING_DESC = "Processing";
    public static final String ORDER_STATUS_ORDERED_DESC = "Ordered";
    public static final String ORDER_STATUS_SHIPPED_DESC = "Shipped";
    public static final String ORDER_STATUS_COMPLETED_DESC = "Completed";
    public static final String ORDER_STATUS_RETURN_PENDING_DESC = "ReturnRequested";
    public static final String ORDER_STATUS_RETURNED_DESC = "Cmplt-Returned";
    public static final String ORDER_STATUS_CANCELLED_DESC = "Cmplt-Cancelled";
    public static final String ORDER_STATUS_PROCCESSED_DESC = "Processed";
    public static final String ORDER_STATUS_NLA_DESC = "Cmplt-NLA";
    public static final String ORDER_STATUS_BADADDR_DESC = "Cmplt-BadAddr";
    public static final String ORDER_STATUS_REFUSED_DESC = "Cmplt-Refused";
    public static final String ORDER_STATUS_UNSHIPABLE_DESC = "Cmplt-Unshippable";
    public static final String ORDER_STATUS_AMAZON_REJECTED_DESC = "OrderPending";
    public static final String ORDER_STATUS_DEMO_DESC = "Demo";
    public static final String ORDER_STATUS_PARTIAL_REPROCESS_DESC = "Reprocess-Partial";
    public static final String ORDER_STATUS_FAILED_DESC = "Failed";
    public static final String ORDER_STATUS_PARTIAL_REFUND_DESC = "Partial Refund Completed";
    public static final String ORDER_STATUS_BACKORDER_REVIEW_DESC = "Backorder Review";
    public static final String ORDER_STATUS_ORDERED_INSTORE_PICKUP_AVAILABLE_DESC = "Ordered-PickupAvailable";
    public static final String ORDER_STATUS_RSL_REQUESTED_DESC = "RSL Requested";
    public static final String ORDER_STATUS_RSL_FORWARDED_DESC = "RSL Forwarded";
    public static final String ORDER_STATUS_RSL_USED_DESC = "RSL Used";
    public static final String ORDER_STATUS_RSL_VENDOR_RECD_ITEM_DESC = "RSL Vdr Recd Item";
    public static final String ORDER_STATUS_RSL_PENDING_VENDOR_RFND_DESC = "RSL Pnd Vdr Rfnd";
    public static final String ORDER_STATUS_COMPLETED_RSL_EXPIRED_DESC = "Cmplt-RSL Expired";
    public static final String ORDER_STATUS_COMPLETED_RTNW_CLOSED_DESC = "Cmplt-RTNW Closed";
    public static final String ORDER_STATUS_COMPLETED_CONVEN_RTN_DESC = "Cmplt-Conven Rtn";
    public static final String ORDER_STATUS_COMPLETED_NOT_RECD_DESC = "Cmplt-Not Recd";
    public static final String ORDER_STATUS_COMPLETED_RTN_DAMAGED_DESC = "Cmplt-Rtn Damaged";
    public static final String ORDER_STATUS_COMPLETED_RTN_WRONG_ITEM_DESC = "Cmplt-Rtn Wrong Item";
    public static final String ORDER_STATUS_COMPLETED_GC_NOT_RECD_DESC = "Cmplt-GC Not Recd";
    public static final String ORDER_STATUS_COMPLETED_GC_RESHIP_REQ_DESC = "Cmplt-GC Reship Req";
    public static final String ORDER_STATUS_COMPLETED_POFP_FWRD_DESC = "Cmplt-PofP Fwrd";
    public static final String ORDER_STATUS_UA_CANCELLED = "Cancelled";
    public static final String ORDER_STATUS_UA_INVALID_ADDRESS = "Invalid Shipping Address";
    public static final String ORDER_STATUS_UA_ITEM_NOT_AVAILABLE = "Item No Longer Available";
    public static final String ORDER_STATUS_UA_RETURNED = "Returned";
    public static final String LINK_TYPE_MAIN = "M";
    public static final String LINK_TYPE_SMALL = "S";
    public static final String LINK_TYPE_LEFT_NAV = "L";
    public static final double TRANSACTION_CHARGE_RATE = 0.0225;
    public static final String SESSION_STATES = "SESS_STATES";
    public static final String TOKEN_ORDERID = "<%orderid%>";
    public static final String USER_DONT_PAY_SHIPPING_AND_TAX = "isUserDontPayShippingAndTax";
    public static final String SYSTEM_MAINTENANCE_MODE = "SYSTEM_MAINTENANCE_MODE";
    public static final String SYSTEM_MAINTENANCE_NOTICE = "SYSTEM_MAINTENANCE_NOTICE";
    public static final String CLIENT_CHASE_BANK = "1";
    public static final String ADDITIONAL_PAGE_TERMS = "TNC";
    public static final String ADDITIONAL_PAGE_TOP_LEVEL = "TL";
    public static final String SHOW_DOLLARS = "showDollars";
    public static final String SHOW_VAR_ORDER_ID = "showVarOrderId";
    public static final String SHOW_TAX_DISCLAIMER = "showTaxDisclaimer";
    public static final String SHOW_FEE_DETAILS = "showFeeDetails";
    public static final String PRICING_TEMPLATE = "pricingTemplate";
    public static final String POINTS_DECIMAL = "points_decimal";
    public static final String SHOW_EMAIL_DISCLAIMER = "showEmailDisclaimer";
    public static final String SHOW_SPAM_FILTER_NOTIFICATION = "showSpamFilterNotification";
    public static final String SHOW_PRODUCT_SPEC_TAB = "showProductSpecTab";
    public static final String SHOW_CATALOG_HOME_CRUMB = "showCatalogHomeCrumb";
    public static final String HIDE_LOGGED_IN_AS = "hideLoggedInAs";
    public static final String DEFAULT_PS_PROGRAM = "defaultPSprogram";
    public static final String DEFAULT_PS_VAR = "defaultPSvar";
    public static final String MAX_CART_SIZE_CONFIG_KEY = "maxCartSize";
    public static final String IGNORE_PROFILE_ADDRESS = "ignoreProfileAddress";
    public static final String ORDER_LINE_ATTR_KEY_VAR_ORDER_LINE_ID = "varOrderLineId";
    public static final String IS_LOCAL_KEY = "isLocal";
    public static final String BEAN_ID_PROD_SVC_DEFAULT_CONFIG = "productServiceDefaultConfig";
    public static final String CONFIG_CATALOG_ID = "catalog_id";
    public static final String SHIP_TO_NAME_LOCKED = "ShipToNameLocked";
    public static final String PRODUCT_CATEGORY_CREDIT_CARD = "CreditCard";
    public static final String CSR_USER_ROLE_VALUE = "Z";
    public static final String TRAVEL_AGENT_ROLE_VALUE = "A";
    public static final String YES_VALUE = "Y";
    public static final String NO_VALUE = "N";
    public static final String NULL_VALUE = "NULL";
    public static final String COUNTRY_CODE_DEFAULT = "DEFAULT";
    public static final String COUNTRY_CODE_CA = "CA";
    public static final String COUNTRY_CODE_US = "US";
    public static final String COUNTRY_CODE_GB = "GB";
    public static final String COUNTRY_CODE_SG = "SG";
    public static final String COUNTRY_CODE_PH = "PH";
    public static final String COUNTRY_CODE_HK = "HK";
    public static final String COUNTRY_CODE_TH = "TH";
    public static final String COUNTRY_CODE_MX = "MX";
    public static final String COUNTRY_CODE_AU = "AU";
    public static final String COUNTRY_CODE_NL = "NL";
    public static final String COUNTRY_CODE_TW = "TW";
    public static final String COUNTRY_CODE_MY = "MY";
    public static final String COUNTRY_CODE_AE = "AE";
    public static final String COUNTRY_CODE_BH = "BH";
    public static final String COUNTRY_CODE_ZA = "ZA";
    public static final String COUNTRY_CODE_RU = "RU";
    public static final String COUNTRY_CODE_FR = "FR";
    public static final String COUNTRY_CODE_CH = "CH";
    public static final String DEFAULT_CA_STATE = "AB";
    public static final String DEFAULT_US_STATE = "GA";
    public static final String DEFAULT_US_ZIP = "30004";
    public static final String DEFAULT_LANGUAGE_CODE = "en";
    public static final String REQUEST_ATTR_COUNTRY = "country";
    public static final String REQUEST_ATTR_LANG = "lang";
    public static final String LOCALE = "locale";
    public static final String DOT = ".";
    public static final String DOT_ENDPOINT = ".endpoint";
    public static final String ENDPOINT = "EndPoint";
    public static final String PROGRAM_TYPE = "programType";
    public static final String SMB_PROGRAM = "smb";
    public static final String PARTNER_TIME_OUT_URL = "partnerTimeOutUrl";
    public static final String PARTNER_SIGN_OUT_URL = "partnerSignOutUrl";

    public static final String ANALYTICS_MATOMO_SITEID = "matomoSiteId";
    public static final String MATOMO_SITE_ID = "matomo.siteId";
    public static final String ANALYTICS_HEAP_APPID = "heapAppId";
    public static final String HEAP_APP_ID = "heap.appId";
    public static final String HEAP_API_ENDPOINT = "heap.api.endpoint";
    /**
     * SYMPRJ-931, All Travelocity orders should set orders.app_version field to 6
     */
    public static final Integer APP_VERSION = 6;
    public static final int MERCHANDISE_QUANTITY_RESTRICTION = 99;
    public static final int MAX_ITEM_PAGE = 400;
    public static final String DEFAULT_VAR_ID = "1";
    public static final String DEFAULT_PROGRAM_ID = "apple_qa";
    public static final String POINT_CURRENCY_STRING = "PNT";
    //Apple related
    public static final int APPLE_SUPPLIER_ID = 200;
    public static final String APPLE_SUPPLIER_ID_STRING = "200";
    public static final String APPLE_MERCHANT_ID = "30001";
    public static final int AMP_SUPPLIER_ID = 40000;
    public static final String AMP_SUPPLIER_ID_STRING = "40000";
    public static final String QUANTITY = "quantity";
    public static final String APPLE_CART_SESSION_OBJECT = "APPLE_CART";
    public static final String ORDER_ID_SESSION_OBJECT = "ORDER_ID";
    public static final String ORDER_ID_FOR_AUTOMATION_TEST = "ORDER_ID_FOR_AUTOMATION_TEST";
    public static final String APPLE_BANNER_NAME = "apple_banner";
    public static final String RESTRICTED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX = "[~*\"\\\\<>]+";
    public static final String CHAR_ONLY_REG_EX = "[A-Za-z]+";
    public static final String CHAR_AND_SPACE_ONLY_REG_EX = "[A-Za-z'. ]{2,}";
    public static final String CHAR_SPACE_AND_DASH_ONLY_REG_EX = "[-A-Za-z'. ]{2,}";
    public static final String CHAR_WITH_ACCENT_ONLY_REG_EX = "[A-Za-zàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð]+";
    public static final String RECEPIENT_NAME_REG_EX = "[A-Za-zàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð,'.\\- ]{1,}";
    public static final String CITY_CA_FRENCH_REG_EX = "[-A-Za-z .'-éàèùâêîôûçëïüÉÀÈÙÂÊÎÔÛÇËÏÜ]+";
    public static final String PHONE_NUM_REG_EX = "^[0-9]{3}[2-9]{1}[0-9]{6}";
    public static final String REPEATED_PHONE_NUM_REG_EX = "\\d*?(\\d)\\1{9,}\\d*";
    public static final String MERC_ADDRESS_LOCKED = "MercAddressLocked";
    public static final String SINGLE_ITEM_PURCHASE = "SingleItemPurchase";
    public static final String MAX_PURCHASE_AMOUNT = "MaxPurchaseAmount";
    public static final String EXCLUDE = "EXCLUDE";
    public static final String PSID = "PSID";
    public static final String CATEGORY = "CATEGORY";
    public static final String ITEM_ID = "itemId";
    public static final String PRODUCT = "product";
    public static final String SUPPLIER = "supplier";
    public static final String SHOP = "shop";
    public static final String PAGE = "page";
    public static final String ORDER_HISTORY_PAGE = "orderHistory";
    public static final String ORDER_HISTORY_URL = "orderHistoryUrl";
    public static final String OPTIONS_JOIN_SEPARATOR = "|";
    public static final String ORDER_PRODUCT_OPTIONS_SPLIT_SEPARATOR = "\\|";
    public static final String BAG = "bag";
    public static final String SHOW_ONLY_ON_EMPTY_CART = "showOnlyOnEmptyCart";
    public static final String APPLE = "apple";
    public static final String DEFAULT_CATALOG_ID = "-1";
    public static final String DEFAULT_LOCALE = "-1";
    public static final String DEFAULT_VAR_PROGRAM = "-1";
    public static final String DEFAULT_PROGRAM_KEY = "default";
    public static final int RECEPIENT_FULL_NAME_LENGTH_MAX = 34;
    public static final int RECEPIENT_FIRST_LAST_NAME_LENGTH_MAX = 17;
    public static final int VITALITY_MAX_MONTH_TERM = 24;
    public static final String PROCESSING_FEE_RATE = "processingFeeRate";
    public static final String CC_VAR_MARGIN = "ccVarMargin";
    public static final String MAX_MONTHLY_PAYMENT = "MaxMonthlyPayment";
    public static final String ACTIVATION_FEE = "ActivationFee";
    public static final String ACTIVATION_FEE_38MM = "ActivationFee38mm";
    public static final String ACTIVATION_FEE_42MM = "ActivationFee42mm";
    public static final String PRICING_MODEL = "PricingModel";
    public static final String PRICING_ID = "pricingId";
    public static final String PROGRAM_ID = "programId";
    public static final String PRICE_TYPE_USER_COST = "UserCost";
    public static final String UPGRADE_FEE = "upgradeFee";
    public static final String EMPLOYER = "EMPLOYER";
    public static final String MONTHS = "months";
    public static final String APPLEWATCH = "APPLEWATCH";
    public static final String ORDER_REFERENCE = "OrderReference";
    public static final String SKU = "SKU";
    public static final String TENANT_ID = "tenantId";
    public static final String EMPLOYER_ID = "employerId";
    public static final String EMPLOYER_NAME = "employerName";
    public static final String CLIENT_ID = "clientId";
    public static final String EMAIL_INCLUDE_EMPLOYER_INFO_KEY = "email.include.employerInfo";
    public static final String FRAUD_SESSION_ID = "fraudSessionID";
    public static final String BANK_TRANSACTION_ID = "bankTransactionID";
    public static final String KOUNT_SERVER_URL_KEY = "kount.server.url";
    public static final String KOUNT_SERVER_UA_MERCHANT_ID_KEY = "kount.server.ua.merchant.id";
    public static final String SHOP_NAME_KEY = "shop_name";
    public static final String BRAND_LIST_KEY = "brandsFilter";
    //TODO: References from CORE ShoppingCartService moved here. Remove this once CoreService ShoppingCart &
    // ShoppingCartItems are removed
    public static final String SHOPPING_CART = "shoppingCart";
    public static final String ENGRAVABLE = "engravable";
    public static final String SEA_NUMBER = "seaNumber";
    public static final String PRICE_OVERRIDE_CODE = "priceOverrideCode";
    public static final String SUPPLIER_DISCOUNT_ITEM_PRICE = "supplierDiscountItemPrice";
    public static final String PROMOTIONAL_OFFER = "promotionalOffer";
    public static final String POINTS = "POINTS";
    public static final String VARIABLE = "VARIABLE";
    public static final String ORDER_LINE_ATTR_KEY_INVERSE_RATE = "inverseRate";
    public static final String PROGRAM_CONFIG_KEY_SEND_INVERSE_RATE = "sendInverseRate";
    public static final String ESTIMATED_MAX_TAX_RATE = "estimatedMaxTaxRate";
    public static final String SHOW_FROM_PRICE = "showFromPrice";
    public static final String SHOW_CASE_SIZE_FROM_PRICE = "showCaseSizeFromPrice";
    public static final String SPARK_POST_TRANSMISSIONS_URL_KEY = "sparkpost.transmissions.url";
    public static final String SPARK_POST_AUTHORIZATION_CODE_KEY = "sparkpost.authorization.code";
    public static final String SPARK_BCC_MAILBOX = "sparkpost.bcc.mailbox";
    public static final String SPARK_ENVIRONMENT = "sparkpost.env";
    public static final String SPARK_ENVIRONMENT_LOWER = "lower";
    public static final String SPARK_ENVIRONMENT_UAT= "uat";
    public static final String SUBSCRIPTION_URL= "subscriptionUrl";
    public static final String REMAINING_COUNT= "remainingCount";
    public static final String EXPIRATION_DATE_TIME= "expirationDateTime";

    //Spark notification msg available
    public static final String SPARK_NOTIFICATION_MSG_AVAILABLE = "notificationMessageAvailable";
    public static final String SPARK_NOTIFICATION_COMMENTS = "notificationComments";
    public static final String SPARK_NOTIFICATION_TC = "notificationTermsAndConditions";
    public static final String SPARK_NOTIFICATION_CUST_SUPPORT = "notificationCustomerSupport";
    public static final String HTTP_CONNECTION_TIMEOUT = "http.connection.timeout";
    public static final String APPLICATION_JSON = "application/json";
    public static final String HTTP_HEADER_ACCPT = "Accept";
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
    public static final String TERMS_AND_CONDITIONS_BASE_PATH = "document.termsAndConditions.base";
    public static final String TERMS_OF_USE_BASE_PATH = "document.termsOfUse.base";
    public static final String PRIVACY_POLICY_BASE_PATH = "document.privacyPolicy.base";
    public static final String TERMS_OF_USE_URL="lgnTermsOfUseUrl";
    public static final String TERMS_ADDITIONAL_CONTENT="termAdditionalContent";
    public static final String PRIVACY_POLICY_URL="privacyPolicyUrl";
    //Shipment delay
    public static final String ORDER_LINE_ATTRIBUTE_KEY_SHIPS_BY = "ShipsBy";
    public static final String ORDER_LINE_ATTRIBUTE_KEY_DELIVERS_BY = "DeliversBy";
    public static final String ORDER_LINE_ATTRIBUTE_KEY_SEND_SHIPMENT_DELAY_NOTIFICATION = "SendShipmentDelayNotification";
    //PPC
    public static final String PPC_CART_ID_ORDER_ATTRIBUTE_KEY = "ppcCartId";
    public static final String PPC_ACCESS_TOKEN_ORDER_ATTRIBUTE_KEY = "ppcAccessToken";
    public static final String PPC_TAX = "ppcTax";
    public static final String PPC_FEE = "ppcFee";
    public static final String PPC_CURRENCY_TYPE = "ppcCurrencyType";
    public static final String PPC_PAY_PER_PERIOD = "ppcPayPerPeriod";
    public static final String PPC_PAY_PERIODS = "ppcPayPeriods";
    public static final String PPC_PAY_DURATION = "ppcPayDuration";
    //Discount code
    public static final String DISCOUNTCODE_LENGTH_MIN_LIMIT_KEY = "discountcode.length.min.limit";
    public static final String DISCOUNTCODE_LENGTH_MAX_LIMIT_KEY = "discountcode.length.max.limit";
    public static final String DISCOUNTCODE_PER_ORDER = "discountCodePerOrder";
    public static final String DISCOUNTCODE_KEY = "discountCode";
    public static final String DISCOUNTCODE_QUERY_PARAM_OTP = "otp.queryparam.discountcode";
    //Anonymouse purchase
    public static final String ANONYMOUS_FLAG = "anon";
    public static final String ANONYMOUS_USER_ID = "Anonymous";
    public static final String ANONYMOUS_PROGRAM_ID = "Anonymous";
    public static final String ANONYMOUS_PURCHASE = "anonymousPurchase";
    public static final String ANONYMOUS_VAR_ID_REQ_PARAM = "v";
    public static final String ANONYMOUS_PROGRAM_ID_REQ_PARAM = "p";
    public static final String ANONYMOUS_LOCALE_REQ_PARAM = "l";
    public static final String ANONYMOUS_DISCOUNT_CODE_REQ_PARAM = "c";
    public static final String ANONYMOUS_UID_CODE_REQ_PARAM = "uid";

    //Pages
    public static final String LOGIN_5_BOX_PAGE = "login.jsp";
    public static final String LOGIN_OTP_PAGE = "OtpLogin.jsp";

    // Image
    public static final String IMAGE_SERVER_BUILD_NUMBER = "imageServerBuildNumber";
    public static final String IMAGE_SERVER_KEY = "imageServer";
    public static final String IMAGE_SERVER_URL_CONFIG ="imageServerUrl";
    public static final String IMAGE_SERVER_URL_KEY = "image.server.url";
    public static final String IMAGE_SERVER_VIP_URL_KEY = "image.server.vip.url";
    public static final String PROGRAM_LOGO_URL = "programLogoUrl";
    public static final String SIGNIN_URL_KEY = "signinUrl";
    public static final String EMAIL_DATE_FORMAT = "MMM dd, YYYY hh:mm a z";

    //Five9
    public static final String FIVE9_CONFIG = "five9Config";
    public static final String FIVE9_TYPE_KEY = "five9.chat.vcc.type";
    public static final String FIVE9_ROOT_URL_KEY = "five9.chat.vcc.root.url";
    public static final String FIVE9_TENANT_KEY = "five9.chat.vcc.tenant";
    public static final String FIVE9_PROFILE_KEY = "five9.chat.vcc.profile";
    public static final String FIVE9_TITLE_KEY = "chatWindowName";
    public static final String FIVE9_TITLE_DEFAULT = "Customer Service Chat";
    public static final String ENABLE_FIVE9_CHAT = "enable_chat";

    //Chase Apple SSO - Analytics
    public static final String CHASE_SSO_ROOT_URL_KEY = "chase.sso.root.url";
    public static final String CHASE_SSO_ROOT_URL = "chaseSsoRootUrl";
    public static final String CHASE_ANALYTICS_ROOT_URL_KEY = "chase.analytics.root.url";
    public static final String CHASE_ANALYTICS_ROOT_URL = "chaseAnalyticsRootUrl";

    public static final String RETAIL_UNIT_BASE_PRICE = "retailUnitBasePrice";
    public static final String RETAIL_UNIT_TAX_PRICE = "retailUnitTaxPrice";
    public static final String BRIDGE2_UNIT_BASE_PRICE = "bridge2UnitBasePrice";
    public static final String ORDER_ATTR_KEY_DISPLAY_PRICE_AMOUNT = "displayPrice.amount";
    public static final String ORDER_ATTR_KEY_DISPLAY_TOTAL_PRICE_AMOUNT = "displayTotalPrice.amount";
    public static final String ORDER_ATTR_KEY_DISPLAY_PRICE_POINTS = "displayPrice.points";
    public static final String ORDER_ATTR_KEY_UNPROMOTED_DISPLAY_PRICE_AMOUNT = "unpromotedDisplayPrice.amount";
    public static final String ORDER_ATTR_KEY_UNPROMOTED_DISPLAY_PRICE_POINTS = "unpromotedDisplayPrice.points";
    public static final String DEFAULT_ORDER_HISTORY_DATE_FORMATE = "order.history.date.format";
    public static final String VAR_ID_SCOTIA = "SCOTIA";
    public static final String POINTS_PATTERN = "###,###";
    public static final String SCOTIA_KEEP_ALIVE_URL = "scotia.keepAliveUrl";
    public static final String EMAIL_USE_DRAFT_TEMPLATE = "emailUseDraftTemplate";
    public static final String WW_SIGN_OUT_URL = "weightwatchers.signOutUrl";
    public static final String WW_NAV_BACK_URL = "weightwatchers.navigateBackUrl";
    public static final String PRICING_TIER = "pricingTier";
    public static final String CONVERSION_RATE = "conversionRate";
    public static final String PAYER_ID = "payerId";
    public static final String REQ_PARAM_FIRST_NAME = "firstname";
    public static final String REQ_PARAM_LAST_NAME = "lastname";
    public static final String INVALID_ADDRESS_ERROR_MSG_KEY = "unableToProcessRequestTryLater";
    public static final String INVALID_ADDRESS_ERROR_DEFAULT_MSG = "We are unable to process your request. Please try after some time.";
    public static final String RBC_PROMO_ALREADY_USED_DECLINE_REASON_CODE = "INVALID_REDEMPTION_OPTION";
    public static final String PAYMENT_TYPE_DISCOUNT = "discount";
    public static final String PAYMENT_TYPE_PAYROLL = "payroll";
    public static final String PAYMENT_TYPE_CARD = "CreditCard";
    public static final String WEBAPP_PAYMENTSERVER_SIGNING_KEY = "webapp.paymentserver.signing.key";
    public static final String WEBAPP_PAYMENTSERVER_SIGNING_IV_KEY = "webapp.paymentserver.signing.initVectorkey";
    public static final String WEBAPP_PAYMENTSERVER_SIGNING_ENCRYPT_KEY = "webapp.paymentserver.signing.encryptkey";
    public static final String PAYMENT_REDIRECT_HOME_URL = "redirect.home.url";
    public static final String VITALITYUS_TVGCORPORATE_PROGRAM = "TVGCorporate";
    public static final String REQ_PARAM_FOR_SAML_RESP = "SAMLResponse";
    public static final String OTP_OVERRIDE_EMAIL_DOMAIN_KEY = "otp.override.email.domain";
    public static final String OTP_OVERRIDE_EMAIL_DOMAIN_ENABLE_KEY = "otp.override.email.domain.enable";
    public static final String OTP_LOGIN = "OTPLogin";
    public static final String OTP_NOTIFICATION_TEMPLATE = "epp-otp-en-us";
    public static final String CSP = "csp";
    public static final String CID = "cid";
    public static final String URL = "url";
    public static final String OTP = "otp";
    public static final String SAML = "saml";
    public static final String VIP = "vip";
    public static final String AND = "&";
    public static final String EQUAL = "=";
    public static final String QUESTION = "?";
    public static final String SLASH = "/";
    public static final String HYPHEN = "-";
    public static final String UNDERSCORE = "_";
    public static final String DOMAIN = "domain";
    public static final String LOGIN_TYPE = "loginType";
    public static final String IS_ACTIVE = "isActive";
    public static final String LOGIN_REDIRECT_URL = "redirect:/login.do?";
    public static final String ONLINE_AUTH_CODE = "onlineAuthCode";
    public static final String OFFLINE_AUTH_CODE = "offlineAuthCode";
    public static final String ADDITIONAL_INFO = "additionalInfo";
    public static final String ORDER_ID = "orderId";
    public static final String VAR_ORDER_ID = "varOrderId";
    public static final String PURCHASE_POST_URL = "purchasePostUrl";
    public static final String METHOD = "method";
    public static final String ENGRAVING_LINE_1 = "engravingLine1";
    public static final String ENGRAVING_LINE_2 = "engravingLine2";
    public static final String ENGRAVING_CODE = "engravingCode";
    public static final String CREDIT_ORDER_LINE_TYPE = "CreditCard";
    public static final String CREDIT_CARD_LAST_FOUR_DIGIT = "ccLast4";
    public static final String CREDIT_CARD_TYPE = "ccType";
    public static final String PRICING_TEMPLATE_POINTS_ONLY = "points_only";
    public static final String CREDENTIAL = "credential";
    public static final String BLACKLIST = "blacklist";
    public static final String ORDER_HOLD_DURATION_IN_MINUTES_KEY = "orderHoldDurationInMinutes";
    public static final String ORDER_HOLD_DURATION_IN_DAYS = "orderHoldDurationInDays";
    public static final String LOGIN_TIME = "loginTime";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssz";
    public static final String SESSION_TIMEOUT_MINUTES = "session.timeout.minutes";
    public static final String SESSION_TIMEOUT_WARNING_MINUTES = "session.timeout.warning";
    public static final String WIDGET_TIMEOUT_MINUTES = "widget.timeout.minutes";
    public static final String UA_MPCONNECT_URL_KEY = "ua.mpconnect.url";
    public static final String UA_MPCONNECT_CID_KEY = "ua.mpconnect.cid";
    public static final String UA_SERVICE_ENDPOINT = "ua.service.endpoint";
    public static final String UA_SERVICE_SID = "ua.service.sid";
    public static final String UA_SERVICE_GET_SUBSCRIPTION = "getSubscriptions";
    public static final String UA_SERVICE_UPDATE_SUBSCRIPTION = "updateSubscriptions";
    public static final String UA_SERVICE_SUBSCRIPTION_DISPLAY_CHECKBOX = "subscription.displayCheckbox";
    public static final String UA_SERVICE_SUBSCRIPTION_IS_CHECKED = "subscription.isChecked";
    public static final String UA_SERVICE_PARAM_TRANSACTION_ID = "transactionID=";
    public static final String UA_SERVICE_PARAM_AUTHENTICATION_CODE = "&authenticationCode=";
    public static final String UA_SERVICE_PARAM_OTHERS = "&channelCode=Email&channelTypeCode=Other&channelTypeSequenceNumber=1&communicationProgramCode=ONP";
    public static final String UA_SERVICE_PARAM_COMMUNICATION_CODE = "&communicationTypeCode=";
    public static final String UA_SERVICE_PARAM_DISPLAY_AND_OPT_CODE = "&displayTypeCode=Html&optInOrOutCode=";
    public static final String UA_SUPPORTED_CHANNEL_CODE_KEY = "supportedChannelCodes";
    public static final String DYNAMIC_HEADER_FOOTER_LOAD = "dynamicHeaderFooterLoad";

    public static final String PROFILE_EMAIL_NOTIFICATION = "profileEmailNotification";
    public static final String VAR_ANALYTIC_CODE = "varAnalyticCode";
    public static final String VAR_ANALYTIC_KEY = "varAnalyticKey";
    public static final String DEFAULT_VAR_ANALYTIC_CODE = "ELEC";
    public static final String DEFAULT_VAR_ANALYTIC_KEY = "ELEC";
    public static final String SEND_VAR_ANALYTIC_DATA = "send.var.analytic.data";
    public static final String VIS_MOCK_ENABLED = "visMockEnabled";
    public static final boolean IS_LOCAL_FALSE = Boolean.FALSE;
    public static final String TIME_ZONE_ID = "timeZoneId";
    public static final String TIME_ZONE = "timeZone";
    public static final String SERVER_TIME_ZONE = "US/Eastern";
    public static final String UA_TIMED_OUT_ERROR_TEXT = "CR112: Online authenticationCode parameter is expired";
    public static final String IGNORE_SUGGESTED_ADDRESS = "IgnoreSuggestedAddress";
    public static final String MELISSA_DATA_DEFAULT_OPTIONS = "DeliveryLines:on";
    public static final String[] MELISSA_DATA_AV_ERROR_CODES = {"AV11", "AV12", "AV13", "AV14", "AV15"};
    public static final Map<String, String> COUNTRY_CODES;
    public static final String SHOP_EXPERIENCE = "shopExperience";
    public static final String DISABLE_REQUEST_CONFIRMATION_EMAIL_PAYROLL_DEDUCTION = "disableRequestConfirmationEmailPayRoll";
    public static final String VIRGINAU_SIGNOUTURL  = "virginau.signOutUrl";
    public static final String VIRGINAU_NAVIGATEBACKURL = "virginau.navigateBackUrl";
    public static final String VIRGINAU_HOMELINKURL = "virginau.homeLinkUrl";
    public static final String VIRGINAU_SIGNOUT_REDIRECT= "virginau.signoutRedirectUrl";
    public static final String VIRGINAU = "VirginAU";
    public static final String ID_TOKEN_HINT = "id_token_hint";
    public static final String POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";
    public static final String REWARD_MIN_LIMIT  = "rewardMinLimit";
    public static final String LIMIT_MAX_QUANTITY  = "LimitMaxQuantity";
    public static final String OAUTH_ATTRIBUTES = "OAUTH_ATTRIBUTES";
    public static final String OAUTH_CHECK_SESSION_IFRAME_URL = "OAUTH_CHECK_SESSION_IFRAME_URL";
    public static final String OAUTH_CLIENT_ID = "OAUTH_CLIENT_ID";
    public static final String OAUTH_TOKEN_SESSION_STATE = "OAUTH_TOKEN_SESSION_STATE";
    public static final String AUTHORIZATION_TOKEN = "authorizationToken";

    public static final String VAR_INTEGRATION_SERVICE_LOCAL_IMPL = "varIntegrationServiceLocalImpl";
    public static final String DISABLE_AMP = "disableAMPProducts";
    public static final String DISABLE = "disable-";

    public static final String VIEW_ANONYMOUS_ORDER_DETAIL = "viewAnonymousOrderDetail";

    public static final String SAML_SECURITY_INTENTIONS = "saml.securityIntentions";

    //var program config
    public static final String ENABLE_IFRAME_RESIZER = "enableIframeResizer";

    public static final String ENABLE_SMART_PRICING = "enableSmartPricing";
    public static final String ENABLE_SMART_PRICING_OVERRIDE = "enableSmartPricingOverride";

    //If any country has ignoreProfileAddress as true then we need to remove that country from this list.
    public static final List<String> COUNTRIES_WITH_NO_STATE = Arrays.asList(
        COUNTRY_CODE_TW,
        COUNTRY_CODE_GB,
        COUNTRY_CODE_SG,
        COUNTRY_CODE_HK,
        COUNTRY_CODE_TH,
        COUNTRY_CODE_AE,
        COUNTRY_CODE_PH,
        COUNTRY_CODE_BH,
        COUNTRY_CODE_ZA,
        COUNTRY_CODE_FR,
        COUNTRY_CODE_CH,
        COUNTRY_CODE_NL
    );


    public static final List<String> COUNTRIES_WITH_NO_CITY = Arrays.asList(
        COUNTRY_CODE_SG
    );

    public static final List<String> COUNTRIES_WITH_NO_POSTALCODE = Arrays.asList(
        COUNTRY_CODE_AE
    );

    //Regex special char for address
    public static final String REGEX_SPECIAL_CHAR_ADDRESS = "([);<>\\\\\"])";

    public static final Map<String, String> LOCALE_ALLOWED_CHAR_ADDRESS_REGEX = new HashMap<>();
    public static final String DEFAULT_ALLOWED_CHAR_ADDRESS_REGEX = "[^1234567890 ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&\\(\\)\\_\\-\\+\\=\\|\\}\\]\\{\\[\\:\\;\\?\\/.,`àèìòùÀÈÌÒÙáéíóúýÁÉÍÓÚÝâêîôûÂÊÎÔÛãñõÃÑÕäëïöüÿÄËÏÖÜåÅæÆçÇðøØ¿¡ß]";
    public static final String IDOLOGY_ENABLED = "idologyEnabled";
    public static final String DISABLE_CASH_ONLY_REDEMPTION = "disableCashOnlyRedemption";

    static {
        LOCALE_ALLOWED_CHAR_ADDRESS_REGEX.put(LOCALE_FR_CH, DEFAULT_ALLOWED_CHAR_ADDRESS_REGEX);
        LOCALE_ALLOWED_CHAR_ADDRESS_REGEX.put(LOCALE_EN_AU, DEFAULT_ALLOWED_CHAR_ADDRESS_REGEX);
        LOCALE_ALLOWED_CHAR_ADDRESS_REGEX.put(LOCALE_NL_NL, DEFAULT_ALLOWED_CHAR_ADDRESS_REGEX);
    }

    public static final Pattern REGEX_NOT_NUMERIC = Pattern.compile("[^0-9]");

    public static final String ORDER_API_PROGRAM_TYPE_CORPORATE = "corporate";
    public static final String ORDER_API_PROGRAM_TYPE_PERSONAL = "personal";

    public static final String UA_STATUS = "ua.status.";
    public static final String ORDER_HISTORY_DEFAULT_DAYS = "orderHistory.default.day";
    public static final String ORDER_HISTORY_ALL_PROGRAMS_PROGRAM_CONFIG_KEY = "orderHistory.allPrograms";
    public static final String KOUNT_ENABLED = "kountEnabled";
    public static final String KOUNT_SESSION_OBJECT = "KOUNT_SESSION";

    public static final String PRODUCT_OPTIONS = "productOptions";
    public static final String SHIPPING_AVAILABILITY = "shippingAvailablity";
    public static final String SHIPPING_AVAILABILITY_OLD = "shippingAvailability_old";
    public static final String DEFAULT_SHIPPING_AVAILABILITY_KEY = "400";
    public static final String SHIPMENT_QUOTE_DATE = "shipmentQuoteDate";
    public static final String SHIPMENT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SHIPMENT_KEY_FORMAT = "shipment.timeframe.key.";
    public static final String SHIPMENT_IN_WEEKS = "weeks";
    public static final String EMAIL_ORDER_DATE_FORMAT = "sparkpost.order.date.format";
    public static final String EMAIL_SHIPMENT_DATE_FORMAT = "sparkpost.shipment.date.format";
    public static final int CENTS_TO_DOLLARS_DIVISOR = 100;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final String VAR_MX = "MX";
    public static final String CITI_LOGIN_REQUIRED = "login_required";
    public static final String CITI_SOURCE_CODE = "sourceCode";
    public static final String CITI_USER_CONSENT = "CITI_USER_CONSENT";
    public static final String EPSILON_HOSTNAME = "Epsilon.hostName";
    public static final String EPSILON_HOSTNAME_GRCS = "Epsilon.hostName.GRCS";
    public static final String EPSILON_HOSTNAME_GRBQ = "Epsilon.hostName.GRBQ";
    public static final String EPSILON_HOSTNAME_GRDI = "Epsilon.hostName.GRDI";
    public static final String EPSILON_HOSTNAME_GRSC = "Epsilon.hostName.GRSC";
    public static final String WEBAPP_HOSTNAME_GRCS = "cardservices";
    public static final String WEBAPP_HOSTNAME_GRBQ = "boq";
    public static final String WEBAPP_HOSTNAME_GRDI = "diners";
    public static final String WEBAPP_HOSTNAME_GRSC = "suncorp";
    public static final String APPLE_PARTNER_CODE = "Epsilon.sso.partnerCode";
    public static final String ORDER_SUMMARY_URL = "orderSummaryUrl";
    public static final String ORDER_SUMMARY_PATH = "/apple-gr/ui/store/order-history/";
    public static final String ORDER_SUMMARY_ENDPOINT = "/apple-gr/citi/ViewOrder.htm?";
    public static final String APPLE_LANDING_ENDPOINT = "/apple-gr/ui/store/";
    public static final String CITI_APPLE_TERMS_ENDPOINT = "/apple-gr/citi/Terms.htm";
    public static final String CITI_HEADER_NAME_FORMAT = "citi_header_name_format";
    //SPARK POST COMMON EMAIL TEMPLATE
    public static final String SPARK_POST_LOGO_URL = "sparkpost.logoUrl";
    public static final String SPARK_POST_SHOW_LOGO = "sparkpost.showLogo";
    public static final String SPARK_POST_HIDE_DELAYED_CUSTOMER_TEXT = "sparkpost.hideDelayedCustomerText";
    public static final String SPARK_POST_SUBJECT_CONFIRMATION = "sparkpost.subject.confirmation";
    public static final String SPARK_POST_SUBJECT_SHIPMENT = "sparkpost.subject.shipment";
    public static final String SPARK_POST_SUBJECT_DELAY = "sparkpost.subject.delay";
    public static final String SPARK_POST_FROM_EMAIL = "sparkpost.fromEmail";
    public static final String SPARK_POST_FROM_NAME = "sparkpost.fromName";
    public static final String SPARK_POST_FOOTER = "sparkpost.footer";
    public static final String SPARK_POST_AMP_TITLE = "sp-title";
    public static final String SPARK_POST_AMP_TRAIL_LENGTH = "sp-trail-length";
    public static final String SPARK_POST_AMP_TRAIL_TEXT = "sp-trail-text";
    public static final String SPARK_POST_AMP_CTA_TEXT = "sp-cta-text";
    public static final String SPARK_POST_AMP_BODY = "sp-body";
    public static final String SPARK_POST_AMP_BODY_LIST = "sp-body-list";
    public static final String SPARK_POST_AMP_FOOTER = "sp-footer";
    public static final String SPARK_POST_AMP_STATIC_LINK = "sp-subscription-url";
    public static final String SPARK_POST_AMP_SUBJECT = "sp-subject";
    public static final String SPARK_POST_RETURN_PATH = "sparkpost.returnPath";
    //Apple workplace
    public static final String AWP = "AWP";
    public static final String VAR_APPLE_WORKPLACE = "AWP";
    public static final String AWP_PROGRAM_DRP = "DRP";
    public static final String AWP_LOGIN = "AWPLogin";
    public static final String CATALOG_NAME_DEFAULT = "default";
    public static final String DRP_IN_ACTIVE = "DRP program inactive for email ";
    public static final String AWP_SERVICE_URL = "awp.service.url";
    public static final String AWP_VALIDATE_DOMAIN_PATH = "/rest/services/validateAWPDomain/";
    public static final String AWP_SHOP_INFO_PATH = "/rest/services/userInfo/";
    public static final String AWP_REQUEST_INFO_PATH = "/rest/services/requests/";
    public static final String AWP_PERSIST_EMPLOYEE_REQUEST_PATH = "/rest/services/requests";
    public static final String AWP_EMPLOYEE_GROUP_PATH = "/rest/services/employeeGroup/";
    public static final String AWP_EMPLOYEE_GROUP_BY_ID_PATH = "/rest/services/employeeGroup/info/";
    public static final String AWP_EMPLOYEE_GROUP_PAYROLL_PATH = "/rest/services/userInfo/employeeGroupPayroll/";
    public static final String AWP_CONFIRM_REQUEST = "/rest/services/confirmRequest/";
    public static final String AWP_COUNTRY_INFO = "/rest/services/countryInfo/";
    public static final String AWP_ORDER_LINE_ATTR_REQ_LINE_NUM = "requestLineNum";
    public static final String AWP_ORDER_LINE_ATTR_REQ_ID = "requestId";
    public static final String AWP_ORDER_LINE_ATTR_UPGRADE_PRICE = "upgradePrice";
    public static final String AWP_URL_REDIRECT_PATH = "/rest/services/urlRedirectDecision/";
    public static final String AWP_PAYROLL_AGREEMENT_DETAILS_PATH = "/rest/payrollAgreement/organization/";
    public static final String AWP_DISPLAY_TERMS_OF_USE = "displayTermsOfUse";

    //Epsilon SAML
    public static final String CITI_SAML_IDP_ENDPOINT_KEY = "saml.idp.epsilon.login.endpoint";
    public static final String CITI_SAML_IDP_PARTNER_CODE_KEY = "saml.idp.epsilon.login.partnerCode";
    public static final String CITI_SAML_IDP_PARTNER_RELAY_STATE = "saml.idp.epsilon.login.relayState";
    public static final String CITI_SAML_IDP_DEFAULT_SESSION_STATE = "A";
    public static final String CITI_SAML_IDP_LOGGED_IN_SESSION_STATE = "L";
    public static final String CITI_EPSILON_VAR_ORDER_MANAGER = "CITIGR";
    public static final String CITI_SAML_REQ_PARAM_FOR_SAML_RESP = "elToken";
    public static final String CITI_RELAY_STATE = "citiRelayState";
    // Product configuration view.
    public static final String SHOW_GRID_VIEW = "showGridView";
    public static final String CITI_SAML_SESSION_STATE = "CITI_SAML_SESSION_STATE";
    public static final String TOTAL_TAX = "showTotalTaxes";
    public static final String TOTAL_FEE = "showTotalFees";
    public static final String INSTALLMENT = "installment";
    public static String cr = "/n/r";
    public static String programSettingType = "programSettingType";
    /**
     * We want to increase the item price by at least 1% across the board to protect us from tax loss, etc Default to 1%
     * price increase.
     */
    @SuppressWarnings({"PublicField", "StaticNonFinalField"})
    public static double b2sBufferMarkup = 1.0;
    public static String TRUE_VALUE = "true";
    public static String FALSE_VALUE = "false";
    public static String ORDER_ADJUSTMENT_TYPE_P = "P";
    public static String ORDER_ADJUSTMENT_TYPE_V = "V";
    public static String USPS = "http://trkcnfrm1.smi.usps.com/PTSInternetWeb/InterLabelInquiry" +
        ".do?strOrigTrackNum=<%TRACK%>";
    public static String UPS =
        "http://wwwapps.ups.com/WebTracking/processInputRequest?sort_by=status&tracknums_displayed=1"
            + "&TypeOfInquiryNumber=T&loc=en_US&InquiryNumber1=<%TRACK%>&track.x=0&track.y=0";
    public static String DHL = "http://track.dhl-usa.com/atrknav.asp?ShipmentNumber=<%TRACK%>";
    public static String FED = "fedexTrackingUrl"; //Populate Tracking URL from VPM
    public static String TFORCE = "tforceTrackingUrl"; //Populate Tracking URL from VPM
    public static String AIR = "http://track.dhl-usa.com/atrknav.asp?ShipmentNumber=<%TRACK%>";
    public static String A1 = "https://agentexchange.etrac.net/etrac/locatejob?cref=<%TRACK%>&subs=82266756";
    public static String ABF = "http://www.abfs.com/trace/abftrace" +
        ".asp?RefType=A&Ref=<%TRACK%>&blnBOL=N&blnPO=N&blnShipper=Y&blnConsignee=Y&blnOrigin=Y&blnDestination=Y"
        + "&blnABFGraphic=Y";
    public static String DYN = "http://deploy.dynamex.com/dynamex/amazon/track.jsp?reference=<%TRACK%>";
    public static String PAR = "http://www.parcelpool.com/tracking/boxinfo_pp.asp?BOXID=<%TRACK%>&keycode=boxid";
    public static String EGL = "http://etracking.eaglegl.com/EGLTrak/eTrackResultsMulti" +
        ".aspx?ModeList=All&lstbasics=Reference&sv=<%TRACK%>";
    public static String UPS_MI = "http://www.ups-mi.net/packageID/PackageID.aspx?PID=<%TRACK%>";
    public static String CEVA = "http://www.cevalogistics.com/en/toolsresources/Pages/CEVATrak.aspx?sv=<%TRACK%>";
    public static String ONTRAC = "https://www.ontrac.com/tracking" +
        ".asp?trackingres=submit&tracking_number=<%TRACK%>&trackBtn.x=19&trackBtn.y=3&trackBtn=trackingres_submit";
    public static String PUROLATOR = "https://eshiponline.purolator.com/ShipOnline/Public/Track/TrackingDetails" +
        ".aspx?pin=<%TRACK%>";
    public static String DHL_G = "http://webtrack.dhlglobalmail.com/?trackingnumber=<%TRACK%>";
    public static String AIT = "http://myait.aitworldwide.com/Net/Tracking.aspx?TrackingNums=<%TRACK%>";
    //add streamlite CSR-2082
    public static String STREAM_LITE = "http://www.streamliteinc.com/PackageTracking/TrackMyPackageResult" +
        ".aspx?refID=<%TRACK%>";
    public static final String TRACK_HTTP = "http";
    public static final String TRACK = "<%TRACK%>";
    public static final String TRACK_NUM = "%TRACK_NUMBER%";
    public static final String TRACK_COUNTRY = "%TRACK_COUNTRY%";
    public static final String TRACK_LOCALE = "%TRACK_LOCALE%";
    public static final String SHIPMENT_CARRIER_USPS = "USPS";
    public static final String SHIPMENT_CARRIER_US_P = "US P";
    public static final String SHIPMENT_CARRIER_US_MAIL = "US MAIL";
    public static final String SHIPMENT_CARRIER_MAIL_EXPRESS = "MAIL EXPRESS";
    public static final String SHIPMENT_CARRIER_UPS_MAIL_INNOVATIONS = "UPS MAIL INNOVATIONS";
    public static final String SHIPMENT_CARRIER_UPS = "UPS";
    public static final String SHIPMENT_CARRIER_DHL = "DHL";
    public static final String SHIPMENT_CARRIER_FEDEX = "FED";
    public static final String SHIPMENT_CARRIER_TFORCE = "TFORCE";
    public static final String SHIPMENT_CARRIER_A1 = "A1";
    public static final String SHIPMENT_CARRIER_ABF = "ABF";
    public static final String SHIPMENT_CARRIER_AIR = "AIR";
    public static final String SHIPMENT_CARRIER_DYN = "DYN";
    public static final String SHIPMENT_CARRIER_PAR = "PAR";
    public static final String SHIPMENT_CARRIER_EAG = "EAG";
    public static final String SHIPMENT_CARRIER_EGL = "EGL";
    public static final String SHIPMENT_CARRIER_CEVA = "CEVA";
    public static final String SHIPMENT_CARRIER_WWW_CEVA = "WWW.CEVA";
    public static final String SHIPMENT_CARRIER_CEVA_START_WITH = "'CEVA";
    public static final String SHIPMENT_CARRIER_ONTRAC = "ONTRAC";
    public static final String SHIPMENT_CARRIER_PUROLATOR = "PUROLATOR";
    public static final String SHIPMENT_CARRIER_DHLG = "DHLG";
    public static final String SHIPMENT_CARRIER_STREAM_LITE = "StreamLite";
    public static final String SHIPMENT_CARRIER_AIT = "AIT";
    public static final String SHIPMENT_CARRIER_TNT="TNT";
    public static final String SHIPMENT_CARRIER_YAMATO="YAMATO";
    public static final String SHIPMENT_CARRIER_SHUN="SHUN";
    public static final String SHIPMENT_CARRIER_SCHENKER="SCHENKER";
    public static final String SHIPMENT_CARRIER_STARTRACK="STARTRACK";
    public static final String SHIPMENT_CARRIER_HTTP_LOWER = "http://";
    public static final String SHIPMENT_CARRIER_HTTP_UPPER = "HTTP://";
    public static final String DISCOUNT_TYPE_DOLLAR = CommonConstants.DOLLAR;
    public static final String IS_ELIGIBLE_FOR_DISCOUNT = "isEligibleForDiscount";
    public static final String IS_ELIGIBLE_FOR_PAYROLL_DEDUCTION = "isEligibleForPayrollDeduction";
    public static final String IS_BROWSE_ONLY = "isBrowseOnly";
    public static final String PROMOTION = "promotion";
    public static final String IS_ANONYMOUS = "isAnonymous";
    public static final String CLIENTGUID = "clientguid";
    public static final String POST_BACK_TYPE = "postBackType";
    public static final String SESSION_TIMEOUT = "sessionTimeout";
    public static final String SESSION_TIMEOUT_WARNING = "sessionTimeoutWarning";
    public static final String LOCATION = "Location";
    public static final String APPLE_SUPPLIER_DEEPLINK_CART = "applecart";
    public static final String EARN_POINTS_RATE = "actualEarnPointFactor";
    public static final String CITI_OBO_REDEEMABLE = "obo_redeemable";
    public static final String CITI_OBO_SESSION_STATE = "O";
    public static final String CITI_OBO_ANONYMS_STATE = "A";
    public static final String CITI_OBO_REDEEM_POINTS_ONLY = "points_only";
    public static final String CITI_OBO_REDEEM_POINTS_DEFAULT = "points_default";
    public static final String CITI_OBO_REDEEM_PAYMENT_TYPE = "paymentType";
    public static final String CITI_OBO_REDEEM_PAYMENT_TEMPLATE = "paymentTemplate";
    public static final String CITI_OBO_REDEEM_VIEW_ONLY = "view_only";
    public static final String CITI_OBO_REDEEM_RESTRICTED = "no";
    public static final String CITI_TERMS_URL = "/apple-gr/ui/store/terms";
    public static final String ORDER_STATUS_INFO_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String DISCOUNT_APPLIED = "DISCOUNT_APPLIED";

    public static final String GIFTCARDS_PHYSICAL = "PHYSICAL";
    public static final String GIFTCARDS_ECARD = "Ecard";
    public static final String GIFTCARDS_SEND_BY_EMAIL = "Send by Email";
    public static final String GIFTCARDS_SEND_BY_MAIL = "Send by Mail";
    public static final String GIFTCARDS_DENOMINATION = "denomination";
    public static final String GIFTCARDS_DELIVERYMETHOD = "deliveryMethod";
    public static final String GIFTCARDS_SHIPPINGMETHOD_TYPE = "ELECTRONIC";
    public static final String GIFTCARDS_APPLEMUSIC = "APPLEMUSIC";
    public static final String GIFTCARDS_ITUNES = "ITUNES";
    public static final String GIFTCARDS_BY_EMAIL = " by Email";
    public static final String GIFTCARDS_BY_MAIL = " by Mail";
    public static final String ORDERHOLDTIME = "OrderHoldTime";
    public static final String ORDERHOLDTIME_GC = "OrderHoldTime-GC";
    public static final String MAXPURCHASEQUANTITY_E_GC = "maxPurchaseQuantity-EGC";
    public static final String MAXPURCHASEQUANTITY_P_GC = "maxPurchaseQuantity-PGC";
    public static final String PAY_FREQUENCY_PAYMENT = "payment";
    public static final String PAY_FREQUENCY_PER_PAYMENT = "perPayment";

    public static final String NAUGHTY_WORD_FOUND = "naughtyWordFound";
    public static final String INVALID_CHARACTER_FOUND = "invalidCharactersFound";

    public static final String IS_EXTERNAL_HEADER = "useExternalHeader";
    public static final String IS_EXTERNAL_FOOTER = "useExternalFooter";
    public static final String ORDER_CONFIRMATION_FLAG = "needsOrderConfirmation";
    public static final String AWP_NONE_SUPPLIER = "NONE";
    public static final String ORDER_ATTIBUTE_EXCLUDE = "sessionState";

    public static final String PHYSICALGIFTCARDMAXVALUE = "physicalGiftcardMaxValue";
    public static final String PRODUCT_RESPONSE_DEFAULT_GROUP = "DEFAULT_GROUP";
    public static final String PRODUCT_RESPONSE_IMAGE_URL_SMALL = "small";
    public static final String PRODUCT_RESPONSE_IMAGE_URL_LARGE = "large";
    public static final String PRODUCT_RESPONSE_IMAGE_URL_MEDIUM = "medium";
    public static final String PRODUCT_RESPONSE_IMAGE_URL_ANGLE = "angle";
    public static final String PRODUCT_RESPONSE_IMAGE_URL_THUMBNAIL = "thumbnail";
    public static final String PAYROLL_AGREEMENT = "PayrollAgreement";

    public static final String SF_PRO_WEB_FONT = "SFProWebFont";
    public static final String ACTIVE_WEB_SHOPS = "activeWebShops";
    public static final String UNIT_PRICE_OVERRIDE = "unitPriceOverride";
    public static final String VIMS_ADDITIONAL_CHANGES = "vimsAdditionalChanges";
    public static final String IS_FULL_CATALOG = "fullCatalog";
    public static final String UN_AUTHORIZED_PAGES = "unAuthorizedPages";
    public static final String INVALID_DISCOUNT_CODE = "invalidDiscountCode";
    public static final String VIMS_ADDITIONAL_INFO= "vimsAdditionalInfo";

    public static final String IS_TWO_WAY_NAV_ENABLED="enable2WayNav";
    public static final String WF_TWO_WAY_NAV_VALUE="2";

    public static final String GIFT_ITEM = "GIFT_ITEM";
    public static final String DISCOUNTED_GIFT_PERCENTAGE = "DISCOUNTED_GIFT_PERCENTAGE";
    public static final String DISCOUNTED_GIFT_POINTS = "DISCOUNTED_GIFT_POINTS";

    public static final String IS_RELATED_PRODUCTS_ENABLED="enableRelatedProducts";

    public static final SecureRandom SECURE_RANDOM = new SecureRandom();
    public static final String ANALYTICS_URL = "analyticsUrl";
    public static final String IS_CASH_REWARD_PRICING = "isCashRewardPricing";

    public static final String SUPPRESS_TIMEOUT_WARNING_AND_KEEPALIVE_FOR_NATIVEAPP =
        "SuppressTimeoutWarningAndKeepaliveForNativeApp";

    static {
        final Map<String, String> aMap = new HashMap<>();
        aMap.put("", "US");
        aMap.put("UnitedStates", "US");
        aMap.put("United States", "US");
        aMap.put("USA", "US");
        aMap.put("US", "US");
        aMap.put("CANADA", "CA");
        aMap.put("CA", "CA");
        aMap.put("UnitedKingdom", "UK");
        aMap.put("United Kingdom", "UK");
        aMap.put("UK", "UK");
        COUNTRY_CODES = Collections.unmodifiableMap(aMap);
    }

    private CommonConstants() {
    }


    public static String getRequestAttributes(final HttpServletRequest request) {
        final Enumeration attrs = request.getAttributeNames();
        final StringBuilder attributes = new StringBuilder();
        while (attrs.hasMoreElements()) {
            final String attrName = (String) attrs.nextElement();
            attributes.append(attrName + " -- " + request.getAttribute(attrName) + ";;;");
        }
        return attributes.toString();
    }

    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX =
        "[!@#$%^&\\(\\)_\\-|\\}\\]\\{\\[:;?/.,`+=A-Za-z0-9_. ]";
    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_US = "en_US";
    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_CA = "en_CA";
    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_GB = "en_GB";
    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_AU = "en_AU";
    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_HK = "en_HK";
    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_SG = "en_SG";
    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_MY = "en_MY";
    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_PH = "en_PH";

    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_CA_MX =
        "[!@#$%^&\\(\\)_\\-\\{\\[:;\\?/.,`àèìòùÀÈÌÒÙáéíóúÁÉÍÓÚâêîôûÂÊÎÔÛãñõÃÑÕäëïöüÿÄËÏÖÜŸåÅæÆçÇøØ¿¡ß+A-Za-z0-9_. ]";
    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_FR_CA = "fr_CA";
    public static final String ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_ES_MX = "es_MX";

    public static final String DISALLOWED_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_ZH_HK = "zh_HK";
    public static final String DISALLOWED_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_ZH_TW = "zh_TW";
    public static final String DISALLOWED_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_TH_TH = "th_TH";
    public static final String DISALLOWED_CHARS_FOR_ENGRAVE_MSG_REG_EX_GLOBAL = "-1";

    public static final String DISALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_ZH = "ㄅㄆㄇㄈㄉㄊㄋㄌㄍㄎㄏㄐㄑㄒㄓㄔㄕㄖㄗㄘㄙㄚㄛㄜㄝㄞㄟㄠㄡㄢㄣㄤㄥㄦㄧㄨㄩヾゝゞ" +
        "ぁあぃいぅうぇえぉおかがきぎくぐけげこごさざしじすずせぜそぞただちぢっつづてでとど" +
        "なにぬねのはばぱひびぴふぶぷへべぺほぼぽまみむめもゃやゅゆょよらりるれろゎわゐゑをんァ" +
        "アィイゥウェエォオカガキギクグケゲコゴサザシジスズセゼソゾタダチヂッツヅテデトドナニヌネ" +
        "ノハバパヒビピフブプヘベペホボポマミムメモャヤュユョヨラリルレロヮワヰヱヲンヴヵヶ" +
        "ДЕЁЖЗИЙКЛМУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    public static final String DISALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_GLOBAL = "\"\\\\~*<>❤";

    public static final Map<String, String> regexPattern;
    public static final Map<String,String> disallowedCharsRegexPattern;


    static {
        Map<String,String> regexPatternAllowed = new HashMap<>();
        Map<String,String> regexPatternNotAllowed = new HashMap<>();

        regexPatternAllowed.put(ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_US,
            ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX);
        regexPatternAllowed.put(ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_CA,
            ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX);
        regexPatternAllowed.put(ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_GB,
            ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX);
        regexPatternAllowed.put(ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_AU,
            ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX);
        regexPatternAllowed.put(ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_HK,
            ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX);
        regexPatternAllowed.put(ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_SG,
            ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX);
        regexPatternAllowed.put(ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_MY,
            ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX);
        regexPatternAllowed.put(ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_EN_PH,
            ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX);
        regexPatternAllowed.put(ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_FR_CA,
            ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX);
        regexPatternAllowed.put(ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_ES_MX, ALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_CA_MX);

        regexPattern = Collections.unmodifiableMap(regexPatternAllowed);

        regexPatternNotAllowed.put(DISALLOWED_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_ZH_HK, DISALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_ZH);
        regexPatternNotAllowed.put(DISALLOWED_CHARS_FOR_ENGRAVE_MSG_REG_EX_LOCALE_ZH_TW, DISALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_ZH);
        regexPatternNotAllowed.put(DISALLOWED_CHARS_FOR_ENGRAVE_MSG_REG_EX_GLOBAL, DISALLOWED_SPECIAL_CHARS_FOR_ENGRAVE_MSG_REG_EX_GLOBAL);

        disallowedCharsRegexPattern = Collections.unmodifiableMap(regexPatternNotAllowed);
    }


    public static String getRequestAttribute(final ServletRequest request, final String attrName) {
        return trimToEmpty(request.getAttribute(attrName) == null ? "" : (String) request.getAttribute(attrName));
    }

    public static String getSignOutUrl() {
        return "/apple-gr/service/signOut";
    }

    public static String getExternalUrl(final HttpServletRequest request, final String urlName) {
        final Map<String, Object> urlMap = (Map<String, Object>) request.getSession().getAttribute(EXTERNAL_URLS);
        if (urlMap != null) {
            for (final Map.Entry<String, Object> url : urlMap.entrySet()) {
                if (urlName.equalsIgnoreCase(url.getKey()) && url.getValue() instanceof String) {
                    return StringUtils.trimToNull((String)url.getValue());
                }
            }
        }
        return null;
    }

    public static boolean isScotiaUser(final User user) {
        if (user != null && StringUtils.isNotBlank(user.getVarId()) && StringUtils.equalsIgnoreCase(VAR_SCOTIA,
            user.getVarId())) {
            return true;
        }
        return false;
    }

    public static String getApplicationProperty(
        final String key,
        final String varId,
        final String programId,
        final Properties appProperties) {

        if (isNotBlank(key) && isNotBlank(varId) && isNotBlank(programId)) {
            return getApplicationPropertyByKeyVarProgramID(key, varId, programId, appProperties);
        } else {
            return null;
        }
    }

    private static String getApplicationPropertyByKeyVarProgramID(
        final String key,
        final String varId,
        final String programId,
        final Properties appProperties) {

        String propertyValue = null;
        final StringBuilder varProgramProperty = new StringBuilder().append(lowerCase(varId)).append('.').append(
            lowerCase(programId)).append('.').append(lowerCase(key));
        propertyValue = appProperties.getProperty(varProgramProperty.toString());
        if (isBlank(propertyValue)) {
            final StringBuilder varProperty = new StringBuilder().append(lowerCase(varId)).append('.').append(
                lowerCase(key));
            propertyValue = appProperties.getProperty(varProperty.toString());
            if (isBlank(propertyValue)) {
                final StringBuilder property = new StringBuilder().append(lowerCase(key));
                propertyValue = appProperties.getProperty(property.toString());
                if (isBlank(propertyValue)) {
                    // get the default
                    return null;
                }
            }
        }
        return propertyValue;
    }

    public static String getApplicationProperty(
        final String key,
        final String varId,
        final String programId,
        final Locale locale,
        final Properties appProperties) {

        final String localeLanguage = locale.getLanguage().toLowerCase();
        String propertyValue=null;
        if (isNotBlank(key) && isNotBlank(varId) && isNotBlank(programId)) {

            propertyValue = getApplicationPropertyWithLocale(key, varId, programId, locale, appProperties);

            if (isBlank(propertyValue)) {
                propertyValue = getApplicationPropertyWithLocaleLanguage(key, varId, programId, locale, appProperties);
            }
        }

        return propertyValue;
    }


    private static String getApplicationPropertyWithLocale(
        final String key,
        final String varId,
        final String programId,
        final Locale locale,
        final Properties appProperties) {
        String propertyValue;
        final StringBuilder varProgramLocaleProperty = new StringBuilder().append(lowerCase(varId)).append('.')
            .append(lowerCase(programId)).append('.').append(key).append('.').append(locale.toString());

        propertyValue = appProperties.getProperty(varProgramLocaleProperty.toString());
        if (isBlank(propertyValue)) {
            final StringBuilder varLocaleProperty = new StringBuilder().append(lowerCase(varId)).append('.')
                .append(key).append('.').append(locale.toString());
            propertyValue = appProperties.getProperty(varLocaleProperty.toString());
        }

        return propertyValue;
    }

    private static String getApplicationPropertyWithLocaleLanguage(
        final String key,
        final String varId,
        final String programId,
        final Locale locale,
        final Properties appProperties) {
        String propertyValue=null;
        final String localeLanguage = locale.getLanguage().toLowerCase();
        final StringBuilder varProgramProperty = new StringBuilder().append(lowerCase(varId)).append('.')
            .append(lowerCase(programId)).append('.').append(key).append('.').append(localeLanguage);
        propertyValue = appProperties.getProperty(varProgramProperty.toString());
        if (isBlank(propertyValue)) {
            final StringBuilder varProperty =
                new StringBuilder().append(lowerCase(varId)).append('.').append(key).append('.')
                    .append(localeLanguage);
            propertyValue = appProperties.getProperty(varProperty.toString());
            if (isBlank(propertyValue)) {
                final StringBuilder property = new StringBuilder().append(key).append('.').
                    append(localeLanguage);
                propertyValue = appProperties.getProperty(property.toString());
                if (isBlank(propertyValue)) {
                    // get the default
                    return getApplicationProperty(key, varId, programId, appProperties);
                }
            }
        }
        return  propertyValue;
    }



    public enum PaymentOption {
        POINTS, CASH, PAYROLL_DEDUCTION, INSTALLMENT
    }

    public enum SupplementalPaymentType {
        FIXED, VARIABLE
    }

    public enum NotificationName {
        CONFIRMATION ("CONFIRMATION"),
        SHIPMENT("SHIPMENT"),
        OTP("OTP"),
        SHIPMENT_DELAY("SHIPMENT_DELAY"),
        ECERT("ECERT"),
        AMP_MUSIC("amp-music"),
        AMP_NEWS_PLUS("amp-news-plus"),
        AMP_TV_PLUS("amp-tv-plus"),
        SERVICE_PLAN("service_plan");

        public final String value;
        private static final Map<String, NotificationName> lookup = new HashMap<>();

        static {
            for (NotificationName n : NotificationName.values()) {
                lookup.put(n.value, n);
            }
        }

        NotificationName(String value) {
            this.value = value;
        }

        public static NotificationName get(final String value) {
            return lookup.get(value);
        }
    }


    public enum NotificationType {
        EMAIL
    }

    public enum StatusChangeQueueProcessStatus {
        PPC_SENT/* email failed */,
        EMAIL_SENT/* ppc failed */,
        SENT /* ppc, email sent */,
        FAILED /* ppc, email failed */,
        INPROGRESS,
        EMAIL_NOT_CONF
    }

    public enum UAHoldQueueStatus {
        APPROVED("APPROVED"), INREVIEW("REVIEW"), DECLINED("DECLINED");

        private String value;

        UAHoldQueueStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        ;
    }

    public enum LoginType {
        OTP("otp"), SAML("saml"), ANONYMOUS(ANONYMOUS_SAML_ATTRIBUTE), FIVEBOX("fivebox"), OAUTH("oauth");

        private String value;

        LoginType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        ;
    }

    public enum EmailType {
        SHIPPING("shippingEmail"), PROFILE("profileEmail");

        private String value;

        EmailType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        ;
    }

    public static enum POST_BACK_TYPES {

        API("api"), URL("url");

        private String value;

        POST_BACK_TYPES(String value) {
            this.value = value;
        }

        public String getValue() {return this.value;}
    }

    public enum PayrollFrequency {
        MONTHLY("frequency.monthly", "month"),
        FOUR_WEEKLY("frequency.4-weekly", "4 weeks"),
        TWO_WEEKLY("frequency.2-weekly", "fortnight"),
        WEEKLY("frequency.weekly", "week");

        private final String label;
        private final String defaultText;

        PayrollFrequency(final String label, final String defaultText) {
            this.label = label;
            this.defaultText = defaultText;
        }

        public String getLabel() {
            return label;
        }

        public String getDefaultText() {
            return defaultText;
        }
    }

    public static final List<String> APPLE_GR_ADDRESS_SUPPORTED_COUNTRIES =
        Arrays.asList(CommonConstants.COUNTRY_CODE_MY,
            CommonConstants.COUNTRY_CODE_PH,
            CommonConstants.COUNTRY_CODE_MX,
            CommonConstants.COUNTRY_CODE_HK,
            CommonConstants.COUNTRY_CODE_AU,
            CommonConstants.COUNTRY_CODE_AE,
            CommonConstants.COUNTRY_CODE_SG,
            CommonConstants.COUNTRY_CODE_TH,
            CommonConstants.COUNTRY_CODE_TW);

    public static final String STORETYPE_CORPORATE = "Corporate";
    public static final String STORETYPE_PERSONAL = "Personal";
    public static final String SPLIT_PAY_CURRENCY_ROUNDING = "splitPayCurrencyRounding";
    public static final Integer CURRENCY_ROUNDING_SCALE_ZERO = 0;
    public static final String CITIGR_CODE = "GR";
    public static final String CITIGR_SRC = "src";

    //Additional SAML Attributes
    public static final String NAVBACK_URL = "navBackURL";
    public static final String POINTS_BALANCE = "pointsBalance";
    public static final String BROWSE_ONLY = "browseOnly";
    public static final String ANONYMOUS_SAML_ATTRIBUTE = "anonymous";
    public static final String NORMAL_LOGIN = "Normal";
    public static final String AGENT_BROWSE = "agentBrowse";

    public static final String PROXY_USER_ID = "proxyUserId";

    public static final String MESSAGE = "message";
    public static final String INPUT_FIELD = "inputField";
    public static final String LINE_1 = "line1";
    public static final String LINE_2 = "line2";

    public static final String COMMA = ",";
    public static final String ANALYTICS = "analytics";
    public static final String SFPROWEBFONT = "SFProWebFont";
    public static final String DISABLED = "disabled";
    public static final  String OPTION_NAME_KEY = "options.name.displayOrderBy";

    //[S-08423] : Health URLs for apple and its dependent components
    public static final String PRICING_SERVICE = "pricing-server";
    public static final String PRICING_SERVICE_URL = "PRICING_SERVICE_URL";
    public static final String PRODUCT_SERVICE = "product-service-server";
    public static final String PS3_HTTP_URL = "PS3_HTTP_URL";
    public static final String IMAGE_SERVER = "Image Server";
    public static final String PAYMENT_SERVER = "Payment Server";
    public static final String CORE_PAYMENT_SERVER = "Core Payment Server";
    public static final String PRODUCT_SERVICE_CLIENT = "product-service-client";
    public static final String KEYSTONE_PAYMENT_SERVICE_CLIENT = "paymentserver-client";
    public static final String DISCOUNT_SERVICE = "discount-code-service";
    public static final String DISCOUNT_SERVICE_URL = "discount.service.url";
    public static final String APEX = "PS & P$";
    public static final String VIMS = "vims";
    public static final String WEBAPP_VERSION = "WebApp-Version";
    public static final String WEBAPP_BUILD_NUMBER = "Build-Number";
    public static final String APP_UP = "UP";
    public static final String APP_DOWN = "DOWN";
    public static final String APP_STATUS_NA = "Not Applicable";
    public static final String FLYWAY_MIGRATE = "flyway-migrate";
    public static final String VERSION = "version";
    public static final String BUILD_NUMBER = "buildNumber";
    public static final String INFO = "/info";
    public static final String PAYMENT_SERVER_BUILD_NUMBER = "Build Number:";
    public static final String B2S_IMAGE_PROXY = "b2r-image-proxy";
    public static final String COLON = ":";
    public static final String OPEN_PARENTHESIS = "(";

    //D-10157:DL customer having errors when trying to purchase Apple Products
    public static final String VERIFY_LOCALE_WITH_COUNTRY = "verifyLocaleWithCountry";

    public static final String ALL_ACCESSORIES = "all-accessories";
    public static final String ACC_ACCESSORIES = "acc-accessories";

    public static final String ACCESSORIES = "accessories";
    public static final String ACC_HYPHEN = "acc-";
    public static final String ALL_HYPHEN = "all-";
    public static final String SWATCH_IMAGE_URL = "swatchImageUrl";
    public static final String COLOR = "color";
    public static final String LANGUAGE = "language";

    public static final String SESSION_RESTART = "sessionRestart";
    public static final String SET_NEW_XSRF_TOKEN = "setNewXsrfToken";

    public static final String DECEASED = "deceased";
    public static final String DECEASED_USER_CHECK = "deceasedUserCheck";

    public static final String PAYMENT_PARAM_DISPATCH = "dispatch";
    public static final String PAYMENT_PARAM_TOKEN = "token";

    public static final Pattern SKU_PATTERN = Pattern.compile("\\w{7}\\/[A-Za-b]{1}");
    public static final Pattern SPACE_PATTERN = Pattern.compile("\\s{2,}");
    public static final Pattern REMOVE_SPECIAL_CHARACTER_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s\\./]+");


    public static final String HTTP_HEADER_PARTNER_CODE = "partner-code";

    public static final String PRICING_SERVICE_CONNECTION_TIMEOUT = "pricingService.connection.timeout";
    public static final String PRICING_SERVICE_READ_TIMEOUT = "pricingService.read.timeout";

    public static final String DISPLAY_PRICE = "displayPrice";
    public static final String DISPLAY_SHIPPING_COST = "displayShippingCost";
    public static final String DISPLAY_ITEM_PRICE = "displayItemPrice";
    public static final String DISPLAY_FEES = "displayFees";
    public static final String DISPLAY_TAXES = "displayTaxes";

    public static final String BASE_PRICE = "basePrice";
    public static final String VAR_PRICE = "varPrice";
    public static final String BRIDGE2_ITEM_MARKUP = "bridge2ItemMarkup";
    public static final String BRIDGE2_SHIPPING_MARKUP = "bridge2ShippingMarkup";
    public static final String VAR_ITEM_MARKUP = "varItemMarkup";
    public static final String VAR_SHIPPING_MARKUP = "varShippingMarkup";
    public static final String BRIDGE2_TAX = "bridge2Tax";
    public static final String BRIDGE2_FEE = "bridge2Fee";
    public static final String SHOW_GST = "showGST";
    public static final String SHOW_EARN_POINTS = "showEarnPoints";

    public static final String SAML_RESPONSE = "SAMLResponse";
    public static final String RELAY_STATE = "RelayState";
    public final static String REDEMPTION = "REDEMPTION";
    public final static String CASH_PRIMARY = "CASH_PRIMARY";
    public final static String BLUE_TIER_PRICING="BlueTierPricing";
    public final static String ROLE_AUS = "ROLE_AUS";
    public final static String ROLE_ADMIN = "ROLE_ADMIN";
    public final static String PASS_BLUETIER_DISCOUNT_TO_USER="passBluetierDiscountToUser";

    public static final String CART_REQUEST_SESSION_OBJECT = "CART_REQUEST";
    public static final String PS_CATEGORIES = "CATEGORIES";
    public static final String CART_RESPONSE_SESSION_OBJECT = "CART_RESPONSE";
    public static final String ANONYMOUS_TIME_OUT_URL = "anonymousTimeOutUrl";

    public final static String FINANCE_API = "financeAPI";
    public final static String FINANCE_MESSAGE_KEY_PREFIX = "finance.message.";
    public final static String FINANCE_MESSAGE_KEY_SUFFIX = ".text";
    public final static String FINANCE_SERVICE_IDENTIFIER_CITI = "citiFinanceService";
    public final static String FINANCE_SERVICE_IDENTIFIER_AMEX = "amexFinanceService";

    public static final String PERCENTAGE = "percentage";
    public static final String DOLLAR = "dollar";

    public static final String DGWP_PERCENTAGE = "Percentage";
    public static final String DGWP_POINTS = "Points";
    public static final String DISABLE_GWP = "disableGwp";

    public static final String ESTABLISHMENT_FEES_PRICE = "establishmentFeesPrice";
    public static final String ESTABLISHMENT_FEES_POINTS = "establishmentFeesPoints";

    public static final String CATEGORIES="categories";
    public static final String OPTIONS = "options";

    public static final String DEFAULT_ENCODING_UTF8 = "UTF-8";

    public static final String URI_TOKEN_TRANSACTION = "/transaction/";
    public static final String ENV_PROPS = "environment.properties";
    public static final String PAY_SERVER_EXTERNAL_URL = "payment.server.url";
    public static final String PAY_SERVER_HEALTH_URL = "payment.server.health.url";
    public static final String PAY_SERVER_INTERNAL_URL = "payment.server.api.url";
    public static final String PAYMENT_TRANSACTION_ID = "TRANSACTION_ID";
    public static final String DBA = "dba";
    public final static String PAYMENT_GATEWAY = "paymentGateway";
    public final static String PF = "PF"; //Paypal Flow paymentGateway
    public final static String BT = "BT"; //Brain Tree paymentGateway
    public static final String QUALITY_SWATCH_IMAGE_URL = "&qlt=95";
    public final static String SEND_CFM_EMAIL_FOR_STARTED_STATUS = "sendCfmEmailforStartedStatus";
    public static final String SLUGS_WITH_FACETS_FILTER = "slugs.with.facets.filter";
    public final static String PARENT_EMAIL = "parentEmail";
    public final static String PARENT_FIRST_NAME = "parentFirstName";
    public final static String PARENT_LAST_NAME = "parentLastName";
    public final static String AUTH_TOKEN="auth-token";
    public final static String CLIENT_ID_WITH_HYPEN="Client-id";
    public final static String STUDENT_EMAIL="Student-email";
    public final static String PRODUCT_TILES_OPTIONS="productTilesOptions";
    public static final String EXCLUDE_CATEGORY = "excludeCategory";

    public static final String QUICK_LINK_ID = "quickLinkId";
    public static final String LINE_NUM = "lineNum";
    public static final String SHIPMENT_DATE = "shipmentDate";
    public static final String PROCESS_STATUS = "processStatus";
    public static final String ORDER_STATUS = "orderStatus";
    public static final String ID = "id";
    public static final String MODIFIED_DATE = "modifiedDate";
    public static final String SHOW_UNAUTHENTICATED = "showUnauthenticated";
    public static final String VAR_PROGRAM_FILTER_ID = "varProgramFilterId";
    public static final String EMAIL = "EMAIL";
    public static final String SUBSIDY = "Subsidy";

    public static final String ALTERNATE = "ALTERNATE";
    public static final String REDIRECT = "REDIRECT";
    public static final String REDIRECT_ON_NO_RESULT = "REDIRECT_ON_NO_RESULT";

    public static final String SEARCH = "search";
    public static final String DOT_FACETS = ".facets";
    public static final String FULFILLMENT_AGENT = "fulfillmentAgent";

    public static final String ALL = "all";
    public static final String EHCACHE_XML = "ehcache.xml";
    public static final String CACHE_PROD_ATTRIBUTE_CONF = "ProductAttributeConfigurationDao_findByCategoryConfiguration";
    public static final String CACHE_CATEGORY_CONF_NAME = "CategoryConfigurationDao_getCategoryConfigurationByName";
    public static final String CACHE_ENGRAVE_CONF = "EngraveConfigurationDao_getByLocale";
    public static final String CACHE_CAROUSEL_TEMPLATES = "CarouselTemplateDao_getCarouselTemplates";

    public static final String VIMS_PRICING_API = "vimsPricingApi";

    public static final String HEADER_URL = ".externalHeaderUrl";
    public static final String EXTERNAL_HEADER_URL = "externalHeaderUrl";

    public static final String SUPPORTED_CREDIT_CARD_TYPES = "supportedCreditCardTypes";

    public static final String ENABLE_APPLE_CARE_SERVICE_PLAN = "enableAppleCareServicePlan";
    public static final String SECONDARY_IMAGE_URL = "secondaryImageUrl";
    public static final String MANUFACTURE_NOTE = "manufacturerNote";
    public static final String DISPLAY_PRODUCT_PAGE_CAROUSEL = "displayProductPageCarousel";

    public static final String TRANSACTION_TYPE_SALE="SALE";

    public enum CreditCardType {
        MASTERCARD("Master Card"),
        VISA("VISA"),
        DISCOVER("Discover"),
        AMEX("American Express"),
        DINERS("Diners Club"),
        JCB("JCB");

        private String value;

        CreditCardType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum CarouselType {
        RECENTLY_VIEWED("recentlyViewed", 1),
        AFFORDABLE_PRODUCT("affordableProduct", 2);

        private final String value;
        private final Integer order;

        CarouselType(String value, Integer order) {
            this.value = value;
            this.order = order;
        }

        public String getValue() {
            return this.value;
        }

        public Integer getOrder() {
            return this.order;
        }

        private static final Map<String, CarouselType> lookup = new HashMap<>();

        static {
            for (CarouselType n : CarouselType.values()) {
                lookup.put(n.value, n);
            }
        }

        public static CarouselType get(final String value) {
            return lookup.get(value);
        }
    }

    //DB Constants
    public static final String SELECT_FROM_AMP_PRODUCT_CONFIGURATION = "select * from amp_product_configuration with (NOLOCK)" +
            " where var_id=?1 and program_id=?2 and is_active=1";

}