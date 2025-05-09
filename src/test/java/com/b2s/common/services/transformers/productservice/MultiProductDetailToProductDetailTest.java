package com.b2s.common.services.transformers.productservice;

import com.b2s.rewards.apple.model.Program;
import com.b2s.common.services.requests.productservice.CoreProductDetailRequest;
import com.b2s.common.services.requests.productservice.MultiProductDetailRequest;
import com.b2s.service.product.client.application.detail.ProductDetailRequest;
import org.easymock.EasyMock;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

/**
 *  This is unit test class for <code>com.b2s.common.services.transformers.productservice.MultiProductDetailToProductDetail</code>
 @author sjonnalagadda
  * Date: 08/05/13
  * Time: 06:06 PM

 */

@RunWith(JUnit4.class)
@Ignore
public class MultiProductDetailToProductDetailTest {

    private  MultiProductDetailToProductDetail multiProductDetailToProductDetail;
    private MultiProductDetailRequest multiProductDetailRequest;

    @Before
    public void setupData(){
        multiProductDetailToProductDetail = new MultiProductDetailToProductDetail();

        final Program program = EasyMock.createMock(Program.class);
        Map<String, Object> configs = new HashMap<>();
        configs.put("catalog_id","rbc");
        configs.put("defaultPSprogram", "rbc");

        expect(program.getVarId()).andReturn("1").anyTimes();
        expect(program.getProgramId()).andReturn("1").anyTimes();
        expect(program.getConfig()).andReturn(configs).anyTimes();
//        expect(program.getCatalogId()).andReturn("rbc").anyTimes();
//        expect(program.getDefaultPSprogram()).andReturn("rbc").anyTimes();

        replay(program);
        multiProductDetailRequest = new MultiProductDetailRequest(Optional.ofNullable(Locale.CANADA_FRENCH), program, "APPLEGR");
    }

    @After
    public void tearDown(){
        multiProductDetailToProductDetail = null;
        multiProductDetailRequest = null;
    }

    @Test
    public void testWhenInputIsNull(){
       try{
           multiProductDetailToProductDetail.transform(null, null, null);
           throw new AssertionError("Exception should not reach this line");
       }catch(IllegalArgumentException iae){
           assertThat(iae, new IsInstanceOf(IllegalArgumentException.class));
       }
    }

    @Test
    public void testWhenInputIsPresentAndNoDetailRequests(){
        try{
            multiProductDetailToProductDetail.transform(multiProductDetailRequest, null, null);
            throw new AssertionError("Exception should not reach this line");
        }catch(IllegalArgumentException iae){
            assertThat(iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenDetailRequestsIsPresent(){
        multiProductDetailRequest.withProductDetailRequest(CoreProductDetailRequest.builder().withPsid("30002XDFVCGDFGH").withNeedVariationsInfo(true).withNeedRealTimeInfo(false).build());
        final Set<ProductDetailRequest> productDetailRequests =  multiProductDetailToProductDetail.transform(multiProductDetailRequest, null, null);
        assertSame("Set size is not matching",1,productDetailRequests.size());
        final ProductDetailRequest productDetailRequest = productDetailRequests.iterator().next();
        assertEquals("PSID is not matching","30002XDFVCGDFGH",productDetailRequest.getPsid());
        assertSame("Variations is not matching",false,productDetailRequest.isWithVariations());
        assertSame("Realtime info is not matching",false,productDetailRequest.isWithRealTimeInfo());
    }


}
