package com.b2s.rewards.apple.validator;

import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.CartItemOption;
import com.b2s.rewards.common.context.AppContext;
import com.b2s.shop.common.User;
import com.b2s.web.B2RReloadableResourceBundleMessageSource;
import org.apache.commons.collections.MapUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddressValidatorTest {

    @InjectMocks
    private AddressValidatorCAImpl addressValidatorCA;

    @InjectMocks
    private AddressValidatorImpl addressValidator;

    @Mock
    ApplicationContext applicationContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHasValidationErrorTrue() {
        AppContext.setApplicationContext(applicationContext);

        MessageSource messageSource = mock(B2RReloadableResourceBundleMessageSource.class);
        when(applicationContext.getBean(anyString())).thenReturn(messageSource);

        User user = getUser();
        Address address = AddressMapper.getAddress(user, getProgram(user));
        boolean result = addressValidator.hasValidationError(address, null, user);
        Assert.assertTrue(result);
    }

    @Test
    public void testCAHasValidationErrorTrue() {
        AppContext.setApplicationContext(applicationContext);

        MessageSource messageSource = mock(B2RReloadableResourceBundleMessageSource.class);
        when(applicationContext.getBean(anyString())).thenReturn(messageSource);

        User user = getUser();
        Address address = AddressMapper.getAddress(user, getProgram(user));
        address.setPhoneNumber("41273921");
        boolean result = addressValidatorCA.hasValidationError(address, null, user);
        Assert.assertTrue(result);
    }

    @Test
    public void testCAHasValidationErrorFalse() {
        AppContext.setApplicationContext(applicationContext);

        MessageSource messageSource = mock(B2RReloadableResourceBundleMessageSource.class);
        when(applicationContext.getBean(anyString())).thenReturn(messageSource);

        User user = getUser();
        Address address = AddressMapper.getAddress(user, getProgram(user));
        address.setFirstName("AAA");
        address.setLastName("ZZZ");
        address.setAddress1("100 MAIN STREET");
        address.setAddress2("STE 345");
        address.setCity("TORONTO.St's-James");
        address.setZip4("1234");
        address.setZip5("T0A 0A0");
        address.setState("ON"); //Valid CA state
        address.setCountry("CA");
        address.setPhoneNumber("4083767229");
        address.setEmail("XYZ@bakkt.com");
        address.setIgnoreSuggestedAddress("false");
        boolean result = addressValidatorCA.hasValidationError(address, null, user);
        Assert.assertFalse(result);
    }


    @Test
    public void testErrorMessagesCA() {
        AppContext.setApplicationContext(applicationContext);

        MessageSource messageSource = mock(B2RReloadableResourceBundleMessageSource.class);
        when(applicationContext.getBean(anyString())).thenReturn(messageSource);

        User user = getUser();
        Address address = AddressMapper.getAddress(user, getProgram(user));
        address.setAddress1("P.O.Box");
        address.setAddress2("P.O.Box");
        address.setCity("Raゑをん39น้ำ");
        address.setState("");
        address.setZip4("P.O.");
        address.setZip5("P.O.Box");
        address.setCountry("");
        address.setPhoneNumber("");
        address.setIgnoreSuggestedAddress("test");
        boolean result = addressValidatorCA.hasValidationError(address, null, user);
        Assert.assertTrue(result);
        Assert.assertTrue(MapUtils.isNotEmpty(address.getErrorMessage()));
        Assert.assertTrue(address.getErrorMessage().containsKey(CartItemOption.FIRST_NAME.getValue()));
        Assert.assertTrue(address.getErrorMessage().containsKey(CartItemOption.ADDRESS1.getValue()));
        Assert.assertTrue(address.getErrorMessage().containsKey(CartItemOption.ADDRESS2.getValue()));
        Assert.assertTrue(address.getErrorMessage().containsKey(CartItemOption.CITY.getValue()));
        Assert.assertTrue(address.getErrorMessage().containsKey(CartItemOption.STATE.getValue()));
        Assert.assertTrue(address.getErrorMessage().containsKey(CartItemOption.ZIP4.getValue()));
        Assert.assertTrue(address.getErrorMessage().containsKey(CartItemOption.ZIP5.getValue()));
        Assert.assertTrue(address.getErrorMessage().containsKey(CartItemOption.COUNTRY.getValue()));
        Assert.assertTrue(address.getErrorMessage().containsKey(CartItemOption.PHONE_NUMBER.getValue()));
        Assert.assertTrue(address.getErrorMessage().containsKey(CartItemOption.EMAIL.getValue()));
        Assert.assertTrue(address.getErrorMessage().containsKey(CartItemOption.IGNORE_SUGGESTED_ADDRESS.getValue()));
    }

    @Test
    public void testGBHasValidationErrorTrue() {
        AppContext.setApplicationContext(applicationContext);

        MessageSource messageSource = mock(B2RReloadableResourceBundleMessageSource.class);
        when(applicationContext.getBean(anyString())).thenReturn(messageSource);

        User user = getGBUser();
        Address address = AddressMapper.getAddress(user, getProgram(user));
        address.setPhoneNumber("077 3821 8969");
        addressValidator = new AddressValidatorGBImpl();
        boolean result = addressValidator.hasValidationError(address, null, user);
        Assert.assertTrue(result);
    }

    private User getUser() {
        final User user = new User();
        user.setUserId("mathi");
        user.setVarId("RBC");
        user.setProgramId("b2s_qa_only");
        user.setState("ON");
        user.setCountry("CA");
        return user;
    }

    private User getGBUser() {
        final User user = new User();
        user.setUserId("eric");
        user.setVarId("GrassRootsUK");
        user.setProgramId("b2s_qa_only");
        user.setCountry("GB");
        return user;
    }
    private Program getProgram(final User user) {
        final Program program = new Program();
        program.setVarId(user.getVarId());
        program.setProgramId(user.getProgramId());
        return program;
    }
}