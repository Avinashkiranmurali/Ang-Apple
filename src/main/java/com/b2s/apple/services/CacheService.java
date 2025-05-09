package com.b2s.apple.services;

import com.b2s.rewards.common.util.CommonConstants;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CacheService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

    @Autowired
    private CacheManager cacheManager;

    //Get Cache
    private Cache getCache(final String cacheName){
        Cache cache = cacheManager.getCache(cacheName);
        if(Objects.isNull(cache)){
            final String errorString = new StringBuilder("Unable to get cache: ").append(cacheName).append(" from CacheManager").toString();
            LOGGER.error(errorString);
            throw new RuntimeException(errorString);
        }
        return cache;
    }

    // Update Cache TimeToLiveSeconds
    public void updateCacheTimeToLiveSeconds(final String cacheName, final long timeToLiveSeconds) {
        Cache cache = getCache(cacheName);
        CacheConfiguration config = cache.getCacheConfiguration();
        LOGGER.info("Updating Cache: {} Time To Live Seconds: {}", cacheName,timeToLiveSeconds);
        config.setTimeToLiveSeconds(timeToLiveSeconds);
    }

    //Controller Clear Cache method
    public void clearCache(final String cacheName){
        if(CommonConstants.ALL.equalsIgnoreCase(cacheName)){
            clearAllCache();
        }else{
            clearOnlySpecificCache(cacheName);
        }
    }

    //Clear only Specific Cache - EhCache
    public void clearOnlySpecificCache(final String cacheName) {
        try {
            Cache cache = getCache(cacheName);
            LOGGER.info("Clearing entries in Cache for name: {}",cacheName);
            cache.removeAll();
        }catch (Exception ex){
            String errorString = new StringBuilder("Error occurred while trying to clear Cache: ").append(cacheName).toString();
            LOGGER.error(errorString,ex);
            throw new RuntimeException(errorString, ex);
        }
    }

    //Clear All Cache - Spring Cache
    public void clearAllCache() {
        try {
            LOGGER.info("To clear entries in All Caches");
            cacheManager.clearAll();
        }catch (Exception ex){
            String errorString = "Error occurred while trying to clear All Caches";
            LOGGER.error(errorString,ex);
            throw new RuntimeException(errorString, ex);
        }
    }
}
