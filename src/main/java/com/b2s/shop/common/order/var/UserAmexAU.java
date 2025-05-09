package com.b2s.shop.common.order.var;

import com.b2s.shop.common.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserAmexAU extends User {

    //TODO these fields need to defined based on the SAML.
    private String addr3;
    private String locality;
    private String region;

    private CardInContext cardInContext;
    private List<CreditCardInfo> additionalCards = new ArrayList<CreditCardInfo>();

    public CardInContext getCardInContext() {
        return cardInContext;
    }

    public void setCardInContext(final CardInContext cardInContext) {
        this.cardInContext = cardInContext;
    }

    public String getAddr3() {
        return addr3;
    }

    public void setAddr3(final String addr3) {
        this.addr3 = addr3;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(final String locality) {
        this.locality = locality;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public List<CreditCardInfo> getAdditionalCards() {
        return additionalCards;
    }

    public void setAdditionalCards(final List<CreditCardInfo> additionalCards) {
        this.additionalCards = additionalCards;
    }

    @Override
    public String toString() {
        return "UserAmexAU{" +
            "addr3='" + addr3 + '\'' +
            ", locality='" + locality + '\'' +
            ", region='" + region + '\'' +
            ", cardInContext=" + cardInContext +
            //", switchAccountsHeading='" + switchAccountsHeading + '\'' +
            ", additionalCards=" + additionalCards +
            '}';
    }

    class CardInContext implements Serializable {

        private static final long serialVersionUID = -4594206010315811182L;
        private String cardArtURL;
        private String cardName;
        private Integer lastFiveDigits;
        private String tokenizedCardNumber;
        private Integer memberSince;

        public CardInContext() {

        }

        public CardInContext(final String cardArtURL, final String cardName, final Integer lastFiveDigits, final String
            tokenizedCardNumber, final Integer memberSince) {
            this.cardArtURL = cardArtURL;
            this.cardName = cardName;
            this.lastFiveDigits = lastFiveDigits;
            this.tokenizedCardNumber = tokenizedCardNumber;
            this.memberSince = memberSince;
        }

        public String getCardArtURL() {
            return cardArtURL;
        }

        public void setCardArtURL(final String cardArtURL) {
            this.cardArtURL = cardArtURL;
        }

        public String getCardName() {
            return cardName;
        }

        public void setCardName(final String cardName) {
            this.cardName = cardName;
        }

        public Integer getLastFiveDigits() {
            return lastFiveDigits;
        }

        public void setLastFiveDigits(final Integer lastFiveDigits) {
            this.lastFiveDigits = lastFiveDigits;
        }

        public String getTokenizedCardNumber() {
            return tokenizedCardNumber;
        }

        public void setTokenizedCardNumber(final String tokenizedCardNumber) {
            this.tokenizedCardNumber = tokenizedCardNumber;
        }

        public Integer getMemberSince() {
            return memberSince;
        }

        public void setMemberSince(final Integer memberSince) {
            this.memberSince = memberSince;
        }

        @Override
        public String toString() {
            return "CardInContext{" +
                "cardArtURL='" + cardArtURL + '\'' +
                ", cardName='" + cardName + '\'' +
                '}';
        }
    }

    class CreditCardInfo implements Serializable {

        private static final long serialVersionUID = 2342605122361146582L;
        private String cardArtURL;
        private String cardName;
        private String redirectURL;

        public CreditCardInfo() {

        }

        public CreditCardInfo(final String cardArtURL, final String cardName, final String redirectURL) {
            this.cardArtURL = cardArtURL;
            this.cardName = cardName;
            this.redirectURL = redirectURL;
        }

        public String getCardArtURL() {
            return cardArtURL;
        }

        public void setCardArtURL(final String cardArtURL) {
            this.cardArtURL = cardArtURL;
        }

        public String getCardName() {
            return cardName;
        }

        public void setCardName(final String cardName) {
            this.cardName = cardName;
        }

        public String getRedirectURL() {
            return redirectURL;
        }

        public void setRedirectURL(final String redirectURL) {
            this.redirectURL = redirectURL;
        }

        @Override
        public String toString() {
            return "CreditCardInfo{" +
                "cardArtURL='" + cardArtURL + '\'' +
                ", cardName='" + cardName + '\'' +
                ", redirectURL='" + redirectURL + '\'' +
                '}';
        }
    }
}

