package com.b2s.rewards.apple.interceptor;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class CacheLoggingListener extends CacheEventListenerFactory implements CacheEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheLoggingListener.class);

    @Override
    public CacheEventListener createCacheEventListener(Properties properties) {
        return this;
    }

    @Override
    public void notifyElementRemoved(Ehcache cache, Element element)
        throws CacheException {
        LOGGER.info("EhCacheLog-Element {} removed from the cache {}, size {}", element.getObjectKey(), cache.getName(),
            cache.getSize());
    }

    @Override
    public void notifyElementPut(Ehcache cache, Element element)
        throws CacheException {
        LOGGER.info("EhCacheLog-Element {} added to the cache {}, size {}", element.getObjectKey(), cache.getName(),
            cache.getSize());
    }

    @Override
    public void notifyElementUpdated(final Ehcache cache, final Element element)
        throws CacheException {
        LOGGER.info("EhCacheLog-Element {} updated in the cache {}, size {}", element.getObjectKey(), cache.getName(),
            cache.getSize());
    }

    @Override
    public void notifyElementExpired(final Ehcache cache, final Element element) {
        LOGGER.info("EhCacheLog-Element {} have been expired from the cache {}, size {}",
            element.getObjectKey(), cache.getName(), cache.getSize());
    }

    @Override
    public void notifyElementEvicted(final Ehcache cache, final Element element) {
        LOGGER.info("EhCacheLog-Element {} have been evicted from the cache {}, size {}",
            element.getObjectKey(), cache.getName(), cache.getSize());
    }

    @Override
    public void notifyRemoveAll(final Ehcache cache) {
        LOGGER.info("EhCacheLog-All Elements have been removed from the cache {}, size {}", cache.getName(),
            cache.getSize());
    }

    /**
     * Clone
     *
     * Making this object clonable is a requirement from the library(EHCACHE) that is being used.
     * Thus in that grounds we Approve the suppression of Sonar rule "S2975"
     *
     * @return Object
     * @throws CloneNotSupportedException
     */
    @SuppressWarnings("squid:S2975")
    @Override
    public Object clone()
        throws CloneNotSupportedException {
        LOGGER.info("EhCacheLog-Clone Listener");
        return super.clone();
    }

    @Override
    public void dispose() {
        LOGGER.info("EhCacheLog-Elements have been disposed from the cache");
    }
}