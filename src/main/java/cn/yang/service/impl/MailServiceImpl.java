package cn.yang.service.impl;

import cn.yang.dto.Email;
import cn.yang.exception.MailException;
import cn.yang.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

@Component
public class MailServiceImpl implements MailService {

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.fromMail.addr}")
    private String from;

    @Override
    public boolean sendSimpleMail(Email email) throws MailException{
        MimeMessage message=null;
        try {
            message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(email.getTo());
            helper.setSubject(email.getSubject());
            helper.setText(email.getContent(), true);

            mailSender.send(message);
            logger.info("邮件已经发送");
            return true;
        } catch (Exception e) {
            logger.error("发送邮件时发生异常！", e);
            throw new MailException(e.getMessage(),message);
        }
    }

    @Override
    public void sendBatchMail(List<Email> emails) throws MailException {
        if(emails.size()<=0)return;
        ArrayList<MimeMessage> mimeMessages=new ArrayList<>();
        try {
            for (Email email : emails) {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setFrom(from);
                helper.setTo(email.getTo());
                helper.setSubject(email.getSubject());
                helper.setText(email.getContent(), true);
                mimeMessages.add(message);
            }
            mailSender.send(mimeMessages.toArray(new MimeMessage[mimeMessages.size()]));
            logger.info("邮件已经发送");
        }catch(Exception e){
            logger.error(e.getMessage(),e);
            throw new MailException(e.getMessage(),mimeMessages.toArray(new MimeMessage[mimeMessages.size()]));
        }
    }
}
