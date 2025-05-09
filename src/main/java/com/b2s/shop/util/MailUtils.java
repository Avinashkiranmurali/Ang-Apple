package com.b2s.shop.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailUtils {

    private static final Logger log = LoggerFactory.getLogger(MailUtils.class);

    private static MailUtils mailUtils;

    public static MailUtils getInstance() {
        if (mailUtils == null) {
            mailUtils = new MailUtils();
        }
        return mailUtils;
    }
    
    public boolean sendIssueMail(String messageContent, String emailTo)  {

        try {
            Properties props = new Properties();
            props.load(MailUtils.class.getResourceAsStream("/src/main/config/uat/WEB-INF/classes/utilconstants.properties"));

            JavaMailSenderImpl sender = new JavaMailSenderImpl();

            // set pamameter for mail sender
            sender.setHost((String) props.get("mail_host"));
            sender.setUsername((String) props.get("mail_user"));
            sender.setPassword((String) props.get("mail_password"));
            sender.setPort(Integer.valueOf((String) props.get("mail_port")));

            // set java mail properties
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            sender.setJavaMailProperties(properties);

            // create message
            MimeMessage msg;
            try {
                msg = sender.createMimeMessage();
            } catch (Exception ex) {
                log.error("An error has occurred", ex);
                return false;
            }

            // use spring MailHelper set msg
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setFrom((String)props.get("mail_csr_sender"));
            helper.setTo(emailTo);

            helper.setText(messageContent, true);

            helper.setSubject("Bridge2Solutions - Customer Support");

            sender.send(msg);

        }  catch (Exception ex) {
            log.error("Cannot send notification email", ex);
            return false;
        }

        return true;
    }
}