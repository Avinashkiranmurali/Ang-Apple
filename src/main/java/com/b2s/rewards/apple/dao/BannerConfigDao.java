package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.BannerConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface BannerConfigDao extends JpaRepository<BannerConfigEntity, Long> {
    List<BannerConfigEntity> findByVarIdInAndProgramIdInAndLocaleInAndIsActiveAndBannerEntityIsActive(final List<String> varIds,
        final List<String> programIds, final List<String> locales, final boolean isActive, final boolean bannerActive);

    default List<BannerConfigEntity> findByVarProgramLocale(final List<String> varIds, final List<String> programIds,
        final List<String> locales) {
        return findByVarIdInAndProgramIdInAndLocaleInAndIsActiveAndBannerEntityIsActive(varIds, programIds, locales, true, true);
    }
}