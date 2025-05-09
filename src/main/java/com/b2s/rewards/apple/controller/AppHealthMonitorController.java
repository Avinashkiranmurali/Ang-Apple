package com.b2s.rewards.apple.controller;

import com.b2s.apple.services.VersionService;
import com.b2s.rewards.apple.model.AppVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author ssrinivasan
 */
@RestController
public class AppHealthMonitorController {

    @Autowired
    private VersionService versionService;

    @GetMapping(value = "/application/health", produces = "application/json")
    public ResponseEntity<AppVersion> getApplicationHealth(
        @RequestParam(required = false, value = "timeout") final Integer timeout) {
        return new ResponseEntity<>(versionService.getWebAppHealth(timeout), HttpStatus.OK);
    }

    @GetMapping(value = "/application/info", produces = "application/json")
    public ResponseEntity<AppVersion> getApplicationInfo() {
        return new ResponseEntity<>(versionService.getWebAppDetails(), HttpStatus.OK);
    }
}
