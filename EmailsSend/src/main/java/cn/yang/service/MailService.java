package cn.yang.service;

import cn.yang.dto.Email;
import cn.yang.exception.MailException;

import java.util.List;

public interface MailService {
    boolean sendSimpleMail(Email email) throws MailException;
    void sendBatchMail(List<Email> emails) throws MailException;
}
