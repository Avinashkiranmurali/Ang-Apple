package com.b2s.shop.common.order.var;

import com.b2s.shop.common.User;

/**
 * @author rjesuraj Date : 8/8/2017 Time : 5:21 PM
 */

public class UserCiti extends User {

    private static final long serialVersionUID = 2003419953987180242L;

    private String headerName;
    private String displayName;
    private String prefixName;
    private String middleName;
    private String suffixName;

    private String addr3;
    private String addr4;
    private String addr5;
    private String addr6;
    private String memberId;
    private String agentId;
    private boolean userConsented;
    private String sourceCode;


    public String getAddr3() {
        return addr3;
    }

    public void setAddr3(final String addr3) {
        this.addr3 = addr3;
    }

    public void setAddr4(final String addr4) {
        this.addr4 = addr4;
    }

    public String getAddr4() {
        return addr4;
    }

    public String getAddr5() {
        return addr5;
    }

    public void setAddr5(final String addr5) {
        this.addr5 = addr5;
    }

    public String getAddr6() {
        return addr6;
    }

    public void setAddr6(final String addr6) {
        this.addr6 = addr6;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(final String memberId) {
        this.memberId = memberId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(final String agentId) {
        this.agentId = agentId;
    }

    public Boolean isUserConsented() {
        return userConsented;
    }

    public void setUserConsented(final String userConsented) {
        this.userConsented = "y".equalsIgnoreCase(userConsented);
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(final String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPrefixName() {
        return prefixName;
    }

    public void setPrefixName(String prefixName) {
        this.prefixName = prefixName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSuffixName() {
        return suffixName;
    }

    public void setSuffixName(String suffixName) {
        this.suffixName = suffixName;
    }

    public void setHeaderName(String headerName){
        this.headerName=headerName;
    }

    public String getHeaderName(){
        return headerName;
    }

}
