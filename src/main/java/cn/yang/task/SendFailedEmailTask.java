package cn.yang.task;

import cn.yang.dto.Email;
import cn.yang.mapper.EmailMapper;
import cn.yang.service.MailService;

import java.util.List;

public class SendFailedEmailTask extends BaseSendEmailTask{
    private List<Email> emails;//发送失败的邮件

    public SendFailedEmailTask(MailService mailService, EmailMapper emailMapper,String subject, List<Email> emails,int countpertime){
        super(mailService,emailMapper,subject,countpertime);
        this.emails=emails;
    }

    @Override
    public List<Email> getEmails() {
        return emails;
    }
}
