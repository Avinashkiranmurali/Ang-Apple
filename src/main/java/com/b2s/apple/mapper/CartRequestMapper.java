package com.b2s.apple.mapper;

import com.b2s.db.model.Order;
import com.b2s.db.model.OrderLine;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.rewards.security.util.ExternalUrlConstants;
import com.b2r.service.payroll.ppc.model.*;
import com.b2s.shop.common.User;
import org.apache.commons.collections.CollectionUtils;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by rpillai on 8/27/2016.
 */
@Component
public class CartRequestMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartRequestMapper.class);

    @Autowired
    private Properties applicationProperties;

    public CartRequest from(final Order order, final Program program, final User user) {
        CartRequest cartRequest = null;
        if(order != null && program != null && PayrollConstants.PAYROLL_DEDUCTION_PAYMENT_OPTION.equals(order.getSelectedPaymentOption())) {
            cartRequest = new CartRequest();
            try {
                cartRequest.setFirstName(order.getFirstname());
                cartRequest.setLastName(order.getLastname());
                cartRequest.setEmail(order.getEmail());
                setCartRequestSubTotal(order, cartRequest);
                setCartRequestTotal(order, cartRequest);
                setCartRequestTotalTax(order, cartRequest);
                setCartRequestTotalShipping(order, cartRequest);
                setCartRequestTotalFee(order, cartRequest);
                setCartRequestDiscountAndTotalSubsidy(order, cartRequest);
                cartRequest.setProducts(getProducts(order));
                cartRequest.setShippingInformation(getShipmentInformation(order));
                cartRequest.setMerchantReturnUrl(user.getHostName() + CommonConstants.getApplicationProperty("ppc.merchant.return.url",user.getVarId(),user.getProgramId(),applicationProperties) + "?orderId=" + order.getOrderId());
                cartRequest.setMerchantCancelUrl(user.getHostName() + CommonConstants.getApplicationProperty("ppc.merchant.cancel.url",user.getVarId(),user.getProgramId(),applicationProperties));
                cartRequest.setKeepAliveUrl(user.getHostName() + applicationProperties.getProperty(ExternalUrlConstants.KEEP_ALIVE_URL_PARTNER_REQ_MAPPING_KEY));
                cartRequest.setEmployerId(user.getEmployerId());
                cartRequest.setEmployerName(user.getEmployerName());
                cartRequest.setEmployeeId(user.getUserId());
                if(order.getOrderId() != null) {
                    cartRequest.setMerchantOrderId(order.getOrderId().toString());
                }
                cartRequest.setLogoUrl(program.getImageUrl());
            } catch (Exception e) {
                LOGGER.error("Error while creating cart request for order id: {}. Exception: {}", order.getOrderId(), e);
            }
        }
        return cartRequest;
    }

    private void setCartRequestDiscountAndTotalSubsidy(final Order order, final CartRequest cartRequest) {
        Money discount = order.getOrderTotalDiscounts();
        if(discount != null && discount.getAmount() != null) {
            cartRequest.setDiscount(discount.getAmount().doubleValue());
            cartRequest.setTotalSubsidy(discount.getAmount().doubleValue());
        }
    }

    private void setCartRequestTotalFee(final Order order, final CartRequest cartRequest) {
        Money totalFee = order.getOrderTotalFeesInMoney();
        if(totalFee != null && totalFee.getAmount() != null) {
            cartRequest.setTotalFee(totalFee.getAmount().doubleValue());
        }
    }

    private void setCartRequestTotalShipping(final Order order, final CartRequest cartRequest) {
        Money totalShipping = order.getOrderTotalShippingInMoney();
        if(totalShipping != null && totalShipping.getAmount() != null) {
            cartRequest.setTotalShipping(totalShipping.getAmount().doubleValue());
        }
    }

    private void setCartRequestTotalTax(final Order order, final CartRequest cartRequest) {
        Money totalTax = order.getOrderTotalTaxesInMoney();
        if (totalTax != null && totalTax.getAmount() != null) {
            cartRequest.setTotalTax(totalTax.getAmount().doubleValue());
        }
    }

    private void setCartRequestTotal(final Order order, final CartRequest cartRequest) {
        Money total = order.getOrderTotalInMoney();
        if(total != null && total.getAmount() != null) {
            cartRequest.setTotal(total.getAmount().doubleValue());
        }
    }

    private void setCartRequestSubTotal(final Order order, final CartRequest cartRequest) {
        Money orderSubTotal = order.getOrderSubTotalInMoney();
        if(orderSubTotal != null && orderSubTotal.getAmount() != null) {
            cartRequest.setSubtotal(orderSubTotal.getAmount().doubleValue());
        }
    }

    private List<Product> getProducts(final Order order) {
        final List<Product> products = new ArrayList<>();;
        if(CollectionUtils.isNotEmpty(order.getOrderLines())) {
            order.getOrderLines().forEach(orderLineObj -> {
                OrderLine orderLine = (OrderLine) orderLineObj;
                if(CommonConstants.APPLE_SUPPLIER_ID_STRING.equals(orderLine.getSupplierId())) {
                    Product product = new Product();
                    product.setSku(orderLine.getSku());
                    product.setLineNumber(orderLine.getLineNum());
                    product.setQuantity(orderLine.getQuantity());
                    product.setDescription(orderLine.getName());
                    product.setName(orderLine.getName());
                    setProductCost(orderLine, product);
                    setProductPrice(orderLine, product);
                    setProductTax(orderLine, product);
                    setProductFee(orderLine, product);
                    setProductShipping(orderLine, product);
                    product.setThumbnail(orderLine.getImageUrl());
                    setProductSubsidy(order, product);

                    products.add(product);
                }
            });
        }
        return products;
    }

    private void setProductSubsidy(final Order order, final Product product) {
        // Adding cart discount as subsidy amount
        Money discount = order.getOrderTotalDiscounts();
        if(discount != null && discount.getAmount() != null) {
            product.setSubsidy(discount.getAmount().doubleValue());
        }
    }

    private void setProductShipping(final OrderLine orderLine, final Product product) {
        if (orderLine.getSupplierShippingPrice() != null) {
            product.setShipping(new BigDecimal(orderLine.getSupplierShippingPrice()).divide(BigDecimal.valueOf(100)).setScale(2).doubleValue());
        }
    }

    private void setProductFee(final OrderLine orderLine, final Product product) {
        BigDecimal fee = orderLine.getTotalFeesInMoneyMinor();
        if (fee != null) {
            product.setFee(fee.doubleValue());
        }
    }

    private void setProductTax(final OrderLine orderLine, final Product product) {
        BigDecimal tax = orderLine.getTotalTaxesInMoneyMinor();
        if (tax != null) {
            product.setTax(tax.doubleValue());
        }
    }

    private void setProductPrice(final OrderLine orderLine, final Product product) {
        if (orderLine.getSupplierItemPrice() != null) {
            product.setPrice(new BigDecimal(orderLine.getSupplierItemPrice()).divide(BigDecimal.valueOf(100)).setScale(2).doubleValue());
        }
    }

    private void setProductCost(final OrderLine orderLine, final Product product) {
        if (orderLine.getVarOrderLinePrice() != null) {
            product.setCost(new BigDecimal(orderLine.getVarOrderLinePrice()).divide(BigDecimal.valueOf(100)).setScale(2).doubleValue());
        }
    }

    private ShippingInformation getShipmentInformation(final Order order) {
        ShippingInformation shippingInformation = null;
        if(order != null) {
            shippingInformation = new ShippingInformation();
            shippingInformation.setFirstName(order.getFirstname());
            shippingInformation.setLastName(order.getLastname());
            shippingInformation.setPhoneNumber(order.getPhone());

            Address address = new Address();
            address.setStreet1(order.getAddr1());
            address.setStreet2(order.getAddr2());
            address.setCity(order.getCity());
            address.setState(order.getState());
            address.setZip(order.getZip());
            shippingInformation.setAddress(address);
        }
        return shippingInformation;
    }
}
