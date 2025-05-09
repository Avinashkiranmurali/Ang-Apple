package com.b2s.common.services.productservice;

import com.b2s.common.CategoryInfo;
import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.requests.productservice.CoreCategoryTaxonomyRequest;
import com.b2s.common.services.transformers.TransformersHolder;
import com.b2s.common.services.transformers.productservice.CategoryNodeToCategoryInfo;
import com.b2s.common.services.transformers.productservice.CoreCategoryToCategoryTaxonomy;
import com.b2s.service.product.client.api.CategoryServiceClient;
import com.b2s.service.product.client.application.categories.CategoryTaxonomyRequest;
import com.b2s.service.product.client.common.CatalogRequestContext;
import com.b2s.service.product.client.domain.CategoryNode;
import org.easymock.EasyMock;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Locale;

import static org.easymock.EasyMock.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

/**
 *  This is unit test class for <code>com.b2s.common.services.productservice.CategoryServiceV3</code>
 @author sjonnalagadda
  * Date: 8/28/13
  * Time: 5:53 PM
 */
@RunWith(JUnit4.class)
public class CategoryServiceV3Test {

    private CategoryServiceV3 categoryServiceV3;
    private CoreCategoryToCategoryTaxonomy coreCategoryToCategoryTaxonomy;
    private CategoryNodeToCategoryInfo categoryNodeToCategoryInfo;
    private TransformersHolder<CoreCategoryTaxonomyRequest,CategoryTaxonomyRequest,CategoryNode,CategoryInfo> categoryTransformersHolder;
    private ProductServiceFactoryWrapper productServiceFactoryWrapper;

    @After
    public void tearDown(){
        this.categoryServiceV3 = null;
        this.coreCategoryToCategoryTaxonomy = null;
        this.categoryNodeToCategoryInfo = null;
        this.categoryTransformersHolder = null;
        this.productServiceFactoryWrapper = null;
    }

    @Test
    public void testWhenFirstArgumentIsNullForConstructor(){
        try {
            this.coreCategoryToCategoryTaxonomy = EasyMock.createMock(CoreCategoryToCategoryTaxonomy.class);
            this.categoryNodeToCategoryInfo = EasyMock.createMock(CategoryNodeToCategoryInfo.class);
            this.categoryTransformersHolder =
                new TransformersHolder<>(this.coreCategoryToCategoryTaxonomy, this.categoryNodeToCategoryInfo);
            this.categoryServiceV3 = new CategoryServiceV3(null, this.categoryTransformersHolder);
            throw new AssertionError("Should not reach this line");
        } catch (final ServiceException se) {
            assertThat("Exception is not matching", se, new IsInstanceOf(ServiceException.class));
        }
        EasyMock.reset(coreCategoryToCategoryTaxonomy, categoryNodeToCategoryInfo);
    }

    @Test
    public void testWhenSecondArgumentIsNullForConstructor(){
        try{
            this.productServiceFactoryWrapper = EasyMock.createMock(ProductServiceFactoryWrapper.class);
            this.categoryServiceV3 =  new CategoryServiceV3(this.productServiceFactoryWrapper,null);
            throw new AssertionError("Should not reach this line");
        }catch(final ServiceException se){
            assertThat("Exception is not matching",se, new IsInstanceOf(ServiceException.class));
        }
        EasyMock.reset(productServiceFactoryWrapper);
    }


    @Test
    public void testWhenRequestTransformerIsAbsent(){
        try{
            this.categoryNodeToCategoryInfo = EasyMock.createMock(CategoryNodeToCategoryInfo.class);
            this.productServiceFactoryWrapper = EasyMock.createMock(ProductServiceFactoryWrapper.class);
            this.categoryTransformersHolder = new TransformersHolder<>(null, this.categoryNodeToCategoryInfo);
            this.categoryServiceV3 =  new CategoryServiceV3(this.productServiceFactoryWrapper,this.categoryTransformersHolder);
            throw new AssertionError("Should not reach this line");
        }catch(final ServiceException se){
            assertThat("Exception is not matching",se, new IsInstanceOf(ServiceException.class));
        }
        EasyMock.reset(categoryNodeToCategoryInfo,productServiceFactoryWrapper);
    }

    @Test
    public void testWhenResponseTransformerIsAbsent(){
        try{
            this.coreCategoryToCategoryTaxonomy = EasyMock.createMock(CoreCategoryToCategoryTaxonomy.class);
            this.productServiceFactoryWrapper = EasyMock.createMock(ProductServiceFactoryWrapper.class);
            this.categoryTransformersHolder = new TransformersHolder<>(coreCategoryToCategoryTaxonomy, null);
            this.categoryServiceV3 =  new CategoryServiceV3(this.productServiceFactoryWrapper,this.categoryTransformersHolder);
            throw new AssertionError("Should not reach this line");
        }catch(final ServiceException se){
            assertThat("Exception is not matching",se, new IsInstanceOf(ServiceException.class));
        }
        EasyMock.reset(coreCategoryToCategoryTaxonomy,productServiceFactoryWrapper);
    }

    @Test
    public void testWhenInputIsNullForTaxonomyRequest() throws ServiceException{
        this.coreCategoryToCategoryTaxonomy = EasyMock.createMock(CoreCategoryToCategoryTaxonomy.class);
        this.categoryNodeToCategoryInfo = EasyMock.createMock(CategoryNodeToCategoryInfo.class);
        this.categoryTransformersHolder = new TransformersHolder<>(this.coreCategoryToCategoryTaxonomy, this.categoryNodeToCategoryInfo);
        this.productServiceFactoryWrapper = EasyMock.createMock(ProductServiceFactoryWrapper.class);
        this.categoryServiceV3 =  new CategoryServiceV3(this.productServiceFactoryWrapper,this.categoryTransformersHolder);
        try{
            this.categoryServiceV3.queryCategoryTaxonomyByLocale(null);
            throw new AssertionError("Should not reach this line");
        }catch(final ServiceException se){
            assertThat("Exception is not matching",se, new IsInstanceOf(ServiceException.class));
        }
        EasyMock.reset(coreCategoryToCategoryTaxonomy,categoryNodeToCategoryInfo,productServiceFactoryWrapper);
    }

    @Test
    public void testWhenTransformerThrowsException() throws ServiceException{
        final CoreCategoryTaxonomyRequest coreCategoryTaxonomyRequest = new  CoreCategoryTaxonomyRequest(Locale.US, "rbc");
        this.coreCategoryToCategoryTaxonomy = EasyMock.createMock(CoreCategoryToCategoryTaxonomy.class);
        this.categoryNodeToCategoryInfo = EasyMock.createMock(CategoryNodeToCategoryInfo.class);
        //noinspection unchecked
        this.categoryTransformersHolder = EasyMock.createMock(TransformersHolder.class);
        final RuntimeException runtimeException = new RuntimeException();
        this.productServiceFactoryWrapper = EasyMock.createMock(ProductServiceFactoryWrapper.class);
        try{
            EasyMock.expect(this.categoryTransformersHolder.getRequestTransformer()).andReturn(this.coreCategoryToCategoryTaxonomy).times(2);
            EasyMock.expect(this.categoryTransformersHolder.getResponseTransformer()).andReturn(this.categoryNodeToCategoryInfo).times(1);
            EasyMock.expect(this.coreCategoryToCategoryTaxonomy.transform(coreCategoryTaxonomyRequest, null, null)).andThrow(runtimeException).times(1);
            EasyMock.replay(this.categoryTransformersHolder);
            EasyMock.replay(this.coreCategoryToCategoryTaxonomy);
            this.categoryServiceV3 =  new CategoryServiceV3(this.productServiceFactoryWrapper,this.categoryTransformersHolder);
            this.categoryServiceV3.queryCategoryTaxonomyByLocale(coreCategoryTaxonomyRequest);
            throw new AssertionError("Should not reach this line");
        }catch(final ServiceException se){
            assertThat("Exception is not matching",se, new IsInstanceOf(ServiceException.class));
        }
        EasyMock.verify(this.categoryTransformersHolder);
        EasyMock.verify(this.coreCategoryToCategoryTaxonomy);
        EasyMock.reset(coreCategoryToCategoryTaxonomy,categoryNodeToCategoryInfo,productServiceFactoryWrapper,categoryTransformersHolder);
    }

    @Test
    public void testWhenTransformerReturnsNull() throws ServiceException{
        final CoreCategoryTaxonomyRequest coreCategoryTaxonomyRequest = new  CoreCategoryTaxonomyRequest(Locale.US, "rbc");
        this.coreCategoryToCategoryTaxonomy = EasyMock.createMock(CoreCategoryToCategoryTaxonomy.class);
        this.categoryNodeToCategoryInfo = EasyMock.createMock(CategoryNodeToCategoryInfo.class);
        //noinspection unchecked
        this.categoryTransformersHolder = EasyMock.createMock(TransformersHolder.class);
        this.productServiceFactoryWrapper = EasyMock.createMock(ProductServiceFactoryWrapper.class);
        EasyMock.expect(this.categoryTransformersHolder.getRequestTransformer()).andReturn(this.coreCategoryToCategoryTaxonomy).times(2);
        EasyMock.expect(this.categoryTransformersHolder.getResponseTransformer()).andReturn(this.categoryNodeToCategoryInfo).times(1);
        EasyMock.expect(this.coreCategoryToCategoryTaxonomy.transform(coreCategoryTaxonomyRequest, null, null)).andReturn(null).times(1);
        EasyMock.replay(this.categoryTransformersHolder);
        EasyMock.replay(this.coreCategoryToCategoryTaxonomy);
        this.categoryServiceV3 =  new CategoryServiceV3(this.productServiceFactoryWrapper,this.categoryTransformersHolder);
        assertNull(this.categoryServiceV3.queryCategoryTaxonomyByLocale(coreCategoryTaxonomyRequest));
        EasyMock.verify(this.categoryTransformersHolder);
        EasyMock.verify(this.coreCategoryToCategoryTaxonomy);
        EasyMock.reset(coreCategoryToCategoryTaxonomy,categoryNodeToCategoryInfo,productServiceFactoryWrapper,categoryTransformersHolder);
    }


    @Test
    public void testWhenServiceReturnsResponse() throws ServiceException{

        final CategoryTaxonomyRequest categoryTaxonomyRequestMock = CategoryTaxonomyRequest.builder()
            .withRequestContext( CatalogRequestContext.builder().withCatalogId("123").build()).build();
        final CoreCategoryTaxonomyRequest coreCategoryTaxonomyRequestMock = EasyMock.createMock(CoreCategoryTaxonomyRequest.class);
        final CategoryServiceClient categoryServiceClientMock = EasyMock.createMock(CategoryServiceClient.class);
        this.coreCategoryToCategoryTaxonomy = EasyMock.createMock(CoreCategoryToCategoryTaxonomy.class);
        this.categoryNodeToCategoryInfo = EasyMock.createMock(CategoryNodeToCategoryInfo.class);
        //noinspection unchecked
        this.categoryTransformersHolder = EasyMock.createMock(TransformersHolder.class);
        this.productServiceFactoryWrapper = EasyMock.createNiceMock(ProductServiceFactoryWrapper.class);
        EasyMock.expect(this.categoryTransformersHolder.getRequestTransformer()).andReturn(this.coreCategoryToCategoryTaxonomy).times(2);
        EasyMock.expect(this.categoryTransformersHolder.getResponseTransformer()).andReturn(this.categoryNodeToCategoryInfo).times(1);
        EasyMock.expect(this.coreCategoryToCategoryTaxonomy.transform(coreCategoryTaxonomyRequestMock, null, null)).andReturn(categoryTaxonomyRequestMock).times(1);
        EasyMock.expect(this.productServiceFactoryWrapper.getCategoryServiceClient()).andReturn(categoryServiceClientMock).times(1);
        EasyMock.expect(categoryServiceClientMock.taxonomy(isA(CategoryTaxonomyRequest.class))).andReturn(null).times(1);
        EasyMock.replay(this.categoryTransformersHolder);
        EasyMock.replay(this.coreCategoryToCategoryTaxonomy);
        EasyMock.replay(this.productServiceFactoryWrapper);
        EasyMock.replay(categoryServiceClientMock);
        this.categoryServiceV3 =  new CategoryServiceV3(this.productServiceFactoryWrapper,this.categoryTransformersHolder);
        assertNull(this.categoryServiceV3.queryCategoryTaxonomyByLocale(coreCategoryTaxonomyRequestMock));
        EasyMock.verify(this.categoryTransformersHolder);
        EasyMock.verify(this.coreCategoryToCategoryTaxonomy);
        EasyMock.verify(this.productServiceFactoryWrapper);
        EasyMock.reset(coreCategoryToCategoryTaxonomy, categoryNodeToCategoryInfo, productServiceFactoryWrapper,
            categoryTransformersHolder);

    }
}
