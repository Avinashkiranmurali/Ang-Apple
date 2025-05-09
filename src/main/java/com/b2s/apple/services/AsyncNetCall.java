package com.b2s.apple.services;

import com.b2s.rewards.apple.model.AppDetails;
import com.b2s.rewards.apple.version.VersionHandler;
import com.b2s.rewards.common.util.CommonConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Created by vkrishnan on 4/17/2019.
 */
@Service
public class AsyncNetCall {

    private static final Logger logger = LoggerFactory.getLogger(AsyncNetCall.class);

    @Autowired
    private DBVersionService dbVersionService;

    @Autowired
    private AppleRestService appleRestService;

    @Async
    public CompletableFuture<AppDetails> getVersion(String appName,String endPoint,int timeOut){
        logger.info("Calling Endpoint: {}", endPoint);
        String responseBody = null;
        AppDetails appDetails = null;
        if(StringUtils.equalsIgnoreCase(appName,CommonConstants.FLYWAY_MIGRATE)){
            try {
                appDetails = dbVersionService.getFlywayVersion(timeOut);
            } catch (DataAccessException e) {
                logger.error("Error occurred while retrieving Flyway version from flyway_schema_history table ", e);
            }
        }else {
            RestTemplate restTemplate = null;
            try
            {
                restTemplate = appleRestService.getRestTemplate(timeOut);
                ResponseEntity<String> response = restTemplate.getForEntity(endPoint, String.class);
                if(Objects.nonNull(response)){
                    responseBody = response.getBody();
                }
            }catch(Exception e) {
                logger.error("Exception occurred while retrieving templates ", e);
            }

            appDetails = parseApplicationDetails(appName,responseBody);
        }

        return CompletableFuture.completedFuture(appDetails);
    }


    private AppDetails parseApplicationDetails(String appName,String responseBody){

        VersionHandler versionHandler = VersionHandlerFactory.getVersionHandler(getHandlerName(appName))
            .orElseThrow(() -> new IllegalArgumentException("Invalid " +
                "application: " + appName));
        return versionHandler.parseApplicationVersionResponse(responseBody,appName);

    }

    private String getHandlerName(String appName){
        return StringUtils.contains(appName, CommonConstants.LOCAL_VAR_ORDER_ID_PREFIX.toLowerCase())
            ?CommonConstants.LOCAL_VAR_ORDER_ID_PREFIX:appName;
    }

}

