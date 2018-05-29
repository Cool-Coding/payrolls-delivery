package cn.yang.controller;


import cn.yang.dto.Email;
import cn.yang.mapper.EmailMapper;
import cn.yang.service.MailService;
import cn.yang.task.SendBatchEmailTask;
import cn.yang.task.SendFailedEmailTask;
import cn.yang.util.ValidateEmail;
import cn.yang.util.CommonUtils;
import cn.yang.util.ThreadWrapper;
import cn.yang.util.Result;
import cn.yang.dto.SendResultMessage;
import com.mysql.jdbc.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author cool-coding
 * 邮件服务控制器
 */
@Controller
public class EmailController {

    /**
     * 上传的excel保存的目录
     */
    @Value("${upload.file.dir}")
    private String  UPLOADED_FOLDER;

    /**
     * 数据库操作mapper
     */
    @Autowired
    private EmailMapper mapper;

    /**
     * 邮件发送服务
     */
    @Autowired
    private MailService mailService;

    /**
      发送结果封装类
     */
    @Autowired
    private SendResultMessage sendResultMessage;

    /**
     * 每次发送的邮件数量
     */
    @Value("${email.count.sent.per}")
    protected int countpertime=0;


    private HashMap<String,String> files = new HashMap();
    private Vector<ThreadWrapper> tasks=new Vector<>();

    /**
     * 首页
     * @return
     */
    @RequestMapping("/")
    String index() {
        return "upload";
    }


    /**
     * 查看发送结果
     * @return
     */
    @RequestMapping("/result")
    String result() {
        return "progress";
    }

    /**
     * 查看发送结果(websocket实现)
     * @param model
     * @return
     */
    @RequestMapping("/list")
    String list(Model model){
        final List<String> subjects = getSubject();
        if(subjects.size()==1){
            model.addAttribute("subject",subjects.get(0));
            return "progress2";
        }else{
            model.addAttribute("subjects", subjects);
            return "selectSubject";
        }
    }

    @PostMapping("/showResult")
    public String showResult(@RequestParam("subject") String subject,Model model){
        model.addAttribute("subject",subject);
        return "progress2";
    }

    /**
     * 上传文件
     * @param file
     * @param model
     * @return
     */
    @PostMapping("/upload")
    public String singleFileUpload(@RequestParam("file") MultipartFile file,Model model) {
        if (file.isEmpty()) {
             model.addAttribute("message", "请选择一个文件上传");
            return "uploadResult";
        }

        try {
            //检查文件类型是否是excel
            boolean check=CommonUtils.checkIsExcel(file.getOriginalFilename());
            if (check) {
                byte[] bytes = file.getBytes();
                String path_str = UPLOADED_FOLDER + file.getOriginalFilename();
                Path path = Paths.get(path_str);

                Files.write(path,bytes);
                //检验邮件
                ValidateEmail validateEmail = new ValidateEmail();
                List<String> messages = validateEmail.check(path_str);
                if(messages.size()>0)
                model.addAttribute("message", messages);
                else {
                    String uuid = UUID.randomUUID().toString();
                    files.put(uuid,path_str);
                    model.addAttribute("message","将会下发"+validateEmail.getRollCount()+"个工资条");
                    model.addAttribute("uuid", uuid);
                }
            }else{
                model.addAttribute("message", file.getOriginalFilename() + "不是excel文件");
            }

        } catch (IOException e){
            model.addAttribute("message","文件保存失败");
        }
        return "uploadResult";
    }

    /**
     * 发送邮件
     * @param subject
     * @param uuid
     * @param model
     * @return
     */
    @PostMapping("/done")
    public String sendEmail(@RequestParam("subject") String subject,@RequestParam("uuid") String uuid, Model model){
        //发送邮件
        if(files.containsKey(uuid)) {
            String path;
            synchronized (files) {
                if(files.containsKey(uuid)) {
                    path = files.get(uuid);
                    files.remove(uuid);
                }else {
                    model.addAttribute("message", "查询不到有需要发送的工资条");
                    return "uploadResult";
                }

            }
            ThreadWrapper sendMailThread = new ThreadWrapper(new SendBatchEmailTask(path,subject, mailService, mapper, countpertime));
            sendMailThread.start();
            tasks.add(sendMailThread);
            model.addAttribute("subject", subject);
            return "progress2";
        }else{
            model.addAttribute("message","查询不到有需要发送的工资条");
            return "uploadResult";
        }
    }

    /**
     * 发送邮件结果
     * @param subject
     * @return
     */
    @PostMapping("/getResult")
    @ResponseBody
    public SendResultMessage getResult(@RequestParam("subject") String subject){
        if(StringUtils.isNullOrEmpty(subject)){
            final List<String> subjects = getSubject();
            if(subjects.size()==1)subject=subjects.get(0);
            else if(subjects.size()>1){
                sendResultMessage.init();
                sendResultMessage.setSubjects(subjects);
                return  sendResultMessage;
            }else{
                sendResultMessage.init();
                sendResultMessage.setFinished(true);
                return sendResultMessage;
            }
        }
        int success = mapper.getCount(subject, Result.SUCCESS);
        List<Email> failedEmails = mapper.getFailedEmails(subject);
        int pendingCount=0;
        int runningCount=0;
        boolean over=false;
        for (ThreadWrapper threadWrapper :tasks){
            if(threadWrapper.isAlive()){
                if(threadWrapper.getTask().getSubject().equals(subject)) {
                    pendingCount += threadWrapper.getTask().getPendingEmails();
                    runningCount += threadWrapper.getTask().getRunningEmails();
                }
            }
        }

        /**
         * 之前是通过判断线程是否都结束来决定邮件是否都发送完毕，但使用websocket是线程主动向
         * 客户端发送消息，所以发送时线程不可能结束，所以通过判断待发和正在发送邮件数量来决定是否
         * 都发送完毕
         */
        if(pendingCount==0 && runningCount==0){
            over=true;
        }

        sendResultMessage.init();//初始化，清空变量
        sendResultMessage.setRunningCount(runningCount);
        sendResultMessage.setPendingCount(pendingCount);
        sendResultMessage.setSuccessCount(success);
        sendResultMessage.setFailedCount(failedEmails.size());
        sendResultMessage.setFailedEmail(failedEmails);
        List<String> subjects=new ArrayList<>();
        subjects.add(subject);
        sendResultMessage.setSubjects(subjects);

        if (over) {
            sendResultMessage.setFinished(true);
        }
        return sendResultMessage;
    }

    /**
     * 发送失败的邮件重新发送
     * @param subject
     * @return
     */
    @PostMapping("/reSend")
    @ResponseBody
    public int reSend(@RequestParam("subject") String subject){
        List<Email> failedEmails = mapper.getFailedEmails(subject);
        if (failedEmails.size()>0) {
           ThreadWrapper sendMailThread = new ThreadWrapper(new SendFailedEmailTask(mailService, mapper,subject, failedEmails,countpertime));
            sendMailThread.start();
            tasks.add(sendMailThread);
            return failedEmails.size();
        }else{
            return 0;
        }
    }

    /**
     * 删除数据库及内存中邮件记录
     * @param subject
     * @return
     */
    @PostMapping("/delete")
    @ResponseBody
    public String delete(@RequestParam("subject") String subject){
        Vector<ThreadWrapper> deadThreads=new Vector<>();
        for (ThreadWrapper threadWrapper :tasks){
            if(!threadWrapper.isAlive()) {
                String subject_str= threadWrapper.getTask().getSubject();
                if(subject.equals(subject_str))
                deadThreads.add(threadWrapper);
            }
        }
        //删除数据库记录
        int count=mapper.deleteEmails(subject);
        if(count<=0) {
            return "删除失败";
        }
        //清除已经完成的任务
        tasks.removeAll(deadThreads);
        return "删除成功,总共删除"+count+"封邮件记录";
    }

    private List<String> getSubject(){
        List<String> subjects=new ArrayList<>();
        for(ThreadWrapper threadWrapper :tasks){
            String sub= threadWrapper.getTask().getSubject();
            if(sub!=null && !sub.equals("")) {
                String sub_trim=sub.trim();
                if(!subjects.contains(sub_trim))subjects.add(sub_trim);
            }
        }
        return subjects;
    }
}
