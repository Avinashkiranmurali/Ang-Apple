package com.b2s.apple.services;

import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.security.saml.service.UserSessionService;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by ssrinivasan on 9/25/2015.
 */
@Service
public class SAMLSessionService implements UserSessionService {
    private static final String SAML_REQUEST_ID = "SAML_REQUEST_ID";

    @Override
    public boolean isSameSession() {
        final RequestAttributes requestAttributes= RequestContextHolder.getRequestAttributes();
        final HttpServletRequest httpServletRequest= ((ServletRequestAttributes) requestAttributes).getRequest();
        final HttpSession session= httpServletRequest.getSession();
        return session.getAttribute(CommonConstants.USER_SESSION_OBJECT)!=null || httpServletRequest.getRequestURI().contains("/service/");
    }

    @Override
    public void setSAMLRequestId(final String samlRequestId) {
        final RequestAttributes requestAttributes= RequestContextHolder.getRequestAttributes();
        final HttpServletRequest httpServletRequest= ((ServletRequestAttributes) requestAttributes).getRequest();
        final HttpSession session= httpServletRequest.getSession();
        session.setAttribute(SAML_REQUEST_ID,samlRequestId);
    }

    @Override
    public String getSAMLRequestId() {
        final RequestAttributes requestAttributes= RequestContextHolder.getRequestAttributes();
        final HttpServletRequest httpServletRequest= ((ServletRequestAttributes) requestAttributes).getRequest();
        final HttpSession session= httpServletRequest.getSession();
        return (String)session.getAttribute(SAML_REQUEST_ID);
    }
}
