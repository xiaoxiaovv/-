package com.istar.mediabroken.utils;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
@Component
public class MailUtil {
    private static final String HOST = "smtp.exmail.qq.com";
    private static final Integer PORT = 465;
    private static final String USERNAME = "bjjmonitor@yqzbw.com";
    private static final String PASSWORD = "20182015ZHxg";
    private static final String EMAILFORM = "bjjmonitor@yqzbw.com";
    private static JavaMailSenderImpl mailSender = createMailSender();
    /**
     * 邮件发送器
     *
     * @return 配置好的工具
     */
    private static JavaMailSenderImpl createMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(HOST);
        sender.setPort(PORT);
        sender.setUsername(USERNAME);
        sender.setPassword(PASSWORD);
        sender.setDefaultEncoding("Utf-8");
        Properties p = new Properties();
        p.setProperty("mail.smtp.timeout", "25000");
        p.setProperty("mail.smtp.auth", "false");
        p.setProperty("mail.smtp.ssl.enable","true");
        sender.setJavaMailProperties(p);
        return sender;
    }

    /**
     * 发送邮件
     *
     * @param to 接受人
     * @param subject 主题
     * @param message 发送内容
     * @throws MessagingException 异常
     * @throws UnsupportedEncodingException 异常
     */
    public static void sendMail(String[] to, String subject, String message) throws MessagingException,UnsupportedEncodingException {
        SimpleMailMessage msg = new SimpleMailMessage() ;
        msg.setFrom(EMAILFORM);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(message);
        mailSender.send(msg);
    }
}
