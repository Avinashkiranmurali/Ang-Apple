package com.b2s.common.services.requests.productservice;

import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 *  This is unit test class for <code>com.b2s.common.services.requests.productservice.CoreProductDetailRequest</code>
 @author sjonnalagadda
  * Date: 08/05/13
  * Time: 11:16 AM
 */

@RunWith(JUnit4.class)
public class CoreProductDetailRequestTest {

    private CoreProductDetailRequest coreProductDetailRequest;

    @After
    public void tearDown(){
        this.coreProductDetailRequest = null;
    }

    @Test
    public void testWhenPsidIsNull(){
        try{
            this.coreProductDetailRequest = CoreProductDetailRequest.builder().withPsid(null).withNeedVariationsInfo(true).withNeedRealTimeInfo(false).build();
            throw new AssertionError("Exception should not reach this line");
        }catch(IllegalArgumentException iae){
            assertThat(iae,new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenPsidIsEmpty(){
        try{
            this.coreProductDetailRequest = CoreProductDetailRequest.builder().withPsid("").withNeedVariationsInfo(true).withNeedRealTimeInfo(false).build();
            throw new AssertionError("Exception should not reach this line");
        }catch(IllegalArgumentException iae){
            assertThat(iae,new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenAllInputsArePresent(){
        this.coreProductDetailRequest = CoreProductDetailRequest.builder().withPsid("30002XYZSDFGH").withNeedVariationsInfo(Boolean.TRUE).withNeedRealTimeInfo(Boolean.FALSE).build();
        assertEquals("Psid is not matching","30002XYZSDFGH", this.coreProductDetailRequest.getPsid());
        assertSame("Variation setting is not matching",Boolean.TRUE, this.coreProductDetailRequest.isNeedVariationsInfo());
        assertSame("Real time setting  is not matching",Boolean.FALSE, this.coreProductDetailRequest.isNeedRealTimeInfo());

    }



}
