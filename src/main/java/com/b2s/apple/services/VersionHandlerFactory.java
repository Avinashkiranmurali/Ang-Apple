package com.b2s.apple.services;

import com.b2s.rewards.apple.version.HtmlOrTextVersionHandler;
import com.b2s.rewards.apple.version.JsonVersionHandler;
import com.b2s.rewards.apple.version.VersionHandler;
import com.b2s.rewards.common.util.CommonConstants;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by vkrishnan on 4/21/2019.
 */
@Service
public class VersionHandlerFactory {
    static Map<String,VersionHandler> versionHandlerMap = new HashMap();
    static {
        versionHandlerMap.put(CommonConstants.IMAGE_SERVER,new HtmlOrTextVersionHandler());
        versionHandlerMap.put(CommonConstants.PAYMENT_SERVER,new HtmlOrTextVersionHandler());
        versionHandlerMap.put(CommonConstants.PRODUCT_SERVICE,new JsonVersionHandler());
        versionHandlerMap.put(CommonConstants.PRICING_SERVICE,new JsonVersionHandler());
        versionHandlerMap.put(CommonConstants.DISCOUNT_SERVICE,new JsonVersionHandler());
        versionHandlerMap.put(CommonConstants.CORE_PAYMENT_SERVER,new JsonVersionHandler());
        versionHandlerMap.put(CommonConstants.AWP,new JsonVersionHandler());
        versionHandlerMap.put(CommonConstants.LOCAL_VAR_ORDER_ID_PREFIX,new JsonVersionHandler());
    }

    public static Optional<VersionHandler> getVersionHandler(String type){
        return Optional.ofNullable(versionHandlerMap.get(type));
    }
}
