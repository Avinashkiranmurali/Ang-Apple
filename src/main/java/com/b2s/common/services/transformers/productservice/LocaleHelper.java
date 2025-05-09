package com.b2s.common.services.transformers.productservice;

import com.b2s.common.services.transformers.Helper;

import java.util.Locale;
import java.util.Optional;

/**
 * <p>
 * Used to encapsulate locale information needed for transformer.
 @author sjonnalagadda
  * Date: 8/23/13
  * Time: 5:29 PM
 *
 */
public class LocaleHelper implements Helper {
    private final Optional<Locale> userLanguage;
    private String hierarchyFromRootNode;
    private String rootCategorySlug;

    public LocaleHelper(final Optional<Locale> userLanguage) {
        this.userLanguage = userLanguage;
    }

    /**
     *
     * @return <code>Locale</code> for transformation purpose.
     */
    @Override
    public Optional<Locale> getUserLanguage() {
        return userLanguage;
    }

    /**
     *
     * @return hierarchy of node.
     */

    public String getHierarchyFromRootNode() {
        return hierarchyFromRootNode;
    }

    /**
     *
     * @param hierarchyFromRootNode hierarchy of current node.
     */

    public void setHierarchyFromRootNode(final String hierarchyFromRootNode) {
        this.hierarchyFromRootNode = hierarchyFromRootNode;
    }

    public String getRootCategorySlug() {
        return rootCategorySlug;
    }

    public void setRootCategorySlug(final String rootCategorySlug) {
        this.rootCategorySlug = rootCategorySlug;
    }
}
