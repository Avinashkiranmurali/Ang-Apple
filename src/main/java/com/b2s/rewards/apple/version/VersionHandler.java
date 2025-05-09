package com.b2s.rewards.apple.version;

import com.b2s.rewards.apple.model.AppDetails;

/**
 * Created by vkrishnan on 4/21/2019.
 */
public interface VersionHandler {
    AppDetails parseApplicationVersionResponse(String response,String applicationName);
}
