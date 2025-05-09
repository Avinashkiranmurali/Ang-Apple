package com.b2s.common;

import com.b2s.common.services.exception.ServiceException;
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

/**
 *  This is unit test class for <code>com.b2s.common.CategoryInfo</code>
 @author sjonnalagadda
  * Date: 8/28/13
  * Time: 12:55 PM

 */

@RunWith(JUnit4.class)
public class CategoryInfoTest {
    private CategoryInfo categoryInfo;

    @Before
    public void setup() throws ServiceException {

    }

    @After
    public void tearDown(){
        this.categoryInfo = null;
    }

    @Test
    public void testWhenFirstInputIsNullForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo(null,"Toys", Locale.US, "playitems/toys","Play items",2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenFirstInputIsEmptyForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo("","Toys", Locale.US, "playitems/toys","Play items",2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenSecondInputIsNullForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo(null,"Toys", Locale.US, "playitems/toys","Play items",2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenSecondInputIsEmptyForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo("","Toys", Locale.US, "playitems/toys","Play items",2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenThirdInputIsNullForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo("toys",null, Locale.US, "playitems/toys","Play items",2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenThirdInputIsEmptyForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo("toys","", Locale.US, "playitems/toys","Play items",2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }


    @Test
    public void testWhenForthInputIsNullForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo("toys","Toys", null, "playitems/toys","Play items",2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenFourthInputIsInvalidLocaleForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo("toys","Toys", new Locale("xy","xyz"), "playitems/toys","Play items",2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenFifthInputIsNullForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo("toys",null, Locale.US, null,"Play items",2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenFifthInputIsEmptyForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo("toys","", Locale.US, "","Play items",2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenSixthInputIsNullForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo("toys",null, Locale.US, "playitems/toys",null,2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenSixthInputIsEmptyForConstructor(){
        try{
            this.categoryInfo = new CategoryInfo("toys",null, Locale.US, "playitems/toys","",2);
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenAllInputsArePresent(){
        this.categoryInfo = new CategoryInfo("toys","Toys", Locale.US, "playitems/toys","Play items",2);
        assertEquals("No matching localized name found", Optional.ofNullable("Toys"),this.categoryInfo.getLocalizedName(Locale.US));
    }

    @Test
    public void testWhenRetrievingNameForInvalidLocale(){
        try{
            this.categoryInfo = new CategoryInfo("toys","Toys", Locale.US, "playitems/toys","Play items",2);
            this.categoryInfo.getLocalizedName( new Locale("xy","xyz"));
            throw new AssertionError("Flow should not reach this line");
        }catch (IllegalArgumentException iae){
            assertThat("Exception not matching",iae, new IsInstanceOf(IllegalArgumentException.class));
        }
    }

    @Test
    public void testWhenNameNotPresentForLocale(){
        this.categoryInfo = new CategoryInfo("toys","Toys", Locale.US, "playitems/toys","Play items",2);
        assertEquals("No matching localized name found", Optional.empty(),this.categoryInfo.getLocalizedName(Locale.CANADA_FRENCH));
    }

    @Test
    public void testAddingNamesForDifferentLocales(){
        this.categoryInfo = new CategoryInfo("toys","Toys", Locale.US, "playitems/toys","Play items",2);
        this.categoryInfo.addLocalizedNameForThisLocale("Tays", Locale.CANADA_FRENCH);
        assertEquals("No matching localized name found", Optional.ofNullable("Tays"), this.categoryInfo.getLocalizedName(Locale.CANADA_FRENCH));
        assertEquals("No matching localized name found",Optional.ofNullable("Toys"),this.categoryInfo.getLocalizedName(Locale.US));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhenHierarchyFromRootNodeInputEmpty() {
        this.categoryInfo = new CategoryInfo("toys","Toys", Locale.US, "","Play items",2);
        this.categoryInfo.addLocalizedNameForThisLocale("Tays", Locale.CANADA_FRENCH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhenRootCategorySlugInputEmpty() {
        this.categoryInfo = new CategoryInfo("toys","Toys", Locale.US, "playitems/toys","",2);
        this.categoryInfo.addLocalizedNameForThisLocale("Tays", Locale.CANADA_FRENCH);
    }
}
