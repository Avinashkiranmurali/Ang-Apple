package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.CarouselEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CarouselDao extends JpaRepository<CarouselEntity, Long> {

    List<CarouselEntity> findByVarIdInAndProgramIdInAndIsActive(final List<String> varIds,
        final List<String> programIds, final boolean isActive);

    default List<CarouselEntity> getActiveCarouselEntities(final List<String> varIds, final List<String> programIds) {
        return findByVarIdInAndProgramIdInAndIsActive(varIds, programIds, true);
    }
}
