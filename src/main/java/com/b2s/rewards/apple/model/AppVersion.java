package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by vkrishnan on 4/21/2019.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppVersion {
    private String status;
    private String version;
    private String build;

    public AppVersion() {
    }

    public AppVersion(final String status, final String version, final String build) {
        this.status = status;
        this.version = version;
        this.build = build;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(final String build) {
        this.build = build;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppVersion{");
        sb.append("status='").append(status).append('\'');
        sb.append("version='").append(version).append('\'');
        sb.append("build='").append(build).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
