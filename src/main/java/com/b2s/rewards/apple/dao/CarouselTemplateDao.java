package com.b2s.rewards.apple.dao;

import com.b2s.apple.entity.CarouselTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CarouselTemplateDao extends JpaRepository<CarouselTemplateEntity, Long> {

    List<CarouselTemplateEntity> findByTemplateNameAndIsActive(final String templateName, final boolean isActive);

    default List<CarouselTemplateEntity> getCarouselTemplates(final String templateName) {
        return findByTemplateNameAndIsActive(templateName, true);
    }
}
