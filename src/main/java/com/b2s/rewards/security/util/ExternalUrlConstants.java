package com.b2s.rewards.security.util;

/**
 * Created by ssrinivasan on 10/7/2015.
 */
public class ExternalUrlConstants {
    public static final String EXTERNAL_URLS = "externalUrls";
    public static final String NAVIGATE_BACK_URL = "navigateBackUrl";//Client URL
    public static final String HOME_LINK_URL = "homeLinkUrl";
    public static final String LOG_OUT_URL = "logOutUrl";
    public static final String SIGN_OUT_URL = "signOutUrl";//Sign Out Redirect URL
    public static final String PARTNER_SIGN_OUT_URLS = "partnerSignOutUrls";
    public static final String VAR_SIGN_OUT_URL = "varSignOutUrl";
    //TODO: create a new story to remove "timeOutUrl" and include it in "timeOutUrls" array
    public static final String TIME_OUT_URL = "timeOutUrl";//Time Out Redirect URL
    public static final String PARTNER_TIME_OUT_URLS = "partnerTimeOutUrls";
    public static final String KEEP_ALIVE_URL = "keepAliveUrl";
    public static final String KEEP_ALIVE_URL_POINTS_BANK = "keepAliveUrlPointsBank";
    public static final String BASE_KEEP_ALIVE_URL = "baseKeepAliveUrl";
    public static final String KEEP_ALIVE_URL_SOURCE = "keepAliveUrlSource";
    public static final String KEEP_ALIVE_URL_VAR = "keepAliveUrlVar";
    public static final String KEEP_ALIVE_URL_SOURCE_REQ_MAPPING = "keepalive-url-source";
    public static final String KEEP_ALIVE_URL_PARTNER_REQ_MAPPING = "partner-keepalive-url";
    public static final String KEEP_ALIVE_URL_PARTNER_REQ_MAPPING_KEY = "ppc.keepalive.url";
    public static final String PURCHASE_POST_URL = "purchase.post.url";
    public static final String LOCAL_USER_PURCHASE_POST_URL = "localuser.purchase.post.url";
    public static final String KEEP_ALIVE_JSONP = "keepAliveJSONP";
    public static final String KEYSTONE_BASE_URL = "keystoneBaseUrl";
    public static final String KEYSTONE_URLS = "keystoneUrls";
    public static final String KEEP_ALIVE = "keepAlive";
    public static final String B2R_KEEP_ALIVE_PATH = "/b2r/keepalive.js?callback=JSON_CALLBACK";
    public static final String MERCH_KEEP_ALIVE_PATH = "/merch/keepalive.js?callback=JSON_CALLBACK";
    public static final String TRAVEL_KEEP_ALIVE_PATH = "/travel/keepalive.js?callback=JSON_CALLBACK";
    public static final String LOG_OUT = "logout";
    public static final String B2R_LOG_OUT_PATH = "/b2r/logout";
    public static final String BALANCE_UPDATE = "balanceUpdate";
    public static final String B2R_BALANCE_PATH = "/b2r/api/participant/balance";
    public static final String MERCH_BALANCE_PATH = "/merch/api/participant/balance";
    public static final String TRAVEL_BALANCE_PATH = "/travel/participant/balance";
    public static final String KEYSTONE_NAVIGATE_BACK_URL = "keystoneNavigateBackUrl"; //Keystone URL

    public enum SessionUrls {
        NAVIGATE_BACK_URL(ExternalUrlConstants.NAVIGATE_BACK_URL),
        SIGN_OUT_URL(ExternalUrlConstants.SIGN_OUT_URL),
        TIME_OUT_URL(ExternalUrlConstants.TIME_OUT_URL),
        KEEP_ALIVE_URL(ExternalUrlConstants.KEEP_ALIVE_URL),
        HOME_LINK_URL(ExternalUrlConstants.HOME_LINK_URL),
        KEYSTONE_NAVIGATE_BACK_URL(ExternalUrlConstants.KEYSTONE_NAVIGATE_BACK_URL);

        private String value;

        SessionUrls(String value) {
            this.value = value;
        }

        public String getValue() {return this.value;};
    }

}
