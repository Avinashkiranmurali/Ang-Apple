package com.b2s.rewards.apple.version;

import com.b2s.rewards.apple.model.AppDetails;
import com.b2s.rewards.apple.model.AppVersion;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Objects;

/**
 * Created by vkrishnan on 4/21/2019.
 */
public class HtmlOrTextVersionHandler implements VersionHandler {
    @Override
    public AppDetails parseApplicationVersionResponse(final String response, final String appName) {
        AppDetails appDetails = new AppDetails();
        appDetails.setApplicationName(appName);
        AppVersion appVersion = processAppVersion(appName,response);
        appDetails.setApplicationInfo(appVersion);
        return appDetails;
    }

    private AppVersion processAppVersion(String appName,String response){
        AppVersion appVersion = new AppVersion();
        switch(appName){
            case CommonConstants.PAYMENT_SERVER:
                appVersion = processPaymentServer(response,appVersion);
                break;
            case CommonConstants.IMAGE_SERVER:
                appVersion = checkImageServer(response,appVersion);
                break;
            default:
                appVersion = appDown(appVersion);
        }
        return appVersion;
    }

    private AppVersion checkImageServer(String response,AppVersion appVersion){
        return StringUtils.isNotBlank(response)?processImageServer(response,appVersion):appDown(appVersion);
    }

    private AppVersion processImageServer(String response,AppVersion appVersion){
        appVersion.setStatus(CommonConstants.APP_UP);
        appVersion.setVersion(StringUtils.substringAfter(response,CommonConstants.VERSION+CommonConstants.EQUAL));
        appVersion.setBuild(CommonConstants.APP_STATUS_NA);
        return appVersion;
    }

    private AppVersion processPaymentServer(String response,AppVersion appVersion){
        return StringUtils.isNotBlank(response)?parseHtmlTags(response,appVersion):appDown(appVersion);
    }

    private AppVersion parseHtmlTags(String response,AppVersion appVersion){
        appVersion.setStatus(CommonConstants.APP_UP);
        appVersion.setVersion(getValue(response,CommonConstants.VERSION));
        appVersion.setBuild(getValue(response,CommonConstants.BUILD_NUMBER));
        return appVersion;
    }

    private String getValue(String response,String id){
        String value = null;
        Document doc = Jsoup.parse(response);
        Element divTag = doc.getElementById(id);
        if(Objects.nonNull(divTag) ){
            value = divTag.text();
            if(StringUtils.equalsIgnoreCase(id,CommonConstants.BUILD_NUMBER)){
                return StringUtils.substringAfter(value,CommonConstants.PAYMENT_SERVER_BUILD_NUMBER).trim();
            }else{
                return StringUtils.substringBetween(value,CommonConstants.COLON,CommonConstants.OPEN_PARENTHESIS).trim();
            }
        }

        return CommonConstants.APP_STATUS_NA;
    }

    private AppVersion appDown(AppVersion appVersion){
        appVersion.setStatus(CommonConstants.APP_DOWN);
        return appVersion;
    }
}
