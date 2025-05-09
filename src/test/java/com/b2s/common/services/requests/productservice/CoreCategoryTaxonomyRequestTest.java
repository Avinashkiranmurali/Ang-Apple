package com.b2s.common.services.requests.productservice;

import com.b2s.common.services.exception.ServiceException;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 *  This is unit test class for <code>com.b2s.common.services.transformers.productservice.CoreCategoryTaxonomyRequest</code>
 @author sjonnalagadda
  * Date: 08/23/13
  * Time: 11:54 AM
 */

@RunWith(JUnit4.class)
public class CoreCategoryTaxonomyRequestTest {

    private  CoreCategoryTaxonomyRequest coreCategoryTaxonomyRequest;

    @After
    public void tearDown(){
        coreCategoryTaxonomyRequest = null;
    }

    @Test
    public void testForExistingLocale() throws ServiceException {
        coreCategoryTaxonomyRequest = new CoreCategoryTaxonomyRequest(Locale.US,"rbc");
        assertThat(coreCategoryTaxonomyRequest.getISO3LanguageCode(), new IsInstanceOf(String.class));
    }
}
