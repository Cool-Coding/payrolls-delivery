package cn.yang.mapper;

import cn.yang.dto.Email;
import cn.yang.util.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailMapperTest {

    @Autowired
    EmailMapper mapper;

    @Test
    public void testInsert () throws SQLException {
        Email email = new Email();
        email.setSubject("2018年5月份工资条test");
        email.setTo("335809476@qq.com");
        try {
            mapper.insert(email);
        }catch (DuplicateKeyException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testQueryByEmail(){
        String email="335809476@qq.com";
        Email email2=mapper.findByTo("2018年5月份工资条test",email);
        if(email2!=null)
        System.out.println(email2.getTo());
    }

    @Test
    public void testCountAll(){
        int count=mapper.countAll("2018年5月份工资条test");
        System.out.println(count);
    }

    @Test
    public void testGetSuccessCount(){
        int count=mapper.getCount("2018年5月份工资条test",Result.SUCCESS);
        System.out.println(count);
    }

    @Test
    public void testUpdateResult(){
        Email email = new Email();
        email.setTo("335809476@qq.com");
        email.setSubject("2018年5月份工资条test");
        email.setMessage("更新成功");
        mapper.updateResult(email, Result.SUCCESS);
    }

    @Test
    public void testDelete(){
        String subject="2018年5月份工资条";
        int count=mapper.deleteEmails(subject);
        System.out.println(count);
    }
}
