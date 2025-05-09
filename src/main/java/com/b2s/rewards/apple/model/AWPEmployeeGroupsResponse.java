package com.b2s.rewards.apple.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.List;

/**
 * @author rjesuraj Date : 4/10/2018 Time : 12:15 PM
 */
@JsonDeserialize(builder = AWPEmployeeGroupsResponse.Builder.class)
public class AWPEmployeeGroupsResponse {

    private final List<AWPEmployeeGroup> employeeGroups;
    private final String companyName;

    private AWPEmployeeGroupsResponse(final Builder builder){
        this.employeeGroups =  builder.employeeGroups;
        this.companyName = builder.companyName;
    }

    public List<AWPEmployeeGroup> getEmployeeGroups() {
        return Collections.unmodifiableList(employeeGroups);
    }

    public String getCompanyName() {
        return companyName;
    }

    public static class Builder {
        private List<AWPEmployeeGroup> employeeGroups;
        private String companyName;

        public Builder withEmployeeGroups(final List<AWPEmployeeGroup> employeeGroups){
            this.employeeGroups = employeeGroups;
            return this;
        }

        public Builder withCompanyName(final String companyName){
            this.companyName = companyName;
            return this;
        }

        public AWPEmployeeGroupsResponse build(){
            return new AWPEmployeeGroupsResponse(this);
        }
    }
}
