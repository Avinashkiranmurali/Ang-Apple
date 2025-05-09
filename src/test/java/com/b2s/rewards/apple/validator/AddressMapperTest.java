package com.b2s.rewards.apple.validator;

import com.b2r.util.address.melissadata.GlobalAddress;
import com.b2s.rewards.apple.model.Address;
import com.b2s.rewards.common.util.CommonConstants;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by vprasanna on 11/20/17.
 */
public class AddressMapperTest {

    @Test
    public void testGetValidatorForCountryUS() throws Exception {
        AddressValidatorIF addressValidatorIF = AddressMapper.getValidatorForCountry("US");
        assertNotNull("Check if you get an instance", addressValidatorIF);
        assertEquals("Check if we have the correct instance", "AddressValidatorImpl{DEFAULT}", addressValidatorIF.toString());
    }

    @Test
    public void testGetValidatorForCountryGB() throws Exception {
        AddressValidatorIF addressValidatorIF = AddressMapper.getValidatorForCountry("GB");
        assertNotNull("Check if you get an instance", addressValidatorIF);
        assertEquals("Check if we have the correct instance", "AddressValidatorGBImpl{GB}",
            addressValidatorIF.toString());
    }

    @Test
    public void testGetValidatorForCountrySG() throws Exception {
        AddressValidatorIF addressValidatorIF = AddressMapper.getValidatorForCountry("SG");
        assertNotNull("Check if you get an instance", addressValidatorIF);
        assertEquals("Check if we have the correct instance", "AddressValidatorSGImpl{SG}", addressValidatorIF.toString());
    }

    @Test
    public void testGetValidatorForCountryHK() throws Exception {
        AddressValidatorIF addressValidatorIF = AddressMapper.getValidatorForCountry("HK");
        assertNotNull("Check if you get an instance", addressValidatorIF);
        assertEquals("Check if we have the correct instance", "AddressValidatorHKImpl{HK}", addressValidatorIF.toString());
    }

    @Test
    public void testGetValidatorForCountryMX() throws Exception {
        AddressValidatorIF addressValidatorIF = AddressMapper.getValidatorForCountry("MX");
        assertNotNull("Check if you get an instance", addressValidatorIF);
        assertEquals("Check if we have the correct instance", "AddressValidatorMXImpl{MX}", addressValidatorIF.toString());
    }

    @Test
    public void testGetValidatorForCountryTW() throws Exception {
        AddressValidatorIF addressValidatorIF = AddressMapper.getValidatorForCountry("TW");
        assertNotNull("Check if you get an instance", addressValidatorIF);
        assertEquals("Check if we have the correct instance", "AddressValidatorTWImpl{TW}", addressValidatorIF.toString());
    }

    @Test
    public void testGetValidatorForCountryPH() throws Exception {
        AddressValidatorIF addressValidatorIF = AddressMapper.getValidatorForCountry("PH");
        assertNotNull("Check if you get an instance", addressValidatorIF);
        assertEquals("Check if we have the correct instance", "AddressValidatorPHImpl{PH}", addressValidatorIF.toString());
    }

    @Test
    public void testGetValidatorForCountryCA() throws Exception {
        AddressValidatorIF addressValidatorIF = AddressMapper.getValidatorForCountry("CA");
        assertNotNull("Check if you get an instance", addressValidatorIF);
        assertEquals("Check if we have the correct instance", "AddressValidatorCAImpl{CA}", addressValidatorIF.toString());
    }

    @Test
    public void testTWAddressLineAllThoroughfareInRange() throws Exception {
        final GlobalAddress globalAddress = new GlobalAddress();
        final Address address = new Address();

        globalAddress.setCountryISO3166_1_Alpha2(CommonConstants.COUNTRY_CODE_TW);
        globalAddress.setAddressLine1("大墩十一街21號 3樓之2");
        globalAddress.setThoroughfare("大墩十一街");
        globalAddress.setPremisesNumber("21號");
        globalAddress.setSubPremises("3樓之2");

        AddressMapper.transformGlobalAddress(globalAddress, address);

        Assert.assertEquals("大墩十一街21號3樓之2", address.getAddress1());
    }

    @Test
    public void testTWAddressLineWithInRange() throws Exception {
        final GlobalAddress globalAddress = new GlobalAddress();
        final Address address = new Address();

        globalAddress.setCountryISO3166_1_Alpha2(CommonConstants.COUNTRY_CODE_TW);
        globalAddress.setAddressLine1("大墩大墩十一街21");

        AddressMapper.transformGlobalAddress(globalAddress, address);

        Assert.assertEquals(globalAddress.getAddressLine1(), address.getAddress1());
    }

    @Test
    public void testTWAddressLine1NonDelimeted() throws Exception {
        final GlobalAddress globalAddress = new GlobalAddress();
        final Address address = new Address();

        globalAddress.setCountryISO3166_1_Alpha2(CommonConstants.COUNTRY_CODE_TW);
        globalAddress.setAddressLine1("大墩大墩十一街21號號號號3樓之2345");

        AddressMapper.transformGlobalAddress(globalAddress, address);

        Assert.assertEquals("大墩大墩十一街21號號號", address.getAddress1());
        Assert.assertEquals("號3樓之2345", address.getAddress2());
    }

    @Test
    public void testTWAddressLineNonWithinRange() throws Exception {
        final GlobalAddress globalAddress = new GlobalAddress();
        final Address address = new Address();

        globalAddress.setCountryISO3166_1_Alpha2(CommonConstants.COUNTRY_CODE_TW);
        globalAddress.setAddressLine1("大墩大墩十一街21號號號號3樓之23大墩大墩十一街21號號");

        AddressMapper.transformGlobalAddress(globalAddress, address);

        Assert.assertEquals("大墩大墩十一街21號號號", address.getAddress1());
        Assert.assertEquals("號3樓之23大墩大墩十一街21號號", address.getAddress2());
    }

    @Test
    public void testTWAddressLineSpaceWithinLimit() throws Exception {
        final GlobalAddress globalAddress = new GlobalAddress();
        final Address address = new Address();

        globalAddress.setCountryISO3166_1_Alpha2(CommonConstants.COUNTRY_CODE_TW);
        globalAddress.setAddressLine1("大墩十一街21號 3樓之2");

        AddressMapper.transformGlobalAddress(globalAddress, address);

        Assert.assertEquals("大墩十一街21號3樓之2", address.getAddress1());
    }

    @Test
    public void testTWAddressLineSpaceSplitWithinLimit() throws Exception {
        final GlobalAddress globalAddress = new GlobalAddress();
        final Address address = new Address();

        globalAddress.setCountryISO3166_1_Alpha2(CommonConstants.COUNTRY_CODE_TW);
        globalAddress.setAddressLine1("大墩十一街街 21號 3樓之2");

        AddressMapper.transformGlobalAddress(globalAddress, address);

        Assert.assertEquals("大墩十一街街", address.getAddress1());
        Assert.assertEquals("21號 3樓之2", address.getAddress2());
    }

    @Test
    public void testTWAddressLineSpaceSplitNotWithinLimit() throws Exception {
        final GlobalAddress globalAddress = new GlobalAddress();
        final Address address = new Address();

        globalAddress.setCountryISO3166_1_Alpha2(CommonConstants.COUNTRY_CODE_TW);
        globalAddress.setAddressLine1("大墩十一 21號 3樓之");
        globalAddress.setThoroughfare("Thorough fare");

        AddressMapper.transformGlobalAddress(globalAddress, address);

        Assert.assertEquals("大墩十一 21號 3樓之", address.getAddress1());
    }

    @Test
    public void testTWAddressLineNonMatchingThoroughfare() throws Exception {
        final GlobalAddress globalAddress = new GlobalAddress();
        final Address address = new Address();

        globalAddress.setCountryISO3166_1_Alpha2(CommonConstants.COUNTRY_CODE_TW);
        globalAddress.setAddressLine1("231號 34樓之 大墩十一大墩十一");
        globalAddress.setThoroughfare("大墩十一大墩十一");
        globalAddress.setPremisesNumber("231號");
        globalAddress.setSubPremises("sub");

        AddressMapper.transformGlobalAddress(globalAddress, address);

        Assert.assertEquals("231號 34樓之 大墩", address.getAddress1());
        Assert.assertEquals("十一大墩十一", address.getAddress2());
    }

    @Test
    public void testTWAddressLinePremiseThoroughfareInRange() throws Exception {
        final GlobalAddress globalAddress = new GlobalAddress();
        final Address address = new Address();

        globalAddress.setCountryISO3166_1_Alpha2(CommonConstants.COUNTRY_CODE_TW);
        globalAddress.setAddressLine1("大墩大墩十一街21號 3樓之2");
        globalAddress.setThoroughfare("大墩大墩十一街");
        globalAddress.setPremisesNumber("21號");
        globalAddress.setSubPremises("3樓之2");

        AddressMapper.transformGlobalAddress(globalAddress, address);

        Assert.assertEquals("21號大墩大墩十一街", address.getAddress1());
        Assert.assertEquals("3樓之2", address.getAddress2());
    }

    @Test
    public void testTWAddressLineSubPremiseThoroughfareInRange() throws Exception {
        final GlobalAddress globalAddress = new GlobalAddress();
        final Address address = new Address();

        globalAddress.setCountryISO3166_1_Alpha2(CommonConstants.COUNTRY_CODE_TW);
        globalAddress.setAddressLine1("大墩大墩十一街21號 3樓之2");
        globalAddress.setThoroughfare("大墩大墩十一街");
        globalAddress.setPremisesNumber("墩十一街21號");
        globalAddress.setSubPremises("3樓之2");


        AddressMapper.transformGlobalAddress(globalAddress, address);

        Assert.assertEquals(globalAddress.getPremisesNumber(), address.getAddress1());
        Assert.assertEquals("大墩大墩十一街3樓之2", address.getAddress2());

    }

    @Test
    public void testTWAddressLineAllThoroughfareNotInRange() throws Exception {
        final GlobalAddress globalAddress = new GlobalAddress();
        final Address address = new Address();

        globalAddress.setCountryISO3166_1_Alpha2(CommonConstants.COUNTRY_CODE_TW);
        globalAddress.setAddressLine1("大墩大墩十一街21號號號號 3樓之2345");
        globalAddress.setThoroughfare("大墩大墩十一街21號號號號號");
        globalAddress.setPremisesNumber("墩十一街21號號號");
        globalAddress.setSubPremises("3樓之2345");

        AddressMapper.transformGlobalAddress(globalAddress, address);

        Assert.assertEquals("大墩大墩十一街21號號號", address.getAddress1());
        Assert.assertEquals("號 3樓之2345", address.getAddress2());
    }
}