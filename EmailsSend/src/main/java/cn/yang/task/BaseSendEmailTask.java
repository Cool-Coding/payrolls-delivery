package cn.yang.task;

import cn.yang.dto.Email;
import cn.yang.exception.MailException;
import cn.yang.mapper.EmailMapper;
import cn.yang.service.MailService;
import cn.yang.util.Result;
import cn.yang.util.SpringUtil;
import cn.yang.util.WebSocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cool-coding
 *这是邮件发送基础类，正常的批量发送和批量发送失败的邮件继承此类，并实现抽像方法{@code getEmails}
 * 还统计正在发送的邮件和待发邮件数量；
 * 成功和失败的邮件数量通过数据库查询，因为用户需要随时查询发送状态，需要这两个统计器是无状态的
 */
public abstract  class BaseSendEmailTask implements Runnable{
    protected String subject;//主题
    protected MailService mailService;//邮件服务
    protected EmailMapper mapper;//数据库操作类

    protected Logger logger= LoggerFactory.getLogger(this.getClass());
    private WebSocketUtil webSocketUtil;

    //记数器
    protected int countpertime=0;
    private int runningEmails=0;
    private int pendingEmails=0;


    public BaseSendEmailTask(MailService mailService, EmailMapper emailMapper,String subject,int countpertime) {
        this.mailService = mailService;
        this.mapper = emailMapper;
        this.countpertime=countpertime;
        this.subject=subject;
        webSocketUtil=SpringUtil.getBean(WebSocketUtil.class);

    }

    public void setRunningEmail(int runningEmails){
        this.runningEmails=runningEmails;
    }

    public void reduceOneRunningEmail(){
        this.runningEmails-=1;
    }

    public int getRunningEmails(){return runningEmails;}


    public void setPendingEmail(int pendingEmails){
        this.pendingEmails=pendingEmails;
    }


    public int getPendingEmails(){return pendingEmails;}

    public void reducePendingEmail(int count){
        this.pendingEmails-=count;
    }

    public String getSubject(){
        return subject;
    }

    @Override
    public void run() {
        List<Email> emails=getEmails();
        if(emails==null || emails.size()<=0)return;
        //新建邮件发送任务，并进行发送
        countpertime=Math.max(countpertime,1);
        int start=0;
        int end=start+countpertime;
        setPendingEmail(emails.size());//记录待发邮件数
        ArrayList<Email> subEmails=new ArrayList<>();
        Email roll;
        while (true) {
            subEmails.clear();//清空待发邮件列表
            for (int i = start; i < end; i++) {
                try {
                    roll = emails.get(i);
                } catch (IndexOutOfBoundsException e) {
                    logger.info(e.getMessage());
                    break;
                }
                //添加到待发送数组中
                subEmails.add(roll);
            }

            //如果有待发的邮件
            if (subEmails.size() > 0) {
                try {
                    //更新记数
                    reducePendingEmail(subEmails.size());
                    setRunningEmail(subEmails.size());
                    //1.开始发送时更新发送进度
                    updateProgress();
                    //发送邮件
                    mailService.sendBatchMail(subEmails);
                }catch(MailException e) {
                    final MimeMessage[] mimeMessages = e.getMimeMessages();
                    for(MimeMessage email:mimeMessages){
                        try {
                            String subject = email.getSubject();
                            Address[] recipients = email.getRecipients(Message.RecipientType.TO);
                            String to = recipients[0].toString();
                            Email failedEmail = new Email(to, subject);
                            failedEmail.setMessage(e.getMessage());
                            //更新发送失败
                            mapper.updateResult(failedEmail, Result.FAILED);
                            reduceOneRunningEmail();
                            subEmails.remove(failedEmail);
                            logger.error(to + "发送失败");
                        } catch (MessagingException ex) {
                            logger.error(ex.getMessage(), e);
                        }
                    }
                }

                //更新发送成功的邮件
                for(Email email:subEmails) {
                    reduceOneRunningEmail();
                    email.setMessage("发送成功");
                    mapper.updateResult(email, Result.SUCCESS);
                }
                //发送结束时更新发送进度
                updateProgress();
            } else {
                break;
            }
            //更新索引
            start=end;
            end=start+countpertime;
        }
    }

    /**
     * 更新发送进度
     */
    protected void updateProgress(){
        webSocketUtil.sendMessage(this.subject);
    }

    /**
     *
     * @return 所有待发的邮件
     */
    public abstract  List<Email>  getEmails();
}
