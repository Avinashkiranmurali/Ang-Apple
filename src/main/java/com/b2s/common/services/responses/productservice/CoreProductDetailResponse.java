package com.b2s.common.services.responses.productservice;

import com.b2s.rewards.model.Product;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * <p>
 * This class encapsulates multiple product detail responses
 * information.
 @author sjonnalagadda
  * Date: 7/29/13
  * Time: 05:28 PM
 *
 */
public class CoreProductDetailResponse {

    private final Map<String, Product> detailResponseByPsid = new HashMap<>();
    private Map<String, Throwable> failures = new HashMap<>();

    /**
     * Adds product detail responses identified by PSID
     * @param psid product unique identifier
     * @param product product detail information
     * @throws IllegalArgumentException when input PSID is null, input product is null, trying to add product that already exist for a given PSID
     */

    public void withProductDetailResponse(final String psid, final Product product){
        if(Optional.ofNullable(psid).isEmpty() || psid.trim().isEmpty()){
            throw new IllegalArgumentException("Product PSID is not present");
        }
        if(Optional.ofNullable(product).isEmpty()){
            throw new IllegalArgumentException("Product information is not present");
        }
        if(Optional.ofNullable(this.detailResponseByPsid.get(psid)).isPresent()){
            throw new IllegalArgumentException("Product with PSID already exist");
        }
        detailResponseByPsid.put(psid,product);
    }

    /**
     * When product detail repository is empty (or) product does not exist in repository, then returns <code>Optional<Product>.absent</code>
     * When product detail is present in the repository then returns product detail information
     * @param psid product unique identifier
     * @return Product detail information
     */

    public Optional<Product> getProductDetailByPsid(final String psid) {
        return Optional.ofNullable(this.detailResponseByPsid.get(psid)) ;
    }

    /**
     * Returns list of product with detailed information
     * @return List of products with detailed information
     */

    public List<Product> getAllProductsDetail() {
        return new ImmutableList.Builder<Product>().addAll(detailResponseByPsid.values().iterator()).build() ;
    }

    public Map<String, Throwable> getFailures() {
        return failures;
    }

    public void setFailures(Map<String, Throwable> failures) {
        this.failures = failures;
    }
}
