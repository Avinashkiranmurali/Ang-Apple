package com.b2s.common.services.transformers.productservice;

import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.requests.productservice.CoreCategoryTaxonomyRequest;
import com.b2s.service.product.client.application.categories.CategoryTaxonomyRequest;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;

/**
 *  This is unit test class for <code>com.b2s.common.services.requests.productservice.CoreCategoryToCategoryTaxonomy</code>
 @author sjonnalagadda
  * Date: 08/23/13
  * Time: 11:54 AM
 */

@RunWith(JUnit4.class)
public class CoreCategoryToCategoryTaxonomyTest {

    private CoreCategoryToCategoryTaxonomy coreCategoryToCategoryTaxonomy;
    private CoreCategoryTaxonomyRequest    coreCategoryTaxonomyRequest;

    @Before
    public void setupData(){
        coreCategoryToCategoryTaxonomy = new CoreCategoryToCategoryTaxonomy();
    }

    @After
    public void tearDown(){
        coreCategoryToCategoryTaxonomy = null;
        coreCategoryTaxonomyRequest = null;
    }

    @Test
    public void testWhenInputIsNull() throws ServiceException {
        try{
            coreCategoryToCategoryTaxonomy.transform(null, null, null);
            throw new AssertionError("Exception should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat(iae,new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Ignore
    public void testWhenInputLocaleIsFoundInLocaleRepo() throws ServiceException {
            coreCategoryTaxonomyRequest = new CoreCategoryTaxonomyRequest(Locale.CANADA_FRENCH, "rbc");
            final CategoryTaxonomyRequest categoryTaxonomyRequest =coreCategoryToCategoryTaxonomy.transform(coreCategoryTaxonomyRequest, null, null);
            assertThat("Language id is missing",categoryTaxonomyRequest.getRequestContext().getLanguage().get().getCode(), isA(String.class));
    }



}
