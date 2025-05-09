package com.b2s.common.services.requests.productservice;

import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.common.services.requests.ClientRequest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * This class encapsulates multiple product detail requests
 * information
  @author sjonnalagadda
  * Date: 7/29/13
  * Time: 11:45 AM
 *
 */
public class MultiProductDetailRequest implements ClientRequest {
    private  final Map<String, CoreProductDetailRequest> productDetailRequests = new HashMap<>();
    private Optional<Locale> userLanguage = Optional.empty();
    private Optional<String> varId = Optional.empty();
    private Optional<String> programId = Optional.empty();
    private Optional<String> pricingTier = Optional.empty();
    private Optional< String > catalogId;
    private Optional< String > partnerCode;


    public MultiProductDetailRequest(final Optional<Locale> userLanguage) {
        this.userLanguage =  userLanguage;
    }

    public MultiProductDetailRequest(final Optional<Locale> userLanguage, final Program program, final String partnerCode) {
        this.userLanguage = userLanguage;
        Object defaultVarIdObj = program.getConfig().get(CommonConstants.DEFAULT_PS_VAR);
        this.varId = Optional.ofNullable((defaultVarIdObj != null && org.apache.commons.lang.StringUtils.isNotBlank(defaultVarIdObj.toString())) ? defaultVarIdObj.toString() : program.getVarId());
        Object defaultProgramIdObj = program.getConfig().get(CommonConstants.DEFAULT_PS_PROGRAM);
        this.programId = Optional.ofNullable((defaultProgramIdObj != null && org.apache.commons.lang.StringUtils.isNotBlank(defaultProgramIdObj.toString())) ? defaultProgramIdObj.toString() : program.getProgramId());
        this.catalogId = Optional.ofNullable(program.getConfig().get(CommonConstants.CONFIG_CATALOG_ID).toString());
        this.pricingTier = Optional.ofNullable(program.getPricingTier());
        this.partnerCode = Optional.ofNullable(partnerCode);
    }

    /**
     * Ties PSID of <code>CoreProductDetailRequest</code> from input to input using <code>Map</code>
     * @param productDetailRequest  holds product detail query information
     * @throws IllegalArgumentException when input is null
     */
    public void withProductDetailRequest(final CoreProductDetailRequest productDetailRequest){
        if(!Optional.ofNullable(productDetailRequest).isPresent()){
            throw new IllegalArgumentException("Product detail request is not present");
        }
        productDetailRequests.put(productDetailRequest.getPsid(),productDetailRequest);
    }

    /**
     * @return <code>Map</code> of <code>ProductDetailRequest</code> with key as PSID used to find product details.
     */

    public Map<String, CoreProductDetailRequest> getProductDetailRequests() {
        return productDetailRequests;
    }

    @Override
    public Optional<Locale> getUserLanguage() {
        return userLanguage;
    }

    public Optional<String> getVarId() {
        return varId;
    }

    public Optional<String> getProgramId() {
        return programId;
    }

    public Optional<String> getPricingTier() {
        return pricingTier;
    }

    public Optional< String > getCatalogId() {
        return catalogId;
    }

    public Optional<String> getPartnerCode() {
        return partnerCode;
    }
}
