package com.tienda.ordenes.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.mail.host", havingValue = "localhost", matchIfMissing = false)
    public JavaMailSender mockMailSender() {
        // Para desarrollo local, creamos un mock del JavaMailSender
        return new JavaMailSenderImpl() {
            @Override
            public void send(jakarta.mail.internet.MimeMessage mimeMessage) {
                // En desarrollo, solo logueamos el intento de envío
                System.out.println("MOCK EMAIL: Se intentó enviar un correo a: " + 
                    getRecipientFromMessage(mimeMessage));
            }
            
            private String getRecipientFromMessage(jakarta.mail.internet.MimeMessage mimeMessage) {
                try {
                    return mimeMessage.getRecipients(jakarta.mail.Message.RecipientType.TO)[0].toString();
                } catch (Exception e) {
                    return "destinatario desconocido";
                }
            }
        };
    }

    @Bean
    @ConditionalOnProperty(name = "spring.mail.host", havingValue = "smtp.gmail.com")
    public JavaMailSender gmailMailSender(@Autowired MailProperties mailProperties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());
        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());
        mailSender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());
        
        Properties props = mailSender.getJavaMailProperties();
        props.putAll(mailProperties.getProperties());
        
        return mailSender;
    }
} 