package im.vinci.server.statistic.utils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

/**
 * Created by zhongzhengkai on 15/12/17.
 */
public class MailUtils {

    public static void sendMail(String subject, String content, String... to) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.exmail.qq.com"); // 指定SMTP服务器
        props.put("mail.smtp.auth", "true"); // 指定是否需要SMTP验证
        Session mailSession = Session.getDefaultInstance(props);
        Message message = new MimeMessage(mailSession);

        String fromAddress = "report@vinci.im";//发件人地址

        message.setFrom(new InternetAddress(fromAddress)); // 发件人

        InternetAddress[] addr = new InternetAddress[to.length];
        for (int i=0; i<to.length; i++) {
            addr[i] = new InternetAddress(to[i]);
        }

        message.addRecipients(Message.RecipientType.TO, addr); // 收件人
//        message.addRecipient(Message.RecipientType.CC, new InternetAddress(to)); // 抄送人
//        message.addRecipient(Message.RecipientType.BCC, new InternetAddress(to)); // 密送人

        message.setSubject(subject); // 邮件主题
        //指定邮箱内容及ContentType和编码方式
        message.setContent(content, "text/html;charset=utf-8");
        //指定邮件发送日期
        message.setSentDate(new Date());
        message.saveChanges();
        Transport transport = mailSession.getTransport("smtp");
        transport.connect(props.getProperty("mail.smtp.host"), fromAddress, "Vinci2015");
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

    public static void sendMailWithPic(String subject, String content, String picName,  String... to) throws MessagingException, UnsupportedEncodingException {
        _sendMailWithPic(subject, content, new String[]{picName}, to);
    }

    public static void sendMailWithPics(String subject, String content, String[] picNames, String... to) throws MessagingException, UnsupportedEncodingException {
        _sendMailWithPic(subject, content, picNames, to);
    }

    /**
     *发送带图片的邮件
     * @param to
     * @param subject
     * @param content
     * @param picName 带全路径的文件名
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    private static void _sendMailWithPic(String subject, String content, String[] picName, String[] to) throws MessagingException,UnsupportedEncodingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.exmail.qq.com"); // 指定SMTP服务器
        props.put("mail.smtp.auth", "true"); // 指定是否需要SMTP验证
        Session mailSession = Session.getDefaultInstance(props);
//        mailSession.setDebug(true);//是否设置debug模式
        Message message = new MimeMessage(mailSession);

        String fromAddress = "report@vinci.im";//发件人地址

        message.setFrom(new InternetAddress(fromAddress)); // 发件人
        InternetAddress[] addr = new InternetAddress[to.length];
        for (int i=0; i<to.length; i++) {
            addr[i] = new InternetAddress(to[i]);
        }

        message.addRecipients(Message.RecipientType.TO, addr); // 收件人
        message.setSubject(subject); // 邮件主题

//        //指定邮箱内容及ContentType和编码方式
//        message.setContent(content, "text/html;charset=utf-8");


        //
        // This HTML mail have to 2 part, the BODY and the embedded image
        //
        MimeMultipart multipart = new MimeMultipart("related");

        // first part  (the html)
        BodyPart messageBodyPart = new MimeBodyPart();
//        messageBodyPart.setContent("<meta http-equiv=Content-Type content=text/html; charset=UTF-8>"+content, "text/html;charset=UTF-8");
        messageBodyPart.setContent(content, "text/html;charset=UTF-8");
        // add it
        multipart.addBodyPart(messageBodyPart);


        for (String e : picName) {
            // second or more part (the image)
            messageBodyPart = new MimeBodyPart();
            DataSource fds = new FileDataSource(e);
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setHeader("Content-ID", "<image>");
            String filename=  MimeUtility.encodeText(fds.getName());//解决中文乱码问题
            messageBodyPart.setFileName(filename);//这句话一定要加,要不然附件后缀名回事***.bin形式
            // add it
            multipart.addBodyPart(messageBodyPart);
        }

        // put everything together
        message.setContent(multipart);

        //指定邮件发送日期
        message.setSentDate(new Date());
        message.saveChanges();
        Transport transport = mailSession.getTransport("smtp");
        transport.connect(props.getProperty("mail.smtp.host"), fromAddress, "Vinci2015");
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

//    public static void main(String[] args) throws Exception{
//        String[] picNames = new String[]{"/Users/zhongzhengkai/Downloads/333.jpg","/Users/zhongzhengkai/Downloads/够.jpg"};
////        sendMailWithPics("fantasticsoul@vinci.im","test_"+System.currentTimeMillis(),"<H1>test</H1><font color=\"red\">我是vinci未来本人</font>",picNames);
////        sendMail("fantasticsoul@vinci.im","test_"+System.currentTimeMillis(),"<h1>test</h1>");
//    }

}
