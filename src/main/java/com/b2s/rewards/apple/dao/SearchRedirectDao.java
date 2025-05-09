package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.SearchRedirectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchRedirectDao extends JpaRepository<SearchRedirectEntity, Integer> {

    List<SearchRedirectEntity> findByCatalogIdInAndVarIdInAndProgramIdInAndSearchKeywordAndActiveIsTrueOrderByCatalogIdDescVarIdDesc(
        final List<String> catalogIds, final List<String> varIds, final List<String> programIds,
        final String searchKeyword);

    default List<SearchRedirectEntity> getAllSearchResults(final List<String> catalogIds, final List<String> varIds,
        final List<String> programIds, final String searchKeyword) {
        return findByCatalogIdInAndVarIdInAndProgramIdInAndSearchKeywordAndActiveIsTrueOrderByCatalogIdDescVarIdDesc(
            catalogIds, varIds, programIds, searchKeyword);
    }
}
