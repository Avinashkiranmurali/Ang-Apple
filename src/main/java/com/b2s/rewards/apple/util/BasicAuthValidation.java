package com.b2s.rewards.apple.util;

import com.b2s.db.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Objects;

import static com.b2s.rewards.common.util.CommonConstants.*;

/*** Created by srukmagathan on 9/23/2016.
 */
@Component
public class BasicAuthValidation {

    private static final Logger LOG = LoggerFactory.getLogger(BasicAuthValidation.class);

    // Compares user basic auth role against varId of given orderId.  If not match the user don't have access

    public boolean isUserHasAccessToOrder(final Order order){

        final UserDetails userDetails= (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(Objects.nonNull(userDetails) && userDetails.getAuthorities().stream()
            .anyMatch(role -> (order.getVarId().toLowerCase().contains(
                role.getAuthority().substring(role.getAuthority().indexOf('_') + 1).toLowerCase())))){

            return true;
        }else{
            LOG.error("BasicAuthValidation: Order ID {} does not belongs to your entity", order.getOrderId());
            return false;
        }
    }

    public boolean hasAccess() {
        final UserDetails userDetails =
            (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userDetails.getAuthorities().stream()
            .anyMatch(role -> ROLE_AUS.equals(role.getAuthority()) || ROLE_ADMIN.equals(role.getAuthority()))) {
            return true;
        } else {
            LOG.error("BasicAuthValidation: User authentication is failed");
            return false;
        }
    }
}
