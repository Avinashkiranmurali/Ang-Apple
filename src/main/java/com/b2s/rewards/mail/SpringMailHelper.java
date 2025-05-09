package com.b2s.rewards.mail;


import com.b2s.rewards.common.util.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.util.Properties;



public class SpringMailHelper implements MailHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SpringMailHelper.class);

    private static final String DEFAULT_HOST = "";

    private static final String DEFAULT_USER = "";

    private static final String DEFAULT_PASSWORD = "";

    private static final int DEFAULT_PORT = -1;

    private JavaMailSenderImpl sender = null;

    public SpringMailHelper() {

        // create mail sender
        sender = new JavaMailSenderImpl();

        // set pamameter for mail sender
        sender.setHost(DEFAULT_HOST);
        sender.setUsername(DEFAULT_USER);
        sender.setPassword(DEFAULT_PASSWORD);
        sender.setPort(DEFAULT_PORT);

        // set java mail properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        sender.setJavaMailProperties(properties);
    }

    public SpringMailHelper(String host, String user, String password, int port) {

        // create mail sender
        sender = new JavaMailSenderImpl();

        //set parameter for mail sender
        sender.setHost(host);
        sender.setUsername(user);
        sender.setPassword(password);
        sender.setPort(port);

        // set java mail properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        sender.setJavaMailProperties(properties);

    }

    public void send(String to, String from, String subject, String content)
            throws Exception {

        // create message
        MimeMessage msg = sender.createMimeMessage();

        // use spring MailHelper set msg
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, CommonConstants.UTF8_ENCODING);

        helper.setFrom(from);
        helper.setTo(to);
        helper.setText(content);
        helper.setSubject(subject);

        sender.send(msg);
    }


    public void sendWithHtml(String to, String from, String cc, String bcc, String subject, String content)
            throws Exception {
        // create message
        MimeMessage msg = null;
        try {
            msg = sender.createMimeMessage();
        } catch (Exception ex) {
            LOG.error("",ex);
            return;
        }

        // use spring MailHelper set msg
        MimeMessageHelper helper = new MimeMessageHelper(msg, true,CommonConstants.UTF8_ENCODING);

        helper.setFrom(from);
        helper.setTo(to);
        if (cc!=null && cc.trim().length()>0) {
            helper.setCc(cc);
        }
        if (bcc!=null && bcc.trim().length()>0) {
            helper.setBcc(bcc);
        }
        helper.setText(content, true);
        helper.setSubject(subject);

        sender.send(msg);
    }

    /*
     * TODO test me! mxu
     */
    public void sendWithHtmlWithExplicitFromAddress(String[] to, String[] cc, String[] bcc, String from, String subject, String content)
            throws Exception {
        MimeMessage msg = null;
        try {
            msg = sender.createMimeMessage();
        } catch (Exception ex) {
            LOG.error("",ex);
            return;
        }

        MimeMessageHelper helper = new MimeMessageHelper(msg, true,CommonConstants.UTF8_ENCODING);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setCc(cc);
        helper.setBcc(bcc);
        helper.setText(content, true);
        helper.setSubject(subject);

        sender.send(msg);
    }



}
