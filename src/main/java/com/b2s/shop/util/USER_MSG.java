package com.b2s.shop.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Craig McLaughlin
 * Date: Sep 16, 2006
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({"ClassNamingConvention"})
public class USER_MSG {

    @SuppressWarnings({"StaticNonFinalField", "FieldCanBeLocal", "UnusedDeclaration","StaticVariableNamingConvention"})
    private static USER_MSG USER_MSGRef = null;

    public static final int ORDER_SUCCESS = 0;
    public static final int GIFT_MESSAGE = 100;
    public static final int ORDER_IN_PROCESS = 103;
    public static final int TERMS_CONDITIONS_TRAVEL = 111;
    public static final int TERMS_CONDITIONS = 101;
    public static final int TERMS_CONDITIONS_SHORT = 104;
    public static final int SEARCH_HELP = 107;
    public static final int CUSTOMER_SERVICE_CONTACT = 102;
    public static final int B2S_CUSTOMER_SERVICE_CONTACT_INFORMATION = 1220;
    public static final int CS_VOUCHER_MSG = 105;
    public static final int CODE_NEW_ACCOUNT_ATTACHED_SUCCESS = 200;
    public static final int CODE_ATTACHED_SUCCESS = 201;
    public static final int TRAVEL_BOOKING_ASSISTANCE_MESSAGE = 202;

    public static final int PROGRAM_RULES = 300;
    @SuppressWarnings("ConstantNamingConvention")
    public static final int FAQS = 301;
    public static final int WELCOME_MESSAGE = 302;
    public static final int CONTACT_US_CONTENT = 303;

    public static final int VAR_PRE_ERROR = -96;
    public static final int VAR_POST_ERROR = -97;
    public static final int SUPPLIER_ERROR = -98;
    public static final int SUPPLIER_FAILED_ORDER_ERROR = -103;
    public static final int EXCEPTION_ERROR = -99;
    public static final int NOT_ENOUGH_POINTS = -100;
    public static final int NO_LONGER_AVAIL = -101;
    public static final int HAS_PENDING_ORDER = -102;
    public static final int FORGOT_PASSWORD = -201;
    public static final int CHANGED_PASSWORD = -200;

    public static final int EMAIL_CANCEL_CNL_TITLE = 5005;
    public static final int EMAIL_CANCEL_CNL_CONTENT = 5006;
    public static final int EMAIL_CANCEL_NLA_TITLE = 5009;
    public static final int EMAIL_CANCEL_NLA_CONTENT = 5010;
    public static final int EMAIL_RETURN_TITLE = 5007;
    public static final int EMAIL_RETURN_CONTENT = 5008;

    //CSR-231
    public static final int EMAIL_PARTIAL_REFUND_TITLE = 5007;
    public static final int EMAIL_PARTIAL_REFUND_CONTENT = 5008;
    //end CSR-231

    public static final int EMAIL_DISCLAIMER_CODE = 6221;
    public static final int TAX_DISCLAIMER_CODE = 6222;
    public static final int RETURNS_DISCLAIMER_CODE = 6223;
    public static final int HTML_PAGE_TITLE_CODE = 6224;
    public static final int FEE_DETAILS_CODE = 6225;
    public static final int SPAM_FILTER_NOTIFICATION_CODE = 6300;

    private static final Logger logger = LoggerFactory.getLogger(USER_MSG.class);


    @SuppressWarnings({"rawtypes", "PublicField", "StaticNonFinalField", "PublicStaticCollectionField"})
    public static HashMap msgs = new HashMap();

    static {
        //noinspection InstantiationOfUtilityClass
        USER_MSGRef = new USER_MSG();
    }


    private USER_MSG() {
        try {

        } catch (@SuppressWarnings("OverlyBroadCatchBlock") final Exception e) {
            logger.error("Unknown Error",e);
        }
    }

}
