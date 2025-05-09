package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.VarProgramCatalogConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface VarProgramCatalogConfigDao extends JpaRepository<VarProgramCatalogConfigEntity, Long> {

    List<VarProgramCatalogConfigEntity> findByCatalogIdInAndVarIdInAndProgramIdInAndNameAndActiveInd(
        final List<String> catalogIds, final List<String> varIds, final List<String> programIds, final String name,
        final String activeInd);

    default List<VarProgramCatalogConfigEntity> getVarProgramCatalogConfigNameValue(final List<String> catalogIds,
        final List<String> varIds, final List<String> programIds, final String name) {
        return findByCatalogIdInAndVarIdInAndProgramIdInAndNameAndActiveInd(catalogIds, varIds, programIds, name, "1");
    }

}


