package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author rjesuraj Date : 4/10/2018 Time : 12:17 PM
 */

@JsonDeserialize(builder = AWPEmployeeGroup.Builder.class)
public class AWPEmployeeGroup {
    private final int employeeGroupId;
    private final long organizationId;
    private final String employeeGroupName;
    private final boolean active;
    private final List<AWPProductGroup> productGroups;


    private AWPEmployeeGroup(final Builder builder){
        this.employeeGroupId=builder.employeeGroupId;
        this.organizationId=builder.organizationId;
        this.employeeGroupName=builder.employeeGroupName;
        this.active=builder.active;
        this.productGroups=builder.productGroups;
    }


    public int getEmployeeGroupId() {
        return employeeGroupId;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public String getEmployeeGroupName() {
        return employeeGroupName;
    }

    public boolean getActive() {
        return active;
    }

    public List<AWPProductGroup> getProductGroups() {
        return Collections.unmodifiableList(productGroups);
    }

    public static class Builder {

        private int employeeGroupId;
        private long organizationId;
        private String employeeGroupName;
        private boolean active;
        private List<AWPProductGroup> productGroups = new ArrayList<>();

        public Builder withEmployeeGroupId(final int employeeGroupId){
            this.employeeGroupId=employeeGroupId;
            return this;
        }

        public Builder withOrganizationId(final long organizationId){
            this.organizationId=organizationId;
            return this;
        }

        public Builder withEmployeeGroupName(final String employeeGroupName){
            this.employeeGroupName=employeeGroupName;
            return this;
        }

        public Builder withActive(final boolean active){
            this.active=active;
            return this;
        }

        public Builder withProductGroups(final List<AWPProductGroup> productGroups){
            this.productGroups.addAll(productGroups);
            return this;
        }

        public AWPEmployeeGroup build(){

            return new AWPEmployeeGroup(this);
        }

    }
}
