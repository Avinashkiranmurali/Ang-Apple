package com.b2s.security.idology;

/**
 * @author rjesuraj Date : 10/1/2019 Time : 5:45 PM
 */

import com.b2s.rewards.apple.model.Cart;
import com.b2s.rewards.apple.model.Program;
import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.idology.model.IDologyContext;
import com.b2s.idology.model.Person;
import com.b2s.idology.service.IDologyContextProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;

@Component
public class IDologyContextProviderImpl implements IDologyContextProvider {

    @Autowired
    private HttpSession httpSession;

    @Override
    public IDologyContext getIdologyContext() {
        final IDologyContext idologyContext = new IDologyContext();
        final User user = (User) httpSession.getAttribute(CommonConstants.USER_SESSION_OBJECT);
        idologyContext.setIdologyEnabled(isIdologyEnabled(user.getVarId(), user.getProgramId()));
        final Cart sessionCart = (Cart) httpSession.getAttribute(CommonConstants.APPLE_CART_SESSION_OBJECT);
        idologyContext.setPerson(createPerson(user));

        return idologyContext;
    }

    public boolean isIdologyEnabled(final String varId, final String programId) {
        final Program program =  (Program)httpSession.getAttribute(CommonConstants.PROGRAM_SESSION_OBJECT);
       return (Boolean) program.getConfig().getOrDefault(CommonConstants.IDOLOGY_ENABLED, Boolean.FALSE);

    }

    private Person createPerson(final User user) {
        final Person person = new Person();
        person.setFirstName(user.getFirstName());
        person.setLastName(user.getLastName());
        person.setAddress(user.getAddr1());
        person.setAddress2(user.getAddr2());
        person.setCity(user.getCity());
        person.setState(user.getState());
        person.setCountry(user.getCountry());
        person.setZip(user.getZip());
        person.setIpAddress(user.getIPAddress());
        person.setEmail(user.getEmail());
        person.setPhone(user.getPhone());

        return person;
    }
}
