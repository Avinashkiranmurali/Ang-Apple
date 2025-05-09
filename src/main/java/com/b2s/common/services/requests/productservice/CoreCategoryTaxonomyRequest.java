package com.b2s.common.services.requests.productservice;

import com.b2s.common.services.exception.ServiceException;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.requests.ClientRequest;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;

/**
 * <p>
 * This class is used to by core platform to find categories in given locale.
 *
 @author sjonnalagadda
  * Date: 8/23/13
  * Time: 11:38 AM
 *
 */
public class CoreCategoryTaxonomyRequest implements ClientRequest {
    private final Locale categoryUserLanguage;

    private final String defaultCatalogId;
    private final boolean onlyWithProducts;

    private String varId;

    private String programId;

    private String shopName;

    public boolean isOnlyWithProducts() {
        return onlyWithProducts;
    }

    /**

     * Constructs CoreCategoryTaxonomyRequest from <code>Locale</code>
     *
     * @param categoryUserLanguage locale information.
     */

    public  CoreCategoryTaxonomyRequest(final Locale categoryUserLanguage, final String defaultCatalogId) throws ServiceException{
        this(categoryUserLanguage,defaultCatalogId,false);
    }

        /**

         * Constructs CoreCategoryTaxonomyRequest from <code>Locale</code>
         *
         * @param categoryUserLanguage locale information.
         */

    public  CoreCategoryTaxonomyRequest(final Locale categoryUserLanguage, final String defaultCatalogId, final boolean onlyWithProducts) throws ServiceException{
        if(!Optional.ofNullable(categoryUserLanguage).isPresent()){
            throw new ServiceException(ServiceExceptionEnums.LOCALE_INFORMATION_ABSENT);
        }
        try{
            categoryUserLanguage.getISO3Language();
        }catch(MissingResourceException mre){
            throw new ServiceException(ServiceExceptionEnums.LOCALE_INFORMATION_ABSENT,mre);
        }
        this.categoryUserLanguage =  categoryUserLanguage;
        this.defaultCatalogId = defaultCatalogId;
        this.onlyWithProducts = onlyWithProducts;
    }

    public String getISO3LanguageCode() {
        return categoryUserLanguage.getISO3Language();
    }


    @Override
    public Optional<Locale> getUserLanguage() {
        return Optional.ofNullable(categoryUserLanguage);
    }

    public String getDefaultCatalogId() {
        return defaultCatalogId;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
}
