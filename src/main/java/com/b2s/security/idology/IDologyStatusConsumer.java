package com.b2s.security.idology;

import com.b2s.idology.model.ValidationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class IDologyStatusConsumer implements Consumer<ValidationResponse> {

    public static final String TRANSACTION_STATUS = "transactionStatus";

    @Autowired
    private HttpSession httpSession;

    @Override
    public void accept(final ValidationResponse validationResponse) {
        final boolean transactionStatus =
                validationResponse.getStatus() == ValidationResponse.ValidationStatus.SUCCESS ||
                        validationResponse.getStatus() == ValidationResponse.ValidationStatus.ID_NOT_LOCATED;

        httpSession.setAttribute(
                TRANSACTION_STATUS,
                transactionStatus);
    }

    public boolean isTransactionSuccess() {
        return Optional.ofNullable(httpSession.getAttribute(TRANSACTION_STATUS))
                .map(status -> (boolean) status)
                .orElse(false);
    }

    public void clearFlag() {
        httpSession.setAttribute(TRANSACTION_STATUS, null);
    }
}
