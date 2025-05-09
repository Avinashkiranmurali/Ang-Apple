package com.b2s.shop.common.order.var;

import com.b2s.common.services.model.Address;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.b2s.rewards.apple.util.AppleUtil.checkMandatory;
import static com.b2s.rewards.apple.util.ChaseUtil.getAddressList;
import static com.b2s.rewards.apple.util.ChaseUtil.zipValidation;

/**
 * @author marumugam 2018-05-08
 */
@Component("varOrderManagerChase")
public class VAROrderManagerChase extends GenericVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerChase.class);
    public static final String CITY = "_CITY";
    public static final String STATE = "_STATE";
    public static final String POSTAL = "_POSTAL";
    public static final String COUNTRY = "_COUNTRY";

    private static final String POINTS = "POINTS";

    private enum SAMLAttributes {
        USERID("userId"),
        VARID("varId"),
        PROGRAMID("programId"),
        LOCALE("locale"),
        NAVBACKURL("navBackURL"),
        KEEPALIVEURL("keepAliveUrl"),
        BROWSEONLY("browseOnly"),
        SESSIONSTATE("sessionState"),
        FIRSTNAME("firstName"),
        LASTNAME("lastName"),
        ADDRESS1_ADDRESSLINE1("address1.addressLine1"),
        ADDRESS1_ADDRESSLINE2("address1.addressLine2"),
        ADDRESS1_CITY("address1.city"),
        ADDRESS1_STATE("address1.state"),
        ADDRESS1_POSTAL("address1.postal"),
        ADDRESS1_COUNTRY("address1.country"),
        ADDRESS2_ADDRESSLINE1("address2.addressLine1"),
        ADDRESS2_ADDRESSLINE2("address2.addressLine2"),
        ADDRESS2_CITY("address2.city"),
        ADDRESS2_STATE("address2.state"),
        ADDRESS2_POSTAL("address2.postal"),
        ADDRESS2_COUNTRY("address2.country"),
        TELEPHONE("telephone"),
        EMAIL("email"),
        POINTBALANCE("pointBalance"),
        ORDER_HISTORY_URL("orderHistoryUrl"),
        ANALYTICS_WINDOW("analyticsWindow"),
        ANALYTICS_URL("analyticsUrl");

        private final String value;

        SAMLAttributes(final String value) {
            this.value = value;
        }
    }

    @Override
    public User selectUser(final HttpServletRequest request) throws B2RException {
        final UserChase user ;
        final Function<String, String> fetchRequestAttribute = key -> CommonConstants.getRequestAttribute(request, key);
        CommonConstants.LoginType loginType = null;
        if (request.getParameter(CommonConstants.USER_ID) != null &&
            request.getParameter(CommonConstants.USER_ID).toLowerCase().contains(CommonConstants.ANONYMOUS_USER_ID.toLowerCase())) {
            user = (UserChase) updateUser(request, new UserChase(), CommonConstants.COUNTRY_CODE_US, true);
            loginType = CommonConstants.LoginType.ANONYMOUS;
        } else if (request.getParameter(CommonConstants.REQ_PARAM_FOR_SAML_RESP) == null) {
            user = (UserChase) selectLocalUser(request, new UserChase());
            user.setAddresses(getAddressList(user));
            loginType = CommonConstants.LoginType.FIVEBOX;
        } else {
            Arrays.stream(SAMLAttributes.values())
                .forEach(samlAttributes -> LOGGER.info("Chase SAML attributes ------" + samlAttributes.value + " ### " +
                    "" + request.getAttribute(samlAttributes.value)));
            LOGGER.info("Chase SAML UserPrincipal ----{}", request.getUserPrincipal().getName());

            samlAttributesValidation(fetchRequestAttribute);
            user = getUser(request);

            loginType = CommonConstants.LoginType.SAML;
        }
        user.setLoginType(loginType.getValue());
        initializeLocaleDependents(request, user,CommonConstants.LOCALE_EN_US,null);
        setChaseSsoConfigurationInSession(request);
        final Program program = getProgram(user);
        setExternalHeaderUrl(program, request);
        updateAdditionalAttributesInSession(request, program, user, loginType);
        prepareMultipleUserAddress(user);
        return user;
    }

    private void samlAttributesValidation(final Function<String, String> fetchRequestAttribute) {
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.USERID.value), SAMLAttributes.USERID.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.VARID.value), SAMLAttributes.VARID.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.PROGRAMID.value),
            SAMLAttributes.PROGRAMID.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.LOCALE.value), SAMLAttributes.LOCALE.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.NAVBACKURL.value),
            SAMLAttributes.NAVBACKURL.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.BROWSEONLY.value),
            SAMLAttributes.BROWSEONLY.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.SESSIONSTATE.value),
            SAMLAttributes.SESSIONSTATE.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.FIRSTNAME.value),
            SAMLAttributes.FIRSTNAME.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.LASTNAME.value), SAMLAttributes.LASTNAME.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.ADDRESS1_ADDRESSLINE1.value),
            SAMLAttributes.ADDRESS1_ADDRESSLINE1.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.ADDRESS1_CITY.value), SAMLAttributes.ADDRESS1_CITY.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.ADDRESS1_STATE.value), SAMLAttributes.ADDRESS1_STATE.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.ADDRESS1_POSTAL.value), SAMLAttributes.ADDRESS1_POSTAL.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.ADDRESS1_COUNTRY.value), SAMLAttributes.ADDRESS1_COUNTRY.value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.POINTBALANCE.value),
            SAMLAttributes.POINTBALANCE.value);

    }

    private void samlAttributesAddressValidation(final Function<String, String> fetchRequestAttribute,
        final String addressIndex) {
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.valueOf(addressIndex + CITY).value),
            SAMLAttributes.valueOf(addressIndex + CITY).value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.valueOf(addressIndex + STATE).value),
            SAMLAttributes.valueOf(addressIndex + STATE).value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.valueOf(addressIndex + POSTAL).value),
            SAMLAttributes.valueOf(addressIndex + POSTAL).value);
        checkMandatory(fetchRequestAttribute.apply(SAMLAttributes.valueOf(addressIndex + COUNTRY).value),
            SAMLAttributes.valueOf(addressIndex + COUNTRY).value);
    }

    private List<Address> getAddresses(final Function<String, String> fetchRequestAttribute, final String telephone) {
        final List<Address> addresses;
        final Set<String> addressIndexes =
            Arrays.stream(SAMLAttributes.values()).filter(attributes -> attributes.name().contains("ADDRESS"))
                .map(attributes -> attributes.name().split("_")[0]).collect(Collectors.toSet());
        addresses =
            addressIndexes.stream().map(addressIndex -> getAddress(fetchRequestAttribute, addressIndex, telephone))
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (addresses.isEmpty()) {
            throw new IllegalArgumentException("Address List cannot be empty");
        }
        return addresses;
    }

    private Address getAddress(final Function<String, String> fetchRequestAttribute, final String addressIndex,
        final String telephone) {
        final String addressLine1 =
            fetchRequestAttribute.apply(SAMLAttributes.valueOf(addressIndex + "_ADDRESSLINE1").value);
        Address address = null;
        if (!addressLine1.isBlank()) {
            samlAttributesAddressValidation(fetchRequestAttribute, addressIndex);
            final String addressId = CommonConstants.REGEX_NOT_NUMERIC.matcher(addressIndex).replaceAll("");
            final Address.AddressBuilder addressBuilder = Address.builder()
                .withAddressId(Long.parseLong(addressId))
                .withAddress1(fetchRequestAttribute.apply(SAMLAttributes.valueOf(addressIndex + "_ADDRESSLINE1").value))
                .withCity(fetchRequestAttribute.apply(SAMLAttributes.valueOf(addressIndex + CITY).value))
                .withState(fetchRequestAttribute.apply(SAMLAttributes.valueOf(addressIndex + STATE).value))
                .withCountry(fetchRequestAttribute.apply(SAMLAttributes.valueOf(addressIndex + COUNTRY).value));

            final String[] postalCode = zipValidation(fetchRequestAttribute.apply(SAMLAttributes.valueOf(addressIndex +
                POSTAL).value)).split("-");
            if (postalCode.length == 2) {
                addressBuilder.withZip5(postalCode[0]);
                addressBuilder.withZip4(postalCode[1]);
            } else {
                addressBuilder.withZip5(postalCode[0]);
            }
            final String addressLine2 =
                fetchRequestAttribute.apply(SAMLAttributes.valueOf(addressIndex + "_ADDRESSLINE2").value);
            if (!addressLine2.isBlank()) {
                addressBuilder.withAddress2(addressLine2);
            }
            if (!telephone.isBlank()) {
                addressBuilder.withPhoneNumber(telephone);
            }
            address=addressBuilder.build();
        }
        return address;
    }

    @Override
    protected UserChase getUser(final HttpServletRequest request) {

        final Function<String, String> fetchRequestAttribute = key -> CommonConstants.getRequestAttribute(request, key);
        final UserChase user = new UserChase();

        final String telephone = fetchRequestAttribute.apply(SAMLAttributes.TELEPHONE.value);
        final String email = fetchRequestAttribute.apply(SAMLAttributes.EMAIL.value);
        final String orderHistoryUrl = fetchRequestAttribute.apply(SAMLAttributes.ORDER_HISTORY_URL.value);
        final String analyticsWindow = fetchRequestAttribute.apply(SAMLAttributes.ANALYTICS_WINDOW.value);
        final String analyticsUrl = fetchRequestAttribute.apply(SAMLAttributes.ANALYTICS_URL.value);

        user.setVarId(CommonConstants.VAR_CHASE);
        user.setUserId(fetchRequestAttribute.apply(SAMLAttributes.USERID.value));
        user.setProgramid(fetchRequestAttribute.apply(SAMLAttributes.PROGRAMID.value));
        user.setLocale(LocaleUtils.toLocale(fetchRequestAttribute.apply(SAMLAttributes.LOCALE.value)));
        user.setBrowseOnly(Boolean.valueOf(fetchRequestAttribute.apply(SAMLAttributes.BROWSEONLY.value)));
        user.setSessionState(fetchRequestAttribute.apply(SAMLAttributes.SESSIONSTATE.value));
        user.setFirstName(fetchRequestAttribute.apply(SAMLAttributes.FIRSTNAME.value));
        user.setLastName(fetchRequestAttribute.apply(SAMLAttributes.LASTNAME.value));
        user.setPoints(Integer.valueOf(fetchRequestAttribute.apply(SAMLAttributes.POINTBALANCE.value)));
        user.setAddresses(getAddresses(fetchRequestAttribute, telephone));

        if (telephone != null) {
            user.setPhone(telephone);
        }
        if (email != null) {
            user.setEmail(email);
        }

        final Map<String, String> additionalInfo = retrieveUserAdditionalInfoWithProgramId(user);
        additionalInfo.put(CommonConstants.VIS_CURRENCY, POINTS);//Hardcoding this as Chase only supports POINTS.
        if (StringUtils.isNotEmpty(user.getSessionState())) {
            additionalInfo.put(SAMLAttributes.SESSIONSTATE.value.toString(), user.getSessionState());
        }
        if(StringUtils.isNotBlank(orderHistoryUrl)){
            additionalInfo.put(CommonConstants.ORDER_HISTORY_URL, orderHistoryUrl);
        }

        populateAdobeAnalyticsInformation(request, user, analyticsWindow, analyticsUrl);

        if (request.getUserPrincipal() == null || StringUtils.isBlank(request.getUserPrincipal().getName())) {
            LOGGER.error("User id(request.getUserPrincipal().getName()) is blank.");
            throw new IllegalArgumentException("User id(request.getUserPrincipal().getName()) is blank.");
        }

        return user;
    }

    private void populateAdobeAnalyticsInformation(final HttpServletRequest request, final UserChase user,
        final String analyticsWindow, final String analyticsUrl) {
        if (StringUtils.isNotBlank(analyticsWindow)) {
            user.setAnalyticsWindow(AppleUtil.parseStringToMap(analyticsWindow, CommonConstants.AND,
                CommonConstants.EQUAL));
            if (StringUtils.isNotBlank(analyticsUrl)) {
                user.setAnalyticsUrl(analyticsUrl);
                request.setAttribute(CommonConstants.ANALYTICS_URL, analyticsUrl);
            }
        }
    }

    /**
     * This method is used to get User points. It has logic to avoid user balance is low issue by returning payment max
     * limit of var program payment option. Do not use this method to populate user points in User object.
     *
     * @param user    -current user
     * @param program
     * @return int
     * @throws B2RException
     */
    @Override
    public int getUserPoints(final User user, final Program program)
            throws B2RException {
        if (Objects.nonNull(program) && program.getIsLocal()) {
            return varIntegrationServiceLocalImpl.getLocalUserPoints(user);
        } else {
            final Optional<String> sid=Optional.ofNullable(user.getSid());
            return varIntegrationServiceRemoteImpl.getUserPoints(user.getVarId(), user.getProgramId(), sid.orElse(user.getUserId()), user.getAdditionalInfo());
        }
    }

    @Override
    protected String getVARId() {
        return CommonConstants.VAR_CHASE;
    }

    private void setChaseSsoConfigurationInSession(final HttpServletRequest request) {
        request.getSession().setAttribute(CommonConstants.CHASE_SSO_ROOT_URL, applicationProperties.getProperty(CommonConstants.CHASE_SSO_ROOT_URL_KEY));
        request.getSession().setAttribute(CommonConstants.CHASE_ANALYTICS_ROOT_URL, applicationProperties.getProperty(CommonConstants.CHASE_ANALYTICS_ROOT_URL_KEY));
    }

    private void setExternalHeaderUrl(
            final Program program,
            final HttpServletRequest request) {
        final String headerUrl = applicationProperties.getProperty(program.getVarId().toLowerCase() + CommonConstants.HEADER_URL);
        final Function<String, String> fetchRequestAttribute = key -> CommonConstants.getRequestAttribute(request, key);
        if (StringUtils.isNotBlank(headerUrl) && MapUtils.isNotEmpty(program.getConfig())) {
            program.getConfig().put(CommonConstants.EXTERNAL_HEADER_URL, headerUrl);
        }

        program.getConfig().put(CommonConstants.DYNAMIC_HEADER_FOOTER_LOAD, Boolean.valueOf(fetchRequestAttribute.apply(CommonConstants.DYNAMIC_HEADER_FOOTER_LOAD)));
        if (request.getParameter(CommonConstants.REQ_PARAM_FOR_SAML_RESP) == null) {
            //five box scenario
            program.getConfig().put(CommonConstants.DYNAMIC_HEADER_FOOTER_LOAD, Boolean.valueOf(request.getParameter(CommonConstants.DYNAMIC_HEADER_FOOTER_LOAD)));

        }
    }

}