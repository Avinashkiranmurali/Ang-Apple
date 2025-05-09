package com.b2s.rewards.apple.controller;

import com.b2s.rewards.apple.model.ShoppingCart;
import com.b2s.rewards.apple.model.ShoppingCartItem;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.apple.services.CartService;
import com.b2s.apple.services.ProgramService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping(value = "/", produces = "application/javascript")
public class CartCountController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProgramService programService;

    @GetMapping(value = "/{varId}/{programId}/{userId}/cart/count")
    public ResponseEntity<String> getCartCount(@PathVariable("varId") String varId, @PathVariable("programId") String
        programId, @PathVariable("userId") String userId, @RequestParam String callback) {
        final User user = new User();
        user.setUserId(userId);
        user.setVarId(varId);
        user.setProgramId(programId);
        if (callback == null || callback.trim().isEmpty()) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
        final Optional<String> cartCountOpt = programService.getProgramConfigValue(varId, CommonConstants
            .DEFAULT_PROGRAM_KEY, CommonConstants.CART_COUNT_ENDPOINT);
        final boolean isVarEnable = Boolean.valueOf(cartCountOpt.orElse(CommonConstants.FALSE_VALUE));
        List<ShoppingCartItem> shoppingCartItem = new ArrayList<ShoppingCartItem>();
        if (isVarEnable) {
            final ShoppingCart shoppingCart = cartService.getShoppingCart(user);
            if (Objects.nonNull(shoppingCart)) {
                shoppingCartItem = shoppingCart.getShoppingCartItems();
            }
            if (CollectionUtils.isNotEmpty(shoppingCartItem)) {
                return new ResponseEntity(new String(callback + "(" + shoppingCartItem.size() + ")"),
                    HttpStatus.OK);
            } else {
                return new ResponseEntity(new String(callback + "(0)"), HttpStatus.OK);
            }
        } else {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }
}


