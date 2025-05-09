package com.b2s.apple.services;

import com.b2s.apple.entity.SearchRedirectEntity;
import com.b2s.apple.mapper.SearchRedirectModelMapper;
import com.b2s.rewards.apple.dao.SearchRedirectDao;
import com.b2s.rewards.apple.model.SearchRedirect;
import com.b2s.shop.common.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_CATALOG_ID;
import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_PROGRAM_KEY;
import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_VAR_PROGRAM;

@Service
public class SearchRedirectService {

    @Autowired
    private SearchRedirectDao searchRedirectDao;

    @Autowired
    private SearchRedirectModelMapper searchRedirectModelMapper;

    /**
     * Get @SearchRedirect object
     *
     * @param user
     * @param catalogId
     * @param keyword
     * @return searchRedirect object or null
     */
    public SearchRedirect getSearchRedirect(final User user, final String catalogId, final String keyword) {

        final List<String> catalogIds = new ArrayList<>();
        catalogIds.add(catalogId);
        catalogIds.add(DEFAULT_CATALOG_ID);

        final List<String> varIds = new ArrayList<>();
        varIds.add(user.getVarId());
        varIds.add(DEFAULT_VAR_PROGRAM);

        final List<String> programIds = new ArrayList<>();
        programIds.add(user.getProgramId());
        programIds.add(DEFAULT_PROGRAM_KEY);

        final List<SearchRedirectEntity> searchRedirectEntities = searchRedirectDao.getAllSearchResults(catalogIds,
            varIds, programIds, keyword.toLowerCase());

        final SearchRedirect searchRedirect = searchRedirectModelMapper.getSearchRedirect(catalogId, user.getVarId(),
            user.getProgramId(), searchRedirectEntities);

        return searchRedirect;
    }
}
