package com.workingspacehub.notification_microservice.services;

import com.workingspacehub.notification_microservice.dto.PasswordRestoreDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;

    }

    @Value("${MAIL_USERNAME}")
    private String from;

    public void sendOtp(PasswordRestoreDTO passwordRestoreDTO) {

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(passwordRestoreDTO.getEmail());
        mailMessage.setSubject("Coworking Place Hub");
        mailMessage.setText(passwordRestoreDTO.getName() + ", Ваш код для сброса пароля: "
                + passwordRestoreDTO.getOtpCode());
        mailMessage.setFrom(from);
        try {
            mailSender.send(mailMessage);
            System.out.println("Письмо с OTP для восстановления пароля отправлено на почту: " + passwordRestoreDTO.getEmail());
            log.info("Письмо с одноразовым паролем отправлено на почту: {}", passwordRestoreDTO.getEmail());
        } catch (Exception e) {
            System.err.println("Ошибка при отправке сообщения: " + e.getMessage());
            log.error("Ошибка при отправке письма с OTP на почту: {}", passwordRestoreDTO.getEmail());

        }
    }

}