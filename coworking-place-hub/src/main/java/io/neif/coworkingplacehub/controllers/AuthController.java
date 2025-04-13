package io.neif.coworkingplacehub.controllers;

import io.neif.coworkingplacehub.dto.AuthClientDTO;
import io.neif.coworkingplacehub.dto.ResponseDTO;
import io.neif.coworkingplacehub.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Все ручки в AuthController доступны без аутентификации и авторизации.
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    // Регистрация нового клиента.
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> createClient(@RequestBody @Valid AuthClientDTO authClientDTO,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ResponseDTO("error", errors));
        }
        authService.saveClient(authClientDTO);
        return ResponseEntity.ok(new ResponseDTO("success", "Пользователь успешно зарегистрирован."));
    }


    // Аутентификация клиента и получение токена авторизации (JWT).
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@RequestBody AuthClientDTO authClientDTO) {
        ResponseDTO response = authService.getAuthorizationToken(authClientDTO);
        if ("error".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
