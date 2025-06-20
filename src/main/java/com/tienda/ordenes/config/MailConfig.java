package com.tienda.ordenes.config;

import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(MailProperties mailProperties) {
        // Validamos si se proporcionó configuración SMTP
        boolean isConfigured = mailProperties.getHost() != null && !mailProperties.getHost().isBlank();

        if (isConfigured) {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(mailProperties.getHost());
            sender.setPort(mailProperties.getPort());
            sender.setUsername(mailProperties.getUsername());
            sender.setPassword(mailProperties.getPassword());
            sender.setDefaultEncoding(mailProperties.getDefaultEncoding().name());

            Properties props = sender.getJavaMailProperties();
            props.putAll(mailProperties.getProperties());

            return sender;
        }

        // Mock por defecto
        return new JavaMailSenderImpl() {
            @Override
            public void send(MimeMessage mimeMessage) {
                System.out.println("📧 MOCK: Se intentó enviar un correo a: "
                        + getRecipientFromMessage(mimeMessage));
            }

            private String getRecipientFromMessage(MimeMessage mimeMessage) {
                try {
                    return mimeMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString();
                } catch (Exception e) {
                    return "desconocido";
                }
            }
        };
    }
}