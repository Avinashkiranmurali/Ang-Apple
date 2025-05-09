package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.AppSessionInfo;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Properties;

/**
 * Gathers device information without interfering with the order process. The redirect also
 * obfuscates the communication between Kount and the customer.
 *
 * Created by gdabhade on 10/30/2015.
 */
@RestController
@RequestMapping(value="/fraudcheck", produces = "application/json;charset=UTF-8")
@ResponseBody
public class FraudCheckController
{
    private static final Logger LOG = LoggerFactory.getLogger(FraudCheckController.class);

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private AppSessionInfo appSessionInfo;


    /**
     *This method redirect logo htm.
     *
     * @param user
     * @param response
     * @return
     */
    @RequestMapping(value = "/redirectlogohtm",method = RequestMethod.GET)
    public ResponseEntity redirectLogoHtm(final HttpServletResponse
        response)
    {
        final String logoType = "/logo.htm?m=";
        final User user = appSessionInfo.currentUser();
        return redirectLogo(user, response, logoType);
    }

    /**
     *This method redirect logo gif.
     *
     * @param user
     * @param merchantId
     * @param session
     * @param response
     * @return
     */
    @RequestMapping(value="/redirectlogogif",method = RequestMethod.GET)
    public ResponseEntity redirectLogoGif(@RequestParam final String merchantId, final HttpSession session, final HttpServletResponse response)
    {
        final String logoType = "/logo.gif?m=";
        final User user = appSessionInfo.currentUser();
        return redirectLogo(user, response, logoType);
    }

    private ResponseEntity redirectLogo(final User user, final HttpServletResponse response, final String logoType) {
        if(user.getAdditionalInfo() == null) {
            user.setAdditionalInfo(new HashMap<>());
        }
        String fraudSessionId = user.getAdditionalInfo().get(CommonConstants.FRAUD_SESSION_ID);
        String url = "https://" + applicationProperties.getProperty(CommonConstants.KOUNT_SERVER_URL_KEY) + logoType +
            applicationProperties.getProperty(CommonConstants.KOUNT_SERVER_UA_MERCHANT_ID_KEY) + "&s=" + fraudSessionId;
        response.setStatus(HttpServletResponse.SC_FOUND);
        try
        {
            response.sendRedirect(url);
        }
        catch(Exception e)
        {
            LOG.error("Error calling appleGrGif service", e);
        }
        return new ResponseEntity<byte[]>(null, null, HttpStatus.FOUND);
    }
}
