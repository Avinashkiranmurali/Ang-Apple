package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.VarProgramMessage;
import com.b2s.rewards.dao.BaseDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by rpillai on 2/24/2017.
 */
@Transactional
public interface VarProgramMessageDao extends BaseDao<VarProgramMessage, Long> {

    List<VarProgramMessage> getMessages(String varId, String programId, String locale);

    List<VarProgramMessage> getMessages(List<String> varIds, List<String> programIds, List<String> locales);

    List<VarProgramMessage> getMessages(List<String> varIds, List<String> programIds, List<String> locales, String  codeType);

}
