package com.b2s.rewards.apple.util;

import com.b2s.apple.model.finance.CreditCardDetails;
import com.b2s.apple.model.finance.FinanceOptionsPaymentProvider;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.db.model.OrderLineAttribute;
import com.b2s.rewards.apple.integration.model.PaymentOptions;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.rewards.security.util.XSSRequestWrapper;
import com.b2s.service.product.common.domain.AvailabilityInformation;
import com.b2s.service.product.common.domain.AvailabilityStatus;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.util.OAuthRequestParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.utils.URIBuilder;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by rperumal on 4/28/2015.
 */
public final class AppleUtil {

    private static final int CENTS_TO_DOLLARS = 100;
    private static final int TWO = 2;
    private static Logger log = LoggerFactory.getLogger(AppleUtil.class);

    private final static List<CommonConstants.NotificationName> AMP_NOTIFICATIONS_LIST;

    static {
        AMP_NOTIFICATIONS_LIST =
                List.of(
                        CommonConstants.NotificationName.AMP_TV_PLUS,
                        CommonConstants.NotificationName.AMP_MUSIC,
                        CommonConstants.NotificationName.AMP_NEWS_PLUS
                );
    }

    private AppleUtil() {
        // This is a utility class, it cannot be instantiated
    }

    public static String getHostName(HttpServletRequest request) {
        String hostName = null;
        URI uri = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUri();
        if (uri != null && org.apache.commons.lang.StringUtils.isNotBlank(uri.toString()) &&
            uri.toString().indexOf(uri.getPath()) != -1) {
            hostName = uri.toString().substring(0, uri.toString().indexOf(uri.getPath()));
        }
        return hostName;
    }

    public static String buildReturnHostPrefix(final HttpServletRequest request) {
        final String requestScheme = request.getScheme();
        final User user = (User) request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);
        if (Objects.nonNull(user)) {
            String propertyKey = String.join(".", user.getVarId().toLowerCase(),
                CommonConstants.PAYMENT_REDIRECT_HOME_URL);
            final String paymentRedirectHomeURL = AppContext.getAppProperty(propertyKey);
            if (StringUtils.isNotEmpty(paymentRedirectHomeURL)) {
                return paymentRedirectHomeURL;
            }
        }
        final int serverPort = request.getServerPort();
        final StringBuilder hostPrefixBuilder =
            new StringBuilder(requestScheme).append("://").append(request.getServerName());
        //noinspection OverlyComplexBooleanExpression
        if (("http".equalsIgnoreCase(requestScheme) && serverPort != 80) ||
            ("https".equalsIgnoreCase(requestScheme) && serverPort != 443)) {
            hostPrefixBuilder.append(':').append(serverPort);
        }
        return hostPrefixBuilder.toString();
    }

    /**
     * Get remaining string after the matching pattern
     *
     * @param pattern  : pattern string to match
     * @param fromText
     * @return
     */
    public static String getPatternValue(String pattern, String fromText) {
        String textValue = null;
        Matcher m = Pattern.compile(pattern).matcher(fromText);
        if (m.find()) {
            return m.group(1);
        }
        return textValue;
    }

    public static String replaceNull(String input) {
        return StringUtils.defaultString(input);
    }

    public static String replaceNullString(final String input) {
        if (StringUtils.isEmpty(input) || input.equalsIgnoreCase("null")) {
            return "";
        }
        return input;
    }

    public static <T> Collector<T, Object, T> singletonCollector() {
        return Collectors.collectingAndThen(
            Collectors.toList(),
            (List<T> list) -> {
                if (list.isEmpty()) {
                    return null;
                }
                if (list.size() > 1) {
                    throw new IllegalStateException();
                }
                return list.get(0);
            }
        );
    }

    // Need return type as string
    public static String dateConverter(final Date actualDate, final String timeZone, final String timeFormat) {

        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);

        sdf.setTimeZone(TimeZone.getTimeZone(timeZone));

        return sdf.format(actualDate);
    }

    public static boolean isPayrollDeductionCompleted(final Order order) {

        return order.getOrderLines().stream().anyMatch(o -> o.getSupplierId().equalsIgnoreCase
            (CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S) &&
            o.getOrderStatus() == CommonConstants.ORDER_STATUS_COMPLETED);

    }

    public static boolean isOrderTypePayrollDeduction(final Order order) {

        return order.getOrderLines().stream().anyMatch(o -> o.getSupplierId().equalsIgnoreCase
            (CommonConstants.SUPPLIER_TYPE_PAYROLLDEDUCTION_S));
    }

    /**
     * convert from cents to Dollars and return the formated value in string eg.  25000 cents will be converted as
     * 250.00 and returned as String data type with two digit precision.
     */
    public static String getAmountInDollarsString(Integer amount) {
        if (amount != null) {
            return BigDecimal.valueOf(amount).divide(new BigDecimal(CENTS_TO_DOLLARS)).setScale(TWO).toString();
        } else {
            return BigDecimal.ZERO.setScale(TWO).toString();
        }
    }

    public static String convertObjectToString(final Object value) {
        String result = null;
        if (value != null) {
            result = String.valueOf(value);
        }
        return result;
    }

    public static void checkMandatory(final String value, final String field) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Field ".concat(field).concat(" cannot be empty"));
        }
    }

    public static void checkNumeric(final String value, final String field) {
        if (!NumberUtils.isParsable(value)) {
            throw new IllegalArgumentException("Field ".concat(field).concat(" should be Numeric"));
        }
    }

    public static void checkMandatoryNumeric(final String value, final String field) {
        checkMandatory(value, field);
        checkNumeric(value, field);
    }

    public static void checkOptionalNumeric(final String value, final String field) {
        if (StringUtils.isNotEmpty(value)) {
            checkNumeric(value, field);
        }
    }

    //For now we encode only ' single quotation and % percentage
    public static String encodeSpecialChar(final String value) {
        String result = value;
        if (StringUtils.isNotEmpty(value)) {
            result = value.replace("%", "%25");
            result = result.replace("'", "%27");
        }
        return result;
    }

    //For now we decode only ' single quotation
    public static String decodeSpecialChar(final String value) {
        String result = value;
        if (StringUtils.isNotEmpty(value)) {
            try {
                result = URLDecoder.decode(value, "UTF-8");
            } catch (final UnsupportedEncodingException e) {
                log.warn("UnsupportedEncodingException while decoding value - {}", value, e);
            } catch (final RuntimeException e) {
                log.error("RuntimeException while decoding value - {}", value, e);
                throw e;
            }
        }
        return result;
    }

    public static String replaceSpecialCharWithEmpty(String value) {
        if (StringUtils.isNotEmpty(value)) {
            value = XSSRequestWrapper.getCanonicalizedString(value);
            final Pattern ptn = Pattern.compile(CommonConstants.REGEX_SPECIAL_CHAR_ADDRESS);
            final Matcher mtch = ptn.matcher(value);
            value = mtch.replaceAll("");
        }
        return value;
    }

    public static String replaceSpecialCharWithEmpty(final String value, final String locale) {
        final String result;

        if (Objects.nonNull(CommonConstants.LOCALE_ALLOWED_CHAR_ADDRESS_REGEX.get(locale)) &&
            StringUtils.isNotEmpty(value)) {
            result = value.replaceAll(CommonConstants.LOCALE_ALLOWED_CHAR_ADDRESS_REGEX.get(locale), "");
        } else {
            result = replaceSpecialCharWithEmpty(value);
        }
        return result;
    }


    public static List<String> getCitiCountryList(final String countryList) {
        if (StringUtils.isNotEmpty(countryList)) {
            return Arrays.asList(countryList.split("\\s*,\\s*"));
        }
        return Collections.emptyList();
    }

    public static String getCategoryIdByLocale(final String catalogPrefix, final Locale locale) {

        return catalogPrefix + "-" + locale.getCountry().toLowerCase() + "-" + locale.getLanguage().toLowerCase();
    }

    //Currency format for 'en_ZA' locale is not converting properly in joda.money.CurrencyUnit class, so that
    //toConvertZACurrencyFormat method is used to get the proper currency format.
    public static String toConvertZACurrencyFormat(String currencyFormat) {
        return currencyFormat.replaceAll("\\s+", "");
    }

    public static String toConvertRUCurrencyFormat(String currencyFormat) {
        return currencyFormat.replace(",", ".");
    }

    public static String toConvertCHCurrencyFormat(String currencyFormat) {
        return currencyFormat.replaceAll("SFr.", "CHF");
    }

    public static boolean isAWPAvailableProduct(final AvailabilityInformation availabilityInformation) {
        if (availabilityInformation.isAvailable()) {
            return AvailabilityStatus.BACKORDERED != availabilityInformation.getAvailabilityStatus();
        }
        return availabilityInformation.isAvailable();
    }

    public static boolean isAWPAvailableProduct(final Program program,
        final AvailabilityInformation availabilityInformation) {
        if (availabilityInformation.isAvailable() &&
            CommonConstants.EXPERIENCE_DRP
                .equalsIgnoreCase((String) program.getConfig().get(CommonConstants.SHOP_EXPERIENCE))) {
            return isAWPAvailableProduct(availabilityInformation);
        }

        return availabilityInformation.isAvailable();
    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error while parsing JSON :", e);
            return "";
        }
    }

    public static String getURLParameterFromRequest(final HttpServletRequest request) {
        final URIBuilder uriBuilder = new URIBuilder();
        final Enumeration<String> enumParamName = request.getParameterNames();
        while (enumParamName.hasMoreElements()) {
            final String key = enumParamName.nextElement();
            uriBuilder.addParameter(key, request.getParameter(key));
        }

        return uriBuilder.toString();
    }

    /**
     * This method is used to check the flag for setting new XsrfToken
     *
     * @param program
     * @return default true
     */
    public static boolean checkXsrfTokenReUse(final Program program) {
        String strTokenFlag = String.valueOf(program.getConfig().get(CommonConstants.SET_NEW_XSRF_TOKEN));
        return StringUtils.isNotBlank(strTokenFlag) ? BooleanUtils.toBoolean(strTokenFlag) : Boolean.TRUE;

    }

    public static void encodeAddressFields(final Address address) {
        address.setAddress1(AppleUtil.encodeSpecialChar(address.getAddress1()));
        if (org.apache.commons.lang.StringUtils.isNotEmpty(address.getAddress2())) {
            address.setAddress2(AppleUtil.encodeSpecialChar(address.getAddress2()));
        }
        if (org.apache.commons.lang.StringUtils.isNotEmpty(address.getAddress3())) {
            address.setAddress3(AppleUtil.encodeSpecialChar(address.getAddress3()));
        }
        if (org.apache.commons.lang.StringUtils.isNotEmpty(address.getBusinessName())) {
            address.setBusinessName(AppleUtil.encodeSpecialChar(address.getBusinessName()));
        }
        address.setCity(AppleUtil.encodeSpecialChar(address.getCity()));
    }

    public static boolean isAccessories(final String slug) {
        return StringUtils.contains(slug, CommonConstants.ACCESSORIES)
            || StringUtils.startsWith(slug, CommonConstants.ACC_HYPHEN)
            || StringUtils.startsWith(slug, CommonConstants.ALL_HYPHEN);
    }

    public static String skuSearch(final String keyword) {
        String searchKeyword = keyword;
        if (Optional.ofNullable(searchKeyword).isEmpty() || searchKeyword.isEmpty()) {
            return null;
        }
        final Matcher matcher = CommonConstants.SKU_PATTERN.matcher(keyword);
        if (matcher.find()) {
            final List<String> keywordList = new ArrayList<>();
            searchKeyword = searchKeyword.replaceAll(CommonConstants.REMOVE_SPECIAL_CHARACTER_PATTERN.pattern(), " ");
            searchKeyword = searchKeyword.replaceAll(CommonConstants.SPACE_PATTERN.pattern(), " ");
            final String[] searchWords = searchKeyword.split(" ");
            for (final String word : searchWords) {
                final Matcher skuMatcher = Pattern.compile("(^(" + CommonConstants.SKU_PATTERN + ")$)").matcher(word
                    .trim());
                if (skuMatcher.find()) {
                    keywordList.add(org.apache.commons.lang.StringUtils.replace(word, "/", ""));
                } else {
                    keywordList.add(word);
                }
            }
            return org.apache.commons.lang.StringUtils.join(keywordList, " ");
        }
        return searchKeyword;
    }

    public static String getOAuthRedirectURI(final HttpServletRequest httpServletRequest, final String redirectUri) {
            XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);

        final String appHostName = getHostName(request);
        final String varId = request.getParameter(CommonConstants.VAR_ID_CAMEL_CASE);
        final String okta = request.getParameter(OAuthRequestParam.OKTA.getValue());
        final String programId = request.getParameter(OAuthRequestParam.PROGRAM_ID.getValue());
        final URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.addParameter(CommonConstants.VAR_ID_CAMEL_CASE, varId);
        if (StringUtils.equalsIgnoreCase(okta, CommonConstants.YES_VALUE)) {
            uriBuilder.addParameter(OAuthRequestParam.OKTA.getValue(), CommonConstants.YES_VALUE);
        }
        if (StringUtils.isNotBlank(programId)) {
            uriBuilder.addParameter(OAuthRequestParam.PROGRAM_ID.getValue(), programId);
        }
        return appHostName + redirectUri + uriBuilder.toString();
    }

    public static String getFinanceOptionsServiceIdentifier(final Program program)
        throws ServiceException {
        String paymentProvider = StringUtils.EMPTY;
        if (Objects.nonNull(program) && Objects.nonNull(program.getRedemptionOptions()) &&
            program.getRedemptionOptions().containsKey(PaymentOptions.FINANCE.getPaymentOption())) {
           final VarProgramRedemptionOption redemptionOption = program.getRedemptionOptions().get(PaymentOptions.FINANCE.getPaymentOption()).stream()
                    .filter(VarProgramRedemptionOption::getActive).findFirst().orElse(null);
            if (Objects.nonNull(redemptionOption )) {
                paymentProvider = redemptionOption.getPaymentProvider();
            }
        }
        //Get the Finance Options Service Identifier from the Payment Provider
        return FinanceOptionsPaymentProvider.lookup(paymentProvider);
    }

    /**
     * populating Billing address
     *
     * @param user
     * @param cardDetails
     */
    public static void populateBillTo(final User user, final CreditCardDetails cardDetails) {
        if (user.getBillTo() == null) {
            final BillTo bill = new BillTo();
            bill.setFirstName(cardDetails.getFirstName());
            bill.setLastName(cardDetails.getLastName());
            final String street = Stream.of(
                cardDetails.getAddr1(),
                cardDetails.getAddr2(),
                cardDetails.getAddr3()
            ).filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(" ")
                );
            if (StringUtils.isNotEmpty(street)) {
                bill.setAddressLine(street);
            }
            bill.setCity(cardDetails.getCity());
            bill.setState(cardDetails.getState());
            bill.setZip(cardDetails.getZip());
            //setting Billing Country
            setDisplayBillingCountry(user, bill, cardDetails);
            user.setBillTo(bill);
        }
    }

    /**
     * To set Billing Country
     *
     * @param user
     * @param bill
     * @param cardDetails
     */
    private static void setDisplayBillingCountry(final User user, final BillTo bill,
        final CreditCardDetails cardDetails) {
        //get Language from user
        final String languageCode = user.getLocale().getLanguage();

        final Locale locale = new Locale(languageCode, cardDetails.getCountry());
        final String countryInLocale = locale.getDisplayCountry(user.getLocale());
        bill.setCountry(countryInLocale);
    }

    /**
     * Validating card details
     *
     * @param cardDetails
     */
    public static void validateInit(CreditCardDetails cardDetails, final HttpServletRequest request)
        throws ServiceException {
        //program session
        final Program program = (Program) request.getSession().getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
        if (Objects.nonNull(program)) {
            filter(cardDetails.getCcNum(), program.getCcFilters());
        }
    }

    /**
     * check if the given Credit Card number matches(starts with) at least one of the BINS from the configured list
     *
     * @param ccNumber
     * @param ccFilters
     */
    private static boolean filter(final String ccNumber, final List<Program.CCBin> ccFilters)
        throws ServiceException {
        final String processingStr = ccNumber.trim();
        boolean valid = false;
        // check if the given Credit Card number matches(starts with) at least one of the BINS from the configured list
        if (CollectionUtils.isNotEmpty(ccFilters)) {
            for (final Program.CCBin ccBIN : ccFilters) {
                if (ccBIN == null || (ccBIN.getFilter() != null && ccBIN.getFilter().isEmpty())) {
                    continue;
                }
                if (processingStr.startsWith(ccBIN.getFilter().trim())) {
                    valid = true;
                    break;
                }
            }
        } else {
            valid = true;
        }

        if (!valid) {
            throw new ServiceException(ServiceExceptionEnums.BIN_FILTER_EXCEPTION);
        }
        return valid;
    }


    /**
     * @param program
     * @param key
     * @param defaultValue
     * @return String value for the key from VPC.
     */
    public static String getValueFromProgramConfig(final Program program, final String key, final String defaultValue) {
        String value = null;
        if (Objects.nonNull(program) && MapUtils.isNotEmpty(program.getConfig()) && program.getConfig().containsKey(key)) {
            value = String.valueOf(program.getConfig().get(key));
        }
        if (StringUtils.isEmpty(value) && StringUtils.isNotEmpty(defaultValue)) {
            return defaultValue;
        }

        return value;
    }

    public static String getValueFromRequest(final String key, final HttpServletRequest request){

        String value=null;

       value=request.getParameter(key);
       if(StringUtils.isEmpty(value)){
           value=(String)request.getAttribute(key);
       }
        return value;
    }

    /**
     * @param product
     * @return String categorySlug for the product
     */
    public static String getCategorySlug(final Product product) {
        return CollectionUtils.isNotEmpty(product.getCategories()) ? product.getCategories().get(0).getSlug() :
            StringUtils.EMPTY;
    }

    /*
     * Genereate random id for payment server redirect use
     */
    public static String getRandomId() {
        SecureRandom random = CommonConstants.SECURE_RANDOM;
        final int n = 50;
        final char[] pw = new char[n];
        int c = 'A';
        int r1 = 0;
        try {
            for (int i = 0; i < n; i++) {
                r1 = random.nextInt(3);
                switch (r1) {
                    case 0:
                        c = '0' + random.nextInt(10);
                        break;
                    case 1:
                        c = 'a' + random.nextInt(26);
                        break;
                    case 2:
                        c = 'A' + random.nextInt(26);
                        break;
                    default:
                        break;
                }
                pw[i] = (char) c;
            }
        } catch (Exception nsae) {
            log.error("Error get random ID: ", nsae);
        }
        return new String(pw);
    }

    public static String getDeeplinkForAngular(String input)
    {
        if(StringUtils.isNotEmpty(input)){
            final Pattern ptn = Pattern.compile("merchandise.*#");
            final Matcher match = ptn.matcher(input);
            return match.replaceAll("ui");
        }
        return input;
    }

    public static String getClientIpAddress(HttpServletRequest httpServletRequest)
    {
        XSSRequestWrapper request = new XSSRequestWrapper(httpServletRequest);
        /**
         Get the correct client IP address from the request.
         Parse through the x-forwarded-for, in case the request came through a proxy,
         then fall back on the RemoteAddress in the request.Get client's IP address**/

        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        /*If it is comma seperated string return the first value else just the IpAddress*/
        return ipAddress.contains(",")?ipAddress.split(",")[0]:ipAddress;

    }
    public static boolean collectionSizeIsGreaterThan(final Collection collection, final int size) {
        return CollectionUtils.isNotEmpty(collection) && CollectionUtils.size(collection) > size;
    }

    public static boolean stringContainsWord(String input, String[] words) {
        return Arrays.stream(words).anyMatch(input::contains);
    }

    public static boolean isStringsEqualIgnoreCase(final String str1, final String str2) {
        return StringUtils.compareIgnoreCase(str1, str2) == 0;
    }

    public static String reflectionToStringJsonStyle(final Object message) {
        return ToStringBuilder.reflectionToString(message, ToStringStyle.JSON_STYLE);
    }

    public static String gsonToJsonString(final Object obj){
        return new Gson().toJson(obj);
    }

    public static boolean getProgramConfigValueAsBoolean(final Program program, final String value) {
        boolean config = false;
        if(Objects.nonNull(program) && MapUtils.isNotEmpty(program.getConfig())) {
            config = BooleanUtils.toBoolean((Boolean)program.getConfig().get(value));
        }

        return config;
    }

    public static String getProgramConfigValueAsString(final Program program, final String value) {
        String config = null;
        if(Objects.nonNull(program) && MapUtils.isNotEmpty(program.getConfig())) {
            config = (String)program.getConfig().get(value);
        }

        return config;
    }

    public static List<String> getProgramConfigValueAsList(final Program program, final String value) {
        List<String> result = new ArrayList<>();
        if(Objects.nonNull(program) && MapUtils.isNotEmpty(program.getConfig())) {
            final String config = (String)program.getConfig().get(value);
            if(StringUtils.isNotBlank(config)) {
                result = Arrays.asList(config.split(","));
            }
        }

        return result;
    }

    public static boolean isProgramConfigValueMatching(final Program program, final String name, final String value) {
        String configValue = null;
        if (Objects.nonNull(program) && MapUtils.isNotEmpty(program.getConfig())) {
            configValue = (String) program.getConfig().get(name);
        }
        return StringUtils.equalsIgnoreCase(configValue, value);
    }

    /**
     * Checks if a string is a valid ISO 4217 currency code
     *
     * @param currencyCode
     * @return
     */
    public static boolean isValidCurrencyCode(final String currencyCode) {
        boolean isValid = false;
        try {
            if (StringUtils.isNotBlank(currencyCode)) {
                final Currency currency = Currency.getInstance(currencyCode);
                isValid = Objects.nonNull(currency);
            }
        } catch (IllegalArgumentException e) {
            log.error("The selected currency {} is not a valid ISO 4217 currency code", currencyCode, e);
            isValid = false;
        }
        return isValid;
    }

    /**
     * Remove the currency sign (first 4 characters :alpha alpha alpha space) from an amount
     * and return a new string with the amount (Number).
     * Returns null if :
     *      the input string is blank
     *      the currency is not a valid ISO 4217 currency
     *      the currency code is not followed by a space
     *  <p>
     * Examples:
     * <blockquote><pre>
     * "USD 123" returns "123"
     * "EUR 0567" returns "0567"
     * "ABC 890" returns null
     * </pre></blockquote>
     *
     * @param amount
     * @return
     */
    public static String getAmountWithoutCurrencyCode(final String amount) {
        String result = null;
        try {
            if (isValidCurrencyCode(StringUtils.left(amount, CommonConstants.CURRENCY_CODE_LENGTH)) &&
                StringUtils.indexOf(amount, StringUtils.SPACE) == CommonConstants.CURRENCY_CODE_LENGTH) {
                result = amount.substring(CommonConstants.CURRENCY_CODE_LENGTH + 1);
            }
        } catch(StringIndexOutOfBoundsException e) {
            log.error("AppleUtil - removeCurrencyFromAmount Substring error", e);
        }
        return result;
    }

    public static void setAllowCORSHeaders(final HttpServletRequest request, final HttpServletResponse response){
        final Map<String, Object> externalUrls = (Map) request.getSession().getAttribute(ExternalUrlConstants.EXTERNAL_URLS);
        if (Objects.nonNull(externalUrls)) {
            final Map<String, Object> keystoneUrls = (Map<String, Object>) externalUrls.get(ExternalUrlConstants.KEYSTONE_URLS);
            final String keystoneBaseUrl = (String) keystoneUrls.get(ExternalUrlConstants.KEYSTONE_BASE_URL);

            response.addHeader("Access-Control-Allow-Origin", XSSRequestWrapper.cleanXSS(keystoneBaseUrl));
            response.addHeader("Access-Control-Allow-Credentials", "true");
        }
    }

    public static boolean isNotificationAmp(final CommonConstants.NotificationName notificationName) {
        return AMP_NOTIFICATIONS_LIST.contains(notificationName);
    }

    public static int getMinCount(final Integer configMaxProductCount, final String propertyMaxSize) {
        final int appMaxCount = Integer.parseInt(propertyMaxSize);

        //Set maxCount based on Application property and Carousel configuration
        //if configMaxCount is null, use appMaxCount, else use smallest value between App and Config
        return Objects.isNull(configMaxProductCount) ? appMaxCount :
            configMaxProductCount < appMaxCount ? configMaxProductCount : appMaxCount;
    }

    public static String sanitizeInputString(final String userInput) {
        return StringEscapeUtils.escapeHtml(
            StringEscapeUtils.escapeXml(
                StringEscapeUtils.escapeJavaScript(userInput)
            )
        );
    }

    public static String getOrderLineAttributeValue(final OrderLine orderLine, final String name) {
        String olaValue = null;
        if (StringUtils.isNotBlank(name) &&
                Objects.nonNull(orderLine) &&
                CollectionUtils.isNotEmpty(orderLine.getOrderAttributes())) {
            olaValue = orderLine.getOrderAttributes().stream()
                    .filter(orderLineAttribute -> name.equals(orderLineAttribute.getName()))
                    .findFirst()
                    .map(OrderLineAttribute::getValue)
                    .orElseGet(null);
        }
        return olaValue;
    }

    /**
     * if splitPay contains "percentage" ==> maxPointsPercentage = paymentMaxLimit
     * if splitPay contains "dollar" ==> maxCashAmount = paymentMaxLimit
     *      it's possible to send both "maxPointsPercentage" and "maxCashAmount" in the same request
     *
     * @param program for Var Program Redemption option
     * @return Split Tender with maxCashAmount/maxPointsPercentage for PS Request
     */
    public static SplitTender getSplitTenderConfig(final Program program) {
        final SplitTender.Builder splitTenderBuilder = SplitTender.builder();
        if (MapUtils.isNotEmpty(program.getRedemptionOptions())) {
            program.getRedemptionOptions()
                    .forEach((optionType, varProgramRedemptionOptions) -> {
                        if (optionType.equalsIgnoreCase(PaymentOptions.SPLITPAY.getPaymentOption())) {
                            varProgramRedemptionOptions.forEach(varProgramRedemptionOption -> {
                                if (varProgramRedemptionOption.getLimitType().equalsIgnoreCase(CommonConstants.DOLLAR)) {
                                    splitTenderBuilder.withMaxCashAmount(Money
                                            .of(program.getTargetCurrency(), varProgramRedemptionOption.getPaymentMaxLimit(), RoundingMode.UNNECESSARY));
                                }
                                if (CommonConstants.PERCENTAGE.equalsIgnoreCase(varProgramRedemptionOption.getLimitType())) {
                                    splitTenderBuilder.withMaxPointsPercentage(BigDecimal.valueOf(varProgramRedemptionOption.getPaymentMaxLimit())
                                            .divide(BigDecimal.valueOf(CommonConstants.CENTS_TO_DOLLARS_DIVISOR), 2, RoundingMode.HALF_UP));
                                }
                            });
                        }
                    });
        }
        return splitTenderBuilder.build();
    }

    public static Map<String, String> parseStringToMap(final String input, final String splitter,
        final String KeyValueSeparator) {
        return Splitter.on(splitter)
            .withKeyValueSeparator(KeyValueSeparator)
            .split(input);
    }
}