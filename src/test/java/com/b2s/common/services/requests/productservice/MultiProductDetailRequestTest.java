package com.b2s.common.services.requests.productservice;

import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 *  This is unit test class for <code>com.b2s.common.services.requests.productservice.MultiProductDetailRequest</code>
 @author sjonnalagadda
  * Date: 08/05/13
  * Time: 10:47 AM
 */


@RunWith(JUnit4.class)
public class MultiProductDetailRequestTest {

    private MultiProductDetailRequest multiProductDetailRequest;

    @Before
    public void setupData(){
        multiProductDetailRequest = new MultiProductDetailRequest(Optional.ofNullable(Locale.CANADA_FRENCH));
    }

    @After
    public void tearDown(){
        multiProductDetailRequest = null;
    }

    @Test
    public void testAddingSearchDetailRequest(){
        final CoreProductDetailRequest coreProductDetailRequest = CoreProductDetailRequest.builder().withPsid("300021XSDFVGH").withNeedVariationsInfo(true).withNeedRealTimeInfo(false).build();
        multiProductDetailRequest.withProductDetailRequest(coreProductDetailRequest);
        assertNotNull("ProductDetail request is null",multiProductDetailRequest.getProductDetailRequests().get("300021XSDFVGH"));
    }

    @Test
    public void testAddingNullSearchDetailRequest(){
        try{
            multiProductDetailRequest.withProductDetailRequest(null);
            throw new AssertionError("Exception should not reach this line");
        }catch(IllegalArgumentException iae){
            assertThat( iae,  new IsInstanceOf(IllegalArgumentException.class));
        }

    }

}
