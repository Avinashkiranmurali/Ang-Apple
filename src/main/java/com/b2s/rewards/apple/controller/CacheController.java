package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cache")
public class CacheController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheController.class);

    @Autowired
    private CacheService cacheService;

    @PutMapping(value = "/{cacheName}/{timeToLiveSeconds}")
    public ResponseEntity<String> updateCacheObject(@PathVariable final String cacheName,
        @PathVariable final Long timeToLiveSeconds) {
        try {
            cacheService.updateCacheTimeToLiveSeconds(cacheName, timeToLiveSeconds);
        } catch(Exception e) {
            final String errorString =
                new StringBuilder("Error updating TimeToLiveSeconds service. CacheName: ").append(cacheName).append(
                    " & timeToLiveSeconds: ").append(timeToLiveSeconds).toString();
            LOGGER.error(errorString, e);
            return new ResponseEntity<>(errorString, HttpStatus.BAD_REQUEST);
        }
        final String errorString =
            new StringBuilder("Updated TimeToLiveSeconds successfully. CacheName: ").append(cacheName).append(
                " & timeToLiveSeconds: ").append(timeToLiveSeconds).toString();
        return new ResponseEntity<>(errorString, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{cacheName}")
    public ResponseEntity<String> clearCache(@PathVariable final String cacheName) {
        try {
            cacheService.clearCache(cacheName);
        } catch(Exception e) {
            final String errorString =
                new StringBuilder("Error calling clearCache service for Cache: ").append(cacheName).toString();
            LOGGER.error(errorString, e);
            return new ResponseEntity<>(errorString, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Clear Cache completed ", HttpStatus.OK);
    }
}
