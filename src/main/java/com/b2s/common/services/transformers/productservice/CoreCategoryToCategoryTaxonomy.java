package com.b2s.common.services.transformers.productservice;

import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.requests.productservice.CoreCategoryTaxonomyRequest;
import com.b2s.common.services.transformers.Helper;
import com.b2s.common.services.transformers.Transformer;
import com.b2s.service.product.client.application.categories.CategoryTaxonomyRequest;
import com.b2s.service.product.client.common.CatalogRequestContext;
import com.b2s.service.product.client.domain.Audience;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Optional;

/**
 * <p>
 * Used to transform Core category request to product service request.
 @author sjonnalagadda
  * Date: 7/11/13
  * Time: 4:13 PM
 *
 */
public class CoreCategoryToCategoryTaxonomy implements Transformer<CoreCategoryTaxonomyRequest,CategoryTaxonomyRequest> {

    @Override
    public CategoryTaxonomyRequest transform(final CoreCategoryTaxonomyRequest coreCategoryTaxonomyRequest, final Helper helper, final Program program){

        if(!Optional.ofNullable(coreCategoryTaxonomyRequest).isPresent()
                || !coreCategoryTaxonomyRequest.getUserLanguage().isPresent()){
            throw new IllegalArgumentException(ServiceExceptionEnums.LOCALE_INFORMATION_ABSENT.getErrorMessage());
        }
        final CatalogRequestContext.Builder builder = CatalogRequestContext.builder();
        builder.withCatalogId(coreCategoryTaxonomyRequest.getDefaultCatalogId());
        String varId = CommonConstants.DEFAULT_VAR_ID;
        String programId = CommonConstants.DEFAULT_PROGRAM_ID;
        if(StringUtils.isNotBlank(coreCategoryTaxonomyRequest.getVarId())) {
            varId = coreCategoryTaxonomyRequest.getVarId();
        }
        if(StringUtils.isNotBlank(coreCategoryTaxonomyRequest.getProgramId())) {
            programId = coreCategoryTaxonomyRequest.getProgramId();
        }
        final Locale locale = coreCategoryTaxonomyRequest.getUserLanguage().orElse(Locale.getDefault());
        final Audience audience = Audience.builder()
            .withVarIdAndProgramId(varId, programId)
            .withCountryCode(locale.getCountry())
            .build();
        builder.withAudience(audience);

        final CatalogRequestContext  requestContext =  builder.build();

        final CategoryTaxonomyRequest.Builder   categoryTaxonomyRequestBuilder =  CategoryTaxonomyRequest.builder();
        if(StringUtils.isNotBlank(coreCategoryTaxonomyRequest.getShopName())) {
            categoryTaxonomyRequestBuilder.withPromoTag(coreCategoryTaxonomyRequest.getShopName());
        }
        categoryTaxonomyRequestBuilder.withRequestContext(requestContext);
        if(coreCategoryTaxonomyRequest.isOnlyWithProducts()) {
            categoryTaxonomyRequestBuilder.withOnlyWithProducts();
        } else{
            categoryTaxonomyRequestBuilder.removeOnlyWithProducts();
        }
        return  categoryTaxonomyRequestBuilder.build();
    }
}
