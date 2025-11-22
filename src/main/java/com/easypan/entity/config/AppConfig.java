package com.easypan.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component("appConfig")
@Data
public class AppConfig {
    @Value("${spring.mail.username:}")
    private String sendUserName;

}
