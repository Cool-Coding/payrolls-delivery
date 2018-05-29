package cn.yang.util;

import cn.yang.task.BaseSendEmailTask;

/**
 * @author cool-coding
 * 自定义线程包装类，以获取线程中的任务
 */
public class ThreadWrapper extends Thread {
    private BaseSendEmailTask task;

    public ThreadWrapper(BaseSendEmailTask task){
        super(task);
        this.task=task;
    }

    public BaseSendEmailTask getTask(){return task;}
}
