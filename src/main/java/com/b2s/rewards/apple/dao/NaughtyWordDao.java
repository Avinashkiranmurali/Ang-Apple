package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.NaughtyWord;
import com.b2s.rewards.dao.BaseDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Created by rperumal on 10/05/2015.
 */
@Repository("naughtyWordDao")
@Transactional
public interface NaughtyWordDao extends BaseDao<NaughtyWord,Long> {

    List<NaughtyWord> getByLocaleOrLanguage(Locale userLocale, String language);
}
