package com.b2s.apple.mapper;

import com.b2s.db.model.Order;
import com.b2r.service.payroll.ppc.model.RedemptionUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class PayrollUpdateTransactionRequestMapper {

    public RedemptionUpdateRequest from(final Order order, final String status) {
        if( order != null ) {
            final RedemptionUpdateRequest redemptionUpdateRequest = new RedemptionUpdateRequest();
            redemptionUpdateRequest.setMerchantOrderId(String.valueOf(order.getOrderId()));
            redemptionUpdateRequest.setOrderStatus(status);
            return redemptionUpdateRequest;
        }
        return null;
    }

}
