## 批量发送工资条 
> <font size="5" >【目录】</font>  
> <font size="4">[1.功能简介](#功能简介)</font>    
> <font size="4">[2.步骤介绍](#步骤介绍)</font>      
> <font size="4">[3.开发环境](#开发环境)</font>      
> <font size="4">[4.开发技术](#开发技术])</font>      
> <font size="4">[5.配置文件](#配置文件)</font>    
> <font size="4">[6.示例截图](#示例截图)</font>  
 
### 功能简介
读取excel中工资条数据，并批量发送邮件,功能比较简单，仅用于学习。
     
### 步骤介绍   
- 工资条模板格式

  |姓名|工资项1|工资项2|....|工资项n|邮件地址|
  |---|---|---|----|----|---|
  | | | | | | |
  
  *首列必须为接收人姓名,最后一列必须为对应的接收人邮件地址。*
1. 用户打开网页，上传excel，服务器进行数据检查，然后返回检查结果，如果检查通过则返回将会下发的工资条数量，并要求用户填写邮件主题
2. 用户填写邮件主题，并确定发送
3. 服务器启动一个线程进行批量发送，每次发送工资条的数量可以配置。
4. 服务器启动线程后，用户可以看见发送进度，发送进度以"饼形"显示(使用了两种实现方式，一种是浏览器定时轮询，一种是websocket服务器实时通知)
5. 如果由于邮件较多，用户没有时间等待，关闭了网页，后续想看进度如何，仍可以输入网址查看。  
>  说明:邮件批量发送是以主题分类，如果用户上传了多次exel属于同一主题，则进度会进行合并显示;
        如果发送过程中，有的邮件发送失败，则将在前端页面显示，用户可以选择重发，都发送成功后，用户可以选择删除服务器硬盘保存的excel及数据库发送记录。
 
### 开发环境 
  - Intellij idea 2017
  - Windows10
  - JDK1.8
  - Mysql5.7.21  
  
### 开发技术 
  - Springboot1.5.12
  - Mybatis1.3.2
  - Websocket
  - Jquery3.1.1
  - Thymeleaf
  - ECharts3.2.3

### 配置文件 
```properties
#web服务端口
server.port=80
management.security.enabled=false

#日志文件
logging.file=email.log

#上传文件大小限制
spring.http.multipart.max-file-size=1Mb

#请求大小限制
spring.http.multipart.max-request-size=10Mb

#smtp配置
spring.mail.host= #邮箱服务smtp地址
spring.mail.username= #邮箱地址
spring.mail.password= #邮箱密码

spring.mail.default-encoding=UTF-8
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.transport.protocol=smtp
mail.fromMail.addr= #邮件头from信息

#自定义配置
#配置文件上传的临时目录
#upload.file.dir=/tmp/
upload.file.dir=D:/

#excel密码
excel.password= #

#每次发送的邮件数
email.count.sent.per=1

#mybaits配置
mybatis.type-aliases-package=cn.yang.entity
spring.datasource.driverClassName = com.mysql.jdbc.Driver
spring.datasource.url = jdbc:mysql://host:3306/database?useUnicode=true&useSSL=false&characterEncoding=utf-8
spring.datasource.username = #数据库用户名
spring.datasource.password = #密码
```
### 示例截图
  1. 工资条  
  ![](https://picabstract-preview-ftn.weiyun.com/ftn_pic_abs_v3/039e1449152e366860f5e7aa4f22c72fc62400ff407ef75eaa0ce33f220d47f099cb95f44be310455dcf1b6f1e79adcb?pictype=scale&from=30113&version=3.3.3.3&uin=542600078&fname=salary07.png&size=750)  
  
  2. 上传工资条  
  ![](https://picabstract-preview-ftn.weiyun.com/ftn_pic_abs_v3/658c170d156bcbe31624ccbfc64b5004c335425b0b1a3119a617d3166de74cbe50ded3e20cbc8a00873e8bfee8b3507d?pictype=scale&from=30113&version=3.3.3.3&uin=542600078&fname=salary01.png&size=750)    
  
  3. 服务器返回检查结果  
  ![](https://picabstract-preview-ftn.weiyun.com/ftn_pic_abs_v3/d11764d6e3d35028c6f2a72b2f7de8ae5f965a1923b5a5d2c29629cd90d9fb9fb374850a7a821f29da9ec817cebc84da?pictype=scale&from=30113&version=3.3.3.3&uin=542600078&fname=salary02.png&size=750)  
  如果检查excel数据失败，则返回消息，修改后重新上传。
  
  4. 检查通过，输入主题后，开始发送工资条  
  ![进度1](https://picabstract-preview-ftn.weiyun.com/ftn_pic_abs_v3/413322a540cb3ba17477544f113b6818c99ceec68c99fd34a355f3bb6f10ee3a7879ebae044204cba91a4cfb43de2431?pictype=scale&from=30113&version=3.3.3.3&uin=542600078&fname=salary03.png&size=750)  
  ![进度2](https://picabstract-preview-ftn.weiyun.com/ftn_pic_abs_v3/2a9ef5d56708edeadfed0354b5656695c623b71b1e4d29297efdcb2ebe9c1a7077fb7c8b18cdecfe0b751d3a52c2214e?pictype=scale&from=30113&version=3.3.3.3&uin=542600078&fname=salary04.png&size=750)  
  ![进度3](https://picabstract-preview-ftn.weiyun.com/ftn_pic_abs_v3/0a5e255343380876c8b538efe052bba266cf0ec5358495974142d3687fcf13ea38131ddfdfc5d75d8e191098a4a6dc70?pictype=scale&from=30113&version=3.3.3.3&uin=542600078&fname=salary05.png&size=750)  
  
  > 关闭网页后，仍可通过输入 ***http://域名:端口号/list(websocket)或http://域名:端口号/result(轮询)*** 实时查看发送进度
  
  发送失败的邮件状态截图:      
  ![邮件状态](https://picabstract-preview-ftn.weiyun.com/ftn_pic_abs_v3/ce846c2aea63557e2819e6da390e7d68e2165ec064bbc0342aa178302fc944917f7f4cf701520bbfe6d3b93c2c7846b6?pictype=scale&from=30113&version=3.3.3.3&uin=542600078&fname=failed_sending.png&size=750)
  
  > 发送结束后，可以选择删除发送记录，服务器将删除上传的excel文件和数据库中的记录。
  
  5.登录邮箱，查看收到的工资条
  ![](https://picabstract-preview-ftn.weiyun.com/ftn_pic_abs_v3/c3cea84a94e21d3a6adcce06a4d9420e44374e455862354255302c3a1aee1cf4c9edb1363e49fd8d0daf177dfdf8f263?pictype=scale&from=30113&version=3.3.3.3&uin=542600078&fname=salary08.png&size=750)  
  
  
  
