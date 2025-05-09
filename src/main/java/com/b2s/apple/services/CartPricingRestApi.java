package com.b2s.apple.services;

import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.apple.model.CartPricingRequestDTO;
import com.b2s.apple.model.CartPricingResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Properties;

import static com.b2s.rewards.common.util.CommonConstants.*;

@Service
public class CartPricingRestApi extends AbstractCartPricingRestApi{
    private static final Logger LOGGER = LoggerFactory.getLogger(CartPricingRestApi.class);

    @Autowired
    private Properties applicationProperties;


    /**/
    /**
     * Sends provided cart request to pricing server to be priced. As a result returns a priced cart response.<br/>
     *
     * @param request CartPricingRequestDTO
     * @return CartPricingResponseDTO
     */
    public CartPricingResponseDTO getPrice(final CartPricingRequestDTO cartRequest) {

        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getTargetUrl()).pathSegment("api").pathSegment("cart");
        final URI uri = builder.build().encode().toUri();

        final HttpHeaders headers = new HttpHeaders();
        headers.set(HTTP_HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HTTP_HEADER_ACCPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HTTP_HEADER_PARTNER_CODE, applicationProperties.getProperty(APEX_HEADER_PARTNERCODE));
        final HttpEntity<?> entity = new HttpEntity<>(cartRequest, headers);

        try {
            final HttpEntity<CartPricingResponseDTO> response = getRestTemplate().exchange(
                uri,
                HttpMethod.POST,
                entity,
                CartPricingResponseDTO.class);

            final CartPricingResponseDTO cartResponse = response.getBody();

            return cartResponse;
        } catch (final HttpClientErrorException e) {
            LOGGER.error("HttpClientErrorException occurred in CartPricingRestApi getPrice() call: ", e);
            throw new HttpClientErrorException(e.getStatusCode(),e.getResponseBodyAsString());
        } catch (final HttpServerErrorException e) {
            LOGGER.error("HttpServerErrorException occurred in CartPricingRestApi getPrice() call: ", e);
            throw new HttpServerErrorException(e.getStatusCode(),"HttpServerErrorException during cartRequest getPrice() call");
        } catch (final Exception e) {
            LOGGER.error("Exception occurred in CartPricingRestApi getPrice() call: ", e);
        }
        return null;
    }

    private String getTargetUrl() {
        return applicationProperties.getProperty(CommonConstants.PRICING_SERVICE_URL);
    }
}
