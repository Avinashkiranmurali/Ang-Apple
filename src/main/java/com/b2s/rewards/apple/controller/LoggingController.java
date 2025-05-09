package com.b2s.rewards.apple.controller;

import com.b2s.rewards.apple.model.UIErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;

/**
 * Created by ssrinivasan on 3/25/2015.
 * Logs UI errors in server logs.
 */
@RestController
@RequestMapping("/log")
public class LoggingController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Logs the errors received through json.
     * @param uiErrors JSON Object.
     * @return
     */
    @PostMapping("/errors")
    public boolean logErrors(@RequestBody UIErrors uiErrors) {
        try {
            LOG.debug("Received UI Error {}", uiErrors);
        } catch (Exception ex) {
            LOG.error("Error while writing client side logs to file", ex);
            return false;
        }
        return true;
    }
}
