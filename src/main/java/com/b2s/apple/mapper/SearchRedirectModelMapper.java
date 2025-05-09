package com.b2s.apple.mapper;

import com.b2s.apple.entity.SearchRedirectEntity;
import com.b2s.rewards.apple.model.SearchRedirect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class SearchRedirectModelMapper {

    /**
     * Retrieves relatively best match @SearchRedirect object from the List of searchRedirectEntities based on override precedence
     * override precedence ==> higher overrideValue overrides lower overrideValue
     * overrideValue = 3 ==> Specific Catalog, Var and Program Ids
     * overrideValue = 2 ==> Specific Catalog and Var Ids, Generic Program Id
     * overrideValue = 1 ==> Specific Catalog Id, Generic Var and Program Ids
     * overrideValue = 0 ==> Generic Catalog, Var and Program Ids
     *
     * @param catalogId
     * @param varId
     * @param programId
     * @param searchRedirectEntities
     * @return searchRedirect object or null
     */
    public SearchRedirect getSearchRedirect(final String catalogId, final String varId, final String programId,
        final List<SearchRedirectEntity> searchRedirectEntities) {

        SearchRedirectEntity searchRedirect = null;
        int overrideValue = -1;
        for (SearchRedirectEntity searchRedirectEntity : searchRedirectEntities) {
            if (searchRedirectEntity.getCatalogId().equalsIgnoreCase(catalogId)) {
                if (searchRedirectEntity.getVarId().equalsIgnoreCase(varId)) {
                    if (searchRedirectEntity.getProgramId().equalsIgnoreCase(programId)) {
                        //overrideValue = 3;    //highest precedence as Catalog, Var and Program Ids matches
                        return buildSearchRedirect(searchRedirectEntity);
                    } else {
                        overrideValue = 2;  //higher precedence as matching Specific Catalog and Var Ids
                        searchRedirect = searchRedirectEntity;
                    }
                } else if (overrideValue < 1) {
                    overrideValue = 1;  //average precedence as only Catalog Id matches
                    searchRedirect = searchRedirectEntity;
                }
            } else if (overrideValue < 0) {
                overrideValue = 0;      //should be default configuration
                searchRedirect = searchRedirectEntity;
            }
        }

        return Objects.nonNull(searchRedirect) ? buildSearchRedirect(searchRedirect) : null;
    }

    /**
     * Build @SearchRedirect object from @SearchRedirectEntity object
     *
     * @param searchRedirectEntity
     * @return
     */
    public SearchRedirect buildSearchRedirect(final SearchRedirectEntity searchRedirectEntity) {
        if (Objects.nonNull(searchRedirectEntity)) {
            final SearchRedirect.Builder searchRedirectBuilder = SearchRedirect.builder()
                .withVarId(searchRedirectEntity.getVarId())
                .withProgramId(searchRedirectEntity.getProgramId())
                .withCatalogId(searchRedirectEntity.getCatalogId())
                .withSearchKeyword(searchRedirectEntity.getSearchKeyword())
                .withActionType(searchRedirectEntity.getActionType())
                .withValue(searchRedirectEntity.getValue())
                .withActive(searchRedirectEntity.isActive());
            return searchRedirectBuilder.build();
        }
        return null;
    }
}
