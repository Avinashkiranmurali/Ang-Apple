package com.b2s.shop.common.order.var;

import com.b2s.apple.config.HttpClientUtilFactory;
import com.b2s.apple.entity.MerchantEntity;
import com.b2s.apple.entity.OrderLineAttributeEntity;
import com.b2s.apple.services.CartOrderConverterService;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.db.model.OrderLineAttribute;
import com.b2s.rewards.apple.dao.MerchantListDao;
import com.b2s.rewards.apple.dao.OrderAttributeValueDao;
import com.b2s.rewards.apple.dao.OrderLineAttributeDao;
import com.b2s.rewards.apple.integration.model.*;
import com.b2s.rewards.apple.model.OrderAttributeValue;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.model.SessionUserInfo;
import com.b2s.rewards.apple.model.UserVarProgramCreditAdds;
import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.service.model.DeliveryMethod;
import com.b2s.shop.common.User;
import com.b2s.shop.util.VarProgramConfigHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.MethodNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.*;

/**
 ** Created by rpillai on 10/6/2015.
 */
@Service
public class VarIntegrationServiceRemoteImpl implements VarIntegrationService {

    private static final Logger LOG = LoggerFactory.getLogger(VarIntegrationServiceRemoteImpl.class);
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private final List<String> sessionIdVARList = Arrays.asList(VAR_SCOTIA, VAR_FDR, VAR_FDR_PSCU);

    @Autowired
    private Properties applicationProperties;

    @Autowired @Qualifier("merchantListDao")
    private MerchantListDao merchantDao;

    protected HttpClientUtil visHttpClientUtil;

    @Autowired @Qualifier("visHttpClientUtil")
    private HttpClientUtil defaultHttpClientUtil;

    @Autowired
    private OrderLineAttributeDao orderLineAttributeDao;

    @Autowired @Qualifier("orderAttributeValueDao")
    private OrderAttributeValueDao orderAttributeValueDao;

    @Autowired
    private VarProgramConfigHelper varProgramConfigHelper;

    public HttpClientUtil setHttpClientUtil(String varId)
    {
        visHttpClientUtil = HttpClientUtilFactory.getCustomHttpClientUtil(varId);
        if(visHttpClientUtil == null) {
            visHttpClientUtil = defaultHttpClientUtil;
            LOG.info("Client not found in the HttpClientUtilFactory, for varId = {}", varId);
            return defaultHttpClientUtil;
        } else {
            LOG.info("Client found in the HttpClientUtilFactory, for varId = {} having timeout value = {}",
                    varId, HttpClientUtilFactory.getTimeoutValue(varId));
            return visHttpClientUtil;
        }
    }

    @Override
    public String getBaseUrl(final String varId) {
        StringBuilder baseUrl = new StringBuilder(applicationProperties.getProperty(CommonConstants.VIS_ACCOUNTS_SERVICE_URL));
        if(StringUtils.isNotBlank(varId)) {
            baseUrl.append("/");
            baseUrl.append(varId.toLowerCase());
        }
        setHttpClientUtil(varId);
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
        if(StringUtils.isNotEmpty(user.getProxyUserId())){
            accountIdentifier.setAgentId(user.getProxyUserId());
        }

        /*
        For VirginAU,VIMS is using sessionId under accountIdentifier.
        This will impact all VARs.
        'sid' will be set under both accountIdentifier's session
        and also under alternateIds.
         */
        if(StringUtils.isNotEmpty(user.getSid())){
            accountIdentifier.setSessionId( Optional.ofNullable(user.getSid()));
        }

        if(MapUtils.isNotEmpty(user.getAdditionalInfo())) {
            accountIdentifier.setAlternateIds(populateAlternateIds(user));
            populateAdditionalInfos(user, redemptionRequest, order.getSupplierId());
            redemptionRequest.getAdditionalInfo().put(VIS_ALTERNATE_ADDRESS_INDICATOR, Boolean.toString(!order.getAddr1().equalsIgnoreCase(user.getAddr1())));

        }
        SplitTenderInfo splitTenderInfo = createSplitTenderInfo(order, user);

        //Delivery Information
        populateDeliveryInformation(order, shipmentDeliveryInfo, populateShippingAddress(order));

        //  OrderLines to Redemption OrderLines
        populateRedemptionOrderLines(order, user, program, redemptionRequest, redemptionOrderLines,
            shipmentDeliveryInfo,
            splitTenderInfo);

        //setAuthorizedUserInformation
        setAuthorizedUserInformation(redemptionRequest, user.getSessionUserInfo());

        //Construct request JSON
        generateRequestJSON(order, user, redemptionRequest,
                accountIdentifier, redemptionOrderLines, shipmentDeliveryInfo);

        return redemptionRequest;
    }

    private void populateRedemptionOrderLines(final Order order, final User user, final Program program,
        final RedemptionRequest redemptionRequest, final List<RedemptionOrderLine> redemptionOrderLines,
        final ShipmentDeliveryInfo shipmentDeliveryInfo,
        final SplitTenderInfo splitTenderInfo) {
        OrderLine lineItem;
        for (Object orderLine : order.getOrderLines()) {
            lineItem = (OrderLine) orderLine;
            if (!lineItem.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S) &&
                !lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S) &&
                !lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S) &&
                !lineItem.getSupplierId().equalsIgnoreCase(AMP_SUPPLIER_ID_STRING)) {
                UnitPriceInfo unitPriceInfo = new UnitPriceInfo();
                BigDecimal totalPrice = updatePriceInfoAndGetTotalPrice(order, lineItem, unitPriceInfo);
                final Boolean unitPriceOverride = (Boolean) program.getConfig().getOrDefault(CommonConstants
                    .UNIT_PRICE_OVERRIDE, Boolean.FALSE);

                populateUnitInfo(order, lineItem, unitPriceInfo, unitPriceOverride);

                HashMap<String, Object> orderLineAdditionalInfo = new HashMap<String, Object>();
                RedemptionOrderLine redemptionOrderLine =
                    populateRedemptionOrderLine(order, program, lineItem, unitPriceInfo, totalPrice);
                redemptionOrderLine.setProductType(getProductType(lineItem.getSupplierId()));

                // VOM specific
                populateConversionRateInfo(order, user, program, redemptionOrderLine);
                final ProductDetails productDetails = getProductDetails(lineItem);
                productDetails.setSku(lineItem.getSku());
                redemptionOrderLine.setProductDetails(productDetails);
                populateOrderLineAdditionalInfo(lineItem, orderLineAdditionalInfo);
                orderLineAdditionalInfo.put(CommonConstants.VAR_ITEM_MARGIN,lineItem.getVarItemMargin());
                redemptionOrderLine.setAdditionalInfo(orderLineAdditionalInfo);
                redemptionOrderLine.setDelivery(shipmentDeliveryInfo);

                //add to redemptionOrderLines
                redemptionOrderLines.add(redemptionOrderLine);
            } else {
                if (lineItem != null && lineItem.getOrderLinePoints() != 0) {

                    splitTenderInfo.setPointsPurchased(-lineItem.getOrderLinePoints());
                    splitTenderInfo.setCash(getPriceWithCurrencyCode(order.getCurrencyCode(),
                        new BigDecimal(-lineItem.getVarOrderLinePrice())
                            .add(new BigDecimal(-lineItem.getVarItemProfitPrice()))
                            .divide(new BigDecimal(100), 2, RoundingMode.UNNECESSARY)
                            .doubleValue()));

                    if (Objects.nonNull(order.getUserVarProgramCreditAdds())) {
                        final UserVarProgramCreditAdds userVarProgramCreditAdds = order.getUserVarProgramCreditAdds();
                        splitTenderInfo.setCcVarMargin(userVarProgramCreditAdds.getCcVarMargin());
                        splitTenderInfo.setCcVarPrice(String.valueOf(userVarProgramCreditAdds.getCcVarPrice()));
                        splitTenderInfo.setCcVarProfit(String.valueOf(userVarProgramCreditAdds.getCcVarProfit()));
                        splitTenderInfo.setPointsPurchased(userVarProgramCreditAdds.getPointsPurchased());
                        splitTenderInfo.setEffectiveConversionRate(String.valueOf(userVarProgramCreditAdds.getEffectiveConversionRate()));
                    }

                    redemptionRequest.setSplitTenderInfo(splitTenderInfo);
                    //  CashBuyInPoints && CashBuyInPrice are deprecated but used for backwards compatibility.
                    redemptionRequest.setCashBuyInPoints(-lineItem.getOrderLinePoints());
                    redemptionRequest.setCashBuyInPrice(getPriceWithCurrencyCode(order.getCurrencyCode(),
                        new BigDecimal(-lineItem.getVarOrderLinePrice())
                            .add(new BigDecimal(-lineItem.getVarItemProfitPrice()))
                            .divide(new  BigDecimal(100), 2, RoundingMode.UNNECESSARY)
                            .doubleValue()));
                }
            }

            setPayrollDeductionInfo(order, redemptionRequest, lineItem);
            setDiscountInfo(order, redemptionRequest, lineItem);

        }
    }

    private void populateUnitInfo(Order order, OrderLine lineItem, UnitPriceInfo unitPriceInfo, Boolean unitPriceOverride) {
        if ( VAR_SCOTIA.equalsIgnoreCase(order.getVarId())) { // TODO - Clean up after the VIS implementation debt
            unitPriceInfo.setUnitPrice( getPriceWithCurrencyCode( order.getCurrencyCode(), new BigDecimal(lineItem.getVarOrderLinePrice()).divide(BigDecimal.valueOf(100)).setScale(2).doubleValue() ) );
            unitPriceInfo.setUnitPointsPrice( lineItem.getOrderLinePoints() );
        } else if (VAR_WELLSFARGO.equalsIgnoreCase(order.getVarId())) {
            unitPriceInfo.setUnitPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), new BigDecimal(lineItem.getSupplierItemPrice()).divide(BigDecimal.valueOf(100)).setScale(2).doubleValue()));
            unitPriceInfo.setUnitPointsPrice( lineItem.getOrderLinePoints() );
        } else if (unitPriceOverride) {
            unitPriceInfo.setUnitPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), new BigDecimal(lineItem.getVarOrderLinePrice()).doubleValue()));
            unitPriceInfo.setUnitPointsPrice(lineItem.getOrderLinePoints());
            unitPriceInfo.setItemPoints(lineItem.getItemPoints().intValue());
        } else {
            unitPriceInfo.setUnitPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), new BigDecimal(lineItem.getSupplierItemPrice()).divide(BigDecimal.valueOf(100)).setScale(2).doubleValue()));
            unitPriceInfo.setUnitPointsPrice(lineItem.getItemPoints().intValue());
            unitPriceInfo.setItemPoints(lineItem.getItemPoints().intValue());
        }
    }

    private void populateConversionRateInfo(Order order, User user, Program program, RedemptionOrderLine redemptionOrderLine) {
        final ConversionRateInfo conversionRateInfo=new ConversionRateInfo();
        conversionRateInfo.setBaseCurrency(order.getCurrencyCode());
        // TODO. quoteId and rate will be done as part of S-03990
        // For now, sending a non-zero value to successful Epsilon integraion for CitiGR
        conversionRateInfo.setQuoteId("123"); //TODO
        conversionRateInfo.setRate(1.11); //TODO where do get currency conversion rate  ?
        if (program != null) {
            if (program.getTargetCurrency() == null && user.getLocale() != null) {
                conversionRateInfo.setTargetCurrency(Currency.getInstance(user.getLocale()).getCurrencyCode());
            } else {
                conversionRateInfo.setTargetCurrency(program.getTargetCurrency().getCode());
            }
        }
        redemptionOrderLine.setConversionRateInfo(conversionRateInfo);
    }

    protected void setDiscountInfo(final Order order, final RedemptionRequest redemptionRequest,
        final OrderLine lineItem) {
        if(lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S)){
            final DiscountInfo discountInfo = new DiscountInfo();
            discountInfo.setCode(lineItem.getItemId());
            discountInfo.setPrice(getPriceWithCurrencyCode(
                order.getCurrencyCode(),
                new BigDecimal(lineItem.getSupplierItemPrice()).divide(ONE_HUNDRED).abs().doubleValue()));
            redemptionRequest.setDiscountInfo(discountInfo);
        }
    }

    protected void setPayrollDeductionInfo(final Order order, final RedemptionRequest redemptionRequest,
        final OrderLine lineItem) {
        if(lineItem.getSupplierId().equalsIgnoreCase(CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S)){
            PayrollDeductionInfo payrollDeductionInfo=new PayrollDeductionInfo();
            payrollDeductionInfo.setTotalPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), order.getOrderTotalPayrollPrice().getAmount().doubleValue()));
            payrollDeductionInfo.setPerPayPeriodPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), order.getOrderTotalPayrollPeriodPrice().getAmount().doubleValue()));
            payrollDeductionInfo.setNumberOfPayPeriods(String.valueOf(order.getPayrollFrequency()));
            redemptionRequest.setPayrollDeductionInfo(payrollDeductionInfo);
        }
    }

    protected void populateOrderLineAdditionalInfo(final OrderLine lineItem,
        final HashMap<String, Object> orderLineAdditionalInfo) {
        //Get engrave Flag
        populateEngraveInfo(lineItem, orderLineAdditionalInfo);

        //Order line additional info for VOM
        orderLineAdditionalInfo.put(CommonConstants.SUPPLIER_NAME,"Apple"); //TODO    Apple or B2S
        orderLineAdditionalInfo.put(CommonConstants.B2S_ITEM_PROFIT_PRICE,lineItem.getB2sItemProfitPrice());
        orderLineAdditionalInfo.put(CommonConstants.VAR_ITEM_PROFIT_PRICE,lineItem.getVarItemProfitPrice());
        orderLineAdditionalInfo.put(CommonConstants.B2S_SHIPPING_PROFIT_PRICE,lineItem.getB2sShippingProfitPrice());
        orderLineAdditionalInfo.put(CommonConstants.VAR_SHIPPING_PROFIT_PRICE,lineItem.getVarShippingProfitPrice());
        orderLineAdditionalInfo.put(CommonConstants.SUPPLIER_ID,lineItem.getSupplierId());

        if(CollectionUtils.isNotEmpty(lineItem.getOrderAttributes())) {
            for(OrderLineAttribute orderLineAttribute : lineItem.getOrderAttributes()) {
                if(orderLineAttribute != null) {
                    if(CommonConstants.VAR_ANALYTIC_CODE.equals(orderLineAttribute.getName())) {
                        orderLineAdditionalInfo.put(orderLineAttribute.getName(), orderLineAttribute.getValue());
                    }
                    if(CommonConstants.VAR_ANALYTIC_KEY.equals(orderLineAttribute.getName())) {
                        orderLineAdditionalInfo.put(orderLineAttribute.getName(), orderLineAttribute.getValue());
                    }
                }
            }
        }
    }

    protected ProductDetails getProductDetails(final OrderLine lineItem) {
        //VOM specific
        final ProductDetails productDetails=new ProductDetails();
        productDetails.setCategory(lineItem.getCategory());
        productDetails.setDescription(lineItem.getName());
        productDetails.setImageUrl(lineItem.getImageUrl());
        productDetails.setColor(lineItem.getColor());
        productDetails.setName(lineItem.getName());
        return productDetails;
    }

    protected RedemptionOrderLine populateRedemptionOrderLine(final Order order, final Program program,
        final OrderLine lineItem, final UnitPriceInfo unitPriceInfo, final BigDecimal totalPrice) {
        RedemptionOrderLine redemptionOrderLine = new RedemptionOrderLine();
        MerchantEntity merchant = merchantDao.getMerchant(Integer.valueOf(lineItem.getSupplierId()), Integer.valueOf(lineItem.getMerchantId()));
        if (merchant != null && StringUtils.isNotBlank(merchant.getName())) {
            redemptionOrderLine.setMerchant(merchant.getName().toUpperCase());
        }
        redemptionOrderLine.setOrderLineId(String.valueOf(lineItem.getLineNum()));
        redemptionOrderLine.setQuantity(lineItem.getQuantity());
        redemptionOrderLine.setProductId(lineItem.getItemId());
        redemptionOrderLine.setDescription(lineItem.getName());
        redemptionOrderLine.setSupplierProductId(lineItem.getItemId());
        redemptionOrderLine.setTotalPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), totalPrice.multiply(new BigDecimal(lineItem.getQuantity())).setScale(2).doubleValue()));
        redemptionOrderLine.setTotalPointsPrice(lineItem.getOrderLinePoints() * lineItem.getQuantity());
        redemptionOrderLine.setCostPerPoint(getPriceWithCurrencyCode(order.getCurrencyCode(), new BigDecimal(1).divide(new BigDecimal(lineItem.getConvRate()).multiply(new BigDecimal(100)), 18, RoundingMode.FLOOR).doubleValue()));
        Boolean sendInverseRate= (Boolean)program.getConfig().getOrDefault(CommonConstants.PROGRAM_CONFIG_KEY_SEND_INVERSE_RATE, Boolean.FALSE);
        if(sendInverseRate) {
            Optional<Double> inverseConvRate = lineItem.getOrderAttributes().stream()
                    .filter(orderLineAttribute -> CommonConstants.ORDER_LINE_ATTR_KEY_INVERSE_RATE.equals(orderLineAttribute.getName()))
                    .findFirst()
                    .map(attribute -> Double.valueOf(attribute.getValue()));
            redemptionOrderLine.setConvRate(inverseConvRate.orElse(lineItem.getConvRate()));
        } else {
            redemptionOrderLine.setConvRate(lineItem.getConvRate());
        }
        redemptionOrderLine.setMargin(lineItem.getVarItemMargin());
        redemptionOrderLine.setTaxRate(lineItem.getTaxRate());
        redemptionOrderLine.setUnitPriceInfo(unitPriceInfo);
        return redemptionOrderLine;
    }

    private Map<String, String> populateAlternateIds(final User user) {
        final Map<String, String> alternateIds = createAlternateIds(user);
        // TODO : REFACTOR the Method to make it behave diffently for SCOTIA
        if (sessionIdVARList.contains(user.getVarId())) {
            if (StringUtils.isNotBlank(user.getSid())) {
                alternateIds.put(CommonConstants.SESSIONID, user.getSid());
            }
        } else if (StringUtils.isNotBlank(user.getSid())) {
            alternateIds.put(CommonConstants.SID, user.getSid());
        }
        return alternateIds;
    }

    protected void populateEngraveInfo(OrderLine lineItem, HashMap<String, Object> orderLineAdditionalInfo) {
        //Get engrave Flag
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

    private void generateRequestJSON(Order order, User user, RedemptionRequest redemptionRequest, AccountIdentifier accountIdentifier, List<RedemptionOrderLine> redemptionOrderLines, ShipmentDeliveryInfo shipmentDeliveryInfo) {
        redemptionRequest.setAccountIdentifier(accountIdentifier);
        redemptionRequest.setOrderId(String.valueOf(order.getOrderId()));
        redemptionRequest.setOrderDate(order.getOrderDateAsISO8601());
        redemptionRequest.setDelivery(shipmentDeliveryInfo);
        redemptionRequest.setOrderlines(redemptionOrderLines);
        redemptionRequest.setTotalPrice(getPriceWithCurrencyCode(order.getCurrencyCode(),order.getOrderTotalInMoney()
            .getAmount().doubleValue()));
        redemptionRequest.setTotalPointsPrice(order.getOrderTotalInPoints());
        redemptionRequest.setStartBalance(user.getBalance());
        if(user.getAdditionalInfo().get("sessionState")!=null) {
            redemptionRequest.setSessionState(user.getAdditionalInfo().get("sessionState"));
        }
    }

    private void setAuthorizedUserInformation(final RedemptionRequest redemptionRequest, final SessionUserInfo authUserInfo) {
        if (Objects.nonNull(authUserInfo)) {
            SessionUserInformation authorizedUserInformation = new SessionUserInformation();
            authorizedUserInformation.setFirstName(authUserInfo.getFirstName());
            authorizedUserInformation.setLastName(authUserInfo.getLastName());
            redemptionRequest.setSessionUserInformation(authorizedUserInformation);
        }
    }

    private CancelRedemptionRequest getCancelRedemptionRequest(final Order order, final int totalPointsRefund) {
        final CancelRedemptionRequest cancelRedemptionRequest = new CancelRedemptionRequest();
        cancelRedemptionRequest.setVarId(order.getVarId());
        cancelRedemptionRequest.setOrderId(String.valueOf(order.getOrderId()));
        cancelRedemptionRequest.setVarOrderId(order.getVarOrderId());
        cancelRedemptionRequest.setTotalPointRefund(totalPointsRefund);
        List<OrderAttributeValue> orderAttributes = order.getOrderAttributeValues();
        if(orderAttributes == null) {
            orderAttributes = new ArrayList<>();
        }
        orderAttributes.addAll(orderAttributeValueDao.getByOrder(order.getOrderId()));
        if(CollectionUtils.isNotEmpty(orderAttributes)) {
            final Map<String, String> additionalInfo = new HashMap<>();
            orderAttributes.forEach(orderAttribute -> {
                additionalInfo.put(orderAttribute.getName(), orderAttribute.getValue());
            });
            cancelRedemptionRequest.setAdditionalInfo(additionalInfo);
        }
        
        AccountIdentifier accountIdentifier =new AccountIdentifier();
        accountIdentifier.setAccountId(order.getUserId());
        accountIdentifier.setVarId(order.getVarId());
        accountIdentifier.setProgramId(order.getProgramId());

        cancelRedemptionRequest.setAccountIdentifier(accountIdentifier);

        return cancelRedemptionRequest;
    }

    // Cancel entire order, as this will be invoked while Order Transaction Rollback
    private PartialCancelRedemptionRequest getPartialCancelRedemptionRequest(final Order order, final User user,
        final Program program) {

        final PartialCancelRedemptionRequest partialCancelRedemptionRequest = new PartialCancelRedemptionRequest();
        final AccountIdentifier accountIdentifier = new AccountIdentifier();
        final List<OrderLineCancellation> cancelOrderLines = new ArrayList<>();
        Double cashPriceValue = 0.0D;
        Integer cashPointValue = 0;
        int tempCashPointValue = 0;

        //sort order line to process by supplier_id to get cashpoint value first
        final List<OrderLine> orderLines = order.getOrderLines();
        orderLines.sort((p1, p2) -> p2.getSupplierId().compareTo(p1.getSupplierId()));

        //AccountIdentifier
        accountIdentifier.setVarId(order.getVarId());
        accountIdentifier.setProgramId(order.getProgramId());
        accountIdentifier.setAccountId(user.getUserId());

        //  Cancel OrderLine(s)
        for (final OrderLine lineItem : orderLines) {
            if (lineItem.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_CREDIT_S)  ) {
                //for D-09044 - Webapp Cancel Flow
                BigDecimal valueInBigDecimal = new BigDecimal(-lineItem.getVarOrderLinePrice()).divide(BigDecimal.valueOf(100)).setScale(2);
                cashPriceValue = valueInBigDecimal.doubleValue();
                cashPointValue =  lineItem.getOrderLinePoints() * -1;  // as cash value points is stored as negative number in DB
                tempCashPointValue = cashPointValue;
            }
            else {
                final UnitPriceInfo unitPriceInfo = new UnitPriceInfo();

                final BigDecimal totalFeesInMoney = lineItem.getTotalFeesInMoneyMinor();
                unitPriceInfo.setFees(getPriceWithCurrencyCode(order.getCurrencyCode(), totalFeesInMoney.divide(new BigDecimal(lineItem.getQuantity())).doubleValue()));

                final BigDecimal totalTaxInMoney = lineItem.getTotalTaxesInMoneyMinor();
                unitPriceInfo.setTaxes(getPriceWithCurrencyCode(order.getCurrencyCode(), totalTaxInMoney.divide(new BigDecimal(lineItem.getQuantity())).doubleValue()));

                final BigDecimal unitPrice = new BigDecimal(lineItem.getSupplierItemPrice()).divide(BigDecimal.valueOf(100)).setScale(2);
                unitPriceInfo.setUnitPrice(getPriceWithCurrencyCode(order.getCurrencyCode(), unitPrice.doubleValue()));
                unitPriceInfo.setUnitPointsPrice(lineItem.getItemPoints().intValue());
                unitPriceInfo.setItemPoints(lineItem.getItemPoints().intValue());

                // Construct OrderLine
                final OrderLineCancellation orderLineCancellation = new OrderLineCancellation();

                final Double costPerPoint = lineItem.getOrderAttributes().stream()
                    .filter(orderLineAttribute -> CommonConstants.ORDER_LINE_ATTR_KEY_INVERSE_RATE.equals(orderLineAttribute.getName()))
                    .findFirst()
                    .map(attribute -> Double.valueOf(attribute.getValue()))
                    .orElseThrow(() -> new IllegalStateException("Order line attribute 'inverseRate' expected, but was not found."));
                orderLineCancellation.setCostPerPoint(getPriceWithCurrencyCode(order.getCurrencyCode(), costPerPoint));

                orderLineCancellation.setOrderLineId(String.valueOf(lineItem.getLineNum()));
                orderLineCancellation.setQuantityToCancel(lineItem.getQuantity());
                orderLineCancellation.setOriginalQuantity(lineItem.getQuantity());
                tempCashPointValue = lineItem.getOrderLinePoints() - tempCashPointValue;
                if(tempCashPointValue < 0) {
                    orderLineCancellation.setTotalPointsRefund(0);
                    tempCashPointValue = Math.abs(tempCashPointValue);
                }else {
                    orderLineCancellation.setTotalPointsRefund(tempCashPointValue);
                    tempCashPointValue = 0;
                }
                orderLineCancellation.setVarOrderLineId(lineItem.getOrderLineType());
                orderLineCancellation.setProductId(lineItem.getItemId());
                orderLineCancellation.setUnitPriceInfo(unitPriceInfo);
                orderLineCancellation.setDelivery(Delivery
                        .builder()
                        .withDeliveryMethod(DeliveryMethod.DOMESTIC_SHIPPING)
                        .withFirstName(order.getFirstname())
                        .withLastName(order.getLastname())
                        .build());
                orderLineCancellation.setCancelReason(CancelReason.CANCELLED_OTHER_REASON);
                cancelOrderLines.add(orderLineCancellation);
            }
        }

        partialCancelRedemptionRequest.setAccountIdentifier(accountIdentifier);
        partialCancelRedemptionRequest.setOrderId(String.valueOf(order.getOrderId()));
        partialCancelRedemptionRequest.setCashBuyInRefund(getPriceWithCurrencyCode(order.getCurrencyCode(), cashPriceValue));
        partialCancelRedemptionRequest.setCancellationId(String.format("%08d", order.getOrderId()));
        partialCancelRedemptionRequest.setVarOrderId(order.getVarOrderId());
        partialCancelRedemptionRequest.setCurrency(user.getAdditionalInfo().get(CommonConstants.VIS_CURRENCY));
        partialCancelRedemptionRequest.setOrderLines(cancelOrderLines);
        partialCancelRedemptionRequest.setAdditionalInfo(getVimsAdditionalInfo(program, order.getOrderAttributeValues()));
        partialCancelRedemptionRequest.setCancelDateTime(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
        return partialCancelRedemptionRequest;
    }

    /**
     * Get VIMS Additional info values based on VPC configuration
     *
     * @param program
     * @param orderAttributes
     * @return
     */
    private Map<String, String> getVimsAdditionalInfo(final Program program,
        final List<OrderAttributeValue> orderAttributes) {
        final Map<String, String> additionalInfo = new HashMap<>();
        final String vimsAdditionInfo = (String) program.getConfig().get(CommonConstants.VIMS_ADDITIONAL_INFO);

        //Setting AdditionalInfos - ProfileId & ProgramCode - For PNC
        if (StringUtils.isNotBlank(vimsAdditionInfo) && CollectionUtils.isNotEmpty(orderAttributes)) {
            List<String> vpcAttributes = Arrays.asList(vimsAdditionInfo.split(","));

            orderAttributes.stream().filter(orderAttribute -> vpcAttributes.contains(orderAttribute.getName()))
                .forEach(orderAttribute -> additionalInfo.put(orderAttribute.getName(), orderAttribute.getValue()));
        }
        return additionalInfo;
    }

    /**
     * Get available balance points from VIS service
     *
     * @return
     * @throws IOException
     */
    @Override
    public int getUserPoints(final String varId, final String programId, final String accountId, final Map<String, String> addInfo) throws B2RException {
        try {

            final AccountBalance accountBalance = getAccountBalanceFromVIS(varId, accountId, addInfo);
            if (accountBalance != null) {
                LOG.info("User {} has {} points in his current var: {}, program {} in VIS. ", accountId,
                    accountBalance.getPointsBalance(), varId, programId);
                return accountBalance.getPointsBalance();
            }
        } catch (final Exception ex) {
            LOG.error("Error while getting user points from VIS response JSON : ", ex);
            throw ex;
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
        final String visUrl = getBaseUrl(varId) + "/accounts/1234567890?authorization_code="+code+"&countryCode="+countryCode;
        return visHttpClientUtil.getHttpResponse(visUrl, AccountInfo.class, HttpMethod.GET, null);
    }

    /**
     * This method returns response code for a url
     *
     * @return response code
     */
    private AccountBalance getAccountBalanceFromVIS(final String varId, final String userId, final Map<String, String> addInfo) throws B2RException {
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
        return visHttpClientUtil.getHttpResponse(visURL.toString(), AccountBalance.class, HttpMethod.GET, null);
    }


    //Get Redemption information from VIS
    @Override
    public RedemptionResponse performRedemption(final Order order, final User user, final Program program) throws Exception {
        RedemptionRequest redemptionRequest = getRedemptionRequest(order, user, program);
        final String visUrl = getBaseUrl(user.getVarId()) + "/redemptions";
        RedemptionResponse redemptionResponse = null;
        try {
            LOG.info("VIS call being made for VarID: {}, ProgramID: {}, VIS url: {}", order.getVarId(),
                order.getProgramId(), visUrl);
            redemptionResponse = visHttpClientUtil.getHttpResponse(visUrl, RedemptionResponse.class, HttpMethod.POST, redemptionRequest);
            setOrderLineType(order, redemptionResponse);
        } catch (Exception e) {
            LOG.error("Error while performing redemption for user id: {}, having var id: {} and program id: {} for the order: {}", user.getUserId(), user.getVarId(), user.getProgramId(), order.getOrderId(), e);
            throw e;
        }
        return redemptionResponse;
    }

    /**
     * Store order_line_type for each order_line if RedemptionResponse related order_line contains varOrderLineId
     *
     * @param order
     * @param redemptionResponse
     */
    private void setOrderLineType(final Order order, final RedemptionResponse redemptionResponse) {
        if (Objects.nonNull(order) && Objects.nonNull(redemptionResponse) &&
            CollectionUtils.isNotEmpty(order.getOrderLines()) &&
            CollectionUtils.isNotEmpty(redemptionResponse.getOrderLines())) {

            redemptionResponse.getOrderLines().stream()
                .filter(redemptionOrderLine -> StringUtils.isNotBlank(redemptionOrderLine.getVarOrderLineId()))
                .forEach(redemptionOrderLine -> {
                    Optional<OrderLine> matchedOrderLine = order.getOrderLines().stream()
                        .filter(orderLine -> Objects.nonNull(orderLine.getLineNum()) &&
                            orderLine.getLineNum().toString().equalsIgnoreCase(redemptionOrderLine.getOrderLineId()))
                        .findFirst();
                    matchedOrderLine
                        .ifPresent(orderLine -> orderLine.setOrderLineType(redemptionOrderLine.getVarOrderLineId()));
                });
        }
    }

    //Get Redemption information from VIS
    @Override
    public CancelRedemptionResponse performPartialCancelRedemption(final Order order, final User user,
        final Program program)
        throws B2RException {
        PartialCancelRedemptionRequest partialCancelRedemptionRequest =
            getPartialCancelRedemptionRequest(order, user, program);
        final String visUrl = getBaseUrl(user.getVarId()) + "/cancellations/partial";
        return visHttpClientUtil.getHttpResponse(visUrl, CancelRedemptionResponse.class, HttpMethod.POST, partialCancelRedemptionRequest);
    }

    @Override
    public CancelRedemptionResponse performCancelRedemption(final Order order, final int totalPointsRefund) throws IOException, Exception {
        final String visUrl = getBaseUrl(order.getVarId()) + "/cancellations/full";
        CancelRedemptionRequest cancelRequest = getCancelRedemptionRequest(order, totalPointsRefund);
        return visHttpClientUtil.getHttpResponse(visUrl, CancelRedemptionResponse.class, HttpMethod.POST, cancelRequest);
    }

    @Override
    public int getLocalUserPoints(final User user) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Method Not Supported for Remote Programs!");
    }


    // Get complete user profile from VIS including Name, Address, account balance etc
    @Override
    public AccountInfo getUserProfile(final String varId, final String accountId, final Map<String, String> additionalInfo){
        // USM user's are identified by sid
        final StringBuilder visURL=new StringBuilder();
        visURL.append(getBaseUrl(varId))
                .append("/accounts/")
                .append(accountId);

        if(additionalInfo!=null && !additionalInfo.isEmpty()) {
            visURL.append('?');
            additionalInfo.forEach((k, v) -> {
                visURL.append(k).append('=').append(v).append('&');
            });
            visURL.deleteCharAt(visURL.length()-1);
        }
        LOG.debug("getUserProfile -- VIS call to retrieve user information : {}", visURL);

        AccountInfo accountInfo = null;
        try {
            accountInfo = visHttpClientUtil.getHttpResponse(visURL.toString(), AccountInfo.class, HttpMethod.GET,null);
        } catch (final Exception e) {
            LOG.error("Error while getting user profile for user id: {}, having var id: {} ", accountId, varId, e);
            throw new RuntimeException(e);
        }
        return accountInfo;
    }

    private String getProductType(final String supplierId) {
        if (CommonConstants.SUPPLIER_TYPE_GIFTCARD_S.equals(supplierId)) {
            return CommonConstants.VIS_PRODUCT_TYPE_GIFTCARD;
        } else if (SUPPLIER_TYPE_SERVICE_PLAN_S.equals(supplierId)) {
            return VIS_PRODUCT_TYPE_SERVICEPLAN;
        }
        return CommonConstants.VIS_PRODUCT_TYPE_MERCHANDISE;
    }

    @Override
    public SessionResponse getUserSession(final User user) {
        SessionRequest sessionRequest = getSessionRequest(user);
        final String visUrl = getBaseUrl(user.getVarId()) + "/sessions";
        SessionResponse sessionResponse = null;
        try {
            sessionResponse = visHttpClientUtil.getHttpResponse(visUrl, SessionResponse.class, HttpMethod.POST, sessionRequest);
        }catch (Exception e){
            LOG.error("Error while performing VIMS Sessions for user id: {}, having var id: {} and program id: {} ",
                user.getUserId(), user.getVarId(), user.getProgramId(), e);
            throw new RuntimeException(e);
        }
        return sessionResponse;
    }

    private SessionRequest getSessionRequest(final User user){
        SessionRequest sessionRequest = new SessionRequest();
        if (Objects.nonNull(user.getAdditionalInfo()) &&
            StringUtils.isNotBlank(user.getAdditionalInfo().get(CommonConstants.AUTHORIZATION_TOKEN))) {
                sessionRequest.setAuthorizationToken(user.getAdditionalInfo().get(CommonConstants.AUTHORIZATION_TOKEN));
        }
        return sessionRequest;
    }

}
