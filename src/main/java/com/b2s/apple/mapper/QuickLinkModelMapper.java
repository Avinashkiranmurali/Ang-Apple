package com.b2s.apple.mapper;

import com.b2s.apple.entity.QuickLinkEntity;
import com.b2s.rewards.apple.model.QuickLink;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class QuickLinkModelMapper {

    /**
     * Converts @List of @QuickLinkEntity objects to @List of @QuickLink objects
     *
     * @param quickLinkEntities
     * @return
     */
    public List<QuickLink> getQuickLinks(final Map<String, QuickLinkEntity> quickLinkEntities) {
        List<QuickLink> quickLinks = new ArrayList<>();
        if (MapUtils.isNotEmpty(quickLinkEntities)) {

            quickLinks = quickLinkEntities.values().stream()
                    .filter(quickLinkEntity -> quickLinkEntity.isDisplay())
                    .map(this::getQuickLink)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return quickLinks;
    }

    /**
     * Converts @QuickLinkEntity object to @QuickLink object
     *
     * @param quickLinkEntity
     * @return
     */
    private QuickLink getQuickLink(final QuickLinkEntity quickLinkEntity) {
        if (Objects.nonNull(quickLinkEntity)) {
            return QuickLink.builder()
                    .withLocale(quickLinkEntity.getQuickLinkId().getLocale())
                    .withLinkCode(quickLinkEntity.getQuickLinkId().getLinkCode())
                    .withVarId(quickLinkEntity.getQuickLinkId().getVarId())
                    .withProgramId(quickLinkEntity.getQuickLinkId().getProgramId())
                    .withLinkText(quickLinkEntity.getLinkText())
                    .withLinkUrl(quickLinkEntity.getLinkUrl())
                    .withOrder(quickLinkEntity.getPriority())
                    .withDisplay(quickLinkEntity.isDisplay())
                    .withShowUnauthenticated(quickLinkEntity.isShowUnauthenticated()).build();
        }
        return null;
    }

}
