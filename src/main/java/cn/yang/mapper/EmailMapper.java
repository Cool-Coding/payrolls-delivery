package cn.yang.mapper;

import cn.yang.dto.Email;
import cn.yang.util.Result;
import org.apache.ibatis.annotations.*;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

@Mapper
public interface EmailMapper {

    @Insert("insert into email(`to`,subject,name,content) values(#{to},#{subject},#{name},#{content})")
    void insert(Email email) throws DuplicateKeyException;

    @Select("select `to`,subject,name,content from email where subject = #{subject} and `to` = #{to}")
    Email findByTo(@Param("subject") String subject,@Param("to") String to);

    /**
     * 查询已发送的邮件数量
     * @return
     */
    @Select("select count(*) from email where subject = #{subject}")
    int countAll(String subject);

    @Select("select count(*) from email where subject = #{subject} and result is null")
    int countPendingEmail(String subject);
    /**
     * 查询发送失败的邮件数量
     * @param subject
     * @return
     */
    @Select("select subject,`to`,name,message,content from email where subject = #{subject} and result = 'N'")
    List<Email> getFailedEmails(@Param("subject") String subject);

    /**
     * 查询发送成功或失败的邮件数量
     * @param subject
     * @return
     */
    @Select("select count(*) from email where subject = #{subject} and result = #{result}")
    int getCount(@Param("subject") String subject, @Param("result") Result result);

    /**
     * 更新发送结果
     * @param email
     * @param result
     */
    @Update("update email set message = #{email.message},result = #{result}  where subject = #{email.subject} and `to` = #{email.to}")
    void updateResult(@Param("email") Email email,@Param("result") Result result);

    @Update("update email set message = #{email.message},content = #{email.content}  where subject = #{email.subject} and `to` = #{email.to}")
    void updateContent(@Param("email") Email email);

    @Delete("delete from email where subject = #{subject}")
    int deleteEmails(String subject);
}
