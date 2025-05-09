package com.b2s.rewards.security.controller;

import com.b2s.rewards.security.saml.AbstractSAMLHandler;
import com.b2s.rewards.security.util.SessionUtil;
import com.b2s.security.saml.EpsilonSAMLResponseException;
import com.b2s.security.saml.SAMLException;
import com.b2s.security.saml.Throw;
import com.b2s.security.saml.servlet.SAMLAuthenticatedRequestWrapper;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * The type Custom saml controller.
 */
@Controller("citiSAMLHandler")
public class CustomSAMLController {
    private static final Logger LOG = LoggerFactory.getLogger(CustomSAMLController.class);

    private String successView = "/login.do";

    @Autowired
    @Qualifier("epsilonSAMLHandler")
    private AbstractSAMLHandler samlHandler;

    @Autowired
    @Qualifier("varConfigMap")
    Map<String,String> varConfigMap;


    /**
     * Sets success view.
     *
     * @param successView the success view
     */
    public void setSuccessView(String successView) {
        this.successView = successView;
    }


    /**
     * Gets saml from epsilon.
     * Note: Even though this looks generic to allow a new client please ensure to add the web.xml entry.
     *
     * @param httpServletRequest  the http servlet request
     * @param httpServletResponse the http servlet response
     * @param client              the client
     * @throws SAMLException                the saml exception
     * @throws EpsilonSAMLResponseException the epsilon saml response exception
     * @throws ServletException             the servlet exception
     * @throws IOException                  the io exception
     */
    @RequestMapping(value = "/{client}/SAML/POST", method = RequestMethod.POST)
    public void getSAMLFromEpsilon(final HttpServletRequest httpServletRequest,
                                   final HttpServletResponse httpServletResponse,
                                   @PathVariable final String client) throws SAMLException,
            EpsilonSAMLResponseException, ServletException, IOException {

        try {
            Throw.when("Custom SAML Handler", samlHandler).isNull();
            SessionUtil.restartSession(httpServletRequest);
            final Response response = samlHandler.handleSAMLResponse(httpServletRequest);

            LOG.info("Retrieving ClientMap using AnnotationConfigApplicationContext");
            String varOrderHandle = varConfigMap.get(client);

            final SAMLMessageContext samlMessageContext = new BasicSAMLMessageContext<>();
            samlMessageContext.setInboundSAMLMessage(response);
            final SAMLAuthenticatedRequestWrapper samlAuthenticatedRequestWrapper =
                    new SAMLAuthenticatedRequestWrapper(httpServletRequest, varOrderHandle, samlMessageContext);

            httpServletRequest.getRequestDispatcher(successView).forward(samlAuthenticatedRequestWrapper,
                    httpServletResponse);
        } catch (final SAMLException ex) {
            LOG.error("Error processing SAML for {} with message {}", client , ex.getMessage());
            throw ex;
        } catch (final Exception ex) {
            LOG.error("Unknown error while login in {}", ex.getMessage());
            throw ex;
        }
    }


    /**
     * Sets saml handler.
     *
     * @param samlHandler the saml handler
     */
    public void setSamlHandler(AbstractSAMLHandler samlHandler) {
        this.samlHandler = samlHandler;
    }

}