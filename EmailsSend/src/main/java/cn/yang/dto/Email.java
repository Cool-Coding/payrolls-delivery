package cn.yang.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class Email implements Serializable{
    private String to;//接收人
    private String name;//接收人姓名

    @JsonIgnore
    private String subject;//主题

    @JsonIgnore
    private String content;//内容
    private String message;//消息

    public Email(){}
    public Email(String to,String subject,String content,String name){
        this.to=to;
        this.subject=subject;
        this.content=content;
        this.name=name;
    };

    public Email(String to,String subject){
        this(to,subject,null,null);
    };
    public Email(String to,String subject,String name){
        this(to,subject,null,name);
    };

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content=content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return this.to.hashCode() + this.subject.hashCode();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==null)return false;
        if(obj instanceof Email) {
            Email email=(Email)obj;
            return this.to.equals(email.getTo()) && this.subject.equals(email.getSubject());
        }else return false;
    }
}
