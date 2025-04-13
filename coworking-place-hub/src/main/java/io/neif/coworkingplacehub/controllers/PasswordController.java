package io.neif.coworkingplacehub.controllers;

import io.neif.coworkingplacehub.dto.ChangePasswordDTO;
import io.neif.coworkingplacehub.dto.RestoreForgottenPasswordDTO;
import io.neif.coworkingplacehub.dto.PasswordRestoreDTO;
import io.neif.coworkingplacehub.dto.ResponseDTO;
import io.neif.coworkingplacehub.security.ClientDetails;
import io.neif.coworkingplacehub.services.PasswordService;
import io.neif.coworkingplacehub.validation.PasswordChangeGroup;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/password")
public class PasswordController {
    private final PasswordService passwordService;

    @Autowired
    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    // Инициализация сброса забытого пароля от аккаунта пользователя.
    // Отправка события через Apache Kafka в Notification-микросервис.1
    // Дальнейшая отправка на E-mail одноразового пароля для восстановления доступа к аккаунту.
    @PostMapping("/reset-password-request")
    public ResponseEntity<ResponseDTO> resetPasswordRequest(@RequestBody PasswordRestoreDTO passwordRestoreDTO) {
        ResponseDTO response = passwordService.resetPasswordRequestProcessing(passwordRestoreDTO);
        if (response.getStatus().equals("success")) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    // Сброс забытого пароля через форму "Забыли пароль?".
    // Ввод одноразового кода для восстановления, полученного по Email и ввод нового пароля.
    @PostMapping("/reset-password")
    public ResponseEntity<ResponseDTO> resetPassword(@RequestBody @Valid RestoreForgottenPasswordDTO otpEntityDTO,
                                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().getFirst().getDefaultMessage();
            return ResponseEntity.badRequest().body(new ResponseDTO("error", Objects.requireNonNull(errorMessage)));
        }
        Integer otpCode = otpEntityDTO.getOtpCode();
        String newPassword = otpEntityDTO.getNewPassword();
        ResponseDTO response = passwordService.replaceForgottenPassword(otpCode, newPassword);
        if ("error".equals(response.getStatus()) || "passwordMatch".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ResponseDTO> changePassword(@AuthenticationPrincipal ClientDetails clientDetails,
                                                      @RequestBody @Validated(PasswordChangeGroup.class) ChangePasswordDTO changePasswordDTO,
                                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String error = bindingResult.getAllErrors().getFirst().getDefaultMessage();
            return ResponseEntity.badRequest().body(new ResponseDTO("error", error));
        }
        String clientName = clientDetails.getUsername();
        ResponseDTO response = passwordService.changePassword(clientName, changePasswordDTO);
        if ("error".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
