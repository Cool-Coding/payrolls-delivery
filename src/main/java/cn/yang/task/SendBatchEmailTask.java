package cn.yang.task;

import cn.yang.dto.Email;
import cn.yang.mapper.EmailMapper;
import cn.yang.service.MailService;
import cn.yang.util.CommonUtils;
import cn.yang.util.ExcelUtil;
import cn.yang.util.Result;
import org.springframework.dao.DuplicateKeyException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 杨光永
 * 批量发送邮件类
 */
public class SendBatchEmailTask extends BaseSendEmailTask{
    private String path;//文件路径

    public SendBatchEmailTask(String path,String subject, MailService mailService, EmailMapper emailMapper,int countpertime) {
        super(mailService,emailMapper,subject,countpertime);
        this.subject = subject;
        this.path=path;
    }

    @Override
    public List<Email> getEmails() {
        ArrayList<String[]> data = null;
        ExcelUtil excelUtil;
        File excel=null;
        try {
            excelUtil = new ExcelUtil();
            excel=new File(path);
            data = excelUtil.readExcel(excel);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            return null;
        }
        final String[] head=data.get(0);
        data.remove(0);//删除表头

        ArrayList<Email> emails=new ArrayList<Email>();
        for(String[] roll:data){
            //信的内容
            String content= CommonUtils.buildHTML(excelUtil.getHeadColors(),head,roll);
            //接收人
            String to=roll[roll.length-1];
            //接收人姓名
            String name=roll[0];

            Email email=new Email(to,subject,content,name);

            //将邮件插入数据库
            try {
                mapper.insert(email);
                //如果插入数据库成功，则添加到待发送数组中
                emails.add(email);
            }catch (DuplicateKeyException e){
                //emails.remove(email);//此处错误，遍历时修改会引起java.util.ConcurrentModificationException
                //首次插入重复的话，则认为失败
                email.setMessage("此邮件显示之前已经发送过，请确认！");
                mapper.updateResult(email, Result.FAILED);
                mapper.updateContent(email);
                //插入数据库失败时更新发送进度
                updateProgress();
                logger.error(e.getMessage(),e);
            }
        }
        //删除exel文件
        boolean delete=excel.delete();
        if (delete){
            logger.info("删除{}成功",excel.getName());
        }else{
            logger.info("删除{}失败",excel.getName());
        }
        return emails;
    }
}
