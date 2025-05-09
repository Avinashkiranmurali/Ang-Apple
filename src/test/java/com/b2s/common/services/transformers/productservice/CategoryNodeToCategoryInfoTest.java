package com.b2s.common.services.transformers.productservice;


import com.b2s.common.CategoryInfo;
import com.b2s.common.services.transformers.Helper;
import com.b2s.service.product.client.domain.CategoryNode;
import org.easymock.EasyMock;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Locale;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *  This is unit test class for <code>com.b2s.common.services.requests.productservice.CategoryNodeToCategoryInfo</code>
 @author sjonnalagadda
  * Date: 08/29/13
  * Time: 11:41 AM
 */

@RunWith(JUnit4.class)
public class CategoryNodeToCategoryInfoTest {

    private CategoryNodeToCategoryInfo categoryNodeToCategoryInfo;

    @Before
    public void setup(){
        categoryNodeToCategoryInfo = new CategoryNodeToCategoryInfo();
    }

    @After
    public void tearDown(){
        categoryNodeToCategoryInfo = null;
    }

    @Test
    public void testWhenFirstArgumentForTransformIsNull(){
        try{
            categoryNodeToCategoryInfo.transform(null, new LocaleHelper(Optional.ofNullable(Locale.CANADA_FRENCH)), null);
            throw new AssertionError("Should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching", iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenSecondArgumentForTransformIsNull(){
        final CategoryNode categoryNodeMock = EasyMock.createMock(CategoryNode.class);
        try{
            categoryNodeToCategoryInfo.transform(categoryNodeMock, null, null);
            throw new AssertionError("Should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching", iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenHelperObjectIsNotMatching(){
        final CategoryNode categoryNodeMock = EasyMock.createMock(CategoryNode.class);
        try{
            final Helper helper = new Helper(){
                @Override
                public Optional<Locale> getUserLanguage(){
                    return Optional.ofNullable(Locale.CANADA_FRENCH);
                }
            };
            categoryNodeToCategoryInfo.transform(categoryNodeMock,helper, null);
            throw new AssertionError("Should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching", iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testTransformation(){
        final CategoryNode categoryNodeMock = EasyMock.createMock(CategoryNode.class);
        final LocaleHelper localeHelperMock = EasyMock.createMock(LocaleHelper.class);
        EasyMock.expect(categoryNodeMock.getName()).andReturn("Toys").times(1);
        EasyMock.expect(categoryNodeMock.getSlug()).andReturn("toys").times(1);
        EasyMock.expect(categoryNodeMock.getDepth()).andReturn(2).times(1);
        EasyMock.expect(localeHelperMock.getUserLanguage()).andReturn(Optional.ofNullable(Locale.CANADA_FRENCH)).times(1);
        EasyMock.expect(localeHelperMock.getHierarchyFromRootNode()).andReturn("/children/play/toys").times(1);
        EasyMock.expect(localeHelperMock.getRootCategorySlug()).andReturn("children").times(1);
        EasyMock.replay(categoryNodeMock);
        EasyMock.replay(localeHelperMock);
        final CategoryInfo categoryInfo = categoryNodeToCategoryInfo.transform(categoryNodeMock,localeHelperMock, null);

        Optional<String> localizedName = categoryInfo.getLocalizedName(Locale.CANADA_FRENCH);
        assertTrue(localizedName.isPresent());
        assertEquals("Not Matching", Optional.ofNullable("Toys").get(),localizedName.get());
        EasyMock.verify(categoryNodeMock,localeHelperMock);

    }



}
