package com.b2s.rewards.apple.model;

import com.b2s.rewards.apple.util.AppleUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * Created by rperumal on 6/5/2015.
 */
public class Gift implements Serializable {

    private static final long serialVersionUID = -3025722801833387732L;
    private Boolean giftWrap;
    private Boolean freeGiftWrap;
    private Integer giftWrapPoints = Integer.valueOf(0);
    private String message1;
    private String message2;
    private String message3;
    private String message4;
    private String message5;

    //Required for JSON deserialization
    public Gift() {
    }

    public Gift(Boolean giftWrap, Boolean freeGiftWrap, String message1, String message2, String message3, String message4, String message5) {
        this.giftWrap = giftWrap;
        this.freeGiftWrap = freeGiftWrap;
        this.message1 = message1;
        this.message2 = message2;
        this.message3 = message3;
        this.message4 = message4;
        this.message5 = message5;
    }

    public Boolean getGiftWrap() {
        return (giftWrap != null ? giftWrap : false) ;
    }

    public void setGiftWrap(Boolean giftWrap) {
        this.giftWrap = giftWrap;
    }

    public Boolean getFreeGiftWrap() {
        return (freeGiftWrap != null ? freeGiftWrap : false) ;
    }

    public void setFreeGiftWrap(Boolean freeGiftWrap) {
        this.freeGiftWrap = freeGiftWrap;
    }

    public Integer getGiftWrapPoints() {
        return giftWrapPoints;
    }

    public void setGiftWrapPoints(Integer giftWrapPoints) {
        this.giftWrapPoints = giftWrapPoints;
    }

    public String getMessage1() {
        return AppleUtil.replaceNull(message1);
    }

    public void setMessage1(String message1) {
        this.message1 = message1;
    }

    public String getMessage2() {
        return AppleUtil.replaceNull(message2);
    }

    public void setMessage2(String message2) {
        this.message2 = message2;
    }

    public String getMessage3() {
        return AppleUtil.replaceNull(message3);
    }

    public void setMessage3(String message3) {
        this.message3 = message3;
    }

    public String getMessage4() {
        return AppleUtil.replaceNull(message4);
    }

    public void setMessage4(String message4) {
        this.message4 = message4;
    }

    public String getMessage5() {
        return AppleUtil.replaceNull(message5);
    }

    public void setMessage5(String message5) {
        this.message5 = message5;
    }

    public boolean hasMessageLengthExceededLimit() {
        if (getMessage1().length() > 30 ||
                getMessage2().length() > 30 ||
                getMessage3().length() > 30 ||
                getMessage4().length() > 30 ||
                getMessage5().length() > 30 )  {
            return true;
        }

        return false;
    }

    public boolean hasGiftMessage() {
        return (!StringUtils.isEmpty(message1) ||
                !StringUtils.isEmpty(message2) ||
                !StringUtils.isEmpty(message3) ||
                !StringUtils.isEmpty(message4) ||
                !StringUtils.isEmpty(message5) );
    }

}
