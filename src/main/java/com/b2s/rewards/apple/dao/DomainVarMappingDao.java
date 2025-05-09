package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.DomainVarMapping;
import com.b2s.rewards.common.util.CommonConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/*** Created by srukmagathan on 8/25/2016.
 */
@Repository(value = "OtpDomainVarDao")
@Transactional
public interface DomainVarMappingDao extends JpaRepository<DomainVarMapping,Integer> {

     DomainVarMapping findByDomainAndLoginTypeAndIsActive(final String domain, final String loginType, final String isActive);

     List<DomainVarMapping> findByLoginTypeAndIsActive(final String url, final String isActive);

     default DomainVarMapping findByDomain(final String domain, final String loginType) {
          return findByDomainAndLoginTypeAndIsActive(domain, loginType, CommonConstants.YES_VALUE);
     }

     default boolean isDomainMappingExist(final String domain,final String loginType) {
          DomainVarMapping domainVarMapping = findByDomainAndLoginTypeAndIsActive(domain, loginType, CommonConstants.YES_VALUE);
          return Objects.nonNull(domainVarMapping);
     }

     default List<DomainVarMapping> getDomainByLoginType(final String loginType){
          return findByLoginTypeAndIsActive(loginType, CommonConstants.YES_VALUE);
     }

}
