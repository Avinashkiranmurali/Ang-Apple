package com.b2s.shop.common.order.var;

import com.b2s.apple.entity.MerchantEntity;
import com.b2s.apple.entity.OrderLineAttributeEntity;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.db.model.OrderLineAttribute;
import com.b2s.rewards.apple.dao.MerchantListDao;
import com.b2s.rewards.apple.dao.OrderLineAttributeDao;
import com.b2s.rewards.apple.integration.model.*;
import com.b2s.rewards.apple.model.Program;
import com.b2s.apple.services.CartOrderConverterService;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.MethodNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static com.b2s.rewards.apple.util.AppleUtil.gsonToJsonString;
import static com.b2s.rewards.common.util.CommonConstants.*;

@Service("VIS_WF_MOCK")
public class VarIntegrationServiceWFMockImpl implements VarIntegrationService {
    private static final Logger LOG = LoggerFactory.getLogger(VarIntegrationServiceWFMockImpl.class);
    public static final String METHOD_NOT_SUPPORTED_FOR_REMOTE_PROGRAMS = "Method Not Supported for Remote Programs!";
    public static final String JSON_TAG_END_NEW_LINE = "    },\n";
    public static final String OPEN_CURLY_BRACE_NEW_LINE = "            {\n";
    public static final String CLOSE_CURLY_BRACE_NEW_LINE = "            }\n";
    public static final String CLOSE_SQUARE_BRACKET_NEW_LINE = "        ],\n";

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private OrderLineAttributeDao orderLineAttributeDao;

    @Autowired
    private MerchantListDao merchantDao;

    @Override
    public String getBaseUrl(final String contextPath) {
        StringBuilder baseUrl = new StringBuilder(applicationProperties.getProperty(CommonConstants.VIS_ACCOUNTS_SERVICE_URL));
        if(StringUtils.isNotBlank(contextPath)) {
            baseUrl.append("/");
            baseUrl.append(contextPath.toLowerCase());
        }
        return baseUrl.toString();
    }

    private RedemptionRequest getRedemptionRequest(Order order, User user, Program program) {
        RedemptionRequest redemptionRequest = new RedemptionRequest();
        AccountIdentifier accountIdentifier = new AccountIdentifier();
        List<RedemptionOrderLine> redemptionOrderLines = new ArrayList<RedemptionOrderLine>();
        ShipmentDeliveryInfo shipmentDeliveryInfo = new ShipmentDeliveryInfo();

        //AccountIdentifier
        accountIdentifier.setVarId(order.getVarId());
        accountIdentifier.setProgramId(order.getProgramId());
        accountIdentifier.setAccountId(user.getUserId());
        if(MapUtils.isNotEmpty(user.getAdditionalInfo())) {
            accountIdentifier.setAlternateIds(getAlternateIds(user, program));
            populateAdditionalInfos(user, redemptionRequest, CommonConstants.APPLE_SUPPLIER_ID_STRING);

        }
        // Split TenderInfo
        SplitTenderInfo splitTenderInfo = createSplitTenderInfo(order, user);

        //Delivery Information
        populateDeliveryInformation(order, shipmentDeliveryInfo, populateShippingAddress(order));

        //  OrderLines to Redemption OrderLines
        populateRedemptionOrderLines(order, program, redemptionRequest,
                redemptionOrderLines, shipmentDeliveryInfo, splitTenderInfo);

        //Construct request JSON
        redemptionRequest.setAccountIdentifier(accountIdentifier);
        redemptionRequest.setOrderId(String.valueOf(order.getOrderId()));
        redemptionRequest.setOrderDate(order.getOrderDateAsISO8601());
        redemptionRequest.setDelivery(shipmentDeliveryInfo);
        redemptionRequest.setOrderlines(redemptionOrderLines);
        redemptionRequest.setTotalPrice(getPriceWithCurrencyCode(order.getCurrencyCode(),order.getOrderTotalInMoney()
                .getAmount().doubleValue()));
        redemptionRequest.setTotalPointsPrice(order.getOrderTotalInPoints());
        redemptionRequest.setStartBalance(user.getBalance());


        return redemptionRequest;
    }

    private void populateRedemptionOrderLines(Order order, Program program, RedemptionRequest redemptionRequest, List<RedemptionOrderLine> redemptionOrderLines, ShipmentDeliveryInfo shipmentDeliveryInfo, SplitTenderInfo splitTenderInfo) {
        OrderLine lineItem;
        for (Object orderLine : order.getOrderLines()) {
            lineItem = (OrderLine) orderLine;
            if (!lineItem.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S)) {
                RedemptionOrderLine redemptionOrderLine =
                        populateRedemptionOrderLine(order, program, shipmentDeliveryInfo, lineItem);

                //add to redemptionOrderLines
                redemptionOrderLines.add(redemptionOrderLine);
            } else {
                if (lineItem.getOrderLinePoints() != 0) {

                    splitTenderInfo.setPointsPurchased(-lineItem.getOrderLinePoints());
                    splitTenderInfo.setCash(getPriceWithCurrencyCode(order.getCurrencyCode(),BigDecimal
                            .valueOf(-lineItem.getVarOrderLinePrice()).add(BigDecimal.valueOf(-lineItem.getVarItemMargin
                                    ())).divide(BigDecimal.valueOf(100)).doubleValue()));
                    redemptionRequest.setSplitTenderInfo(splitTenderInfo);
                    //  CashBuyInPoints && CashBuyInPrice are deprecated but used for backwards compatibility.
                    redemptionRequest.setCashBuyInPoints(-lineItem.getOrderLinePoints());
                    redemptionRequest.setCashBuyInPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), new
                            BigDecimal(-lineItem.getVarOrderLinePrice()).add(BigDecimal.valueOf(-lineItem.getVarItemMargin
                            ())).divide(BigDecimal.valueOf(100)).doubleValue()));

                }
            }

            if(lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S)){
                PayrollDeductionInfo payrollDeductionInfo=new PayrollDeductionInfo();
                payrollDeductionInfo.setTotalPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), order.getOrderTotalPayrollPrice().getAmount().doubleValue()));
                payrollDeductionInfo.setPerPayPeriodPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), order.getOrderTotalPayrollPeriodPrice().getAmount().doubleValue()));
                payrollDeductionInfo.setNumberOfPayPeriods(String.valueOf(order.getPayrollFrequency()));
                redemptionRequest.setPayrollDeductionInfo(payrollDeductionInfo);
            } else if(lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S)){
                DiscountInfo discountInfo=new DiscountInfo();
                discountInfo.setCode(lineItem.getItemId());
                discountInfo.setPrice(getPriceWithCurrencyCode(order.getCurrencyCode(),new BigDecimal(lineItem.getSupplierItemPrice()).doubleValue()));
                redemptionRequest.setDiscountInfo(discountInfo);
            }

        }
    }

    private RedemptionOrderLine populateRedemptionOrderLine(Order order, Program program, ShipmentDeliveryInfo shipmentDeliveryInfo, OrderLine lineItem) {
        UnitPriceInfo unitPriceInfo = new UnitPriceInfo();
        BigDecimal totalPrice = updatePriceInfoAndGetTotalPrice(order, lineItem, unitPriceInfo);

        if ( VAR_SCOTIA.equalsIgnoreCase(order.getVarId()) ) { // TODO - Clean up after the VIS implementation debt
            unitPriceInfo.setUnitPrice( getPriceWithCurrencyCode( order.getCurrencyCode(), new BigDecimal(lineItem.getVarOrderLinePrice()).divide(BigDecimal.valueOf(100)).setScale(2).doubleValue() ) );
            unitPriceInfo.setUnitPointsPrice( lineItem.getOrderLinePoints() );
        } else {
            unitPriceInfo.setUnitPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), new BigDecimal(lineItem.getSupplierItemPrice()).divide(BigDecimal.valueOf(100)).setScale(2).doubleValue()));
            unitPriceInfo.setUnitPointsPrice(lineItem.getItemPoints().intValue());
        }

        HashMap<String, Object> orderLineAdditionalInfo = new HashMap<String, Object>();
        RedemptionOrderLine redemptionOrderLine = new RedemptionOrderLine();
        MerchantEntity merchant = merchantDao.getMerchant(Integer.valueOf(lineItem.getSupplierId()), Integer.valueOf(lineItem.getMerchantId()));
        if (merchant != null && StringUtils.isNotBlank(merchant.getName())) {
            redemptionOrderLine.setMerchant(merchant.getName().toUpperCase());
        }
        redemptionOrderLine.setOrderLineId(String.valueOf(lineItem.getLineNum()));
        redemptionOrderLine.setQuantity(lineItem.getQuantity());
        redemptionOrderLine.setProductId(lineItem.getItemId());
        redemptionOrderLine.setProductType(VIS_PRODUCT_TYPE_MERCHANDISE);
        redemptionOrderLine.setDescription(lineItem.getName());
        redemptionOrderLine.setSupplierProductId(lineItem.getItemId());
        redemptionOrderLine.setTotalPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), totalPrice.multiply(new BigDecimal(lineItem.getQuantity())).setScale(2).doubleValue()));
        redemptionOrderLine.setTotalPointsPrice(lineItem.getOrderLinePoints() * lineItem.getQuantity());
        redemptionOrderLine.setCostPerPoint(getPriceWithCurrencyCode(order.getCurrencyCode(), new BigDecimal(1).divide(new BigDecimal(lineItem.getConvRate()).multiply(new BigDecimal(100)),18, RoundingMode.FLOOR).doubleValue()));
        redemptionOrderLine.setConvRate(lineItem.getConvRate());
        redemptionOrderLine.setUnitPriceInfo(unitPriceInfo);

        // VOM specific
        final ConversionRateInfo conversionRateInfo=new ConversionRateInfo();
        conversionRateInfo.setBaseCurrency(order.getCurrencyCode());
        conversionRateInfo.setQuoteId(""); //TODO
        conversionRateInfo.setRate(0.00); //TODO where do get currency conversion rate  ?
        conversionRateInfo.setTargetCurrency(program.getTargetCurrency().getCode());
        redemptionOrderLine.setConversionRateInfo(conversionRateInfo);

        //VOM specific
        final ProductDetails productDetails=new ProductDetails();
        productDetails.setCategory(lineItem.getCategory());
        productDetails.setDescription(lineItem.getName());
        productDetails.setImageUrl(lineItem.getImageUrl());
        productDetails.setColor(lineItem.getColor());
        productDetails.setName(lineItem.getName());
        redemptionOrderLine.setProductDetails(productDetails);

        //Get engrave Flag
        getEngraveFlag(lineItem, orderLineAdditionalInfo);

        //Order line additional info for VOM
        getAdditionalInfoForVOM(order, lineItem, orderLineAdditionalInfo, redemptionOrderLine);

        redemptionOrderLine.setDelivery(shipmentDeliveryInfo);
        return redemptionOrderLine;
    }

    private void getAdditionalInfoForVOM(Order order, OrderLine lineItem, HashMap<String, Object> orderLineAdditionalInfo, RedemptionOrderLine redemptionOrderLine) {
        orderLineAdditionalInfo.put(CommonConstants.SUPPLIER_NAME,"Apple"); //TODO    Apple or B2S
        orderLineAdditionalInfo.put(CommonConstants.B2S_ITEM_PROFIT_PRICE,lineItem.getB2sItemProfitPrice());
        orderLineAdditionalInfo.put(CommonConstants.VAR_ITEM_PROFIT_PRICE,lineItem.getVarItemProfitPrice());
        orderLineAdditionalInfo.put(CommonConstants.B2S_SHIPPING_PROFIT_PRICE,lineItem.getB2sShippingProfitPrice());
        orderLineAdditionalInfo.put(CommonConstants.VAR_SHIPPING_PROFIT_PRICE,lineItem.getVarShippingProfitPrice());
        orderLineAdditionalInfo.put(CommonConstants.SUPPLIER_ID,lineItem.getSupplierId());
        orderLineAdditionalInfo.put(CommonConstants.VAR_ITEM_MARGIN,lineItem.getVarItemMargin());
        if(CollectionUtils.isNotEmpty(lineItem.getOrderAttributes())) {
            Map<String, Object> orderLineAdditionalInfoSubset = lineItem.getOrderAttributes().stream().filter(orderLineAttribute1 -> CommonConstants.VAR_ANALYTIC_CODE.equals(orderLineAttribute1.getName()) || CommonConstants.VAR_ANALYTIC_KEY.equals(orderLineAttribute1.getName()))
                    .collect(Collectors.toMap(OrderLineAttribute::getName, OrderLineAttribute::getValue));
            orderLineAdditionalInfo.putAll(orderLineAdditionalInfoSubset);

        }
        redemptionOrderLine.setAdditionalInfo(orderLineAdditionalInfo);
    }

    private void getEngraveFlag(OrderLine lineItem, HashMap<String, Object> orderLineAdditionalInfo) {
        final OrderLineAttributeEntity lineAttribute = orderLineAttributeDao.findByOrderIdAndLineNumAndName(
                lineItem.getOrderId(),
                lineItem.getLineNum(),
                CartOrderConverterService.ENGRAVING_CODE).stream().filter(orderLineAttributeEntity -> orderLineAttributeEntity != null
                && StringUtils.isNotBlank(orderLineAttributeEntity.getValue())).findFirst().orElse(null);
        if (lineAttribute != null) {
            orderLineAdditionalInfo.put("engraved", true);
        } else {
            orderLineAdditionalInfo.put("engraved", false);
        }
    }

    /**
     * Get available balance points from VIS service
     *
     * @return
     * @throws IOException
     */
    @Override
    public int getUserPoints(final String varId, final String programId, final String accountId, final Map<String, String> addInfo) {
        try {

            final AccountBalance accountBalance = getAccountBalanceFromVIS(varId, accountId, addInfo);
            if (accountBalance != null) {
                LOG.info("User {} has {} points in his current var: {}, program {} in VIS. ", accountId,
                        accountBalance.getPointsBalance(), varId, programId);
                return accountBalance.getPointsBalance();
            }
        } catch (final Exception ex) {
            LOG.error("Error while getting user points from VIS response JSON : ", ex);
        }
        LOG.info("User {} has no account in VIS with his current var: {}, program {}. ", accountId, varId, programId);
        return 0;

    }

    /**
     * This method returns response code for a url
     *
     * @return response code
     */
    public AccountInfo getAccountInfo(String code, String varId, String countryCode) throws Exception {
        Gson gson = new Gson();
        final String visUrl = getBaseUrl(varId) + "/accounts/1234567890?authorization_code="+code+"&countryCode="+countryCode;
        LOG.info("getAccountInfo -- VIS call to retrieve user information : {} ", visUrl);

        String jsonInString = "{\n" +
                "    \"accountStatus\": {\n" +
                "        \"statusCode\": \"ACTIVE\",\n" +
                "        \"statusMessage\": \"Active\",\n" +
                "        \"accessType\": \"STANDARD\"\n" +
                JSON_TAG_END_NEW_LINE +
                "    \"accountBalance\": {\n" +
                "        \"pointsBalance\": 25000,\n" +
                "        \"pointsName\": \"Points\",\n" +
                "        \"currency\": \"POINTS\"\n" +
                JSON_TAG_END_NEW_LINE +
                "    \"userInformation\": {\n" +
                "        \"additionalInfo\": {\n" +
                "            \"memberCardAccounts\": [],\n" +
                "            \"ran\": \"123\",\n" +
                "            \"redemptionCode\": \"testRedemptionCode\",\n" +
                "            \"currency\": \"POINTs\",\n" +
                "            \"redemptionAllowedIndicator\": \"Y\",\n" +
                "            \"autoRedemptionIndicator\": false,\n" +
                "            \"ownerType\": \"AUTHSIGNR\",\n" +
                "            \"ranSubProductCode\": \"CO\",\n" +
                "            \"eliteIndicator\": \"Y\",\n" +
                "            \"tpbIndicator\": \"Y\",\n" +
                "            \"wfaIndicator\": \"N\",\n" +
                "            \"serviceLevel\": \"Cosmos\",\n" +
                "            \"cosmoIndicator\": \"Y\"\n" +
                JSON_TAG_END_NEW_LINE +
                "        \"address\": {\n" +
                "            \"line1\": \"5500 Windward Pkwy\",\n" +
                "            \"line2\": \"suite 450\",\n" +
                "            \"city\": \"Alpharetta\",\n" +
                "            \"stateCode\": \"GA\",\n" +
                "            \"postalCode\": \"30005\",\n" +
                "            \"countryCode\": \"US\"\n" +
                JSON_TAG_END_NEW_LINE +
                "        \"emailAddresses\": [\n" +
                OPEN_CURLY_BRACE_NEW_LINE +
                "                \"type\": \"PERSONAL\",\n" +
                "                \"email\": \"hranganathan@bridge2solutions.com\"\n" +
                JSON_TAG_END_NEW_LINE +
                CLOSE_SQUARE_BRACKET_NEW_LINE +
                "        \"firstName\": \"Harish\",\n" +
                "        \"lastName\": \"Ranganathan\",\n" +
                "        \"phoneNumbers\": [\n" +
                OPEN_CURLY_BRACE_NEW_LINE +
                "                \"type\": \"HOME\",\n" +
                "                \"number\": \"6782986000\"\n" +
                CLOSE_CURLY_BRACE_NEW_LINE +
                "        ]\n" +
                JSON_TAG_END_NEW_LINE +
                "    \"programId\": \"A1\"\n" +
                "}";
        final AccountInfo accountInfo = gson.fromJson(jsonInString, AccountInfo.class);
        final String gsonToJsonString = gsonToJsonString(accountInfo);
        LOG.info("++++ getAccountInfo Response: {}", gsonToJsonString);
        return accountInfo;
    }

    /**
     * This method returns response code for a url
     *
     * @return response code
     */
    private AccountBalance getAccountBalanceFromVIS(final String varId, final String userId, final Map<String, String> addInfo) throws Exception {
        Gson gson = new Gson();
        final StringBuilder visURL=new StringBuilder();
        visURL.append(getBaseUrl(varId))
                .append("/accounts/")
                .append(userId)
                .append("/balance");

        final Map <String,String> additionalInfo=addInfo;

        if(additionalInfo!=null && !additionalInfo.isEmpty()) {
            visURL.append('?');
            additionalInfo.forEach((k, v) -> {
                visURL.append(k).append('=').append(v).append('&');
            });
            visURL.deleteCharAt(visURL.length() - 1);
        }
        LOG.info("getUserProfile -- VIS call to retrieve user information : {}", visURL);

        String jsonInString = "{\n" +
                "  \"pointsBalance\": 25000,\n" +
                "  \"pointsName\": \"Points\",\n" +
                "  \"currency\": \"POINTS\"\n" +
                "}";

        final AccountBalance accountBalance = gson.fromJson(jsonInString, AccountBalance.class);
        final String gsonToJsonString = gsonToJsonString(accountBalance);
        LOG.info("++++ getAccountBalanceFromVIS Response: {}", gsonToJsonString);
        return accountBalance;

    }


    //Get Redemption information from VIS
    @Override
    public RedemptionResponse performRedemption(final Order order, final User user, final Program program) throws Exception {
        Gson gson = new Gson();
        RedemptionRequest redemptionRequest = getRedemptionRequest(order, user, program);
        final String visUrl = getBaseUrl(user.getVarId()) + "/redemptions";
        RedemptionResponse redemptionResponse = null;
        try {
            SecureRandom random = CommonConstants.SECURE_RANDOM;
            LOG.info("++++ performRedemption URL: {}", visUrl);
            String gsonToJsonString = gsonToJsonString(redemptionRequest);
            LOG.info("++++ performRedemption Request: {}", gsonToJsonString);

            String jsonInString = "{\n" +
                    "    \"orderId\": \"22\",\n" +
                    "    \"varOrderId\": \"552664\",\n" +
                    "    \"orderLines\": [\n" +
                    "        {\n" +
                    "            \"orderLineId\": \"12\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"additionalInfo\": {}\n" +
                    "}";

            redemptionResponse =  gson.fromJson(jsonInString, RedemptionResponse.class);
            redemptionResponse.setOrderId(String.valueOf(order.getOrderId()));
            redemptionResponse.setVarOrderId(String.valueOf(random.nextInt(100000)));
            gsonToJsonString = gsonToJsonString(redemptionResponse);
            LOG.info("++++ performRedemption Response: {}", gsonToJsonString);
        } catch (Exception e) {
            LOG.error("Error while performing redemption for user id: {}, having var id: {} and program id: {} for the order: {}", user.getUserId(), user.getVarId(), user.getProgramId(), order.getOrderId(), e);
            throw e;
        }
        return redemptionResponse;
    }

    //Get Redemption information from VIS
    @Override
    public CancelRedemptionResponse performPartialCancelRedemption(final Order order, final User user,
        final Program program) throws Exception {
        throw new MethodNotSupportedException(METHOD_NOT_SUPPORTED_FOR_REMOTE_PROGRAMS);
    }

    @Override
    public CancelRedemptionResponse performCancelRedemption(final Order order, final int totalPointsRefund) throws IOException, Exception {
        throw new MethodNotSupportedException(METHOD_NOT_SUPPORTED_FOR_REMOTE_PROGRAMS);
    }

    @Override
    public int getLocalUserPoints(final User user) throws MethodNotSupportedException {
        throw new MethodNotSupportedException(METHOD_NOT_SUPPORTED_FOR_REMOTE_PROGRAMS);
    }


    // Get complete user profile from VIS including Name, Address, account balance etc
    @Override
    public AccountInfo getUserProfile(final String varId, final String accountId, final Map<String, String> additionalInfo){
        Gson gson = new Gson();
        final StringBuilder visURL=new StringBuilder();
        visURL.append(getBaseUrl(varId)).append("/accounts/")
                .append(accountId);

        if(additionalInfo!=null && !additionalInfo.isEmpty()) {
            visURL.append('?');
            additionalInfo.forEach((k, v) -> {
                visURL.append(k).append('=').append(v).append('&');
            });
            visURL.deleteCharAt(visURL.length()-1);
        }
        LOG.info("getUserProfile -- VIS call to retrieve user information : {}", visURL);

        String jsonInString = "{\n" +
                "    \"accountStatus\": {\n" +
                "        \"statusCode\": \"ACTIVE\",\n" +
                "        \"statusMessage\": \"Active\",\n" +
                "        \"accessType\": \"STANDARD\"\n" +
                JSON_TAG_END_NEW_LINE +
                "    \"accountBalance\": {\n" +
                "        \"pointsBalance\": 25000,\n" +
                "        \"pointsName\": \"Points\",\n" +
                "        \"currency\": \"POINTS\"\n" +
                JSON_TAG_END_NEW_LINE +
                "    \"userInformation\": {\n" +
                "        \"additionalInfo\": {\n" +
                "            \"autoRedemptionIndicator\": false,\n" +
                "            \"ownerType\": \"PRIMARY\",\n" +
                "            \"serviceLevel\": \"Standard\",\n" +
                "            \"profileId\": \"123456\",\n" +
                "            \"agentUserName\": \"SAN MATH\",\n" +
                "            \"redemptionCode\": \"SM100\"\n" +
                "        },\n" +
                "        \"address\": {\n" +
                "            \"line1\": \"5500 Windward Pkwy\",\n" +
                "            \"line2\": \"suite 450\",\n" +
                "            \"city\": \"Alpharetta\",\n" +
                "            \"stateCode\": \"GA\",\n" +
                "            \"postalCode\": \"30005\",\n" +
                "            \"countryCode\": \"US\"\n" +
                "        },\n" +
                "        \"emailAddresses\": [\n" +
                OPEN_CURLY_BRACE_NEW_LINE +
                "                \"type\": \"PERSONAL\",\n" +
                "                \"email\": \"hranganathan@bridge2solutions.com\"\n" +
                CLOSE_CURLY_BRACE_NEW_LINE +
                CLOSE_SQUARE_BRACKET_NEW_LINE +
                "        \"firstName\": \"Harish\",\n" +
                "        \"lastName\": \"Ranganathan\",\n" +
                "        \"phoneNumbers\": [\n" +
                OPEN_CURLY_BRACE_NEW_LINE +
                "                \"type\": \"HOME\",\n" +
                "                \"number\": \"6782986000\"\n" +
                "            },\n" +
                OPEN_CURLY_BRACE_NEW_LINE +
                "                \"type\": \"MOBILE\",\n" +
                "                \"number\": \"3096415745\"\n" +
                CLOSE_CURLY_BRACE_NEW_LINE +
                CLOSE_SQUARE_BRACKET_NEW_LINE +
                "        \"deceased\": false\n" +
                JSON_TAG_END_NEW_LINE +
                // "    \"programId\": \"A1\"\n" +
                "    \"owners\": [\n" +
                "        {\n" +
                "            \"firstName\": \"MATH\",\n" +
                "            \"lastName\": \"SAM\",\n" +
                "            \"type\": \"PRIMARY\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        final AccountInfo accountInfo = gson.fromJson(jsonInString, AccountInfo.class);
        final String gsonToJsonString = gsonToJsonString(accountInfo);
        LOG.info("++++ getUserProfile Response: {}", gsonToJsonString);
        return accountInfo;
    }

    @Override
    public SessionResponse getUserSession(final User user) {
        return null;
    }
}
