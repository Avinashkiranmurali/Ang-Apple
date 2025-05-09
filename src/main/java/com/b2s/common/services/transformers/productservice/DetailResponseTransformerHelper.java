package com.b2s.common.services.transformers.productservice;

import com.b2s.common.services.transformers.Helper;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * Used to hold data from request needed for transforming detail response.
 @author sjonnalagadda
  * Date: 08/06/13
  * Time: 03:14 PM
 *
 */
public class DetailResponseTransformerHelper implements Helper {
   private final Optional<String> productType;
   private final Set<String> psidsFromDetailRequests;
   private final Optional<Locale> userLanguage;

   public DetailResponseTransformerHelper(final Optional<String> productType, final Set<String> psidsFromDetailRequests,
                                    final Optional<Locale> userLanguage) {
        this.productType = productType;
        this.psidsFromDetailRequests = psidsFromDetailRequests;
        this.userLanguage =  userLanguage;

   }

    public Optional<String> getProductType() {
        return productType;
    }

    public Set<String> getPsidsFromDetailRequests() {
        return psidsFromDetailRequests;
    }

    @Override
    public Optional<Locale> getUserLanguage() {
        return this.userLanguage;
    }

}
