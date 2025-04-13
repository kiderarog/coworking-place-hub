package com.workingspacehub.notification_microservice.handlers;

import com.workingspacehub.notification_microservice.dto.PasswordRestoreDTO;
import com.workingspacehub.notification_microservice.events.PasswordRestoreEvent;
import com.workingspacehub.notification_microservice.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class PasswordRestoreEventHandler {

    private final EmailService emailService;


    public PasswordRestoreEventHandler(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "password-restore-request-event-topic")
    public void handle(PasswordRestoreEvent passwordRestoreEvent) {
        log.info("Получено сообщение из Kafka Topic (инициализация сброса забытого пароля)." +
                " Пользователь с E-mail: {}", passwordRestoreEvent.getEmail());
        emailService.sendOtp(new PasswordRestoreDTO(
                passwordRestoreEvent.getEmail(),
                passwordRestoreEvent.getOtpCode(),
                passwordRestoreEvent.getName()
        ));
    }
}
