package com.b2s.apple.mapper;

import com.b2s.rewards.apple.model.*;
import com.b2s.shop.common.User;
import com.b2s.apple.model.CartPricingResponseDTO;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.money.CurrencyUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.when;

/**
 *  This is unit test class for <code>com.b2s.apple.mapper.PricingServiceResponseMapperV2</code>
 @author skathirvel

 */

public class PricingServiceResponseMapperV2Test extends PricingServiceMock {

    @InjectMocks
    private PricingServiceResponseMapperV2 pricingServiceResponseMapperV2;

    @Mock
    private Properties applicationProperties;

    private ObjectMapper objectMapper;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(applicationProperties.getProperty("discountcode" + ".perOrder")).thenReturn("1");
        objectMapper = setObjectMapper();
    }


    /**
     * This will set all attributes in Shopping cart using the other input parameters
     */
    @Test
    public void testPopulateCartPrices() {

        User user = new User();
        user.setVarId("1");
        user.setProgramId("1");
        user.setUserId("raji");

        Cart cart = getCart();
        CartPricingResponseDTO cartPricingResponseDTO = null;
        try {
            //Sample Pricing Response
            File pricingJSONFile = ResourceUtils.getFile("classpath:json/pricing_response_SG_30001MK0C2ZAA.json");
            pricingJSONFile.setExecutable(false);
            pricingJSONFile.setReadable(true);
            pricingJSONFile.setWritable(false);
            //JSON to Object Mapper using Jackson
            cartPricingResponseDTO = objectMapper.readValue(pricingJSONFile, CartPricingResponseDTO.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pricingServiceResponseMapperV2.populateCartPrices(cartPricingResponseDTO, user, cart, getProgram());
        //Asserting Conversion Rate
        Assert.assertTrue(cart.getConvRate().equals(cartPricingResponseDTO.getProducts().stream().
                                                        findFirst().get().getConversionRates().get()
                                                            .getFxRates().get(CurrencyUnit.of("PNT")).getRate().doubleValue()));

    }

    /**
     * This will return Cart object
     */
    private Cart getCart() {

        Cart cart = new Cart();
        CartTotal cartTotal = new CartTotal();
        Price price = new Price(700.0, "CAD", 100000);

        cartTotal.setPrice(price);
        cart.setCartTotal(cartTotal);
        cart.setDisplayCartTotal(cartTotal);

        CartItem cartItem = new CartItem();
        cartItem.setProductId("30001MK0C2ZA/A");
        cartItem.setQuantity(1);
        cartItem.setProductDetail(getProduct());
        cartItem.setDiscountType("points");
        List li = new ArrayList<CartItem>();
        li.add(cartItem);
        cart.setCartItems(li);

        SupplementaryPaymentLimit supplementaryPaymentLimit = new SupplementaryPaymentLimit();
        supplementaryPaymentLimit.setRewardsMinLimit(new Price(Double.valueOf(140), "CAD", 20000));
        supplementaryPaymentLimit.setPaymentMaxLimit(new Price(Double.valueOf(560), "CAD", 80000));
        cart.setSupplementaryPaymentLimit(supplementaryPaymentLimit);

        return cart;
    }

    /**
     * This will return Program object
     */
    private Program getProgram(){

        final Program program = new Program();
        program.setVarId("1");
        program.setProgramId("b2s_qa_only");
        final List<com.b2s.rewards.apple.model.PaymentOption> paymentOptions = new ArrayList<>();
        final com.b2s.rewards.apple.model.PaymentOption paymentOption = new com.b2s.rewards.apple.model.PaymentOption();
        paymentOption.setPaymentOption("PAYROLL_DEDUCTION");
        paymentOption.setSupplementaryPaymentLimitType("P");
        paymentOption.setSupplementaryPaymentMaxLimit(80);
        paymentOption.setIsActive(true);
        paymentOptions.add(paymentOption);
        program.setPayments(paymentOptions);
        Map map = new HashMap<>();
        map.put("catalogId", "apple");
        program.setConfig(map);
        program.setTargetCurrency(CurrencyUnit.USD);
        return program;
    }

    /**
     * This will return Product object
     */
    private Product getProduct(){
        Product product = new Product();
        product.setPsid("30001MK0C2ZA/A");
        product.setName("Dummy product name");

        Offer offer = new Offer();
        offer.setIsEligibleForPayrollDeduction(false);
        List<Offer> offers = new ArrayList<>();
        offers.add(offer);
        product.setOffers(offers);
        return product;
    }
}