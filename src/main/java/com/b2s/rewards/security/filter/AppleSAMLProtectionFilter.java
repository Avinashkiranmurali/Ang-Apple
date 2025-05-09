package com.b2s.rewards.security.filter;

import com.b2s.security.saml.filter.SAMLProtectionFilter;
import com.b2s.security.saml.service.ProtectionDeciderService;
import com.b2s.security.saml.service.SAMLAuthnRequestBuilder;
import com.b2s.security.saml.service.UserSessionService;
import com.b2s.security.saml.util.SAMLObjectFactory;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.ws.message.encoder.MessageEncoder;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import static org.springframework.util.Assert.notNull;

public class AppleSAMLProtectionFilter  implements Filter, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLProtectionFilter.class);

    private ProtectionDeciderService protectionDeciderService;

    private SAMLAuthnRequestBuilder samlAuthnRequestBuilder;

    private SAMLObjectFactory samlObjectFactory;

    private UserSessionService userSessionService;

    private Properties applicationProperties;

    private Properties hostToEntityConfig;

    private MessageEncoder messageEncoder= new HTTPRedirectDeflateEncoder();

    /**
     * Sets the ProtectionDeciderService
     *
     * @param protectionDeciderService The ProtectionDeciderService object
     */
    public void setProtectionDeciderService(final ProtectionDeciderService protectionDeciderService) {
        this.protectionDeciderService = protectionDeciderService;
    }

    /**
     * Sets the SAMLAuthnRequestBuilder
     *
     * @param samlAuthnRequestBuilder The SAMLAuthnRequestBuilder object
     */
    public void setSamlAuthnRequestBuilder(final SAMLAuthnRequestBuilder samlAuthnRequestBuilder) {
        this.samlAuthnRequestBuilder = samlAuthnRequestBuilder;
    }

    /**
     * Sets the SAMLObjectFactory
     *
     * @param samlObjectFactory The SAMLObjectFactory object
     */
    public void setSamlObjectFactory(final SAMLObjectFactory samlObjectFactory) {
        this.samlObjectFactory = samlObjectFactory;
    }

    /**
     * Sets the UserSessionService
     *
     * @param userSessionService The UserSessionService object
     */
    public void setUserSessionService(final UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    public void setProperties(final Properties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void setHostToEntityConfig(Properties hostToEntityConfig) {
        this.hostToEntityConfig = hostToEntityConfig;
    }

    public void setMessageEncoder(final MessageEncoder messageEncoder) {
        this.messageEncoder = messageEncoder;
    }

    /**
     * Validates the state of this object
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(protectionDeciderService, "protectionDeciderService was not set");
        notNull(samlAuthnRequestBuilder, "samlAuthnRequestBuilder was not set");
        notNull(samlObjectFactory, "samlObjectFactory was not set");
        notNull(userSessionService, "userSessionService was not set");
        notNull(messageEncoder, "messageEncoder was not set");
    }

    /**
     * Performs the filtering of the request. The filtering will consult the protectionDeciderService to determine if
     * protection is required. If it is required, it will then check to see if the user has been identified. If
     * protection is required and the user has not been identified, the SAML SP-Initiated flow will be started resulting
     * in an AuthnRequest being generated and the UA redirected to the configured IdP.
     *
     * @param request  The ServletRequest
     * @param response The ServletResponse
     * @param chain    The FilterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws ServletException, IOException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        final String serverName = request.getServerName();
        // redirect user to IDP for authentication if necessary
        if (protectionDeciderService.isProtectionRequired(serverName) && !userSessionService.isSameSession()) {
            final String idpEndpointURL = protectionDeciderService.getIdpURL(serverName);
            final AuthnRequest authnRequest = samlAuthnRequestBuilder.buildSamlAuthRequest();
            if(Objects.nonNull(applicationProperties) && Objects.nonNull(applicationProperties.get("samlauthcontext."+serverName))){
                authnRequest.setID("APPLE"+authnRequest.getID());
                authnRequest.setRequestedAuthnContext(null);
                authnRequest.getNameIDPolicy().setSPNameQualifier(null);
                if(Objects.nonNull(hostToEntityConfig) && Objects.nonNull(hostToEntityConfig.getProperty(serverName))) {
                    authnRequest.getIssuer().setValue(hostToEntityConfig.getProperty(serverName));
                }
            }

            userSessionService.setSAMLRequestId(authnRequest.getID());
            authnRequest.setDestination(idpEndpointURL);
            final HttpServletResponseAdapter responseAdapter = new HttpServletResponseAdapter(httpServletResponse, httpServletRequest.isSecure());
            final SAMLMessageContext<SAMLObject, AuthnRequest, SAMLObject> authnRequestContext = new BasicSAMLMessageContext<>();
            final AssertionConsumerService assertionConsumerService = samlObjectFactory.createSAMLObject(AssertionConsumerService.class);
            assertionConsumerService.setLocation(idpEndpointURL);
            authnRequestContext.setPeerEntityEndpoint(assertionConsumerService);
            authnRequestContext.setOutboundSAMLMessage(authnRequest);
            authnRequestContext.setOutboundMessageTransport(responseAdapter);
            authnRequestContext.setRelayState(ServletUriComponentsBuilder.fromCurrentRequest().build().toString());
            LOG.debug("request set in SAML message {}", authnRequestContext.getOutboundSAMLMessage());
            try {
                messageEncoder.encode(authnRequestContext);
            } catch (final MessageEncodingException ex) {
                LOG.error("Unable to encode AuthnRequest", ex);
                throw new ServletException("Error encoding AuthnRequest", ex);
            }
        } else {
            chain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final FilterConfig config) throws ServletException {
        //implementing init() method in filter interface
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        //implementing destroy() method in filter interface
    }
}
