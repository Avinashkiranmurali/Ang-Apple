package com.b2s.shop.common.order.var;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class TransactionIdInterceptor implements ClientHttpRequestInterceptor {
    protected static final String MDC_ATTRIBUTE_TRANX_ID = "tranxId";
    protected static final String HTTP_HEADER_X_B3_TRACE_ID = "X-B3-TraceId";

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        final String transactionId = MDC.get(MDC_ATTRIBUTE_TRANX_ID);
        if (transactionId != null) {
            request.getHeaders().add(HTTP_HEADER_X_B3_TRACE_ID, formatToHex(transactionId));
        }
        return execution.execute(request, body);
    }

    private String formatToHex(String transactionId) {
        return String.format("%016x", NumberUtils.createLong(transactionId));
    }
}
