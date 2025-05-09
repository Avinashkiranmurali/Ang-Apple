package com.b2s.rewards.mail;


public interface MailHelper {

    /**
     *
     * create on 2006-6-8 11:19:55
     * description :
     *	send sample text email
     * @param to
     * @param subject
     * @param content
     * @throws Exception
     *
     * @return void
     */
    public void send(String to, String from, String subject, String content) throws Exception;

    /**
     *
     * create on 2006-6-8 11:19:36
     * description :
     *	send html text email
     * @param to
     * @param subject
     * @param content
     * @throws Exception
     *
     * @return void
     */
    public void sendWithHtml(String to, String from, String cc, String bcc, String subject, String content) throws Exception;


}
