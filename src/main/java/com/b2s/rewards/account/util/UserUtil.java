package com.b2s.rewards.account.util;

import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import com.b2s.rewards.model.Address;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


public class UserUtil {

    /**
	 * Get User object from HTTP session.
	 *
	 * @param session The HTTP session
	 * @return The User object
	 */
	public static User getUserFromSession(HttpSession session){
		User user =(User) session.getAttribute(CommonConstants.USER_SESSION_OBJECT);
		return user;
	}

	/*
	 * get client ip
	 */
	public String getClientIp(HttpServletRequest request){
		return request.getRemoteAddr();
	}

	/*
	 * get client agent
	 */
	public String getClientAgent(HttpServletRequest request){
		return request.getRemoteUser();
	}

	/*
	 * get user identity string for log
	 */
	public String getUserString(HttpSession session){
		User user = this.getUserFromSession(session);
		String result = "";
		if (user!=null)
			result = user.getVarId()+"-"+user.getProgramId()+"-"+user.getUserId();
		return result;
	}

    public static Address createAddressFromUser(User user) {
        Address userAddress = new Address();
        userAddress.setAddress1(user.getAddr1());
        userAddress.setAddress2(user.getAddr2());
        userAddress.setCity(user.getCity());
        userAddress.setCountry(user.getCountry());
        return userAddress;
    }

    /**
	 * Find out is user HTTP session has timed out.
	 * We try to display a custom timeout page here.
	 *
	 * @param request
	 * @return
	 */
	public static boolean isSessionTimeout(HttpServletRequest request){
		if (request.getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT)!=null) {
			return false;
		} else {
			return true;
		}
	}
    
}
