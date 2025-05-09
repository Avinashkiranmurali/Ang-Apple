package com.b2s.rewards.security.filter;

import com.b2s.rewards.common.util.CommonConstants;
import com.b2s.shop.common.User;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.security.SecureRandom;

public class LoggerMDCFilter implements Filter {

    private static String hostName;
    static{
        try{
            InetAddress addr = InetAddress.getLocalHost();
            hostName = addr.getHostName();
        }catch(Exception e){
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        //overriding init() method in filter interface
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            SecureRandom random = CommonConstants.SECURE_RANDOM;
            User user = (User) ((HttpServletRequest) request).getSession().getAttribute(CommonConstants.USER_SESSION_OBJECT);
            if (user!=null){
                MDC.put("user",user.getVarId()+":"+user.getProgramid()+":"+user.getUserId());
            }else{
                MDC.put("user","NA:NA:NA");
            }
            MDC.put("host",hostName);
            MDC.put("tranxId",(System.currentTimeMillis())+String.valueOf(random.nextInt(10000)));
            chain.doFilter(request, response);
        }finally{
            MDC.remove("user");
            MDC.remove("host");
            MDC.remove("tranxId");
        }
    }

    @Override
    public void destroy() {
        //overriding destroy() method in filter interface
    }

}