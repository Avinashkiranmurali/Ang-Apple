package com.b2s.rewards.apple.model;

import java.util.Map;

/**
 * Created by rperumal on 2/19/2016
 *
 * ActivationFee model
 *
 */

public class ActivationFee {
    private Map<String, String> watchSport;
    private Map<String, String> watch;

    public Map<String, String> getWatchSport() {
        return watchSport;
    }

    public void setWatchSport(Map<String, String> watchSport) {
        this.watchSport = watchSport;
    }

    public Map<String, String> getWatch() {
        return watch;
    }

    public void setWatch(Map<String, String> watch) {
        this.watch = watch;
    }
}