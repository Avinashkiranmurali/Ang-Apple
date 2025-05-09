package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.MercSearchFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by rperumal on 10/20/2015.
 */
@Repository("mercSearchFilterDao")
@Transactional
public interface MercSearchFilterDao extends JpaRepository<MercSearchFilter,Long> {

    MercSearchFilter findBySearchFilterId(Integer searchFilterId);
    default MercSearchFilter getById(Integer searchFilterId) {
        return findBySearchFilterId(searchFilterId);
    }

    MercSearchFilter findByVarIdAndProgramIdAndFilterTypeAndFilterNameAndFilterValue(String varId, String programId,
        String filterType, String filterName, String filterValue);
    default MercSearchFilter getByPK(String varId, String programId, String filterType, String filterName,
        String filterValue) {
        return findByVarIdAndProgramIdAndFilterTypeAndFilterNameAndFilterValue(varId, programId, filterType,
            filterName, filterValue);
    }

    default void add(MercSearchFilter filter) {
        saveAndFlush(filter);
    }

    default void remove(MercSearchFilter filter) {
        delete(filter);
    }

}
