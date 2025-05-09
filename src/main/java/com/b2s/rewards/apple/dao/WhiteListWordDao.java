package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.WhiteListWord;
import com.b2s.rewards.dao.BaseDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;


@Repository("whiteListWordDao")
@Transactional
public interface WhiteListWordDao extends BaseDao<WhiteListWord,Long> {

    public List<WhiteListWord> getWhitelistWords(Locale userLocale, String language);

}
