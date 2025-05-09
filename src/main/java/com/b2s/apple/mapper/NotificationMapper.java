package com.b2s.apple.mapper;

import com.b2s.apple.entity.OrderDiagnosticInfoEntity;
import com.b2s.apple.services.ServicePlanInfoService;
import com.b2s.apple.services.VarProgramMessageService;
import com.b2s.db.model.*;
import com.b2s.rewards.apple.dao.OrderAttributeValueDao;
import com.b2s.rewards.apple.dao.OrderDiagnosticInfoDao;
import com.b2s.rewards.apple.dao.ShipmentNotificationDao;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.ContextUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.merchandise.action.CartCalculationUtil;
import com.b2s.spark.api.apple.to.IAppleEmailRequest;
import com.b2s.spark.api.apple.to.INotification;
import com.b2s.spark.api.apple.to.impl.Address;
import com.b2s.spark.api.apple.to.impl.Product;
import com.b2s.spark.api.apple.to.impl.*;
import com.b2s.spark.client.to.EmailData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.b2s.rewards.common.util.CommonConstants.*;

/**
 * Created by rpillai on 8/25/2016.
 */
@Component
public class NotificationMapper {

    private static final String DOCUMENT_EXTENSION = ".pdf";
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationMapper.class);

    @Autowired
    private ShipmentNotificationDao shipmentNotificationDao;

    @Value("#{shipmentTrackingUrls}")
    private Map<String, String> shipmentTrackingUrls;

    @Autowired
    private Properties applicationProperties;

    @Autowired
    private OrderAttributeValueDao orderAttributeValueDao;

    @Autowired
    private VarProgramMessageService varProgramMessageService;

    @Autowired
    private ServicePlanInfoService servicePlanInfoService;

    @Autowired
    private ContextUtil contextUtil;

    @Autowired
    private OrderDiagnosticInfoDao orderDiagnosticInfoDao;

    private String getOrderDateFormat(final String varId, final String programId, final Locale locale){
        String orderDateFormat = getApplicationProperty( EMAIL_ORDER_DATE_FORMAT,varId, programId, locale, applicationProperties);
        if(StringUtils.isEmpty(orderDateFormat)) {
            orderDateFormat = getApplicationProperty( EMAIL_ORDER_DATE_FORMAT,varId, programId, applicationProperties);
        }
        return orderDateFormat;
    }

    private String getShipmentDateFormat(final String varId, final String programId, final Locale locale){
        String shippingDateFormat = getApplicationProperty( EMAIL_SHIPMENT_DATE_FORMAT,varId, programId, locale, applicationProperties );
        if(StringUtils.isEmpty(shippingDateFormat)) {
            shippingDateFormat = getApplicationProperty( EMAIL_SHIPMENT_DATE_FORMAT,varId, programId, applicationProperties );
        }
        return shippingDateFormat;
    }

    /**
     * Find the order attribute from a list of order attributes for the given name
     *
     * @param orderAttributeValueList
     * @param attributeName
     * @return
     */
    private Optional<OrderAttributeValue> findOrderAttribute(final List<OrderAttributeValue> orderAttributeValueList, final String attributeName) {
       return orderAttributeValueList
            .stream()
            .filter(orderAttributeValue ->
                (orderAttributeValue != null
                    && orderAttributeValue.getName() != null
                    && orderAttributeValue.getName().equalsIgnoreCase(attributeName)
                    && StringUtils.isNotBlank(orderAttributeValue.getValue())))
            .findFirst();
    }

    public OrderEmailData getOrderEmailData(final Order order, final Program program, final Notification notification,
        final Integer lineNum, final Properties dbProperties, final String subscriptionUrl) {

        final OrderEmailData orderEmailData = new OrderEmailData();
        final Locale orderLocale = getLocaleFromOrder(order);
        final MessageSource messageSource = contextUtil.getMessageSource(order.getVarId());
        final String pointName = messageSource.getMessage(program.getPointName(), null, program.getPointName(), orderLocale);

        orderEmailData.setImageServer(applicationProperties.getProperty(CommonConstants.IMAGE_SERVER_URL_KEY));

        orderEmailData.setSubject(notification.getSubject());
        setOrderInfo(order, program, orderEmailData, orderLocale);

        // set branding info like program name, logo etc
        final Branding branding = new Branding();
        branding.setVarName(program.getVarId());
        branding.setProgramName(program.getName());

        // set the locale in branding
        final Locale locale = getLocaleFromOrder(order);
        Messages messages = createBrandingMessage(program,locale);
        branding.setMessages(messages);
        branding.setLocale(locale.toString());
        branding.setTermsAndConditionsURL(buildTermsURL(TERMS_AND_CONDITIONS_BASE_PATH, locale));
        branding.setTermsOfUseURL(messageSource.getMessage(TERMS_OF_USE_URL,null,"urlNotFound",locale));
        branding.setPrivacyPolicyURL(messageSource.getMessage(PRIVACY_POLICY_URL,null,"urlNotFound",locale));
        branding.setTermsAdditionalContent(messageSource.getMessage(TERMS_ADDITIONAL_CONTENT,null,null,locale));

        // SparkPost Common Email Template Attributes
        populateCommonTemplateAttributes(order, program, notification, dbProperties, orderEmailData, messageSource,
            branding);
        branding.setCurrentDate(getCurrentNotifDate());

        orderEmailData.setBranding(branding);

        // set AMP data for AMP service notifications
        if (AppleUtil.isNotificationAmp(NotificationName.get(notification.getName()))) {
            orderEmailData.setAmpData(getAmpData(notification.getName(), dbProperties, subscriptionUrl));
        }

        // Set Service Plan Info notification
        if (NotificationName.SERVICE_PLAN.value.equals(notification.getName())) {
            orderEmailData.setServicePlanInfo(getServicePlanInfo(order.getOrderId(), lineNum));
        }

        // set the list of products
        populateProductListAndDiscountInfo(order, program, notification, lineNum, orderEmailData, pointName);

        // Purchase value information
        populatePaymentDetail(order, orderEmailData, pointName, program);


        // set shipping address
        populateShipmentAddress(order, orderEmailData);

        // set contact info
        final ContactInfo contactInfo = new ContactInfo();
        contactInfo.setPhone(order.getPhone());
        contactInfo.setEmail(order.getEmail());
        orderEmailData.setContactInfo(contactInfo);
        setShipmentInformation(order, program, notification, lineNum, orderEmailData, orderLocale);

        orderEmailDataSetWorkPlace(order, orderEmailData);
        return orderEmailData;
    }

    private void populateShipmentAddress(final Order order, final OrderEmailData orderEmailData) {
        final Address address = new Address();
        address.setFirstName(order.getFirstname());
        address.setLastName(order.getLastname());
        address.setBusinessName(order.getBusinessName());
        address.setAddressLine1(order.getAddr1());
        address.setAddressLine2(order.getAddr2());
        address.setAddressLine3(order.getAddr3());
        address.setCity(order.getCity());
        address.setPostalCode(order.getZip());
        address.setState(order.getState());
        orderEmailData.setShippingAddress(address);
    }

    private void populateProductListAndDiscountInfo(final Order order, final Program program, final Notification notification,
        final Integer lineNum, final OrderEmailData orderEmailData, final String pointName) {
        final List<Product> products = new ArrayList<>();
        final List<Discount> discounts = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(order.getOrderLines())) {
            if(lineNum != null && lineNum > 0) {
                final Optional<OrderLine> orderLineOpt = order.getOrderLines().stream().filter(
                    ol -> ol.getLineNum().equals(lineNum) &&
                        !ol.getSupplierId().equalsIgnoreCase(AMP_SUPPLIER_ID_STRING)).findFirst();
                orderLineOpt.ifPresent(orderLine -> {
                    final Product product = getProduct(orderLine, order, pointName, program);
                    productSetECertCodeFromShipmentTrackingUrl(order, notification, lineNum, product);
                    products.add(product);
                    // get partner order id from shipment notification
                    orderEmailData.getOrderInfo().setSupplierOrderID(orderLine.getSupplierOrderId());
                });
            } else {
                loadProductsAndDiscounts(order, program, pointName, products, discounts);
            }

        }
        orderEmailData.setProducts(products);
        orderEmailData.setDiscounts(discounts);
    }

    private void populateCommonTemplateAttributes(final Order order, final Program program,
        final Notification notification, final Properties dbProperties, final OrderEmailData orderEmailData,
        final MessageSource messageSource, final Branding branding) {
        if (dbProperties != null && !dbProperties.isEmpty()) {
            String emailLogoUrl = (String) dbProperties.get(CommonConstants.SPARK_POST_LOGO_URL);
            try {
                if (StringUtils.isNotBlank(emailLogoUrl) && !new URI(emailLogoUrl).isAbsolute()) {
                    emailLogoUrl = applicationProperties.getProperty(CommonConstants.IMAGE_SERVER_URL_KEY) +
                        messageSource.getMessage(emailLogoUrl, null, emailLogoUrl,
                            getLocaleFromOrder(order));
                }
            } catch (URISyntaxException e) {
                LOGGER.error(
                    "Error while mapping email image url for var id: {}, program id: {}, logo url {}. Exception: {}",
                    program.getVarId(), program.getProgramId(), emailLogoUrl, e);
            }

            //set logo information
            populateLogoInfo(order.getProgramLogoUrl(), program, dbProperties, branding, emailLogoUrl);

            //Populate Email subject
            setEmailSubject(notification, dbProperties, orderEmailData, branding);

            branding.setFromEmail((String) dbProperties.getOrDefault(CommonConstants.SPARK_POST_FROM_EMAIL, null));
            branding.setFromName((String) dbProperties.getOrDefault(CommonConstants.SPARK_POST_FROM_NAME, null));
            branding.setFooter((String) dbProperties.getOrDefault(CommonConstants.SPARK_POST_FOOTER, null));
        }
    }

    private void setEmailSubject(final Notification notification, final Properties dbProperties,
        final OrderEmailData orderEmailData, final Branding branding) {
        if(NotificationName.CONFIRMATION.name().equals(notification.getName())) {
            branding.setSubject((String) dbProperties.getOrDefault(CommonConstants.SPARK_POST_SUBJECT_CONFIRMATION, null));
        } else if(NotificationName.SHIPMENT.name().equals(notification.getName())) {
            branding.setSubject((String) dbProperties.getOrDefault(CommonConstants.SPARK_POST_SUBJECT_SHIPMENT, null));
        } else if(NotificationName.SHIPMENT_DELAY.name().equals(notification.getName())) {
            String subject = (String) dbProperties.getOrDefault(CommonConstants.SPARK_POST_SUBJECT_DELAY, null);
            if (StringUtils.isNotEmpty(subject)) {
                subject = subject.replace("{{orderInfo.varOrderID}}", orderEmailData.getOrderInfo().getVarOrderID());
            }
            branding.setSubject(subject);
            if (Boolean.parseBoolean((String) dbProperties.getOrDefault(CommonConstants.SPARK_POST_HIDE_DELAYED_CUSTOMER_TEXT, null))) {
                branding.getMessages().setCustomerService(null);
            }
        }
    }

    // Check whether the order has custom program url. e.g. BSWIFT may have different logo urls
    // The logic in the following line is implemented only for Amex VAR
    private void populateLogoInfo(final String logoUrl, final Program program, final Properties dbProperties,
        final Branding branding, final String emailLogoUrl) {
        String programLogoUrl = logoUrl;
        if (StringUtils.isBlank(programLogoUrl)) {
            programLogoUrl = emailLogoUrl;
        }
        branding.setLogoURL((StringUtils.isNotBlank(programLogoUrl)) ? programLogoUrl : program.getImageUrl());

        branding.setShowLogo(Boolean.parseBoolean((String) dbProperties.getOrDefault(CommonConstants.SPARK_POST_SHOW_LOGO, null)));
    }

    private AmpData getAmpData(String ampService, Properties dbProperties, String subscriptionUrl) {
        final AmpData ampData = new AmpData();
        if (dbProperties != null && !dbProperties.isEmpty() && StringUtils.isNotEmpty(ampService)) {
            ampData.setSubscriptionUrl(subscriptionUrl);
            ampData.setFooter((String) dbProperties.get(ampService + "-" + SPARK_POST_AMP_FOOTER));
            ampData.setBody((String) dbProperties.get(ampService + "-" + SPARK_POST_AMP_BODY));
            ampData.setCtaText((String) dbProperties.get(ampService + "-" + SPARK_POST_AMP_CTA_TEXT));
            ampData.setTitle((String) dbProperties.get(ampService + "-" + SPARK_POST_AMP_TITLE));
            ampData.setTrailLength((String) dbProperties.get(ampService + "-" + SPARK_POST_AMP_TRAIL_LENGTH));
            ampData.setTrailText((String) dbProperties.get(ampService + "-" + SPARK_POST_AMP_TRAIL_TEXT));
            ampData.setSubject((String) dbProperties.get(ampService + "-" + SPARK_POST_AMP_SUBJECT));
            String ampBodyListStr = StringEscapeUtils.unescapeJava((String) dbProperties.get(ampService + "-" + SPARK_POST_AMP_BODY_LIST));
            if (StringUtils.isNotEmpty(ampBodyListStr)) {
                ampData.setBodyList(Stream.of(ampBodyListStr.split(NEW_LINE_REGEX)).collect(Collectors.toList()));
            }
        }
        return ampData;
    }

    private NotificationDate getCurrentNotifDate() {
        LocalDate now = LocalDate.now();
        NotificationDate notificationDate = new NotificationDate();
        notificationDate.setDay(String.valueOf(now.getDayOfMonth()));
        notificationDate.setYear(String.valueOf(now.getYear()));
        notificationDate.setMonth(String.valueOf(now.getMonthValue()));
        return notificationDate;
    }

    private ServicePlanInfo getServicePlanInfo(final long orderId, final Integer lineNum) {
        final ServicePlanInfo servicePlanInfo = new ServicePlanInfo();
        final ServicePlanData servicePlanData = servicePlanInfoService.getServicePlanData(orderId, lineNum);

        if(Objects.nonNull(servicePlanData)) {
            servicePlanInfo.setPlanId(servicePlanData.getPlanId());
            servicePlanInfo.setPlanUrl(servicePlanData.getPlanUrl());
            servicePlanInfo.setPlanEndDate(servicePlanData.getPlanEndDate().toString());
            servicePlanInfo.setHardwareSerialNumber(servicePlanData.getHardwareSerialNumber());
            servicePlanInfo.setHardwareDescription(servicePlanData.getHardwareDescription());
        }
        return servicePlanInfo;
    }

    private void loadProductsAndDiscounts(final Order order, final Program program, final String pointName,
        final List<Product> products, final List<Discount> discounts) {
        order.getOrderLines().forEach(orderLineObj -> {
            final OrderLine orderLine = (OrderLine) orderLineObj;
            if(orderLine != null) {
                if(CommonConstants.APPLE_SUPPLIER_ID_STRING.equals(orderLine.getSupplierId()) ||
                        CommonConstants.SUPPLIER_TYPE_GIFTCARD_S.equals(orderLine.getSupplierId()) ||
                        SUPPLIER_TYPE_SERVICE_PLAN_S.equals(orderLine.getSupplierId())) {
                    final Product product = getProduct(orderLine,order, pointName,program);
                    products.add(product);
                } else if(CommonConstants.SUPPLIER_TYPE_DISCOUNTCODE_S.equals(orderLine.getSupplierId())) {
                    final Discount discount = new Discount();
                    discount.setCode(orderLine.getItemId());
                    discount.setAmount(
                        Money.ofMinor(CurrencyUnit.of(getLocaleFromOrder(order)), -orderLine.getSupplierItemPrice()).toString());
                    discount.setAmountPoints(formatPoints(orderLine.getItemPoints().intValue()) + ' ' +
                        pointName);
                    discount.setDescription(orderLine.getName());
                    discounts.add(discount);
                }
            }
        });
    }

    private void productSetECertCodeFromShipmentTrackingUrl(final Order order, final Notification notification,
        final Integer lineNum, final Product product) {
        // set the eCertCode value trackingUrl
        if(NotificationName.ECERT.name().equals(notification.getName())){
            final OrderLineShipmentNotification
                shipmentNotification = shipmentNotificationDao.getShipmentNotification(order.getOrderId(), lineNum);
            if(shipmentNotification != null){
                product.seteCertCode(shipmentNotification.getTrackingUrl());
            }
        }
    }

    private void orderEmailDataSetWorkPlace(final Order order, final OrderEmailData orderEmailData) {
        if (order instanceof OrderAWP) {
            OrderAWP orderAWP = (OrderAWP) order;
            final Workplace workPlace = new Workplace();
            workPlace.setPayrollAgreementURL(orderAWP.getPayrollAgreementUrl());
            orderEmailData.setWorkplace(workPlace);
        } else {
            orderEmailData.setWorkplace(null);
        }
    }

    private void setOrderInfo(final Order order, final Program program, final OrderEmailData orderEmailData,
        final Locale orderLocale) {
        // set basic order info
        final OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderID(order.getOrderId().toString());
        orderInfo.setVarOrderID(order.getVarOrderId());
        final DateFormat emailDateFormat = new SimpleDateFormat(
            getOrderDateFormat(program.getVarId(), program.getProgramId(), orderLocale), orderLocale);
        final List<OrderAttributeValue> orderAttributeValueList = orderAttributeValueDao.getByOrder(order.getOrderId());
        if (CollectionUtils.isNotEmpty(orderAttributeValueList)) {
            applyOrderEmployerNameAndId(program, orderInfo, orderAttributeValueList);

            final Optional<OrderAttributeValue>
                timeZoneAttribute = findOrderAttribute(orderAttributeValueList, CommonConstants.TIME_ZONE_ID);
            final String orderDate = timeZoneAttribute.map(value ->
                emailDateFormat.format(
                    CartCalculationUtil.convertTimeZone(order.getOrderDate(), TimeZone.getTimeZone(value.getValue()))))
                .orElse(emailDateFormat.format(order.getOrderDate()));
            orderInfo.setOrderDate(orderDate);

            final OrderAttributeValue orderHistoryUrl = findOrderAttribute(orderAttributeValueList,
                CommonConstants.ORDER_HISTORY_URL).orElse(null);

            if (Objects.nonNull(orderHistoryUrl)) {
                orderInfo.setOrderHistoryUrl(orderHistoryUrl.getValue());
                LOGGER.info("Order History URL in order_attribute table {}", orderHistoryUrl.getValue());
            }
        } else {
            orderInfo.setOrderDate(emailDateFormat.format(order.getOrderDate()));
        }

        if (AppleUtil.getProgramConfigValueAsBoolean(program, VIEW_ANONYMOUS_ORDER_DETAIL)) {
            final OrderDiagnosticInfoEntity orderDiagnosticInfo =
                orderDiagnosticInfoDao.getHostNameByOrderId(order.getOrderId().toString());
            if (Objects.nonNull(orderDiagnosticInfo)) {
                final String hostname = orderDiagnosticInfo.getHostname();
                final String uid = order.getOrderId() + "|" + order.getEmail();
                final StringBuilder orderUrl = new StringBuilder(hostname)
                    .append("/apple-gr/AnonLogin?v=").append(program.getVarId())
                    .append("&p=").append(program.getProgramId())
                    .append("&l=").append(LocaleUtils.toLocale(order.getLanguageCode() + "_" + order.getCountryCode()))
                    .append("&uid=")
                    .append(Base64.getEncoder().encodeToString(uid.getBytes()));
                orderInfo.setOrderHistoryUrl(orderUrl.toString());
            }
        }
        orderEmailData.setOrderInfo(orderInfo);
    }

    private void setShipmentInformation(final Order order, final Program program, final Notification notification,
        final Integer lineNum, final OrderEmailData orderEmailData, final Locale orderLocale) {
        // set shipment information for shipment emails.
        if(lineNum != null && lineNum > 0) {
            if(NotificationName.SHIPMENT_DELAY.name().equals(notification.getName())) {
                orderEmailDataSetShipmentDelay(order, lineNum, orderEmailData);
            } else if(NotificationName.SHIPMENT.name().equals(notification.getName())) {
                orderEmailDataSetShipment(order, program, lineNum, orderEmailData, orderLocale);
            }

        }
    }

    private void orderEmailDataSetShipment(final Order order, final Program program, final Integer lineNum,
        final OrderEmailData orderEmailData, final Locale orderLocale) {
        final Shipment shipment = new Shipment();
        final OrderLineShipmentNotification
            shipmentNotification = shipmentNotificationDao.getShipmentNotification(order.getOrderId(), lineNum);
        if (shipmentNotification != null) {
            shipment.setCarrier(shipmentNotification.getShippingCarrier());

            final String shipmentDateFormatForVarProgramLocale =
                getShipmentDateFormat(program.getVarId(), program.getProgramId(), orderLocale);
            final SimpleDateFormat shipmentDateFormat =
                new SimpleDateFormat(shipmentDateFormatForVarProgramLocale, getLocaleFromOrder(order));
            shipment.setShipmentDate(formatShipmentDateOrNull(shipmentNotification.getShipmentDate(), shipmentDateFormat));

            shipment.setTrackingNumber(shipmentNotification.getTrackingNumber());
            String trackingUrl = shipmentNotification.getTrackingUrl();
            if (StringUtils.isBlank(trackingUrl)) {
                trackingUrl = shipmentTrackingUrls.get(shipment.getCarrier());
            }
            if (StringUtils.isNotBlank(trackingUrl)) {
                shipment.setTrackingURL(MessageFormat.format(trackingUrl, shipment.getTrackingNumber()));
            }
            orderEmailData.setShipment(shipment);
        }
    }

    private void orderEmailDataSetShipmentDelay(final Order order, final Integer lineNum,
        final OrderEmailData orderEmailData) {
        final Optional<OrderLine>
            orderLineOpt = order.getOrderLines().stream().filter(ol -> ((OrderLine) ol).getLineNum().equals(lineNum)).findFirst();
        if(orderLineOpt.isPresent() && CollectionUtils.isNotEmpty(orderLineOpt.get().getOrderAttributes())) {
            final ShipmentDelay shipmentDelay = new ShipmentDelay();
            for(OrderLineAttribute orderLineAttribute : orderLineOpt.get().getOrderAttributes()) {
                if(CommonConstants.ORDER_LINE_ATTRIBUTE_KEY_SHIPS_BY.equals(orderLineAttribute.getName())){
                    shipmentDelay.setEstimatedShip(orderLineAttribute.getValue());
                }
                if(CommonConstants.ORDER_LINE_ATTRIBUTE_KEY_DELIVERS_BY.equals(orderLineAttribute.getName())){
                    shipmentDelay.setEstimatedDeliver(orderLineAttribute.getValue());
                }
            }
            orderEmailData.setShipmentDelay(shipmentDelay);
        }
    }

    private Messages createBrandingMessage(final Program program, final Locale locale) {
        //Set customised message.
        Messages messages = new Messages();
        final Properties messageProperties =
            varProgramMessageService
                .getMessages(Optional.ofNullable(program.getVarId()), Optional.ofNullable(program.getProgramId()),
                    locale.toString());
        final String comments = messageProperties.getProperty(CommonConstants.SPARK_NOTIFICATION_COMMENTS);
        final String customerSupport = messageProperties.getProperty(CommonConstants.SPARK_NOTIFICATION_CUST_SUPPORT);
        final String termsAndCondition = messageProperties.getProperty(CommonConstants.SPARK_NOTIFICATION_TC);

        messages.setComments(StringUtils.isNotBlank(comments) ? comments : null);
        messages.setCustomerService(StringUtils.isNotBlank(customerSupport) ? customerSupport : null);
        messages.setTermsAndConditions(StringUtils.isNotBlank(termsAndCondition) ? termsAndCondition :
            null);
        return messages;
    }

    private void applyOrderEmployerNameAndId(final Program program, final OrderInfo orderInfo,
        final List<OrderAttributeValue> orderAttributeValueList) {
        // Add employer information, if exists. This is applicable only for BSWIFT EPP now.
        final Object emailIncludeEmployerInfoObj = program.getConfig().get(CommonConstants.EMAIL_INCLUDE_EMPLOYER_INFO_KEY);
        final Boolean emailIncludeEmployerInfo = (emailIncludeEmployerInfoObj != null && StringUtils.isNotBlank(emailIncludeEmployerInfoObj.toString())) ? new Boolean(emailIncludeEmployerInfoObj.toString()) : false;
        if(emailIncludeEmployerInfo) {
            final Optional<OrderAttributeValue>
                employerNameAttribute = findOrderAttribute(orderAttributeValueList, CommonConstants.EMPLOYER_NAME);
            if (employerNameAttribute.isPresent()) {
                orderInfo.setEmployerName(employerNameAttribute.get().getValue());
            }

            final Optional<OrderAttributeValue> clientIdAttribute = findOrderAttribute(orderAttributeValueList, CommonConstants.CLIENT_ID);
            if (clientIdAttribute.isPresent()) {
                orderInfo.setEmployerId(clientIdAttribute.get().getValue());
            }
        }
    }

    private Locale getLocaleFromOrder(final Order order) {

        return new Locale(order.getLanguageCode(), order.getCountryCode());
    }

    private String formatShipmentDateOrNull(final Date shipmentDate, final DateFormat shipmentDateFormat) {

        if (shipmentDate == null) {
            return null;
        }
        return shipmentDateFormat.format(shipmentDate);
    }

    public Product getProduct(final OrderLine orderLine, final Order order, final String pointName,
        final Program program) {
        final Product product = new Product();
        BigDecimal itemPrice = BigDecimal.valueOf(orderLine.getSupplierItemPrice());
        final BigDecimal supplierSalesTax = BigDecimal.valueOf(orderLine.getSupplierTaxPrice());
        final Locale locale = getLocaleFromOrder(order);

        String itemPoints = null;
        String itemTotalPoints = null;

        final boolean isCashRewardPricing = AppleUtil.isProgramConfigValueMatching(program, PRICING_TEMPLATE, POINTS_DECIMAL);
        if (isCashRewardPricing) {
            itemPoints = formatCashRewardPricing(orderLine.getItemPoints().intValue() * orderLine.getQuantity(), locale, pointName);
            itemTotalPoints = formatCashRewardPricing(orderLine.getOrderLinePoints(), locale, pointName);
        } else {
            itemPoints = formatPoints(orderLine.getItemPoints().intValue() * orderLine.getQuantity()) + ' ' + pointName;
            itemTotalPoints = formatPoints(orderLine.getOrderLinePoints()) + ' ' + pointName;
        }

        product.setItemPoints(itemPoints);
        product.setItemTotalPoints(itemTotalPoints);
        if(!StringUtils.equalsIgnoreCase(CommonConstants.AWP_NONE_SUPPLIER,order.getFullfillmentPartnerName())) {

            product.setItemTotalPrice(formatPrice((Money.ofMinor(CurrencyUnit.of(locale), orderLine.getVarOrderLinePrice().longValue())).getAmount(), locale));

            if(BundledPricingOption.BUNDLED.equals(program.getBundledPricingOption())){
                itemPrice = itemPrice.add(supplierSalesTax);
            }
            product.setItemPrice(formatPrice((Money.ofMinor(CurrencyUnit.of(locale), itemPrice.longValue())).getAmount(), locale));
        }
        product.setQuantity(orderLine.getQuantity().toString());
        product.setImageURL(orderLine.getImageUrl());
        product.setItemImageURL(orderLine.getImageUrl());
        product.setItemName(orderLine.getName());
        product.setSku(orderLine.getSku());
        product.setAppleSKU(orderLine.getAppleSku());

        if (StringUtils.isNotBlank(orderLine.getPaymentFrequency()) && (CommonConstants.EXPERIENCE_DRP.equalsIgnoreCase
            ((String) program.getConfig().get(CommonConstants.SHOP_EXPERIENCE)))) {
            product.setPayrollFrequency(orderLine.getPaymentFrequency());
            product.setPayrollDuration(orderLine.getPaymentDuration().toString());
            product.setPayrollAmount(formatPrice(BigDecimal.valueOf(orderLine.getPayrollAmount()),locale));
            product.setPayrollPeriodPricing(formatPrice(BigDecimal.valueOf((orderLine.getPayrollTotalAmount()))
                .setScale(2),locale));
        }

        orderLine.getOrderAttributes().forEach(orderLineAttribute -> {
            if (orderLineAttribute.getName().equalsIgnoreCase(CommonConstants.ENGRAVING_LINE_1)) {
                product.setEngravingLine1(orderLineAttribute.getValue());
            } else if (orderLineAttribute.getName().equalsIgnoreCase(CommonConstants.ENGRAVING_LINE_2)) {
                product.setEngravingLine2(orderLineAttribute.getValue());
            } else if (orderLineAttribute.getName().equalsIgnoreCase(CommonConstants.SHIPPING_AVAILABILITY)) {
                product.setItemAvailability(orderLineAttribute.getValue());
            }
        });
        return product;
    }

    private void populatePaymentDetail(final Order order, final OrderEmailData orderEmailData,final String pointsName, final  Program program) {

        final boolean showGST = (Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_GST, Boolean.FALSE);
        final boolean passBlueTierDiscountToUser= (Boolean) program.getConfig().getOrDefault(CommonConstants.PASS_BLUETIER_DISCOUNT_TO_USER, Boolean.TRUE);
        final OrderTotals orderTotals = new OrderTotals();
        final Locale locale = getLocaleFromOrder(order);
        calculateOrderTotalsItemsTotal(order, program, orderTotals, locale);

        Money orderTotal = setOrderTotalsOrderTotal(order, program, passBlueTierDiscountToUser, orderTotals, locale);
        setOrderTotalsShippingTotal(order, orderTotals, locale);
        setOrderTotalsTaxTotal(order, showGST, orderTotals, locale);

        setOrderTotalsEarnPoints(order, pointsName, program, orderTotals);

        setOrderTotalsDiscountTotal(order, orderTotals, locale);
        setOrderTotalsFeeTotal(order, orderTotals, locale);


        final List<OrderLine> orderLines = order.getOrderLines();

        // Tax and CustomeTotal to be shown only for VitalityCA and its single item purchase
        orderTotals.setTax( orderLines.stream()
            .filter(o -> o.getSupplierId().equalsIgnoreCase(CommonConstants.APPLE_SUPPLIER_ID_STRING))
            .limit(1)
            .map(o -> o.getTaxes())
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(OrderLineTax::getName, orderLineTax -> formatPrice(orderLineTax.getAmount(), locale))));

        CustomTotals customTotals=new CustomTotals();
        orderLines.stream()
            .filter(o -> o.getSupplierId().equalsIgnoreCase(CommonConstants.APPLE_SUPPLIER_ID_STRING))
            .limit(1)
            .map(orderLine -> orderLine.getOrderAttributes())
            .flatMap(Collection::stream)
            .forEach(orderLineAttribute -> {
                if (orderLineAttribute.getName().equalsIgnoreCase(CommonConstants.ACTIVATION_FEE)) {
                    customTotals.setInitialPayment(formatPrice(new BigDecimal(orderLineAttribute.getValue()).setScale(2),locale));
                }
                if (orderLineAttribute.getName().equalsIgnoreCase(CommonConstants.UPGRADE_FEE)){
                    customTotals.setUpgradeFee(formatPrice(new BigDecimal(orderLineAttribute.getValue()).setScale(2), locale));
                }
            });

        orderTotals.setCustomTotals(customTotals);

        String itemsTotalPoints = null;
        String orderTotalPoints = null;
        String shippingTotalPoints = null;
        String taxTotalPoints = null;
        String feeTotalPoints = null;
        String taxAndFeeTotalPoints = null;

        // Set order information in points.
        final boolean isCashRewardPricing = AppleUtil.isProgramConfigValueMatching(program, PRICING_TEMPLATE, POINTS_DECIMAL);
        if (isCashRewardPricing) {
            itemsTotalPoints = formatCashRewardPricing(order.getOrderSubTotalInPoints(), locale, pointsName);
            orderTotalPoints = formatCashRewardPricing(order.getOrderTotalInPoints(), locale, pointsName);
            shippingTotalPoints = formatCashRewardPricing(order.getOrderTotalShippingInPoints(), locale, pointsName);
            taxTotalPoints = formatCashRewardPricing(order.getOrderTotalTaxesInPoints(), locale, pointsName);
            feeTotalPoints = formatCashRewardPricing(order.getOrderTotalFeesInPoints(), locale, pointsName);
            taxAndFeeTotalPoints = formatCashRewardPricing(order.getOrderTotalInPoints() - order.getOrderSubTotalInPoints(), locale, pointsName);
        } else {
            itemsTotalPoints = formatPoints(order.getOrderSubTotalInPoints()) + ' ' + pointsName;
            orderTotalPoints = formatPoints(order.getOrderTotalInPoints()) + ' ' + pointsName;
            shippingTotalPoints = formatPoints(order.getOrderTotalShippingInPoints()) + ' ' + pointsName;
            taxTotalPoints = formatPoints(order.getOrderTotalTaxesInPoints()) + ' ' + pointsName;
            feeTotalPoints = formatPoints(order.getOrderTotalFeesInPoints()) + ' ' + pointsName;
            taxAndFeeTotalPoints = formatPoints(order.getOrderTotalInPoints() - order.getOrderSubTotalInPoints()) + ' ' + pointsName;
        }
        orderTotals.setItemsTotalPoints(itemsTotalPoints);
        orderTotals.setOrderTotalPoints(orderTotalPoints);

        orderTotals.setShippingTotalPoints(shippingTotalPoints);
        orderTotals.setTaxTotalPoints(taxTotalPoints);
        orderTotals.setFeeTotalPoints(feeTotalPoints);
        orderTotals.setTaxAndFeeTotalPoints(taxAndFeeTotalPoints);

        orderEmailData.setOrderTotals(orderTotals);

        // set various payment tenders
        final int orderTotalInPoints=order.getOrderTotalInPoints();
        final PaymentTenders paymentTenders = new PaymentTenders();

        if (CommonConstants.EXPERIENCE_DRP.equalsIgnoreCase((String) program.getConfig().get(CommonConstants.SHOP_EXPERIENCE))) {
            setPaymentTendersPayrollFrequencyAndDuration(order, program, locale, orderTotal, paymentTenders);

        }else {
            // Points payment  - Sum cashBuyInPoints since it will be negative values
            final int totalPointsPaid = orderTotalInPoints + order.getOrderTotalCashBuyInPoints();

            String pointsPaid = null;
            String orderTotalPointsStr = null;

            if (isCashRewardPricing) {
                pointsPaid = formatCashRewardPricing(totalPointsPaid, locale, pointsName);
                orderTotalPointsStr = formatCashRewardPricing(orderTotalInPoints, locale, pointsName);
            } else {
                pointsPaid = formatPoints(totalPointsPaid) + ' ' + pointsName;
                orderTotalPointsStr = formatPoints(orderTotalInPoints) + ' ' + pointsName;
            }
            paymentTenders.setPointsPaid(pointsPaid);
            paymentTenders.setOrderTotalPoints(orderTotalPointsStr);
            setPaymentTendersCcPaid(order, locale, paymentTenders);
            setPaymentTendersPayrollPaid(order, locale, paymentTenders);

            final Money payrollPeriodPrice = order.getOrderTotalPayrollPeriodPrice();

            if (payrollPeriodPrice != null && payrollPeriodPrice.getAmount() != null &&
                payrollPeriodPrice.getAmount().doubleValue() > 0) {
                paymentTenders.setPayrollPeriodPricing(formatPrice(payrollPeriodPrice.getAmount(), locale));
                paymentTenders.setPayrollFrequency(String.valueOf(order.getPayrollFrequency()));
                paymentTenders.setPayrollDuration(order.getPayrollDuration());
            }
        }

        orderEmailData.setPaymentTenders(paymentTenders);
    }

    private void setPaymentTendersPayrollFrequencyAndDuration(final Order order, final Program program,
        final Locale locale, final Money orderTotal, final PaymentTenders paymentTenders) {
        OrderLine line = (OrderLine) order.getOrderLines().stream()
            .filter(o -> Objects.nonNull(((OrderLine) o).getPaymentDuration())&& (((OrderLine) o).getPaymentDuration() != 0))
            .findFirst().orElse(null);

        if(Objects.nonNull(line)){
            final BigDecimal payPerPeriod;
            if (Objects.nonNull(program) && Objects.nonNull(orderTotal) && Objects.nonNull(orderTotal.getAmount())
                && Objects.nonNull(line.getPaymentDuration())  &&
                !StringUtils.equalsIgnoreCase(CommonConstants.AWP_NONE_SUPPLIER,order.getFullfillmentPartnerName())) {
                payPerPeriod = getPayPerPeriodPrice(program, orderTotal.getAmount(), line.getPaymentDuration());
                paymentTenders.setPayrollPeriodPricing(formatPrice(payPerPeriod, locale));
                setPaymentTendersPayrollPayment(orderTotal, paymentTenders);
            }
            setPaymentTendersPayrollFrequency(order, locale, paymentTenders, line);
            paymentTenders.setPayrollDuration(line.getPaymentDuration().toString());
        }
    }

    private void setPaymentTendersPayrollPayment(final Money orderTotal, final PaymentTenders paymentTenders) {
        //Setting the payroll payment flag
        if (StringUtils.isNotBlank(paymentTenders.getPayrollPeriodPricing()) &&
            orderTotal.getAmount().doubleValue() != 0.0) {
            paymentTenders.setPayrollPayment(CommonConstants.TRUE_VALUE);
        } else {
            paymentTenders.setPayrollPayment(CommonConstants.FALSE_VALUE);
        }
    }

    private void setPaymentTendersPayrollFrequency(final Order order, final Locale locale,
        final PaymentTenders paymentTenders, final OrderLine line) {
        if(order.isEmployerManaged()) {
            final MessageSource messageSource = contextUtil.getMessageSource(order.getVarId());
            final String payFrequency = messageSource.getMessage(CommonConstants.PAY_FREQUENCY_PER_PAYMENT , null, CommonConstants.PAY_FREQUENCY_PER_PAYMENT , locale);
            paymentTenders.setPayrollFrequency(payFrequency);

        } else {
            paymentTenders.setPayrollFrequency(line.getPaymentFrequency());
        }
    }

    private void setPaymentTendersPayrollPaid(final Order order, final Locale locale,
        final PaymentTenders paymentTenders) {
        // Payroll deduction.
        final Money payrollPrice = order.getOrderTotalPayrollPrice();
        if (payrollPrice != null && payrollPrice.getAmount() != null && payrollPrice.getAmount().doubleValue() > 0) {
            paymentTenders.setPayrollPaid(formatPrice(payrollPrice.getAmount(), locale));
        }
    }

    private void setPaymentTendersCcPaid(final Order order, final Locale locale, final PaymentTenders paymentTenders) {
        // Card payment
        final Money ccPaid = order.getOrderTotalMoneyPaid();
        if (!ccPaid.isNegativeOrZero()) {
            paymentTenders.setCcPaid(formatPrice(ccPaid.getAmount(), locale));
        }
    }

    private void setOrderTotalsFeeTotal(final Order order, final OrderTotals orderTotals, final Locale locale) {
        final Money feeTotal = order.getOrderTotalFeesInMoney();
        if (feeTotal != null) {
            orderTotals.setFeeTotal(formatPrice(feeTotal.getAmount(), locale));
        }
    }

    private void setOrderTotalsDiscountTotal(final Order order, final OrderTotals orderTotals, final Locale locale) {
        final Money discountTotal = order.getOrderTotalDiscounts();
        if(discountTotal != null && discountTotal.getAmount() != null && discountTotal.getAmount().doubleValue() > 0) {
            orderTotals.setDiscountsTotal(formatPrice(discountTotal.getAmount(), locale));
        }
    }

    private void setOrderTotalsEarnPoints(final Order order, final String pointsName, final Program program,
        final OrderTotals orderTotals) {
        final boolean showEarnPoints = (Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_EARN_POINTS, Boolean.FALSE);
        if(showEarnPoints){
            orderTotals.setEarnTotalPoints(formatPoints(order.getEarnedPoints()) + ' ' + pointsName);
        }
    }

    private void setOrderTotalsTaxTotal(final Order order, final boolean showGST, final OrderTotals orderTotals,
        final Locale locale) {
        final Money taxTotal = order.getOrderTotalTaxesInMoney();
        if(taxTotal != null) {
            orderTotals.setTaxTotal(formatPrice(taxTotal.getAmount(), locale));
        }

        if(showGST && Objects.nonNull(order.getGstAmount())){
            orderTotals.setTaxTotal(formatPrice(BigDecimal.valueOf(order.getGstAmount()), locale));
        }
    }

    private void setOrderTotalsShippingTotal(final Order order, final OrderTotals orderTotals, final Locale locale) {
        final Money shippingTotal = order.getOrderTotalShippingInMoney();
        if(shippingTotal != null) {
            orderTotals.setShippingTotal(formatPrice(shippingTotal.getAmount(), locale));
        }
    }

    private Money setOrderTotalsOrderTotal(final Order order, final Program program,
        final boolean passBlueTierDiscountToUser, final OrderTotals orderTotals, final Locale locale) {
        Money orderTotal = calculateOrderTotal(order, program, passBlueTierDiscountToUser);

        if(orderTotal != null && !StringUtils.equalsIgnoreCase(CommonConstants.AWP_NONE_SUPPLIER,order.getFullfillmentPartnerName())) {
            orderTotals.setOrderTotal(formatPrice(orderTotal.getAmount(), locale));
        }
        return orderTotal;
    }

    private Money calculateOrderTotal(final Order order, final Program program,
        final boolean passBlueTierDiscountToUser) {
        Money orderTotal = null;

        if ((CommonConstants.EXPERIENCE_DRP.equalsIgnoreCase
            ((String) program.getConfig().get(CommonConstants.SHOP_EXPERIENCE)))) {
            orderTotal = order.getDRPOrderTotalInMoney();
        } else {
            if(BundledPricingOption.BUNDLED.equals(program.getBundledPricingOption())){
                if(passBlueTierDiscountToUser){
                    orderTotal = order.getOrderTotalInMoney().plus(order.getSupplierTaxPriceInMoney());
                }else{
                    orderTotal = order.getVarOrderPriceTotalInMoney();
                }

            }else{
                orderTotal = order.getOrderTotalInMoney();
            }
        }
        return orderTotal;
    }

    private void calculateOrderTotalsItemsTotal(final Order order, final Program program, final OrderTotals orderTotals,
        final Locale locale) {
        // set order informations in dollars.
        final Money orderSubTotalInMoney = order.getOrderSubTotalInMoney();
        if(orderSubTotalInMoney != null && !StringUtils.equalsIgnoreCase(CommonConstants.AWP_NONE_SUPPLIER,order.getFullfillmentPartnerName())) {
            if(BundledPricingOption.BUNDLED.equals(program.getBundledPricingOption())) {
                orderTotals.setItemsTotal(formatPrice(order.getOrderTotalInMoney().plus(order.getSupplierTaxPriceInMoney()).getAmount(),locale));
            }else{
                orderTotals.setItemsTotal(formatPrice(orderSubTotalInMoney.getAmount(), locale ));
            }
        }
    }

    private String formatPoints(final int points) {
        final NumberFormat numberFormat = new DecimalFormat(CommonConstants.POINTS_PATTERN);
        return numberFormat.format(Long.valueOf(points));
    }

    private String formatPrice(final BigDecimal price, final Locale locale) {
        String formattedValue;
        final NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formattedValue = formatter.format(price);

        if(CommonConstants.LOCALE_EN_ZA.equals(locale.toString())){
            formattedValue = AppleUtil.toConvertZACurrencyFormat(formattedValue);
        }

        if(CommonConstants.LOCALE_RU_RU.equals(locale.toString())){
            formattedValue = AppleUtil.toConvertRUCurrencyFormat(formattedValue);
        }

        if(CommonConstants.LOCALE_FR_CH.equals(locale.toString())){
            formattedValue = AppleUtil.toConvertCHCurrencyFormat(formattedValue);
        }
        return formattedValue;
    }

    private String formatCashRewardPricing(final Integer cashReward, final Locale locale, final String pointName) {
        final BigDecimal cashRewardInDollar = BigDecimal.valueOf(cashReward)
                .divide(BigDecimal.valueOf(CENTS_TO_DOLLARS_DIVISOR), 2, RoundingMode.HALF_UP);
        return formatPrice(cashRewardInDollar, locale) + ' ' + pointName;
    }

    public IAppleEmailRequest populateOrderEmailRequest(final String[] userEmail,
        final Optional<Notification> notificationOpt, final EmailData emailData, final Boolean isBccNeeded,
        final String returnPath) {
        return new IAppleEmailRequest() {
            public EmailData getEmailData() { return emailData; }
            public Boolean isDraftMode() { return Boolean.valueOf(applicationProperties.getProperty(CommonConstants.EMAIL_USE_DRAFT_TEMPLATE)); }
            public INotification getNotification() {
                return populateNotification(notificationOpt.orElse(new Notification()));
            }
            public String[] getRecipients() { return userEmail; }
            public String getReturnPath() { return returnPath; }

            public String[] getBccRecipients() { return isBccNeeded ? new String[]{applicationProperties.getProperty(CommonConstants.SPARK_BCC_MAILBOX)} : null; }

            public String getSparkAuthorizationCode() {
                return applicationProperties.getProperty(CommonConstants.SPARK_POST_AUTHORIZATION_CODE_KEY);
            }

            public String getHttpConnectionTimeout() {
                return applicationProperties.getProperty(CommonConstants.HTTP_CONNECTION_TIMEOUT);
            }
        };

    }

    private INotification populateNotification(final Notification notificationOpt) {
        return new INotification() {
            public Integer getId() { return notificationOpt.getId(); }
            public String getVarId() { return notificationOpt.getVarId(); }
            public String getProgramId() { return notificationOpt.getProgramId(); }
            public String getType() { return notificationOpt.getType(); }
            public String getName() { return notificationOpt.getName(); }
            public String getTemplateId() {
                //S-19136 - fetch sparkPost TemplateId dynamically based on environment
                String templateId = notificationOpt.getTemplateId();
                    final String env = applicationProperties.getProperty(SPARK_ENVIRONMENT);
                    if (SPARK_ENVIRONMENT_LOWER.equalsIgnoreCase(env) || SPARK_ENVIRONMENT_UAT.equalsIgnoreCase(env)) {
                        templateId = String.join(HYPHEN, templateId, env);
                    }
                LOGGER.info("Email request - Template id : {} ", templateId);
                return templateId;
            }
            public String getLocale() { return notificationOpt.getLocale(); }
            public String getSubject() { return notificationOpt.getSubject(); }
            public Boolean getIsActive() { return notificationOpt.getIsActive(); }
            public String getLastupdateUser() { return notificationOpt.getLastupdateUser(); }
            public Timestamp getLastupdateTime() { return notificationOpt.getLastupdateTime();
            }
        };
    }

    public static BigDecimal getPayPerPeriodPrice(Program program, BigDecimal price, int payPeriods) {
        BigDecimal payPerPeriod;
        final boolean showExactPayPerPeriod =
            (Boolean) program.getConfig().getOrDefault(CommonConstants.SHOW_EXACT_PAY_PER_PERIOD, Boolean.FALSE);
        if (showExactPayPerPeriod) {
            // This will show exact payPerPeriod including decimal places
            payPerPeriod = price.divide(new BigDecimal(payPeriods), CommonConstants.TWO, RoundingMode.CEILING);
        } else {
            // This will round payPerPeriod to dollars
            payPerPeriod = price.divide(new BigDecimal(payPeriods), CommonConstants.ZERO, RoundingMode.CEILING);
        }
        return payPerPeriod;
    }

    public String buildTermsURL(final String filePath,final Locale locale){
        StringBuilder builder=new StringBuilder();
        builder.append(applicationProperties.getProperty(CommonConstants.IMAGE_SERVER_VIP_URL_KEY));
        builder.append(applicationProperties.getProperty(filePath));
        builder.append("-");
        builder.append(locale.toString());
        builder.append(DOCUMENT_EXTENSION);

        return builder.toString();
    }
}