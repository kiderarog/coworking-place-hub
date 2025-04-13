package io.neif.coworkingplacehub.controllers;

import io.neif.coworkingplacehub.dto.ClientDTO;
import io.neif.coworkingplacehub.dto.EditProfileDTO;
import io.neif.coworkingplacehub.dto.ResponseDTO;
import io.neif.coworkingplacehub.security.ClientDetails;
import io.neif.coworkingplacehub.services.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ClientController {
    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // Получает данные профиля клиента по его аутентификации.
    @GetMapping("/profile")
    public ResponseEntity<ClientDTO> showProfile(@AuthenticationPrincipal ClientDetails clientDetails) {
        return ResponseEntity.ok(clientService.showProfile(clientDetails.getUsername()));
    }

    // Редактирует профиль клиента, проверяя входные данные.
    @PostMapping("/edit")
    public ResponseEntity<ResponseDTO> editProfile(@AuthenticationPrincipal ClientDetails clientDetails,
                                                   @RequestBody @Valid EditProfileDTO editProfileDTO,
                                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ResponseDTO("error", errors));
        }
        String clientName = clientDetails.getUsername();
        ResponseDTO response = clientService.editProfile(clientName, editProfileDTO);
        if ("error".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Удаляет профиль клиента из системы.
    @PostMapping("/delete")
    public ResponseEntity<ResponseDTO> deleteClient(@AuthenticationPrincipal ClientDetails clientDetails) {
        return ResponseEntity.ok(clientService.deleteProfile(clientDetails.getUsername()));
    }
}
