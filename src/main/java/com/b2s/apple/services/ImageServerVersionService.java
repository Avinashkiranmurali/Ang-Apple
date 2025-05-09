package com.b2s.apple.services;

import com.b2s.rewards.apple.model.AppDetails;
import com.b2s.rewards.common.util.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Created by dsheth on 3/23/2020.
 * For S-10608 [Tech Debt] Image server CSS version stamp references apple-gr (No QA required)
 * Method to get the image server version no.
 */

@Service
public class ImageServerVersionService {
    private static final Logger logger = LoggerFactory.getLogger(ImageServerVersionService.class);

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private AsyncNetCall asyncNetCall;

    /**
     * Returns the the image server version no from its version-info.txt. If there is any error, "1.0" is returned.
     *
     * @return String
     */
    public String getVersion() {
        int timeoutInSeconds = 6 * 1000;
        String retValue = getVersionWtihoutNetworkCall();
        try {
            CompletableFuture<AppDetails> appVersionDetails = asyncNetCall.getVersion(CommonConstants.IMAGE_SERVER,
                    applicationProperties.getProperty(CommonConstants.IMAGE_SERVER_VIP_URL_KEY) + "/version-info.txt",
                    timeoutInSeconds);

            retValue = appVersionDetails.get().getApplicationInfo().getVersion().trim();
        } catch (Throwable e) {
            logger.error("Problem while making network call for Image Server Version", e);
        }
        return retValue;
    }

    public String getVersionWtihoutNetworkCall() {
        return "1.0";
    }
}
