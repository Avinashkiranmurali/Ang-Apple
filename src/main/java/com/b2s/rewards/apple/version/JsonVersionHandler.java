package com.b2s.rewards.apple.version;

import com.b2s.rewards.apple.model.AppDetails;
import com.b2s.rewards.apple.model.AppVersion;
import com.b2s.rewards.common.util.CommonConstants;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by vkrishnan on 4/21/2019.
 */
public class JsonVersionHandler implements VersionHandler {
    private static final Logger logger = LoggerFactory.getLogger(JsonVersionHandler.class);

    private static final String AWP_BUILD_VERSION = "$.details.buildInfo.details.version";
    private static final String DISCOUNT_SERVICE_BUILD_VERSION = "$.build.version";
    private static final String DEFAULT_BUILD_VERSION = "$['application.version']";

    private static final String DISCOUNT_SERVICE_BUILD_NAME = "$.build.name";
    private static final String DEFAULT_BUILD_NAME = "$['application.name']";

    @Override
    public AppDetails parseApplicationVersionResponse(final String response, final String appName) {
        AppDetails appDetails = new AppDetails();
        AppVersion appVersion = new AppVersion();

        if(StringUtils.isNotBlank(response)){
            appDetails.setApplicationName(getAppName(response,appName));
            processAppVersion(appVersion,response,appName);
        }else{
            appDetails.setApplicationName(appName);
            appVersion.setStatus(CommonConstants.APP_DOWN);
        }

        appDetails.setApplicationInfo(appVersion);
        return appDetails;
    }

    private void processAppVersion(AppVersion appVersion,String response,String appName){
            appVersion.setStatus(CommonConstants.APP_UP);
            final String buildVersion = getBuildVersion(response,appName);
            appVersion.setVersion(getValue(buildVersion,CommonConstants.VERSION));
            appVersion.setBuild(getValue(buildVersion,CommonConstants.BUILD_NUMBER));
    }

    private String getValue(String buildVersion,String id){
        if(StringUtils.isNotBlank(buildVersion)){
            return StringUtils.equals(id,CommonConstants.VERSION)?
                StringUtils.substringBefore(buildVersion,CommonConstants.HYPHEN):StringUtils.substringAfter
                (buildVersion,CommonConstants.HYPHEN);
        }else{
            return CommonConstants.APP_STATUS_NA;
        }
    }

    private String getBuildVersion(String response,String appName){
        String version = null;
        try {
            switch (appName) {
                case CommonConstants.AWP:
                    version = JsonPath.parse(response).read(AWP_BUILD_VERSION);
                    break;
                case CommonConstants.DISCOUNT_SERVICE:
                case CommonConstants.CORE_PAYMENT_SERVER:
                    version = JsonPath.parse(response).read(DISCOUNT_SERVICE_BUILD_VERSION);
                    break;
                default:
                    version = JsonPath.parse(response).read(DEFAULT_BUILD_VERSION);
            }
        }catch(Exception ex){
            logger.warn("Error while getting build version from JSON response for app Name {}",appName);
        }
        return version;
    }

    private String getAppName(String response,String appName){
        String name = null;
        try {
            switch (appName) {
                case CommonConstants.AWP:
                    name = appName;
                    break;
                case CommonConstants.DISCOUNT_SERVICE:
                case CommonConstants.CORE_PAYMENT_SERVER:
                    name = JsonPath.parse(response).read(DISCOUNT_SERVICE_BUILD_NAME);
                    break;
                default:
                    name = JsonPath.parse(response).read(DEFAULT_BUILD_NAME);
            }
        }catch(Exception ex){
            logger.warn("Error while getting build name from JSON response for app Name {}",appName);
        }
        return name;
    }

}
