package io.neif.coworkingplacehub.exception;

import io.neif.coworkingplacehub.dto.ResponseDTO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ResponseDTO> handleDuplicateKeyException(DuplicateKeyException e) {
        ResponseDTO response = new ResponseDTO(
                "Клиент с таким username, email или номером телефона уже существует!",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(OtpValidationException.class)
    public ResponseEntity<ResponseDTO> handleOtpValidationException(OtpValidationException e) {
        ResponseDTO response = new ResponseDTO(
                "Введены неверные данные для сброса пароля.",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleUserNotFound(UsernameNotFoundException e) {
        ResponseDTO response = new ResponseDTO(
                "Пользователь не найден.",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

    }


    @ExceptionHandler(ErrorAddingCoworkingException.class)
    public ResponseEntity<ResponseDTO> handleErrorCreatingCoworkingException(ErrorAddingCoworkingException e) {
        ResponseDTO response = new ResponseDTO(
                "При создании коворкинга произошла ошибка.",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CoworkingNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleCoworkingNotFoundException(CoworkingNotFoundException e) {
        ResponseDTO response = new ResponseDTO(
                "Такого коворкинга не существует.",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ErrorWorkspaceBookingException.class)
    public ResponseEntity<ResponseDTO> handleErrorSpotBookingException(ErrorWorkspaceBookingException e) {
        ResponseDTO response = new ResponseDTO(
                e.getMessage(), System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


}
