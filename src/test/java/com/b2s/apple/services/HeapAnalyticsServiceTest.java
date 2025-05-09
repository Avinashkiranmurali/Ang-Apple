package com.b2s.apple.services;

import com.b2s.apple.model.HeapAnalyticsRequest;
import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.db.model.OrderLineAttribute;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.apple.util.HttpClientUtil;
import com.b2s.rewards.common.exception.B2RException;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class HeapAnalyticsServiceTest {

    @InjectMocks
    private HeapAnalyticsService heapAnalyticsService;

    @Mock
    private HttpClientUtil httpClient;
    @Mock
    private Properties applicationProperties;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        MockMvcBuilders.standaloneSetup(heapAnalyticsService).build();;
    }

    @Test
    public void testTrackEvent() throws B2RException {
        Program program = new Program();
        program.setConfig(Map.of(CommonConstants.ANALYTICS_HEAP_APPID, "heap"));
        Order order = new Order();
        User user = new User();
        user.setUserId("test");
        OrderLine orderLine = new OrderLine();

        OrderLineAttribute olaDPA = new OrderLineAttribute();
        olaDPA.setName(CommonConstants.ORDER_ATTR_KEY_DISPLAY_PRICE_AMOUNT);
        olaDPA.setValue("200.0");
        OrderLineAttribute olaDTPA = new OrderLineAttribute();
        olaDTPA.setName(CommonConstants.ORDER_ATTR_KEY_DISPLAY_TOTAL_PRICE_AMOUNT);
        olaDTPA.setValue("2000");
        OrderLineAttribute olaDPP = new OrderLineAttribute();
        olaDPP.setName(CommonConstants.ORDER_ATTR_KEY_DISPLAY_PRICE_POINTS);
        olaDPP.setValue("2000");
        OrderLineAttribute olaUDPA = new OrderLineAttribute();
        olaUDPA.setName(CommonConstants.ORDER_ATTR_KEY_UNPROMOTED_DISPLAY_PRICE_AMOUNT);
        olaUDPA.setValue("200.0");
        OrderLineAttribute olaUDPP = new OrderLineAttribute();
        olaUDPP.setName(CommonConstants.ORDER_ATTR_KEY_UNPROMOTED_DISPLAY_PRICE_POINTS);
        olaUDPP.setValue("2000");
        orderLine.setOrderAttributes(List.of(olaDPA, olaDTPA, olaDPP, olaUDPA, olaUDPP));

        order.setOrderLines(List.of(orderLine));
        when(applicationProperties.getProperty(any(String.class))).thenReturn("KEY_VALUE");
        when(httpClient.getHttpResponseWithHeaders(anyString(), any(Class.class), any(HttpMethod.class), any(HeapAnalyticsRequest.class), any(HttpHeaders.class))).thenReturn(null);

        try {
            heapAnalyticsService.trackEvent(order, program, user, AnalyticsService.AnalyticsEventName.ORDER_CANCELLATION);
        } catch (Exception e) {
            Assert.fail("Exception not expected");
        }

    }
}
