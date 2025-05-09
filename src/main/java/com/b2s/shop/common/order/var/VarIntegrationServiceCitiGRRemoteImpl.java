package com.b2s.shop.common.order.var;

import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.dao.MerchantListDao;
import com.b2s.rewards.apple.dao.OrderLineAttributeDao;
import com.b2s.rewards.apple.integration.model.*;
import com.b2s.rewards.apple.model.OrderAttributeValue;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.model.Merchant;
import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.money.CurrencyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.VIS_ALTERNATE_ADDRESS_INDICATOR;
import static com.b2s.rewards.common.util.CommonConstants.VIS_PRODUCT_TYPE_MERCHANDISE;

/**
 ** Created by rpillai on 8/21/2017
 */
@Service
public class VarIntegrationServiceCitiGRRemoteImpl extends VarIntegrationServiceRemoteImpl {

    private static final Logger LOG = LoggerFactory.getLogger(VarIntegrationServiceCitiGRRemoteImpl.class);

    private static final String CONTEXT_PATH = "citigr";

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private MerchantListDao merchantDao;

    @Autowired
    private OrderLineAttributeDao orderLineAttributeDao;


    @Override
    public String getBaseUrl(final String varId) {
        StringBuilder baseUrl = new StringBuilder(applicationProperties.getProperty(CommonConstants.VIS_ACCOUNTS_SERVICE_URL));
        baseUrl.append("/");
        baseUrl.append(CONTEXT_PATH);
        setHttpClientUtil(varId);
        return baseUrl.toString();
    }

    @Override
    public RedemptionResponse performRedemption(final Order order, final User user, final Program program) throws Exception {

        String targetCurrency="";
        ConversionRateInfo conversionRateInfo;
        if (Objects.nonNull(program)) {
            if (Objects.isNull(program.getTargetCurrency()) && Objects.nonNull(user.getLocale())) {
                targetCurrency = Currency.getInstance(user.getLocale()).getCurrencyCode();
            } else {
                targetCurrency = program.getTargetCurrency().getCode();
            }
        }
        try {
            //Base Currency for getting Epsilon's FxRate will always be in USD
            conversionRateInfo = getConversionRate(user.getVarId(), CurrencyUnit.USD.toString(), targetCurrency);
        }
        catch (Exception e) {
            LOG.error("Error while performing getConversionRate for var id: {} and program id: {}" +" for the order: {}", user.getVarId(), user.getProgramId(), order.getOrderId(), e);
            throw e;
        }

        RedemptionRequest redemptionRequest = getRedemptionRequest(order, user, program, conversionRateInfo);
        final String visUrl = getBaseUrl(user.getVarId()) + "/redemptions";
        RedemptionResponse redemptionResponse = null;
        try {
            redemptionResponse = visHttpClientUtil.getHttpResponse(visUrl, RedemptionResponse.class, HttpMethod.POST, redemptionRequest);
        } catch (Exception e) {
            LOG.error("Error while performing redemption for user id: {}, having var id: {} and program id: {} for the order: {}", user.getUserId(), user.getVarId(), user.getProgramId(), order.getOrderId(), e);
            throw e;
        }
        return redemptionResponse;
    }

    private RedemptionRequest getRedemptionRequest(Order order, User user, Program program, ConversionRateInfo conversionRateInfo) {
        RedemptionRequest redemptionRequest = new RedemptionRequest();
        AccountIdentifier accountIdentifier = new AccountIdentifier();
        List<RedemptionOrderLine> redemptionOrderLines = new ArrayList<RedemptionOrderLine>();
        ShipmentDeliveryInfo shipmentDeliveryInfo = new ShipmentDeliveryInfo();

        //AccountIdentifier
        accountIdentifier.setVarId(order.getVarId());
        accountIdentifier.setProgramId(order.getProgramId());
        accountIdentifier.setAccountId(user.getUserId());

        if(user instanceof UserCiti) {
            accountIdentifier.setAgentId(((UserCiti)user).getAgentId());
        }

        if(MapUtils.isNotEmpty(user.getAdditionalInfo())) {
            accountIdentifier.setAlternateIds(getAlternateIds(user, program));
            populateCitiAdditionalInfo(order, user, redemptionRequest);
        }
        SplitTenderInfo splitTenderInfo = createSplitTenderInfo(order, user);

        //Delivery Information
        Address shippingAddress = populateShippingAddress(order);
        shippingAddress.setLine3(order.getAddr3());
        populateDeliveryInformation(order, shipmentDeliveryInfo, shippingAddress);

        //  OrderLines to Redemption OrderLines
        populateRedemptionOrderLines(order, program, conversionRateInfo, redemptionRequest, redemptionOrderLines,
                shipmentDeliveryInfo, splitTenderInfo);

        //Construct request JSON
        redemptionRequest.setAccountIdentifier(accountIdentifier);
        redemptionRequest.setOrderId(String.valueOf(order.getOrderId()));
        redemptionRequest.setOrderDate(order.getOrderDateAsISO8601());
        redemptionRequest.setDelivery(shipmentDeliveryInfo);
        redemptionRequest.setOrderlines(redemptionOrderLines);
        redemptionRequest.setTotalPrice(getPriceWithCurrencyCode(order.getCurrencyCode(),order.getOrderTotalWithSupplierTaxInMoney()
                .getAmount().doubleValue()));
        redemptionRequest.setTotalPointsPrice(order.getOrderTotalInPoints());
        redemptionRequest.setStartBalance(user.getBalance());

        return redemptionRequest;
    }

    private void populateRedemptionOrderLines(final Order order, final Program program,
                                              final ConversionRateInfo conversionRateInfo, final RedemptionRequest redemptionRequest,
                                              final List<RedemptionOrderLine> redemptionOrderLines,
                                              final ShipmentDeliveryInfo shipmentDeliveryInfo,
                                              final SplitTenderInfo splitTenderInfo) {
        OrderLine lineItem;
        for (Object orderLine : order.getOrderLines()) {
            lineItem = (OrderLine) orderLine;
            if (!lineItem.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S) && !lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S)
                    && !lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S)) {
                UnitPriceInfo unitPriceInfo = new UnitPriceInfo();
                BigDecimal totalPrice = updateCitiPriceInfoAndGetTotalPrice(order, lineItem, unitPriceInfo);

                HashMap<String, Object> orderLineAdditionalInfo = new HashMap<String, Object>();
                RedemptionOrderLine redemptionOrderLine = populateRedemptionOrderLine(order, program, lineItem, unitPriceInfo, totalPrice);
                redemptionOrderLine.setCostPerPoint(getPriceWithCurrencyCode(order.getCurrencyCode(), new BigDecimal(1).divide(new BigDecimal(lineItem.getConvRate()).multiply(new BigDecimal(100)), 18, RoundingMode.FLOOR).setScale(4, RoundingMode.HALF_UP).doubleValue()));
                redemptionOrderLine.setProductType(VIS_PRODUCT_TYPE_MERCHANDISE);

                //VOM specific
                getFxRate(conversionRateInfo, lineItem, redemptionOrderLine);

                final ProductDetails productDetails = getProductDetails(lineItem);
                redemptionOrderLine.setProductDetails(productDetails);

                populateOrderLineAdditionalInfo(lineItem, orderLineAdditionalInfo);
                orderLineAdditionalInfo.put(CommonConstants.VAR_ITEM_MARGIN,lineItem.getVarItemMargin());
                redemptionOrderLine.setAdditionalInfo(orderLineAdditionalInfo);
                redemptionOrderLine.setDelivery(shipmentDeliveryInfo);

                //add to redemptionOrderLines
                redemptionOrderLines.add(redemptionOrderLine);
            } else {
                if (lineItem != null && lineItem.getOrderLinePoints() != 0) {

                    getSplitTenderInfo(order, program, redemptionRequest, splitTenderInfo, lineItem);
                    //  CashBuyInPoints && CashBuyInPrice are deprecated but used for backwards compatibility.
                    redemptionRequest.setCashBuyInPoints(-lineItem.getOrderLinePoints());
                    populateCashBuyInPrice(order, redemptionRequest, lineItem);
                }
            }

            setPayrollDeductionInfo(order, redemptionRequest, lineItem);
            setDiscountInfo(order, redemptionRequest, lineItem);

        }
    }

    private BigDecimal updateCitiPriceInfoAndGetTotalPrice(final Order order, final OrderLine lineItem,
                                                           final UnitPriceInfo unitPriceInfo) {
        BigDecimal totalPrice = generatePriceInfoAndGetTotalPrice(order, lineItem, unitPriceInfo);
        BigDecimal totalTaxInMoney = new BigDecimal(lineItem.getSupplierTaxPrice()).divide(BigDecimal.valueOf(100)).setScale(2);
        totalPrice = totalPrice.add(totalTaxInMoney);

        //Taxes is 0.0 for Epsilon, supplier price, unit price is added with tax after VAT Changes.
        unitPriceInfo.setTaxes(getPriceWithCurrencyCode(order.getCurrencyCode(), 0.0D));
        unitPriceInfo.setSupplierPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), totalPrice.setScale(2).doubleValue()));

        unitPriceInfo.setUnitPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), totalPrice.setScale(2).doubleValue()));
        unitPriceInfo.setUnitPointsPrice(lineItem.getItemPoints().intValue());

        totalPrice = totalPrice.add(BigDecimal.valueOf(lineItem.getShippingPoints() * lineItem.getConvRate()));
        return totalPrice;
    }

    private void getFxRate(ConversionRateInfo conversionRateInfo, OrderLine lineItem,
                           RedemptionOrderLine redemptionOrderLine) {
        if (conversionRateInfo !=null) {
            redemptionOrderLine.setConversionRateInfo(conversionRateInfo);
            //save fxRate in order_line.fx_rate
            lineItem.setFxRate(conversionRateInfo.getRate());
        }
    }

    private void populateCashBuyInPrice(Order order, RedemptionRequest redemptionRequest, OrderLine lineItem) {
        redemptionRequest.setCashBuyInPrice(getPriceWithCurrencyCode(order.getCurrencyCode(),
                new BigDecimal(-lineItem.getVarOrderLinePrice())
                        .add(new BigDecimal(-lineItem.getVarItemProfitPrice()))
                        .divide(new  BigDecimal(100), 2, RoundingMode.UNNECESSARY)
                        .doubleValue()));
    }

    private void getSplitTenderInfo(Order order, Program program, RedemptionRequest redemptionRequest, SplitTenderInfo splitTenderInfo, OrderLine lineItem) {
        splitTenderInfo.setPointsPurchased(-lineItem.getOrderLinePoints());
        splitTenderInfo.setCash(getPriceWithCurrencyCode(order.getCurrencyCode(),
                new BigDecimal(-lineItem.getVarOrderLinePrice())
                        .add(new BigDecimal(-lineItem.getVarItemProfitPrice()))
                        .divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY)
                        .doubleValue()));

        final String ccVarMargin = (String) program.getConfig().get(CommonConstants.CC_VAR_MARGIN);
        if(StringUtils.isNotEmpty(ccVarMargin)){
            splitTenderInfo.setCcVarMargin(Float.valueOf(ccVarMargin));
        }else{
            splitTenderInfo.setCcVarMargin(0F);
        }

        final String processingFeeRate = (String) program.getConfig().get(CommonConstants.PROCESSING_FEE_RATE);
        if(StringUtils.isNotEmpty(processingFeeRate)){
            splitTenderInfo.setProcessingFeeRate(Float.valueOf(processingFeeRate));
        }else{
            splitTenderInfo.setProcessingFeeRate(0F);
        }

        redemptionRequest.setSplitTenderInfo(splitTenderInfo);
    }

    private void populateCitiAdditionalInfo(final Order order, final User user,
                                            final RedemptionRequest redemptionRequest) {
        // additional info  -  It is new map object required for VOM integration
        Map<String, String> additionalInfo = user.getAdditionalInfo();
        additionalInfo.put(CommonConstants.VIS_ADDTNL_INFO_KEY_COUNTRY_CODE, user.getCountry());
        additionalInfo.put(CommonConstants.VIS_ADDTNL_INFO_KEY_LANGUAGE_CODE, user.getLocale().getISO3Language().toUpperCase());

        final String orderSummaryUrl = constructOrderSummaryURL(user.getHostName(),CommonConstants.ORDER_SUMMARY_ENDPOINT,order.getOrderId());
        if (StringUtils.isNotBlank(orderSummaryUrl) ) {
            additionalInfo.put(CommonConstants.ORDER_SUMMARY_URL,orderSummaryUrl);
        }
        user.setAdditionalInfo(additionalInfo);
        populateAdditionalInfos(user, redemptionRequest, CommonConstants.APPLE_SUPPLIER_ID_STRING);
        redemptionRequest.getAdditionalInfo().put(VIS_ALTERNATE_ADDRESS_INDICATOR, Boolean.toString(!order.getAddr1().equalsIgnoreCase(user.getAddr1())));
    }

    private ConversionRateInfo getConversionRate(final String varId, final String baseCurrency, final String targetCurrency)throws Exception {
        final StringBuilder visURL=new StringBuilder();
        visURL.append(getBaseUrl(varId)).append("/fxRate/").append(baseCurrency).append("/").append(targetCurrency);
        return visHttpClientUtil.getHttpResponse(visURL.toString(), ConversionRateInfo.class, HttpMethod.GET, null);
    }

    private String constructOrderSummaryURL(final String hostname, final String orderSummaryEndpoint,final Long orderId){
        final StringBuilder orderSummaryURL = new StringBuilder();
        return orderSummaryURL.append(hostname).append(orderSummaryEndpoint).append("orderId=").append(orderId)
                .toString();
    }

    @Override
    public CancelRedemptionResponse performPartialCancelRedemption(final Order order, final User user,
                                                                   final Program program)
            throws B2RException {

        final String visUrl = getBaseUrl(user.getVarId()) + "/cancellations/partial";
        PartialCancelRedemptionRequest partialCancelRedemptionRequest =
                buildPartialCancelRedemptionRequest(order, user);

        return visHttpClientUtil.getHttpResponse(visUrl, CancelRedemptionResponse.class, HttpMethod.POST, partialCancelRedemptionRequest);
    }

    private PartialCancelRedemptionRequest buildPartialCancelRedemptionRequest(final Order order, final User user) {
        final PartialCancelRedemptionRequest cancelRedemptionRequest = new PartialCancelRedemptionRequest();

        cancelRedemptionRequest.setOrderId(String.valueOf(order.getOrderId()));
        cancelRedemptionRequest.setCancellationId(order.getVarOrderId() + "1" +System.currentTimeMillis());
        cancelRedemptionRequest.setVarOrderId(order.getVarOrderId());

        List<OrderAttributeValue> orderAttributes = order.getOrderAttributeValues();
        if(CollectionUtils.isNotEmpty(orderAttributes)) {
             cancelRedemptionRequest.setCurrency(orderAttributes.stream().filter(orderAttribute -> orderAttribute.getName().equals(CommonConstants.VIS_CURRENCY)).findFirst().map(OrderAttributeValue::getValue).orElse(null));
        }

        final BigDecimal totalFeesInMoney = BigDecimal.ZERO;
        final BigDecimal totalTaxInMoney = BigDecimal.ZERO;
        int totalItemPoints = 0;

        for(OrderLine lineItem : order.getOrderLines()) {
            if (lineItem.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S)) {
                cancelRedemptionRequest.setCashBuyInRefund(getPriceWithCurrencyCode(order.getCurrencyCode(),
                        new BigDecimal(-lineItem.getVarOrderLinePrice())
                                .add(new BigDecimal(-lineItem.getVarItemProfitPrice()))
                                .divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY)
                                .doubleValue()));

                totalItemPoints = -lineItem.getOrderLinePoints();


            }else{
                totalFeesInMoney.add(lineItem.getTotalFeesInMoneyMinor());
                totalTaxInMoney.add(lineItem.getTotalTaxesInMoneyMinor());
                totalItemPoints += lineItem.getItemPoints().intValue();
            }
        }

        OrderLine lineItem = order.getOrderLines().get(0);
        final OrderLineCancellation orderLineCancellation = new OrderLineCancellation();
        orderLineCancellation.setOrderLineId(String.valueOf(1));
        orderLineCancellation.setCostPerPoint(getPriceWithCurrencyCode(order.getCurrencyCode(), new BigDecimal(1).divide(new BigDecimal(lineItem.getConvRate()).multiply(new BigDecimal(100)), 18, RoundingMode.FLOOR).setScale(4, RoundingMode.HALF_UP).doubleValue()));

        final UnitPriceInfo unitPriceInfo = new UnitPriceInfo();

        unitPriceInfo.setFees(getPriceWithCurrencyCode(order.getCurrencyCode(), totalFeesInMoney.divide(BigDecimal.valueOf(100)).setScale(2).doubleValue()));
        unitPriceInfo.setTaxes(getPriceWithCurrencyCode(order.getCurrencyCode(), totalTaxInMoney.divide(BigDecimal.valueOf(100)).setScale(2).doubleValue()));

        if(!order.isSplitTenderOrder()) {
            unitPriceInfo.setUnitPointsPrice(order.getOrderTotalInPoints());
            unitPriceInfo.setUnitPrice(getPriceWithCurrencyCode(order.getCurrencyCode(),order.getOrderTotalWithSupplierTaxInMoney()
                    .getAmount().doubleValue()));
        }else{
            unitPriceInfo.setUnitPointsPrice(totalItemPoints);
            unitPriceInfo.setUnitPrice(getPriceWithCurrencyCode(order.getCurrencyCode(),
                    new BigDecimal(lineItem.getVarOrderLinePrice())
                            .add(new BigDecimal(-lineItem.getVarItemProfitPrice()))
                            .divide(new  BigDecimal(100), 2, RoundingMode.UNNECESSARY)
                            .doubleValue()));
        }

        orderLineCancellation.setUnitPriceInfo(unitPriceInfo);

        //set varOrderLineId if order_line.order_line_type is not blank
        if (StringUtils.isNotBlank(lineItem.getOrderLineType())) {
            orderLineCancellation.setVarOrderLineId(lineItem.getOrderLineType());
        }

        orderLineCancellation.setQuantityToCancel(1);
        orderLineCancellation.setTotalPointsRefund(0);
        orderLineCancellation.setMerchant(Merchant.APPLE.name());
        orderLineCancellation.setCancelReason(CancelReason.RETURNED);


        final List<OrderLineCancellation> orderLinesToCancel = new ArrayList<>();
        orderLinesToCancel.add(orderLineCancellation);
        cancelRedemptionRequest.setOrderLines(orderLinesToCancel);

        final AccountIdentifier accountIdentifier = new AccountIdentifier();

        accountIdentifier.setVarId(order.getVarId());
        accountIdentifier.setProgramId(order.getProgramId());
        accountIdentifier.setAccountId(order.getUserId());
        cancelRedemptionRequest.setAccountIdentifier(accountIdentifier);

        Map<String, String> additionalInfo = user.getAdditionalInfo();
        additionalInfo.put(CommonConstants.VIS_ADDTNL_INFO_KEY_COUNTRY_CODE, user.getCountry());
        additionalInfo.put(CommonConstants.VIS_ADDTNL_INFO_KEY_LANGUAGE_CODE, user.getLocale().getISO3Language().toUpperCase());
        additionalInfo.put(VIS_ALTERNATE_ADDRESS_INDICATOR, Boolean.toString(!order.getAddr1().equalsIgnoreCase(user.getAddr1())));
        cancelRedemptionRequest.setAdditionalInfo(additionalInfo);

        cancelRedemptionRequest.setFullReturn(true); // This called during error. So always full return only.
        cancelRedemptionRequest.setCancelDateTime(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));

        return cancelRedemptionRequest;
    }


}
