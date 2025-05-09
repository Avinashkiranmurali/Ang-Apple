package com.b2s.shop.common.order.var;

import com.b2s.apple.entity.DemoUserEntity;
import com.b2s.apple.services.CartService;
import com.b2s.common.util.EncryptionUtil;
import com.b2s.db.model.Order;
import com.b2s.rewards.apple.dao.DemoUserDao;
import com.b2s.rewards.apple.dao.PricingModelConfigurationDao;
import com.b2s.rewards.apple.integration.model.AccountInfo;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.apple.util.PricingUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2s.shop.common.User;
import com.b2s.shop.util.VarProgramConfigHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public abstract class VAROrderManagerVITALITY extends GenericVAROrderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VAROrderManagerVITALITY.class);

    private static final String VAR_ID = "Vitality";
    private static final String ACTIVATION_FEE = "activationFee";
    private static final String FINANCED_AMOUNT = "financedAmount";
    private static final String MAX_MONTHLY_PAYMENT = "maxMonthlyPayment";
    private static final String PROGRAM_LENGTH = "programLength";
    private static final String DELTA = "delta";
    private static final String CODE = "code";
    private static final String PRICING_ATTRIBUTE_LOG = "VIMS Pricing Attribute {} -- {}";

    @Autowired
    private VarProgramConfigHelper varProgramConfigHelper;

    @Autowired
    protected PricingModelConfigurationDao pricingModelConfigurationDao;

    @Autowired
    private CartService cartService;

    @SuppressWarnings("unchecked")
    public VAROrderManagerVITALITY() {
    }

    @Override
    public User selectUser(final HttpServletRequest request) {
        UserVitality user = new UserVitality();
        user.setVarId(getVARId());
        initializeLocaleDependents(request, user);
        Program program = null;
        try {
            try {
                if (StringUtils.isNotBlank(request.getParameter(CODE))) {
                    final AccountInfo accountInfo = varIntegrationServiceRemoteImpl
                            .getAccountInfo(request.getParameter(CODE), user.getVarId(), user.getCountry());
                    if (accountInfo != null && accountInfo.getUserInformation() != null &&
                            MapUtils.isNotEmpty(accountInfo.getUserInformation().getAdditionalInfo())) {
                        final Map<String, String> additionalInfo = accountInfo.getUserInformation().getAdditionalInfo();
                        user.setUserId(additionalInfo.get("memberId"));
                        user.setProgramId(additionalInfo.get("programId"));

                        //Select program information from database
                        program = getProgram(user);

                        setUserPricingInfo(user, program, additionalInfo);
                        user.setAdditionalInfo(additionalInfo);
                    } else {
                        LOGGER.error("Account information is empty from VIS for var id {} having context path {}",
                                getVARId(), VAR_ID);
                        final Map<String, String> additionalInfo = new HashMap<>();
                        additionalInfo.put(CommonConstants.PRICING_ID, "");
                        user.setAdditionalInfo(additionalInfo);
                    }
                } else {
                    user.setUserId(request.getParameter("userid"));
                    user.setPassword(request.getParameter("pword"));
                    user.setProgramId(request.getParameter("programid"));
                    final DemoUserEntity.DemoUserId demoUserId = new DemoUserEntity.DemoUserId();
                    demoUserId.setProgramId(user.getProgramId());
                    demoUserId.setVarId(user.getVarId());
                    demoUserId.setUserId(user.getUserId());
                    final DemoUserEntity demoUserEntity =
                            demoUserDao.findByDemoUserIdAndPassword(demoUserId, EncryptionUtil.encrypt(user.getPassword()));
                    user = (UserVitality) user.select(demoUserEntity);
                    if (Objects.isNull(user)) {
                        return null;
                    }
                    final Map<String, String> additionalInfo = new HashMap<>();
                    additionalInfo.put(CommonConstants.PRICING_ID, request.getParameter(CommonConstants.PRICING_ID));
                    user.setAdditionalInfo(additionalInfo);
                }

                //Select program information from database
                if(Objects.isNull(program)) {
                    program = getProgram(user);
                }
                request.getSession().setAttribute(CommonConstants.PROGRAM_SESSION_OBJECT, program);

                // Set Bag Menu Urls from DB
                addOrUpdateExternalUrls(request, program, user.getLocale().toString(), null);
            } catch(final Exception e) {
                LOGGER.error("Error while user login for var id {}. Exception: {}", getVARId(), e);
                throw e;
            }

            setSessionTimeOut(request, user);
            //Cart cannot have more than one item. Empty cart in case of cart quantity restriction.
            if (StringUtils.isNotBlank(user.getProgramId()) &&
                    AppleUtil.getProgramConfigValueAsBoolean(program, CommonConstants.SINGLE_ITEM_PURCHASE)) {
                LOGGER.info(
                        "Attempting to empty cart if cart has item due to the SingleItemPurchase restriction for the " +
                                "current var/program: {}/{}",
                        user.getVarId(), user.getProgramId());
                cartService.emptyCart(user);
            }
        } catch(final Exception e) {
            LOGGER.error("Error while selectUser: ", e);
        }

        prepareUserAddress(user);
        return user;
    }

    private void setUserPricingInfo(final UserVitality user, final Program program, final Map<String, String> additionalInfo)
            throws B2RException {
        if (AppleUtil.getProgramConfigValueAsBoolean(program, CommonConstants.VIMS_PRICING_API)) {

            final String activationFee = AppleUtil.getAmountWithoutCurrencyCode(additionalInfo.get(ACTIVATION_FEE));
            final String financedAmount = AppleUtil.getAmountWithoutCurrencyCode(additionalInfo.get(FINANCED_AMOUNT));
            final String maxMonthlyPayment =
                AppleUtil.getAmountWithoutCurrencyCode(additionalInfo.get(MAX_MONTHLY_PAYMENT));
            final String programLength = additionalInfo.get(PROGRAM_LENGTH);
            final String delta = StringUtils.defaultIfBlank(additionalInfo.get(DELTA), "0");

            if (!NumberUtils.isParsable(activationFee) ||
                    !NumberUtils.isParsable(financedAmount) ||
                    !NumberUtils.isParsable(maxMonthlyPayment) ||
                    !NumberUtils.isParsable(programLength) ||
                    !NumberUtils.isParsable(delta)) {
                LOGGER.error("The user -{}- does not have pricing info with the selected program {}", user.getUserId(), user.getProgramId());
                throw new B2RException("Pricing info not available");
            }

            LOGGER.info("VarID:{} ProgramID:{} userID: {} - VIMS Pricing Attributes", program.getVarId(), program.getProgramId(), user.getUserId());
            LOGGER.info(PRICING_ATTRIBUTE_LOG, ACTIVATION_FEE, activationFee);
            LOGGER.info(PRICING_ATTRIBUTE_LOG, FINANCED_AMOUNT, financedAmount);
            LOGGER.info(PRICING_ATTRIBUTE_LOG, MAX_MONTHLY_PAYMENT, maxMonthlyPayment);
            LOGGER.info(PRICING_ATTRIBUTE_LOG, PROGRAM_LENGTH, programLength);
            LOGGER.info(PRICING_ATTRIBUTE_LOG, DELTA, delta);

            if (!Double.valueOf(financedAmount).equals(Double.valueOf(maxMonthlyPayment) * Integer.valueOf(programLength))) {
                LOGGER.warn("maxMonthlyPaymentValue( {} )*paymentLength( {} ) != financedAmountValue( {} ) ",
                        maxMonthlyPayment, programLength, financedAmount);
            }

            user.setActivationFee(activationFee);
            user.setFinancedAmount(financedAmount);
            user.setMaxMonthlyPayment(maxMonthlyPayment);
            user.setProgramLength(Integer.valueOf(programLength));
            user.setDelta(delta);

            //This is to avoid persisting redundantly in order_attribute table
            additionalInfo.remove(ACTIVATION_FEE);
            additionalInfo.remove(FINANCED_AMOUNT);
            additionalInfo.remove(MAX_MONTHLY_PAYMENT);
            additionalInfo.remove(PROGRAM_LENGTH);
            additionalInfo.remove(DELTA);
        }
    }

    @Override
    public boolean placeOrder(Order order, User user, Program program) {
        order.setVarOrderId(order.getOrderId().toString());
        return true;
    }

    @Override
    public boolean cancelOrder(Order order) {
        return true;
    }

    @Override
    public boolean cancelOrder(Order order, User user, Program program) {
        return true;
    }

    @Override
    public int getUserPoints(User user, Program program) {
        final String maxPurchaseAmount = varProgramConfigHelper.getValue(user.getVarId(), user.getProgramId(), CommonConstants.MAX_PURCHASE_AMOUNT);
        return (StringUtils.isBlank(maxPurchaseAmount) ? 0 : Integer.parseInt(maxPurchaseAmount));
    }

    @Override
    protected String getVARId() {
        return VAR_ID;
    }

    protected String getLocale() {
        return CommonConstants.LOCALE_EN_US; }

    private void initializeLocaleDependents(HttpServletRequest request, User user) {
        final String defaultLocale = getLocale();
        Locale locale = getLocale(request, defaultLocale);
        if (defaultLocale.equalsIgnoreCase(locale.toString())) {
            request.setAttribute(CommonConstants.LOCALE,defaultLocale);
        }
        setCountry(user);
        user.setLocale(locale);
    }

    public void setCountry(final User user){
        user.setCountry(CommonConstants.COUNTRY_CODE_US);
    }

    @Override
    public boolean isOrderReadyForProcessing() {
        return false;
    }

    /**
     * calculate upgrade cost & total amount due based on the Configuration data
     *
     * @param product
     * @param user
     **/
    @Override
    public void computePricingModel(final Product product, final User user, final Program program) {
        //skip pricing model calculation if no configuration is found
        final String activationFee = loadActivationFee((UserVitality) user, product, program);
        if (isNotBlank(activationFee)) {
            try {
                //TODO: change country & pricing distribution based on Vitality data
                if (MapUtils.isNotEmpty(user.getAdditionalInfo())) {
                    configurePricingModel(product, activationFee, pricingModelConfigurationDao, (UserVitality) user, program);
                }
            } catch (final NumberFormatException nfe) {
                LOGGER.error(
                        "Apple Product Detail: INVALID activation Fee found in Var/Program Configuration. Skipping " +
                                "Pricing model calculation");
            }
        } else {
            LOGGER.info(
                    "Apple Product Detail: No activation Fee found in Var/Program Configuration. Skipping Pricing model calculation");
        }
    }

    private PricingModel createPricingModelFromUserInfo(UserVitality user, final Program program, Price b2sItemPrice) {
        final PricingModel pricingModel = new PricingModel();
        final double activationFeeValue = Double.parseDouble(user.getActivationFee());
        pricingModel.setActivationFee(activationFeeValue);
        final double maxMonthlyPaymentValue = Double.parseDouble(user.getMaxMonthlyPayment());
        pricingModel.setPaymentValue(maxMonthlyPaymentValue);
        pricingModel.setDelta(Double.valueOf(user.getDelta()));
        final int paymentLength = user.getProgramLength();
        pricingModel.setRepaymentTerm(paymentLength);
        pricingModel.setMonthsSubsidized(0); //Applicable only for TVGCorporate
        final double financedAmountValue = Double.parseDouble(user.getFinancedAmount());
        setPricingUpgradeAndTotalDue(user, program, b2sItemPrice.getAmount(), pricingModel, financedAmountValue);

        return pricingModel;
    }

    private PricingModel createPricingModelFromConfig(final User user, final Program program, String activationFee,
                                                      String pricingKey, PricingModelConfigurationDao pricingModelConfigurationDao, Price b2sItemPrice) {
        final PricingModelConfiguration pricingModelConfiguration =
                pricingModelConfigurationDao.getSubsidyByVarIdProgramIdPriceKey(user.getVarId(), user.getProgramId(), pricingKey);

        if (pricingModelConfiguration != null) {
            final PricingModel pricingModel = new PricingModel();
            final Double activationFeeValue = Double.parseDouble(activationFee);
            pricingModel.setActivationFee(activationFeeValue);
            try {
                BeanUtils.copyProperties(pricingModel, pricingModelConfiguration);
            } catch (final IllegalAccessException e) {
                LOGGER.error("ProductServiceV3.computePricingModel(): Property access Exception while " +
                        "accessing PricingModelConfiguration for PricingModel", e);
            } catch (final InvocationTargetException e) {
                LOGGER.error(
                        "ProductServiceV3.computePricingModel(): Property write Exception while creating " +
                                "PricingModel from PricingModelConfiguration", e);
            }
            setPricingUpgradeAndTotalDue(user, program, b2sItemPrice.getAmount(), pricingModel, null);

            return pricingModel;
        } else {
            LOGGER.info(
                    "Apple Product Detail: No PricingModelConfiguration found for var/program/priceKey: {}/{}/{}. " +
                            "Skipping Pricing model calculation.", user.getVarId(), user.getProgramId(), pricingKey);
            return null;
        }
    }

    private void setPricingUpgradeAndTotalDue(final User user, final Program program, final Double priceAmount, final PricingModel pricingModel, final Double financedAmount) {
        final Double upgradeCost = PricingUtil
            .calculateUpgradeCost(user, program, priceAmount, pricingModel.getActivationFee(), financedAmount,
                pricingModel.getPaymentValue(), pricingModel.getDelta());
        pricingModel.setUpgradeCost(upgradeCost);
        final Double totalDueTodayBeforeTax =
            PricingUtil.calculateTotalDueTodayBeforeTax(user, program, upgradeCost, pricingModel.getActivationFee());
        pricingModel.setTotalDueTodayBeforeTax(totalDueTodayBeforeTax);
    }

    private void configurePricingModel(Product product, String activationFee, PricingModelConfigurationDao pricingModelConfigurationDao, UserVitality user, Program program) {
        PricingModel pricingModel;
        final Price b2sItemPrice = product.getOffers().get(0).getB2sItemPrice();
        if (AppleUtil.getProgramConfigValueAsBoolean(program, CommonConstants.VIMS_PRICING_API)) {
            pricingModel = createPricingModelFromUserInfo(user, program, b2sItemPrice);
        } else {
            String pricingKey = user.getAdditionalInfo().get(CommonConstants.PRICING_ID);
            if(isProgramTVGCorporate(user.getProgramId())) {
                pricingKey = product.getCategoryBasedPricingKey(pricingKey);
            } else {
                pricingKey = product.getProductBasedPricingKey();      // pricingKey eg."apple-watch|38mm"
            }
            pricingModel = createPricingModelFromConfig(user, program, activationFee, pricingKey, pricingModelConfigurationDao, b2sItemPrice);
        }

        if (Objects.nonNull(pricingModel)) {
            Map<String, Object> additionalInfo = Optional.ofNullable(product.getAdditionalInfo()).orElseGet(HashMap::new);
            additionalInfo.put(CommonConstants.PRICING_MODEL, pricingModel);
            product.setAdditionalInfo(additionalInfo);
        }
    }

    private String loadActivationFeeFromConfig(final User user, final Product product) {
        String activationFees = null;
        if (CollectionUtils.isNotEmpty(product.getCategories()) && StringUtils.isNotEmpty(product.getCategories().get(0).getSlug())) {
            final String categorySlug = product.getCategories().get(0).getSlug();
            final Map<String, String> productPricing = getAllActivationFees(user.getVarId(), user.getProgramId()).get(categorySlug);
            if(MapUtils.isNotEmpty(productPricing)) {
                activationFees = productPricing.get(product.getOptionValue("caseSize").replace(" ", ""));
            }
        }
        return activationFees;
    }

    private String loadActivationFee(final UserVitality user, final Product product, final Program program) {
        String activationFee;
        if (AppleUtil.getProgramConfigValueAsBoolean(program, CommonConstants.VIMS_PRICING_API)) {
            activationFee = user.getActivationFee();
        } else {
            activationFee = loadActivationFeeFromConfig(user, product);
        }
        return activationFee;
    }

    @Override
    public Map<String, Map<String,String>> getAllActivationFees(final String varId, final String programId) {
        final List<PricingModelConfiguration> pricingModelConfigurations = pricingModelConfigurationDao.getByVarIdProgramIdPriceType(varId, programId, CommonConstants.ACTIVATION_FEE);
        final Map<String, Map<String,String>> activationFeesPricing = new HashMap<>();
        if (pricingModelConfigurations != null) {
            pricingModelConfigurations.stream().forEach(pricingModel -> {
                final String watchSlug = StringUtils.substringBefore(pricingModel.getPriceKey(), "|");
                final String caseSize = StringUtils.substringAfter(pricingModel.getPriceKey(), "|");
                Map<String, String> values = activationFeesPricing.get(watchSlug);
                if (values == null) {
                    values = new HashMap<>();
                }
                values.put(caseSize, pricingModel.getPaymentValue().toString());
                activationFeesPricing.put(watchSlug, values);
            });
        }
        return activationFeesPricing;
    }

    protected boolean isProgramTVGCorporate(final String programID) {
        return CommonConstants.VITALITYUS_TVGCORPORATE_PROGRAM.equalsIgnoreCase(programID);
    }

}
