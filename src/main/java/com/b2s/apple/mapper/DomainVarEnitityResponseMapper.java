package com.b2s.apple.mapper;

import com.b2s.rewards.apple.model.DomainVarMapping;
import com.b2s.rewards.apple.model.order.DomainVarEnitityResponse;
import org.springframework.stereotype.Component;

/**
 * Created by sjayaraman on 1/31/2019.
 */
@Component
public class DomainVarEnitityResponseMapper {

    public DomainVarEnitityResponse from(final DomainVarMapping domainVarMapping){
        final DomainVarEnitityResponse.Builder domainVarEnitityResponse = DomainVarEnitityResponse.builder();
        domainVarEnitityResponse.withId(domainVarMapping.getId());
        domainVarEnitityResponse.withDomain(domainVarMapping.getDomain());
        domainVarEnitityResponse.withVarId(domainVarMapping.getVarId());
        domainVarEnitityResponse.withProgramId(domainVarMapping.getProgramId());
        domainVarEnitityResponse.withCreatedDate(domainVarMapping.getCreatedDate());
        domainVarEnitityResponse.withIsActive(domainVarMapping.getIsActive());
        domainVarEnitityResponse.withLoginType(domainVarMapping.getLoginType());
        domainVarEnitityResponse.withUserId(domainVarMapping.getUserId());
        domainVarEnitityResponse.withEmail(domainVarMapping.getEmail());
        domainVarEnitityResponse.withDisplayTermsOfUse(domainVarMapping.isDisplayTermsOfUse());
        domainVarEnitityResponse.withEmployeeGroupDetails(domainVarMapping.getEmployeeGroupDetails());
        return domainVarEnitityResponse.build();
    }
}
