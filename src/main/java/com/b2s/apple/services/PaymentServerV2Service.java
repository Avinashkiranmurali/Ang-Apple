package com.b2s.apple.services;

import com.b2r.paymentserver.api.PaymentServerClient;
import com.b2r.paymentserver.api.model.AuthStatus;
import com.b2r.paymentserver.api.model.PaymentAuth;
import com.b2r.paymentserver.api.model.PaymentGateway;
import com.b2r.paymentserver.api.model.request.CreateTransactionRequest;
import com.b2r.paymentserver.api.model.response.CaptureResponse;
import com.b2s.apple.entity.PaymentEntity;
import com.b2s.apple.model.finance.CreditCardDetails;
import com.b2s.rewards.apple.model.*;
import com.b2s.rewards.apple.util.AppleUtil;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.shop.common.order.CreditTransactionManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.*;
import static org.apache.commons.lang3.StringUtils.substringAfter;

@Component
public class PaymentServerV2Service {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServerV2Service.class);

    @Autowired
    private PaymentServerClient paymentServerClient;

    @Autowired
    private VarProgramMessageService varProgramMessageService;

    @Autowired
    private Properties applicationProperties;

    /**
     * Calls Payment Service to create Transaction
     *
     * @param user
     * @param program
     * @param sId
     * @param overrideProgramDemoForPaymentServerMode
     * @param requestDomain
     * @param chargeAmt
     * @param randomId
     * @return txId
     */
    public String createTransaction(
        final User user,
        final Program program,
        final String sId,
        final boolean overrideProgramDemoForPaymentServerMode,
        final String requestDomain,
        final double chargeAmt,
        final String randomId) {

        final CreateTransactionRequest request = new CreateTransactionRequest();
        request.setPaymentAuth(
            createPaymentAuth(user, program, sId, overrideProgramDemoForPaymentServerMode, chargeAmt, randomId));
        request.setRequestDomain(requestDomain);
        request.setPaymentGateway(getPaymentGateway(program));

        LOGGER.info(
            "Creating payment transaction record on payment server using varId:{} programId:{} userId:{}" +
                " requestDomain:{} randomId:{} with amount: {}",
            user.getVarId(), user.getProgramId(), user.getUserId(), requestDomain, randomId, chargeAmt);

        final URI txURI = paymentServerClient.createTransaction(request);
        //This is required while capturing the transaction. Credit card capture will fail if this is not set.
        LOGGER.info("Retrieved txn URI {}", txURI);

        if (Objects.isNull(txURI)) {
            LOGGER.error("PaymentTransaction response from payment server is null, aborted the transaction");
            throw new RuntimeException("Payment transaction is NULL - caused by an exception on payment server");
        }

        final String txId = substringAfter(txURI.toString(), URI_TOKEN_TRANSACTION);
        LOGGER.info("PaymentTransaction created successfully. Payment Id is:{}.", txId);
        return txId;
    }

    /**
     * Initialize PaymentAuth to create Transaction
     *
     * @param user
     * @param program
     * @param sessionId
     * @param overrideProgramDemoForPaymentServerMode
     * @param chargeAmt
     * @param randomId
     * @return paymentAuth object
     */
    private PaymentAuth createPaymentAuth(final User user, final Program program, final String sessionId,
        final boolean overrideProgramDemoForPaymentServerMode, final double chargeAmt, final String randomId) {

        final PaymentAuth paymentAuth = new PaymentAuth();
        paymentAuth.setAmount(chargeAmt);
        paymentAuth.setDemo(program.getIsDemo() || overrideProgramDemoForPaymentServerMode);
        paymentAuth.setInsertTime(new Date());
        paymentAuth.setProgramId(user.getProgramId());
        paymentAuth.setVarId(user.getVarId());
        paymentAuth.setRandomId(randomId);
        paymentAuth.setSessionId(sessionId);
        paymentAuth.setAuthStatus(AuthStatus.NEW);
        paymentAuth.setUserId(user.getUserId());

        final Locale userLocale = user.getLocale();
        paymentAuth.setLanguage(userLocale.getLanguage());
        paymentAuth.setCountry(userLocale.getCountry());
        paymentAuth.setCurrency(Currency.getInstance(userLocale).getCurrencyCode());

        paymentAuth.setAllowedBINs(getAllowedBins(program));
        paymentAuth.setAllowedCardTypes(getAllowedCardTypes(program));

        final String messageDbaName = getDbaName(user);
        if (StringUtils.isNotBlank(messageDbaName)) {
            paymentAuth.setDbaName(messageDbaName);
        }
        return paymentAuth;
    }

    /**
     * Match VPC supportedCreditCardTypes with Keystone allowedCardTypes based on CreditCardType Enum
     * as allowedCardTypes configured in Keystone is not matching with our VPC supportedCreditCardTypes
     *
     * @param program
     * @return allowedCardTypes in ',' separated
     */
    private String getAllowedCardTypes(final Program program) {
        final String supportedCardTypes = (String) program.getConfig().get(SUPPORTED_CREDIT_CARD_TYPES);
        try {
            if (StringUtils.isNotBlank(supportedCardTypes) && !supportedCardTypes.equalsIgnoreCase("NONE")) {

                final String[] creditCardTypes = StringUtils.split(supportedCardTypes, ',');
                final StringBuilder allowedCardTypes = new StringBuilder();
                for (final String cardType : creditCardTypes) {
                    allowedCardTypes.append(CreditCardType.valueOf(cardType.trim()).getValue());
                    allowedCardTypes.append(",");
                }
                return allowedCardTypes.substring(0, allowedCardTypes.length() - 1);
            }
            return null;
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Card type not supported - {} ", supportedCardTypes, ex);
            return null;
        }
    }

    /**
     * Method to retrieve allowed Credit Card Bin filters
     *
     * @param program
     * @return allowedBins in ',' separated
     */
    private String getAllowedBins(final Program program) {
        final StringBuilder ccBinBuilder = new StringBuilder();
        final List<Program.CCBin> ccBinList = program.getCcFilters();
        if (CollectionUtils.isNotEmpty(ccBinList)) {
            for (Program.CCBin ccBin : ccBinList) {
                if (Objects.nonNull(ccBin) && StringUtils.isNotEmpty(ccBin.getFilter())) {
                    ccBinBuilder.append(ccBin.getFilter().trim()).append(COMMA);
                }
            }
            ccBinBuilder.deleteCharAt(ccBinBuilder.length() - 1);
        }
        return ccBinBuilder.toString();
    }

    /**
     * Method to retrieve Payment Gateway based on Var and Program
     *
     * @param program
     * @return paymentGateway
     */
    private PaymentGateway getPaymentGateway(final Program program) {
        final String paymentGateway = (String) program.getConfig().getOrDefault(PAYMENT_GATEWAY, PF);

        if (PF.equalsIgnoreCase(paymentGateway)) {
            return PaymentGateway.PAYPAL;
        } else if (BT.equalsIgnoreCase(paymentGateway)) {
            return PaymentGateway.BRAINTREE;
        } else {
            LOGGER.warn("VPC paymentGateway is incorrectly configured for VAR {} :Program {}", program.getVarId(),
                program.getProgramId());
            return PaymentGateway.PAYPAL;
        }
    }

    /**
     * Add Credit Card Line item to Cart Object
     *
     * @param cardDetails
     * @param sessionCart
     * @param user
     * @param program
     */
    public void addCCLineItemInCart(final CreditCardDetails cardDetails, final Cart sessionCart, final User user,
        final Program program) {
        //Setting First and last name in card details
        if (StringUtils.isNotBlank(cardDetails.getCcUsername())) {
            int spaceLocation = cardDetails.getCcUsername().trim().indexOf(" ");
            if (spaceLocation != -1) {
                cardDetails.setFirstName(cardDetails.getCcUsername().substring(0, spaceLocation));
                cardDetails.setLastName(cardDetails.getCcUsername().substring(spaceLocation).trim());
            } else {
                cardDetails.setFirstName(cardDetails.getCcUsername());
                cardDetails.setLastName("");
            }
        }

        if (Objects.isNull(sessionCart.getCreditItem())) {
            sessionCart.setCreditItem(new CreditItem());
        }

        //save credit card holder's first and last name
        sessionCart.getCreditItem().setCcFirstName(cardDetails.getFirstName());
        sessionCart.getCreditItem().setCcLastName(cardDetails.getLastName());

        sessionCart.getCreditItem().setCcLast4(cardDetails.getLast4());
        sessionCart.getCreditItem().setCreditCardType(cardDetails.getCcType());

        //setting Billing address
        AppleUtil.populateBillTo(user, cardDetails);

        final com.b2s.rewards.model.Product coreProduct = CreditTransactionManager
            .getCreditItem(user, sessionCart, program);
        if (coreProduct.getHasOffers() && coreProduct.getDefaultOffer().getItemPrice() != 0) {
            final Product appleProduct = new Product().transform(coreProduct);
            final Offer defaultOffer = transformCoreOfferToAppleOffer(
                coreProduct.getDefaultOffer(),
                appleProduct.getDefaultOffer(),
                getConvRate(sessionCart));
            appleProduct.setOffers(Collections.singletonList(defaultOffer));
            setCreditLineItem(sessionCart, appleProduct);
        }
    }

    /**
     * Service call to capture Transaction
     *
     * @param user
     * @param payment
     * @param transactionId
     * @return status
     */
    public boolean captureTransaction(final User user, final PaymentEntity payment, final String transactionId) {
        final StringBuilder buildPaymentServerUrl =
            new StringBuilder(applicationProperties.getProperty(PAY_SERVER_INTERNAL_URL))
                .append(URI_TOKEN_TRANSACTION)
                .append(transactionId);

        final URI transactionURI;
        try {
            transactionURI = new URI(buildPaymentServerUrl.toString());
        } catch (URISyntaxException uri) {
            LOGGER
                .error("URISyntaxException occurred| UserId: {} | VarID: {} URL {}", user.getUserId(), user.getVarId(),
                    buildPaymentServerUrl);
            return false;
        }
        final CaptureResponse response = paymentServerClient.captureTransaction(transactionURI);

        if (Objects.isNull(response)) {
            LOGGER.error(
                "CREDIT | UserId: {} | VarID: {} FAILED WHEN CONNECTING WITH PAYMENT SERVICE. NO RESPONSE FROM CORE " +
                    "PAYMENT SERVICE",
                user.getUserId(), user.getVarId());
            return false;
        }

        if (response.isError()) {
            LOGGER.error("CREDIT| UserId: {} | VarID: {} | Response message: {}", user.getUserId(), user.getVarId(),
                response.getResponseMessage());
            return false;
        }

        payment.setTransactionId(response.getTransactionId());
        payment.setVarPaymentRefId(response.getReferenceNumber());
        payment.setTransactionTime(new Date());
        payment.setTransactionType(CommonConstants.TRANSACTION_TYPE_SALE);

        return true;
    }

    /**
     * Get DBA name based on VPM
     *
     * @param user
     * @return DBA name
     */
    private String getDbaName(User user) {
        final Properties dbProperties =
            varProgramMessageService.getMessages(
                Optional.ofNullable(user.getVarId()),
                Optional.ofNullable(user.getProgramId()),
                user.getLocale().toString());

        if (Objects.nonNull(dbProperties) && !dbProperties.isEmpty()) {
            return dbProperties.getProperty(DBA);
        }
        return null;
    }

    /**
     * Get Conversion Rate
     *
     * @param sessionCart
     * @return convRate
     */
    private Double getConvRate(final Cart sessionCart) {
        if (Objects.nonNull(sessionCart) && CollectionUtils.isNotEmpty(sessionCart.getCartItems())) {
            final CartItem cartItem = sessionCart.getCartItems().get(0);
            if (Objects.nonNull(cartItem) && Objects.nonNull(cartItem.getProductDetail()) &&
                Objects.nonNull(cartItem.getProductDetail().getDefaultOffer())) {
                return cartItem.getProductDetail().getDefaultOffer().getConvRate();
            }
        }
        return null;
    }

    /**
     * Set Cart with Credit Line Item
     * remove cart item element with creditCard supplier id 20000 from cart.cartItem list
     *
     * @param cart
     * @param product
     */
    private void setCreditLineItem(Cart cart, Product product) {

        //Check to see if a credit line already exists
        for (CartItem cartItem : cart.getCartItems()) {
            if (cartItem.getSupplierId() == SUPPLIER_TYPE_CREDIT) {
                cart.getCartItems().remove(cartItem);
                break;
            }
        }

        //add item to cart to creditProduct pricing
        CartItem cartItem = new CartItem();
        cartItem.setProductId(CAT_CREDIT_STR);
        cartItem.setSupplierId(SUPPLIER_TYPE_CREDIT);
        cartItem.setProductDetail(product);
        cartItem.setQuantity(1);
        cart.setCreditLineItem(cartItem);
    }

    /**
     * Transform Core Offer to Apple Offer
     *
     * @param defaultCoreOffer
     * @param defaultAppleOffer
     * @param convRate
     * @return Offer
     */
    private Offer transformCoreOfferToAppleOffer(
        final com.b2s.rewards.model.Offer defaultCoreOffer, final Offer defaultAppleOffer, final Double convRate) {

        final Offer result = (Objects.isNull(defaultAppleOffer)) ? new Offer() : defaultAppleOffer;
        result.setSku(defaultCoreOffer.getSku());
        result.setConvRate(convRate);

        if (Optional.ofNullable(defaultCoreOffer.getSupplierItemPrice()).isPresent()) {
            result.setBasePrice(
                new Price(
                    defaultCoreOffer.getSupplierItemPrice().getMoney(),
                    defaultCoreOffer.getSupplierItemPrice().getPoints()));
            result.setUnpromotedSupplierItemPrice(
                new Price(
                    defaultCoreOffer.getSupplierItemPrice().getMoney(),
                    defaultCoreOffer.getSupplierItemPrice().getPoints()));
        }
        result.setOrgShippingPrice(defaultCoreOffer.getShippingPrice());

        if (Optional.ofNullable(defaultCoreOffer.getB2sItemPrice()).isPresent()) {
            result.setB2sItemPrice(
                new Price(
                    defaultCoreOffer.getB2sItemPrice().getMoney(),
                    defaultCoreOffer.getB2sItemPrice().getPoints()));
        }
        if (Optional.ofNullable(defaultCoreOffer.getB2sShippingPrice()).isPresent()) {
            result.setB2sShippingPrice(
                new Price(
                    defaultCoreOffer.getB2sShippingPrice().getMoney(),
                    defaultCoreOffer.getB2sShippingPrice().getPoints()));
        }

        if (Optional.ofNullable(defaultCoreOffer.getB2sPrice()).isPresent()) {
            result.setTotalPrice(new Price(
                defaultCoreOffer.getB2sPrice().getMoney(),
                defaultCoreOffer.getB2sPrice().getPoints()));
        }
        if (Optional.ofNullable(defaultCoreOffer.getVarPrice()).isPresent()) {
            final Money varPriceAmount =
                Money.of(
                    defaultCoreOffer.getCurrency(),
                    BigDecimal.valueOf(defaultCoreOffer.getVarPrice()).movePointLeft(2));
            result.setVarPrice(new Price(varPriceAmount, 0));
            result.setUnpromotedVarPrice(new Price(varPriceAmount, 0));
        }

        if (Optional.ofNullable(defaultCoreOffer.getItemPrice()).isPresent()) {
            result.setOrgItemPrice(defaultCoreOffer.getItemPrice());
        }
        if (Optional.ofNullable(defaultCoreOffer.getShippingPrice()).isPresent()) {
            result.setOrgShippingPrice(defaultCoreOffer.getShippingPrice());
        }

        if (Optional.ofNullable(defaultCoreOffer.getB2sItemMargin()).isPresent()) {
            result.setB2sItemMargin(defaultCoreOffer.getB2sItemMargin());
        }
        if (Optional.ofNullable(defaultCoreOffer.getVarItemMargin()).isPresent()) {
            result.setVarItemMargin(defaultCoreOffer.getVarItemMargin());
        }

        if (Optional.ofNullable(defaultCoreOffer.getB2sItemProfitPrice()).isPresent()) {
            result.setB2sItemProfitPrice(new Price(defaultCoreOffer.getB2sItemProfitPrice(), "", 0));
        }
        if (Optional.ofNullable(defaultCoreOffer.getVarItemProfitPrice()).isPresent()) {
            result.setVarItemProfitPrice(new Price(defaultCoreOffer.getVarItemProfitPrice(), "", 0));
        }
        return result;
    }
}
