package cn.yang.controller;

import cn.yang.util.SpringUtil;
import cn.yang.util.WebSocketUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * websocket服务器
 * 与客户端建立连接，并记录连接session
 */

@ServerEndpoint("/{subject}")
@Component
public class WebSocketServer {

    private static Logger log = Logger.getLogger(WebSocketServer.class);

    private static WebSocketUtil webSocketUtil;


    @OnOpen
    public void onOpen(@PathParam("subject") String subject ,
                       Session session){
        log.info("[WebSocketServer] Connected : subject = "+ subject);
        webSocketUtil.add(subject , session);
    }

    /*
    Send Message
     */
    @OnMessage
    public void onMessage(@PathParam("subject") String subject,
                            String message) {
        log.info("[WebSocketServer] Received Message : subject = "+ subject + " , message = " + message);
        if (message.equals("&")){
        }else{
            if("first".equals(message)){
                webSocketUtil.sendMessage(subject);
            }else {
                webSocketUtil.receive(subject, message);
            }
        }
    }




    @Autowired
    public void setWebSocketUtil(WebSocketUtil webSocketUtil) {
        WebSocketServer.webSocketUtil = webSocketUtil;
    }



    /*
        Errot
         */
    @OnError
    public void onError(@PathParam("subject") String subject,
                        Throwable throwable,
                        Session session) {
        log.info("[WebSocketServer] Connection Exception : subject = "+ subject + " , throwable = " + throwable.getMessage());
        webSocketUtil.remove(subject,session);
    }

    /*
    Close Connection
     */
    @OnClose
    public void onClose(@PathParam("subject") String subject,
                        Session session) {
        log.info("[WebSocketServer] Close Connection : subject = " + subject);
        webSocketUtil.remove(subject,session);
    }
}