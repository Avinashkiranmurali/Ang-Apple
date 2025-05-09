package com.b2s.rewards.apple.controller;

import com.b2s.rewards.apple.integration.model.aep.CreateMerchantRequest;
import com.b2s.rewards.apple.dao.MerchantListDao;
import com.b2s.apple.entity.MerchantEntity;
import io.jsonwebtoken.lang.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private MerchantListDao merchantDao;

    @RequestMapping(value = "/merchants/create",method = RequestMethod.POST)
    public ResponseEntity<String> createNewMerchant(@RequestBody final CreateMerchantRequest createMerchantRequest, final BindingResult bindingResult){
        logger.info("Entering into createNewMerchant() method in ServiceController class");
        final Set<String> errors = getErrors(bindingResult);
        if (Collections.isEmpty(errors)) {
            if (Objects.nonNull(merchantDao.getMerchant(createMerchantRequest.getSupplierId(), createMerchantRequest.getMerchantId()))) {
                return new ResponseEntity("Merchant information already exists.", HttpStatus.BAD_REQUEST);
            } else {
                MerchantEntity merchantEntity = new MerchantEntity();
                merchantEntity.setMerchantId(createMerchantRequest.getMerchantId());
                merchantEntity.setSupplierId(createMerchantRequest.getSupplierId());
                merchantEntity.setName(createMerchantRequest.getMerchantName());
                merchantEntity.setSimpleName(createMerchantRequest.getMerchantSimpleName());
                merchantDao.save(merchantEntity);
                logger.info("Merchant information successfully added.");
                return new ResponseEntity<>("success", HttpStatus.OK);
            }
        }
        logger.error("Adding new merchant information failed.");
        return new ResponseEntity(errors, HttpStatus.BAD_REQUEST);
    }
    private Set<String> getErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream().map(fieldError -> fieldError.getField())
                .collect(Collectors.toSet());
    }

}
