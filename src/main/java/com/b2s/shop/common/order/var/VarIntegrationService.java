package com.b2s.shop.common.order.var;

import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.integration.model.*;
import com.b2s.rewards.apple.model.BillTo;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.model.DeliveryMethod;
import com.b2s.service.model.ShippingMethod;
import com.b2s.shop.common.User;
import org.apache.commons.lang.StringUtils;
import org.apache.http.MethodNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 *Created by rpillai on 3/3/2016.
 */
public interface VarIntegrationService {

    RedemptionResponse performRedemption(final Order order, final User user, final Program program) throws Exception;

    CancelRedemptionResponse performPartialCancelRedemption(final Order order, final User user, final Program program) throws IOException, Exception;

    CancelRedemptionResponse performCancelRedemption(final Order order, final int totalPointsRefund) throws IOException, Exception;

    int getLocalUserPoints(User user) throws MethodNotSupportedException;
    
    int getUserPoints(String varId, String programId, String accountId, Map<String, String> addInfo) throws MethodNotSupportedException, B2RException;

    AccountInfo getUserProfile(String varId, String accountId, Map<String, String> additionalInfo);

    String getBaseUrl(final String contextPath);

    SessionResponse getUserSession(final User user);

    /**
     * @param currencyCode
     * @param price
     * @return Concatenated value as  "USD 3600.00"
     */
    default String getPriceWithCurrencyCode(String currencyCode, Double price) {
        return currencyCode.concat(" ").concat(String.valueOf(price));
    }

    default SplitTenderInfo createSplitTenderInfo(Order order, User user) {
        SplitTenderInfo splitTenderInfo=new SplitTenderInfo();

        if(user.getBillTo()!=null){
            BillTo billTo=user.getBillTo();
            Address billingAddress =new Address();
            billingAddress.setLine1(billTo.getAddressLine());
            billingAddress.setCity(billTo.getCity());
            billingAddress.setCountryCode(billTo.getCountry());
            billingAddress.setStateCode(billTo.getState());
            billingAddress.setPostalCode(billTo.getZip());

            splitTenderInfo.setBillingAddress(billingAddress);
            splitTenderInfo.setFirstName(billTo.getFirstName());
            splitTenderInfo.setLastName(billTo.getLastName());
        }

        order.getOrderAttributeValues().forEach(attributeValue -> {
            switch (attributeValue.getName()){
                case CommonConstants.CREDIT_CARD_LAST_FOUR_DIGIT :
                    splitTenderInfo.setLastFourDigitsOfCreditCard(attributeValue.getValue());
                    break;
                case CommonConstants.CREDIT_CARD_TYPE :
                    splitTenderInfo.setCreditCardType(attributeValue.getValue());
                    break;
                default:
                    break;
            }
        });
        return splitTenderInfo;
    }

    default void populateDeliveryInformation(final Order order,
        final ShipmentDeliveryInfo shipmentDeliveryInfo, final Address shippingAddress) {
        //Delivery Information
        shipmentDeliveryInfo.setDeliveryMethod(DeliveryMethod.DOMESTIC_SHIPPING.toString());
        shipmentDeliveryInfo.setEmailAddress(order.getEmail());
        shipmentDeliveryInfo.setFirstName(order.getFirstname());
        shipmentDeliveryInfo.setLastName(order.getLastname());
        shipmentDeliveryInfo.setPhoneNumber(order.getPhone());
        shipmentDeliveryInfo.setShippingAddress(shippingAddress);
        shipmentDeliveryInfo.setShippingMethod(ShippingMethod.STANDARD.toString());
    }

    default Address populateShippingAddress(final Order order) {
        Address shippingAddress = new Address();
        shippingAddress.setCity(order.getCity());
        shippingAddress.setCountryCode(order.getCountryCode());
        shippingAddress.setLine1(order.getAddr1());
        shippingAddress.setLine2(order.getAddr2());
        shippingAddress.setLine3(""); //TODO:  which value??
        shippingAddress.setPostalCode(order.getZip());
        shippingAddress.setStateCode(order.getState());
        return shippingAddress;
    }

    default void populateAdditionalInfos(final User user,
        final RedemptionRequest redemptionRequest, final String supplierId) {
        // additional info  -  It is new map object required for VOM integration
        redemptionRequest.setAdditionalInfo(user.getAdditionalInfo());
        //adding currency for VIS request
        redemptionRequest.setCurrency(user.getAdditionalInfo().get(CommonConstants.VIS_CURRENCY));

        if(user.getAdditionalInfo().containsKey(CommonConstants.CLIENT_CODE_CAMEL_CASE) && user.getAdditionalInfo().get(CommonConstants.CLIENT_CODE_CAMEL_CASE) != null) {
            redemptionRequest.getAdditionalInfo().put(CommonConstants.CLIENT_CODE, user.getAdditionalInfo().get(CommonConstants.CLIENT_CODE_CAMEL_CASE));
        }
        redemptionRequest.getAdditionalInfo().put(CommonConstants.SUPPLIER_ID, supplierId);
        redemptionRequest.getAdditionalInfo().put(CommonConstants.SHIP_DESC,"Static shipping description - Message to be confirmed");
        if ( StringUtils.isNotBlank(user.getPricingTier()) ) {
            redemptionRequest.getAdditionalInfo().put(CommonConstants.PRICING_TIER, user.getPricingTier());
        }
    }

    default Map<String, String> getAlternateIds(final User user, final Program program) {
        final Map<String, String> alternateIds = createAlternateIds(user);
        if (StringUtils.isNotBlank(user.getSid())) {
            alternateIds.put(CommonConstants.SID, user.getSid());
        }
        return alternateIds;
    }

    default Map<String, String> createAlternateIds(final User user) {
        final Map<String, String> alternateIds = new HashMap<>();
        final String fraudSessionId = user.getAdditionalInfo().get(CommonConstants.FRAUD_SESSION_ID);
        if(StringUtils.isNotBlank(fraudSessionId)) {
            alternateIds.put(CommonConstants.FRAUD_SESSION_ID, fraudSessionId);
        }
        return alternateIds;
    }

    default void populateUnitPriceInfo(final Order order, final OrderLine lineItem, final UnitPriceInfo unitPriceInfo) {
        unitPriceInfo.setAdditionalCost(new HashMap<String, String>());  // N/A for apple but need a placeholder for VIS
        unitPriceInfo.setMarkups(getPriceWithCurrencyCode(order.getCurrencyCode(), 0.0D));
        unitPriceInfo.setShippingCost(getPriceWithCurrencyCode(order.getCurrencyCode(), lineItem.getShippingPoints() * lineItem.getConvRate()));
        unitPriceInfo.setSupplierShippingCost(getPriceWithCurrencyCode(order.getCurrencyCode(), lineItem.getSupplierShippingPrice() * lineItem.getConvRate()));  // TODO seems to be wrong

        unitPriceInfo.setSupplierTax(getPriceWithCurrencyCode(order.getCurrencyCode(), 0.0D));
    }

    default BigDecimal updatePriceInfoAndGetTotalPrice(final Order order, final OrderLine lineItem, final UnitPriceInfo unitPriceInfo) {
        BigDecimal totalPrice = generatePriceInfoAndGetTotalPrice(order, lineItem, unitPriceInfo);

        BigDecimal totalTaxInMoney = lineItem.getTotalTaxesInMoneyMinor();
        unitPriceInfo.setTaxes(getPriceWithCurrencyCode(order.getCurrencyCode(), totalTaxInMoney.doubleValue()));
        totalPrice = totalPrice.add(totalTaxInMoney);
        //D-09050 ALL Vars ItemCost = supplier_item_price + b2s_item_profit
        unitPriceInfo.setSupplierPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), new BigDecimal(lineItem.getSupplierItemPrice() + lineItem.getB2sItemProfitPrice()).divide(BigDecimal.valueOf(100)).setScale(2).doubleValue()));

        totalPrice = totalPrice.add(BigDecimal.valueOf(lineItem.getShippingPoints() * lineItem.getConvRate()));
        return totalPrice;
    }

    default BigDecimal generatePriceInfoAndGetTotalPrice(final Order order, final OrderLine lineItem,
        final UnitPriceInfo unitPriceInfo) {
        final Logger LOG = LoggerFactory.getLogger(VarIntegrationService.class);
        populateUnitPriceInfo(order, lineItem, unitPriceInfo);

        BigDecimal totalPrice = new BigDecimal(lineItem.getSupplierItemPrice()).divide(BigDecimal.valueOf(100)).setScale(2);

        BigDecimal totalFeesInMoney = lineItem.getTotalFeesInMoneyMinor();
        BigDecimal b2sTaxPrice = new BigDecimal(lineItem.getB2sTaxPrice() != null ? lineItem.getB2sTaxPrice() : 0).divide(BigDecimal.valueOf(100)).setScale(2);
        LOG.info("totalFeesInMoney -------------- {}", totalFeesInMoney);
        LOG.info("b2sTaxPrice -------------- {}", b2sTaxPrice);
        final BigDecimal totalFees = totalFeesInMoney.add(b2sTaxPrice);
        LOG.info("total -------------- {}", totalFees);
        unitPriceInfo.setFees(getPriceWithCurrencyCode(order.getCurrencyCode(), totalFees.doubleValue()));
        totalPrice = totalPrice.add(totalFeesInMoney);
        return totalPrice;
    }
}
