package cn.yang.dto;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @author cool-coding
 * 发送邮件结果消息结构
 */
@Component
public class SendResultMessage implements Serializable{
    private int pendingCount;//待发邮件数
    private int runningCount;//正在发送的邮件
    private int successCount;//发送成功的邮件
    private int failedCount;//发送失败的邮件
    private List<Email> failedEmail;//发送失败的邮件
    private List<String> subjects;//所有主题

    private boolean finished;//是否结束

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public void setFailedEmail(List<Email> failedEmail) {
        this.failedEmail = failedEmail;
    }

    public List<Email> getFailedEmail() {
        return failedEmail;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public void init(){
        pendingCount=0;
        successCount=0;
        failedCount=0;
        failedEmail=null;
        finished=false;
        subjects=null;
    }

    public int getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(int runningCount) {
        this.runningCount = runningCount;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }
}
