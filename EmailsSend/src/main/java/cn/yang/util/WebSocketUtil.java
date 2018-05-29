package cn.yang.util;


import cn.yang.controller.EmailController;
import cn.yang.dto.SendResultMessage;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketUtil {

    private Logger log = Logger.getLogger(WebSocketUtil.class);

    public Map<String, ArrayList<Session>> clients = new ConcurrentHashMap<>();

    @Autowired
    private EmailController emailController;


    private int linkCount(){
        final Collection<ArrayList<Session>> values = clients.values();
        int sum=0;
        for(ArrayList<Session> sessions:values){
            for(Session session:sessions){
                sum +=1;
            }
        }
        return sum;
    }

    /*
    Add Session
     */
    public void add(String subject, Session session) {
        if(clients.get(subject)==null){
            ArrayList<Session> sessions=new ArrayList<Session>();
            sessions.add(session);
            clients.put(subject,sessions);
        }else{
            final ArrayList<Session> sessions = clients.get(subject);
            if(!sessions.contains(session)) {
                System.out.println("session:"+session);
                sessions.add(session);
            }
        }

        log.info("当前连接数 = " + linkCount());

    }

    /*
    Receive Message
     */
    public void receive(String subject, String message) {
        log.info("收到消息 : subject = " + subject + " , Message = " + message);
        log.info("当前连接数 = " + linkCount());
    }

    /*
    Remove Session
     */
    public void remove(String subject,Session session) {
        clients.get(subject).remove(session);
        log.info("当前连接数 = " + linkCount());

    }

    /*
    Get Session
     */
    public boolean sendMessage(String subject) {
        log.info("当前连接数 = " + linkCount());
        if(clients.get(subject) == null){
            return false;
        }else{
            //调用控制器中的getResult方法，获取当前取主题所有线程合计值
            final SendResultMessage result = emailController.getResult(subject);
            String json=new JacksonJsonProvider().toJson(result);
            final ArrayList<Session> sessions = clients.get(subject);
            //添加线程同步，以免多个线程同时向同一个session发送数据，导致
            // The remote endpoint was in state [TEXT_FULL_WRITING] which is an invalid state for called method
            //错误
            synchronized (this) {
                for (Session session : sessions) {
                    session.getAsyncRemote().sendText(json);
                }
            }
            return true;
        }

    }
}
