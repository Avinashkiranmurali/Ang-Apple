package com.b2s.common.services.transformers.productservice;


import com.b2s.common.services.exception.ServiceExceptionEnums;
import com.b2s.common.services.requests.productservice.CoreProductDetailRequest;
import com.b2s.common.services.requests.productservice.MultiProductDetailRequest;
import com.b2s.common.services.transformers.Helper;
import com.b2s.common.services.transformers.Transformer;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.model.SplitTender;
import com.b2s.service.product.client.application.detail.ProductDetailRequest;
import com.b2s.service.product.client.common.CatalogRequestContext;
import com.b2s.service.product.client.domain.Audience;

import java.util.*;

import static com.b2s.rewards.apple.util.AppleUtil.getProgramConfigValueAsBoolean;
import static com.b2s.rewards.apple.util.AppleUtil.getSplitTenderConfig;
import static com.b2s.rewards.common.util.CommonConstants.ENABLE_SMART_PRICING;

/**
 * <p>
 * Used to transform multi product detail request from model to product service detail request.
 * Date: 7/29/13
 * Time: 01:09 PM
 * @author sjonnalagadda
 */
public class MultiProductDetailToProductDetail implements Transformer<MultiProductDetailRequest,Set<ProductDetailRequest>> {

    /**
     *
     * @param from wrapper which holds multiple product detail query information
     * @return Return set of <code>ProductDetailRequest</code>.
     */
    @Override
    public Set<ProductDetailRequest> transform(final MultiProductDetailRequest from, final Helper helper, final Program program){
        if(!Optional.ofNullable(from).isPresent()){
            throw new IllegalArgumentException(ServiceExceptionEnums.PRODUCT_DETAIL_REQUEST_ABSENT.getErrorMessage());
        }
        if(!Optional.ofNullable(from.getProductDetailRequests()).isPresent() ||
                from.getProductDetailRequests().isEmpty()){
            throw new IllegalArgumentException(ServiceExceptionEnums.PRODUCT_DETAIL_REQUEST_ABSENT.getErrorMessage());
        }

        final Optional<Locale> userLanguage = from.getUserLanguage();
        final CatalogRequestContext.Builder  requestContextBuilder = CatalogRequestContext.builder();
        final Optional<String> varId = from.getVarId();

        final Optional<String> programId = from.getProgramId();

        final Optional<String> catalogId = from.getCatalogId();
        final Optional<String> pricingTier = from.getPricingTier();
        final Optional<String> partnerCode = from.getPartnerCode();

        Audience audience = null;
        Audience.Builder  builder = null;

        if(varId.isPresent() && programId.isPresent() && pricingTier.isPresent()) {
            builder = Audience.builder()
                .withVarIdAndProgramId(varId.get(), programId.get())
                .withPricingTier(pricingTier.get());                ;
        } else if (varId.isPresent() && programId.isPresent()) {
            builder = Audience.builder().withVarIdAndProgramId(varId.get(),programId.get());
        } else if (varId.isPresent()) {
            builder = Audience.builder().withVarId(varId.get());
        }

        if (Objects.nonNull(builder)) {
            if(userLanguage.isPresent()) {
                builder.withCountryCode(userLanguage.get().getCountry());
            }
            audience = builder.build();
        }


        requestContextBuilder.withAudience(audience);
        requestContextBuilder.withCatalogId(catalogId.orElse(null));
        requestContextBuilder.withPartnerCode(partnerCode.orElse(null));
        final CatalogRequestContext requestContext = requestContextBuilder.build();

        final Set<ProductDetailRequest> productDetailRequestSet = new HashSet<>();

        for(final String pSid:from.getProductDetailRequests().keySet()){
            final CoreProductDetailRequest coreProductDetailRequest =  from.getProductDetailRequests().get(pSid);

            final String psid = coreProductDetailRequest.getPsid();
            final boolean isNeedRealTimeInfo = coreProductDetailRequest.isNeedRealTimeInfo();

            final ProductDetailRequest.Builder productDetailRequestBuilder = ProductDetailRequest.builder();

            productDetailRequestBuilder.withPsid(psid);
            productDetailRequestBuilder.withRealTimeInfo(isNeedRealTimeInfo);
            productDetailRequestBuilder.withRequestContext(requestContext);
            productDetailRequestBuilder.withTargetCurrencies(coreProductDetailRequest.getTargetCurrencies());
            productDetailRequestBuilder.withVariations(coreProductDetailRequest.isNeedVariationsInfo());

            if (getProgramConfigValueAsBoolean(program, ENABLE_SMART_PRICING)) {//Mathi- Add test cases
                SplitTender splitTender = getSplitTenderConfig(program);
                splitTender.getMaxCashAmount().ifPresent(productDetailRequestBuilder::withMaxCashAmount);
                splitTender.getMaxPointsPercentage().ifPresent(productDetailRequestBuilder::withMaxPointsPercentage);
            }

            final ProductDetailRequest productDetailRequest = productDetailRequestBuilder.build();

            productDetailRequestSet.add(productDetailRequest);
        }
        return productDetailRequestSet;
    }
}
