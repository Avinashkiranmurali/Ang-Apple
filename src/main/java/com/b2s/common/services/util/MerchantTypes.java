package com.b2s.common.services.util;

import java.util.HashMap;
import java.util.Map;

/**
  * <p>
  *  Encapsulates all merchant type, merchant code and description.
   @author sjonnalagadda
  * Date: 7/11/13
  * Time: 4:13 PM
  *
 **/

public enum MerchantTypes {

    BARNESANDNOBLE(400,"Barnes And Noble"),
    BESTBUY(200,"Best Buy"),
    BUYDOTCOM(500,"Buy.com"),
    ALMO(30000,"ALMO"),
    BABYAGEDOTCOM(30025,"BabyAge.com"),
    BAMBECO(30050,"Bambeco"),
    BUILDDOTCOM(30075,"Build.com"),
    CHASEFINANCIAL(90000, "Chase Financial Services"),
    COOKINGDOTCOM(30225,"Cooking.com"),
    DANDH(30250,"D and H"),
    DISCOUNTGOLFWORLD(30300,"Discount Golf World"),
    DRUGSTOREDOTCOM(30350, "Drugstore.com"),
    DSISYSTEMS(30355,"DSI Systems"),
    EBAGS(30360,"eBags"),
    ENTERTAINMENTEARTH(30365,"Entertainment Earth"),
    HIPDIGITAL(100, "HipDigital"),
    ICEDOTCOM(30375,"Ice.com"),
    INGRAMCDF(30400,"Ingram CDF"),
    OVERSTOCK(30425, "Overstock"),
    INGRAMMICRO(30450,"Ingram Micro"),
    PETRADOTCOM(30455, "Petra.com"),
    PREMCO(30850,"Premco"),
    REPLINK(300,"RepLink"),
    SCENTIMENTS(30460, "Scentiments.com"),
    SEARS(30475, "Sears"),
    SED(30550,"SED"),
    SHOEBUYDOTCOM(30575, "Shoebuy.com"),
    SHOPPERSCHOICE(30585, "ShoppersChoice.com"),
    SPEXPLUS(30600,"Spex Plus"),
    SPORTSAUTHORITY(30650, "Sports Authority"),
    SWI(30700, "SWI"),
    TEAMFANSHOP(30750, "Team Fan Shop"),
    VLCDIST(30800, "VLC Distribution"),
    UNBEATABLESALE(30825, "Unbeatablesales.com"),
    MERCENT(30830, "Mercent"),
    TICKETNETWORK(10030, "Ticket Network"),
    GAD(10040, "Great American Days"),
    VIATOR(10080, "Viator"),
    CASHSTAR(60000, "CashStar"),
    GIFTANGO(60100, "Giftango"),
    NGC(60200, "NGC"),
    BASSPRO(30060, "Bass Pro Shops"),
    THANKYOU(31000, "Thankyou"),
    KOINZ(70000, "Koinz"),
    MARITZ(30845, "Maritz"),
    RYMAX(30370, "Rymax");

    private final Integer merchantId;
    private final String merchantName;

    private static final Map<Integer,MerchantTypes> MERCHANT_ID_TO_NAME_MAPPER = new HashMap<Integer,MerchantTypes>();

    static {
        for(final MerchantTypes merchantTypes : MerchantTypes.values()){
            MERCHANT_ID_TO_NAME_MAPPER.put(merchantTypes.getMerchantId(), merchantTypes);
        }
    }

    /**
     * Constructs a Merchant object
     *
     * @param merchantId unique code for merchant.
     * @param merchantName display name for merchant.
     */

    private MerchantTypes(final Integer merchantId, final String merchantName){
        this.merchantId = merchantId;
        this.merchantName = merchantName;
    }

    /**
     * @return Integer unique code for merchant.
     */

    Integer getMerchantId(){
        return merchantId;
    }

    /**
     * @return String merchant name.
     */

    String getMerchantName(){
        return merchantName;
    }

    /**
     * @return MerchantTypes based on merchant unique code .
     */

    public static MerchantTypes get(final int merchantIdInput) {
        return MERCHANT_ID_TO_NAME_MAPPER.get(merchantIdInput);
    }
}
