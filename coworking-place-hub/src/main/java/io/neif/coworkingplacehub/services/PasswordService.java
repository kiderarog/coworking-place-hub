package io.neif.coworkingplacehub.services;

import io.neif.coworkingplacehub.dto.ChangePasswordDTO;
import io.neif.coworkingplacehub.dto.PasswordRestoreDTO;
import io.neif.coworkingplacehub.dto.ResponseDTO;
import io.neif.coworkingplacehub.events.PasswordRestoreEvent;
import io.neif.coworkingplacehub.exception.OtpValidationException;
import io.neif.coworkingplacehub.models.Client;
import io.neif.coworkingplacehub.models.OneTimePassword;
import io.neif.coworkingplacehub.repositories.ClientRepository;
import io.neif.coworkingplacehub.repositories.OneTimePasswordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PasswordService {

    private final KafkaTemplate<String, PasswordRestoreEvent> kafkaTemplate;
    private final OneTimePasswordRepository otpRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final OneTimePasswordRepository oneTimePasswordRepository;

    private static final SecureRandom random = new SecureRandom();

    public PasswordService(KafkaTemplate<String, PasswordRestoreEvent> kafkaTemplate,
                           OneTimePasswordRepository otpRepository,
                           ClientRepository clientRepository,
                           PasswordEncoder passwordEncoder,
                           OneTimePasswordRepository oneTimePasswordRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.otpRepository = otpRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.oneTimePasswordRepository = oneTimePasswordRepository;
    }


    // Метод для случайной генерации одноразового пароля для отправки на почту.
    private Integer generateOtp() {
        return random.nextInt(100000, 999999);
    }


    // Метод для генерации одноразового пароля перед оправкой на почту пользователя.
    @Transactional
    public void createOtpForEmail(String email) {
        OneTimePassword otp = new OneTimePassword();
        otp.setEmail(email);
        otp.setOtpCode(generateOtp());
        otp.setIssuedAt(LocalDateTime.now());
        otp.setExpAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otp);
        log.info("Создан одноразовый пароль (OTP) для пользователя {}: Код {} (Действителен до {})",
                email, otp.getOtpCode(), otp.getExpAt());

    }

    // Метод для валидации введенного одноразового кода и проверка его срока действия.
    // В случае ввода истекшего кода, код удаляется из БД как недействительный.
    @Transactional
    public Optional<String> validateOtp(Integer otpCode) {
        log.info("Запрос на проверку OTP: {}", otpCode);

        Optional<OneTimePassword> optionalOtpEntity = otpRepository.findByOtpCode(otpCode);
        if (optionalOtpEntity.isPresent()) {
            OneTimePassword otpEntity = optionalOtpEntity.get();
            if (otpEntity.getExpAt().isBefore(LocalDateTime.now())) {
                log.warn("Истёк срок действия OTP {} для пользователя {}. Код удалён.", otpCode, otpEntity.getEmail());
                otpRepository.delete(otpEntity);
                return Optional.empty();
            }
            log.info("OTP {} успешно верифицирован для пользователя {}", otpCode, otpEntity.getEmail());
            return Optional.of(otpEntity.getEmail());
        }
        log.error("Ошибка валидации OTP: код {} не найден в системе.", otpCode);
        return Optional.empty();
    }

    // Метод для инициализации сброса пароля.
    // В Notification-микросервис через Apache Kafka отправляется триггер для отправки E-mail.
    @Transactional
    public void passwordResetRequest(PasswordRestoreDTO passwordRestoreDTO) {
        log.info("Запрос на сброс пароля для пользователя {}.", passwordRestoreDTO.getEmail());

        createOtpForEmail(passwordRestoreDTO.getEmail());
        Integer otpCode = otpRepository.findByEmail(passwordRestoreDTO.getEmail())
                .map(OneTimePassword::getOtpCode)
                .orElseThrow(() -> {
                    log.error("Ошибка: OTP-код не найден для пользователя {}.", passwordRestoreDTO.getEmail());
                    return new OtpValidationException("OTP-код не найден!");
                });
        Optional<Client> optionalClient = clientRepository.findByEmail(passwordRestoreDTO.getEmail());
        if (optionalClient.isPresent()) {
            String name = optionalClient.get().getName();
            PasswordRestoreEvent passwordRestoreEvent = new PasswordRestoreEvent();
            passwordRestoreEvent.setEmail(passwordRestoreDTO.getEmail());
            passwordRestoreEvent.setName(name);
            passwordRestoreEvent.setOtpCode(otpCode);
            try {
                log.info("Отправка события сброса пароля для {} в Kafka.", passwordRestoreDTO.getEmail());
                SendResult<String, PasswordRestoreEvent> result = kafkaTemplate.send(
                        "password-restore-request-event-topic", UUID.randomUUID().toString(), passwordRestoreEvent).get();
                log.info("Событие успешно отправлено в Kafka. Результат: {}", result);
            } catch (Exception e) {
                log.error("Ошибка при отправке события в Kafka: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            log.error("Ошибка: пользователь {} не найден в системе.", passwordRestoreDTO.getEmail());
            throw new UsernameNotFoundException("Нет такого пользователя!");
        }
    }


    @Transactional
    public ResponseDTO resetPasswordRequestProcessing(PasswordRestoreDTO passwordRestoreDTO) {
        Optional<Client> optionalClient = clientRepository.findByEmail(passwordRestoreDTO.getEmail());
        if (optionalClient.isPresent()) {
            passwordResetRequest(passwordRestoreDTO);
            return new ResponseDTO("success", "Код восстановления отправлен на Ваш Email-адрес.");
        }
        return new ResponseDTO("error", "Пользователь с таким Email не найден");
    }


    // Метод для проверки введенного одноразового пароля и смены пароля в случае успешного ввода OTP.
    @Transactional
    public ResponseDTO replaceForgottenPassword(Integer otpCode, String newPassword) {
        log.info("Запрос на сброс пароля с OTP-кодом {}.", otpCode);
        Optional<String> optionalEmail = validateOtp(otpCode);
        if (optionalEmail.isEmpty()) {
            log.warn("Ошибка сброса пароля: введен неверный или истекший OTP-код {}.", otpCode);
            return new ResponseDTO("error", "Введен неверный или истекший код.");
        }
        String email = optionalEmail.get();
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Ошибка сброса пароля: клиент с email {} не найден.", email);
                    return new OtpValidationException("Ошибка валидации OTP-кода.");
                });
        if (passwordEncoder.matches(newPassword, client.getPassword())) {
            log.warn("Ошибка сброса пароля: клиент {} ввел текущий пароль вместо нового.", email);
            return new ResponseDTO("error", "Вы ввели действующий пароль.");
        }
        client.setPassword(passwordEncoder.encode(newPassword));
        clientRepository.save(client);
        oneTimePasswordRepository.deleteByEmail(email);
        log.info("Пароль успешно изменен для пользователя {}.", email);
        return new ResponseDTO("success", "Пароль успешно изменен.");
    }



    // Метод для смены пароля в личном кабинете (если пользователь помнит свой актуальный пароль).
    @Transactional
    public ResponseDTO changePassword(String clientName, ChangePasswordDTO changePasswordDTO) {
        log.info("Запрос на смену пароля от пользователя {}.", clientName);
        Client client = clientRepository.findByUsername(clientName)
                .orElseThrow(() -> {
                    log.error("Ошибка смены пароля: пользователь {} не найден.", clientName);
                    return new RuntimeException("Пользователь не найден.");
                });
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), client.getPassword())) {
            log.warn("Ошибка смены пароля: пользователь {} ввел действующий пароль вместо нового.", clientName);
            return new ResponseDTO("error", "Вы ввели действующий пароль.");
        }
        if (!passwordEncoder.matches(changePasswordDTO.getPassword(), client.getPassword())) {
            log.warn("Ошибка смены пароля: неверный текущий пароль для пользователя {}.", clientName);
            return new ResponseDTO("error", "Неверный текущий пароль.");
        }
        client.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        clientRepository.save(client);
        log.info("Пользователь {} успешно сменил пароль.", clientName);
        return new ResponseDTO("success", "Пароль успешно изменён.");
    }


    // Удаляет просроченные одноразовые пароли (OTP) из базы данных по расписанию в 00:01 ежедневно.
    // Отдельно одноразовые пароли удаляются из БД после востребования (ввода, вне зависимости от успешности операции).
    @Scheduled(cron = "${cron.expression}")
    public void cleanExpiredOtps() {
        oneTimePasswordRepository.deleteAllByExpAtBefore(LocalDateTime.now());
        log.info("База данных с OTP очищена в {}", LocalDateTime.now());
    }
}
