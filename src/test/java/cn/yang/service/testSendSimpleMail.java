package cn.yang.service;

import cn.yang.Application;
import cn.yang.dto.Email;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class testSendSimpleMail {

    @Autowired
    private MailService mailService;

    @Test
    public void testSimpleMail() throws Exception {
        Email email=new Email("335809476@qq.com","简历","这是我的简历，请查收！");
        mailService.sendSimpleMail(email);
    }
}
