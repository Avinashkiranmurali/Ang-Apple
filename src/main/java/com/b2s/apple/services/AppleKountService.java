package com.b2s.apple.services;

import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.client.kount.model.KountDecision;
import com.client.kount.model.KountRequest;
import com.client.kount.model.KountSession;
import com.kount.ris.util.Address;
import com.kount.ris.util.CartItem;
import com.kount.ris.util.MerchantAcknowledgment;
import com.kount.ris.util.payment.NoPayment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by rpillai on 7/9/2018.
 */
@Service
public class AppleKountService {

    private static final Logger logger = LoggerFactory.getLogger(AppleKountService.class);

    @Autowired
    private com.client.kount.KountService kountService;

    public KountDecision getDecision(final Cart cart, final User user, final Program program, final KountSession kountSession) {
        KountDecision kountDecision = KountDecision.APPROVED;
        try {
            KountRequest kountRequest = new KountRequest();
            kountRequest.setAddress(new Address(cart.getShippingAddress().getAddress1(),
                    cart.getShippingAddress().getAddress2(),
                    cart.getShippingAddress().getCity(),
                    cart.getShippingAddress().getState(),
                    cart.getShippingAddress().getZip5(), cart.getShippingAddress().getCountry()));
            kountRequest.setBillingAddress(new Address());
            kountRequest.setKountSession(kountSession);
            kountRequest.setEmail(cart.getShippingAddress().getEmail());
            kountRequest.setIpAddress(user.getIPAddress());
            kountRequest.setCcAmount(CartOrderConverterService.getAmountInCents(cart.getCost()));
            kountRequest.setCurrency(program.getTargetCurrency().toCurrency());
            kountRequest.setCart(getKountCartItems(cart));
            kountRequest.setParticipantId(user.getUserId());
            kountRequest.setAgentId(user.getProxyUserId());
            kountRequest.setVarId(user.getVarId());
            kountRequest.setProgramId(user.getProgramId());
            kountRequest.setParticipantName(user.getFullName());
            kountRequest.setTotal(CartOrderConverterService.getAmountInCents(cart.getCartTotal().getPrice().getAmount()));
            kountRequest.setPayment(new NoPayment());
            kountRequest.setMerchantAcknowledgment(MerchantAcknowledgment.YES);
            kountRequest.setShippingPhoneNumber(cart.getShippingAddress().getPhoneNumber());
            logger.info("Kount fraud detection is enabled. Sending the kount request: {}", kountRequest);
            kountDecision = kountService.getDecision(kountRequest);
            logger.info("Decision from Kount: {}", kountDecision);
        } catch(Exception e) {
            logger.error("Error while getting kount decision", e);
        }
        return kountDecision;
    }

    public boolean isKountEnabled(final Program program) {
        if(program != null && program.getConfig() !=null) {
            return (Boolean) program.getConfig().getOrDefault(CommonConstants.KOUNT_ENABLED, Boolean.FALSE);
        } else {
            return Boolean.FALSE;
        }
    }

    private List<CartItem> getKountCartItems(final Cart cart) {
        return cart.getCartItems()
                .stream()
                .filter(cartItem1 -> cartItem1.getSupplierId().equals(CommonConstants.SUPPLIER_TYPE_MERC))
                .map(cartItem -> new CartItem(
                    (CollectionUtils.isNotEmpty(cartItem.getProductDetail().getCategories())?
                        cartItem.getProductDetail().getCategories().get(0).getName(): StringUtils.EMPTY),
                    cartItem.getProductDetail().getName(),
                    cartItem.getProductDetail().getShortDescription(),
                    cartItem.getQuantity(),
                    CartOrderConverterService
                        .getAmountInCents(cartItem.getProductDetail().getDefaultOffer().getDisplayPrice().getAmount())))
                .collect(Collectors.toList());
    }
}
