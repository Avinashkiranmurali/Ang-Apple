package com.b2s.apple.services;

import com.b2s.apple.entity.QuickLinkEntity;
import com.b2s.apple.mapper.QuickLinkModelMapper;
import com.b2s.rewards.apple.dao.QuickLinkDao;
import com.b2s.rewards.apple.model.QuickLink;
import com.b2s.shop.common.User;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_PROGRAM_KEY;
import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_VAR_PROGRAM;

@Service
public class QuickLinkService {

    @Autowired
    private QuickLinkDao quickLinkDao;

    @Autowired
    private QuickLinkModelMapper quickLinkModelMapper;

    public List<QuickLink> getByVarIdProgramIdLocale(final User user) {
        List<QuickLink> quickLinks = null;
        Map<String,QuickLinkEntity> mapOfentities=new HashMap<>();
        String locale=user.getLocale().toString();

        mapOfentities.putAll(getMapOfQuickLinkEntities(quickLinkDao.getByVarIdProgramIdLocaleLinkCode(DEFAULT_VAR_PROGRAM, DEFAULT_PROGRAM_KEY, locale,user.isAnonymous())));
        mapOfentities.putAll(getMapOfQuickLinkEntities(quickLinkDao.getByVarIdProgramIdLocaleLinkCode(user.getVarId(), DEFAULT_PROGRAM_KEY, locale,user.isAnonymous())));
        mapOfentities.putAll(getMapOfQuickLinkEntities(quickLinkDao.getByVarIdProgramIdLocaleLinkCode(user.getVarId(), user.getProgramId(), locale,user.isAnonymous())));

        Set<QuickLink> set = new HashSet<>(quickLinkModelMapper.getQuickLinks(mapOfentities));
        if (CollectionUtils.isNotEmpty(set)) {
            quickLinks = set.stream()
                    .sorted(Comparator.comparingInt(QuickLink::getOrder))
                    .collect(Collectors.toList());
        }
        return quickLinks;
    }


    private Map<String,QuickLinkEntity> getMapOfQuickLinkEntities(List<QuickLinkEntity> entities){

        return entities.stream().collect(Collectors.toMap(entity -> entity.getQuickLinkId().getLinkCode(),a -> a));
    }
}
