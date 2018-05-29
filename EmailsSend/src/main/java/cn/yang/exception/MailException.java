package cn.yang.exception;

import javax.mail.internet.MimeMessage;

public class MailException extends Exception {
    private MimeMessage[] mimeMessages;

    public MailException(String message,MimeMessage... mimeMessages){
        super(message);
        this.mimeMessages=mimeMessages;
    }

    public MimeMessage[] getMimeMessages() {
        return mimeMessages;
    }
}
