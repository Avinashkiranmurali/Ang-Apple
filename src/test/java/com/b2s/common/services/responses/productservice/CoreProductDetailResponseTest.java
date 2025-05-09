package com.b2s.common.services.responses.productservice;


import com.b2s.rewards.model.Product;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

/**
 *  This is unit test class for <code>com.b2s.common.services.responses.productservice.CoreProductDetailResponse</code>
 @author sjonnalagadda
  * Date: 08/05/13
  * Time: 11:48 AM
 */

@RunWith(JUnit4.class)
public class CoreProductDetailResponseTest {
    private  CoreProductDetailResponse coreProductDetailResponse;

    @Before
    public void setUp(){
         this.coreProductDetailResponse = new CoreProductDetailResponse();
    }

    @After
    public void tearDown(){
        this.coreProductDetailResponse = null;
    }

    @Test
    public void testWhenInputPsidIsNull(){
        try{
            this.coreProductDetailResponse.withProductDetailResponse(null,new Product());
            throw new AssertionError("Exception should not reach this line");
        }catch(IllegalArgumentException iae){
            assertThat(iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenPsidIsEmpty(){
        try{
            this.coreProductDetailResponse.withProductDetailResponse("",new Product());
            throw new AssertionError("Exception should not reach this line");
        }catch(IllegalArgumentException iae){
            assertThat(iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenProductDetailIsNull(){
        try{
            this.coreProductDetailResponse.withProductDetailResponse("30002XDFGGHHJJKUL",null);
            throw new AssertionError("Exception should not reach this line");
        }catch(IllegalArgumentException iae){
            assertThat(iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenAddingDuplicatePsid(){
        this.coreProductDetailResponse.withProductDetailResponse("30002XDFGGHHJJKUL",new Product());
        try{
            this.coreProductDetailResponse.withProductDetailResponse("30002XDFGGHHJJKUL",new Product()); //Should  not reach this line.
            throw new AssertionError("Exception should not reach this line");
        }catch(IllegalArgumentException iae){
            assertThat(iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenAllInputsArePresent(){
        this.coreProductDetailResponse.withProductDetailResponse("30002XDFGGHHJJKUL",new Product());
        assertThat(this.coreProductDetailResponse.getProductDetailByPsid("30002XDFGGHHJJKUL").get(), new IsInstanceOf(Product.class));
    }

    @Test
    public void testRetrieveAllProducts(){
        this.coreProductDetailResponse.withProductDetailResponse("30002XDFGGHHJJKUL",new Product());
        this.coreProductDetailResponse.withProductDetailResponse("30002XDFGGHHJJZVL",new Product());
        assertSame("Size of list is not matching", 2, this.coreProductDetailResponse.getAllProductsDetail().size());
    }

    @Test
    public void testRetrieveAllProductsWhenNoProducts(){
        assertSame("Size of list is not matching", 0, this.coreProductDetailResponse.getAllProductsDetail().size());
    }

}
