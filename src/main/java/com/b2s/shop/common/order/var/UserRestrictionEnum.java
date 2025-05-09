package com.b2s.shop.common.order.var;

/**
 * @author rjesuraj Date : 3/3/2017 Time : 12:27 PM
 */
public enum  UserRestrictionEnum {
    WHITE_LIST("whitelist"),
    BLACK_LIST("blacklist");

    private final String authType;
    UserRestrictionEnum(final String authType){
        this.authType=authType;
    }
    public String getValue(){
        return authType;
    }

}
