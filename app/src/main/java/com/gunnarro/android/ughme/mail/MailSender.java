package com.gunnarro.android.ughme.mail;

public class MailSender {

    private static final String MAIL_HOST = "smtp.gmail.com";
    private final String user;
    private final String password;

    public MailSender(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients) {
        /**
         try {
         MimeMessage message = new MimeMessage(session);
         DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
         message.setSender(new InternetAddress(sender));
         message.setSubject(subject);
         message.setDataHandler(handler);
         if (recipients.indexOf(',') > 0) {
         message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
         } else {
         message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
         }
         Transport.send(message);
         } catch (Exception e) {
         e.printStackTrace();
         }
         */
    }
}
