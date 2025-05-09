package com.b2s.apple.mapper;

import com.b2s.apple.entity.AMPProductConfigEntity;
import com.b2s.rewards.apple.model.AMPConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by ssundaramoorthy on 7/8/2021.
 */
@Component
public class AMPMapper {

    public List<AMPConfig> getProgramConfigs(final List<AMPProductConfigEntity> ampProductConfigs) {
        return ampProductConfigs
            .stream()
            .filter(Objects::nonNull)
            .map(ampConfigEntity -> getAmpConfig(ampConfigEntity))
            .collect(Collectors.toList());
    }

    protected AMPConfig getAmpConfig(final AMPProductConfigEntity ampConfigEntity) {
        return AMPConfig.builder()
            .withCategory(ampConfigEntity.getCategory())
            .withItemId(ampConfigEntity.getItemId())
            .withUseStaticLink(ampConfigEntity.getUseStaticLink())
            .withUpdateDate(ampConfigEntity.getUpdateDate())
            .withUpdatedBy(ampConfigEntity.getUpdatedBy())
            .withDuration(ampConfigEntity.getDuration())
            .build();
    }
}