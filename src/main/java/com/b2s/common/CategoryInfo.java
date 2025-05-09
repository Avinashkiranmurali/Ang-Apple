package com.b2s.common;

import com.b2s.common.services.exception.ServiceExceptionEnums;

import java.util.*;

/**
 * <p>
 * Used to transform Core category request to product service request.
 @author sjonnalagadda
  * Date: 8/23/13
  * Time: 2:09 PM
 *
 */

public class CategoryInfo {

    private final String slug;
    private final String hierarchyFromRootNode;
    private final String rootCategorySlug;
    private final Integer depth;
    private final Map<String, String> localizedNameMapper = new HashMap<>(2);

    /**
     * Constructs a CategoryInfo from browseNodeId, slug, category display name and locale for name.
     * @param slug unique identifier for a category.
     * @param name display name of category.
     * @param nameLocale locale associated with name.
     * @param hierarchyFromRootNode used to denote the tree structure this category belongs to.
     * @param rootCategorySlug slug of root category used as pricing category code for image resizing.
     * @throws IllegalArgumentException if any of the arguments is null (or) locale is not recognized.
     */

    public CategoryInfo(final String slug, final String name,
                        final Locale nameLocale, final String hierarchyFromRootNode, final String rootCategorySlug,
        final Integer depth){
        validateSlug(slug);
        validateName(name);
        validateLocale(nameLocale);
        validateHierarchyFromRootNode(hierarchyFromRootNode);
        validateRootCategorySlug(rootCategorySlug);
        this.slug = slug;
        this.hierarchyFromRootNode = hierarchyFromRootNode;
        this.rootCategorySlug = rootCategorySlug;
        this.depth = depth;
        localizedNameMapper.put(nameLocale.getISO3Language(), name);
    }

    /**
     * @param slugInput unique identifier for a category.
     * @throws IllegalArgumentException if input is null (or) empty.
     */

    private void validateSlug(final String slugInput) {
        if(Optional.ofNullable(slugInput).isEmpty() || slugInput.trim().intern().isEmpty()){
            throw new IllegalArgumentException(ServiceExceptionEnums.SLUG_ABSENT.getErrorMessage());
        }
    }


    /**
     * @param nameInput name for a category.
     * @throws IllegalArgumentException if input is null (or) empty.
     */

    private void validateName(final String nameInput) {
        if(Optional.ofNullable(nameInput).isEmpty() || nameInput.trim().intern().isEmpty()){
            throw new IllegalArgumentException(ServiceExceptionEnums.CATEGORY_NAME_ABSENT.getErrorMessage());
        }
    }

    /**
     * @param nameLocale locale associated with name.
     * @throws IllegalArgumentException if input is null (or) unable to get locale.
     */

    private void validateLocale(final Locale nameLocale) {
        if(Optional.ofNullable(nameLocale).isEmpty() ){
            throw new IllegalArgumentException(ServiceExceptionEnums.LOCALE_INFORMATION_ABSENT.getErrorMessage());
        }
        try{
            nameLocale.getISO3Language();
        }catch(MissingResourceException mre){
            throw new IllegalArgumentException(ServiceExceptionEnums.LOCALE_INFORMATION_ABSENT.getErrorMessage(), mre);
        }
    }

    /**
     *
     * @param hierarchyFromRootNodeInput
     *  @throws IllegalArgumentException if input is null (or) empty.
     */

    private void validateHierarchyFromRootNode(final String hierarchyFromRootNodeInput){
        if(Optional.ofNullable(hierarchyFromRootNodeInput).isEmpty() || hierarchyFromRootNodeInput.trim().intern().isEmpty()){
            throw new IllegalArgumentException(ServiceExceptionEnums.HIERARCHY_FROM_ROOT_NODE_ABSENT.getErrorMessage());
        }
    }


    /**
     *
     * @param rootCategorySlugInput
     *  @throws IllegalArgumentException if input is null (or) empty.
     */

    private void validateRootCategorySlug(final String rootCategorySlugInput){
        if(Optional.ofNullable(rootCategorySlugInput).isEmpty() || rootCategorySlugInput.trim().intern().isEmpty()){
            throw new IllegalArgumentException(ServiceExceptionEnums.ROOT_CATEGORY_NAME_IS_ABSENT.getErrorMessage());
        }
    }


    /**
     * Adds localized name.
     * @param name display name of category.
     * @param nameLocale locale associated with name.
     * @throws IllegalArgumentException if any of the arguments is null (or) locale is not recognized.
     */
    public void addLocalizedNameForThisLocale(final String name, final Locale nameLocale){
        validateName(name);
        validateLocale(nameLocale);
        localizedNameMapper.put(nameLocale.getISO3Language(), name);
    }

    /**
     * Retrieves localized name of category.  If not found for a given locale (or) then returns <code>Optional.absent</code>
     * @param nameLocale locale to retrieve name.
     * @throws  IllegalArgumentException when locale is null (or) invalid locale.
     */

    public Optional<String> getLocalizedName(final Locale nameLocale){
        validateLocale(nameLocale);
        if(Optional.ofNullable(localizedNameMapper.get(nameLocale.getISO3Language())).isPresent()){
          return Optional.ofNullable(localizedNameMapper.get(nameLocale.getISO3Language()).intern());
        }
        return Optional.empty();
    }

    /**
     * @return  slug associated with Category
     */

    public String getSlug() {
        return slug;
    }

    public String getHierarchyFromRootNode() {
        return hierarchyFromRootNode;
    }

    public String getRootCategorySlug() {
        return rootCategorySlug;
    }

    public Integer getDepth() {
        return depth;
    }

    @Override
    public boolean equals(final Object aObject) {
        if (this == aObject) {
            return true;
        }
        if (!(aObject instanceof CategoryInfo)) {
            return false;
        }
        final CategoryInfo otherCategoryInfo = (CategoryInfo) aObject;
        if (hierarchyFromRootNode != null ? !hierarchyFromRootNode.equals(otherCategoryInfo.hierarchyFromRootNode) :otherCategoryInfo.hierarchyFromRootNode != null){
            return false;
        }
        if (slug != null ? !slug.equals(otherCategoryInfo.slug) :otherCategoryInfo.slug != null){
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return slug != null ? slug.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CategoryInfo{" +
                ", slug='" + slug + '\'' +
                ", hierarchyFromRootNode='" + hierarchyFromRootNode + '\'' +
                ", localizedNameMapper=" + localizedNameMapper +
                '}';
    }
}

