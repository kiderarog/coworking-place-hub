package io.neif.coworkingplacehub.controllers;

import io.neif.coworkingplacehub.dto.AddCoworkingDTO;
import io.neif.coworkingplacehub.dto.CoworkingStatsForAdminDTO;
import io.neif.coworkingplacehub.dto.PriceChangingDTO;
import io.neif.coworkingplacehub.dto.ResponseDTO;
import io.neif.coworkingplacehub.services.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    private String getAdminUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // Добавляет новый коворкинг на основе переданных данных.
    @PostMapping("/add")
    public ResponseEntity<ResponseDTO> addCoworking(@RequestBody AddCoworkingDTO addCoworkingDTO) {
        String adminUsername = getAdminUsername();
        ResponseDTO response = adminService.createCoworking(addCoworkingDTO, adminUsername);
        if (response.getStatus().equals("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Удаляет коворкинг по его идентификатору.
    @PostMapping("/delete/{coworkingId}")
    public ResponseEntity<ResponseDTO> deleteCoworking(@PathVariable("coworkingId") UUID coworkingId) {
        String adminUsername = getAdminUsername();
        ResponseDTO response = adminService.deleteCoworking(coworkingId, adminUsername);
        if (response.getStatus().equals("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Замораживает или размораживает коворкинг.
    @PostMapping("/freeze/{coworkingId}")
    public ResponseEntity<ResponseDTO> freezeCoworking(@PathVariable("coworkingId") UUID coworkingId,
                                                       @RequestParam("freeze") boolean freeze) {
        String adminUsername = getAdminUsername();
        ResponseDTO response = adminService.freezeCoworking(coworkingId, freeze, adminUsername);
        if (response.getStatus().equals("error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Позволяет управлять ценовой политикой коворкинга.
    @PostMapping("/price/{coworkingId}")
    public ResponseEntity<ResponseDTO> changePrice(
            @PathVariable("coworkingId") UUID coworkingId,
            @Valid @RequestBody PriceChangingDTO priceChangingDTO) {
        String adminUsername = getAdminUsername();
        ResponseDTO response = adminService.changePrice(coworkingId, priceChangingDTO, adminUsername);
        if (response.getStatus().equals("error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // Получение статистики и данных о коворкинге по его ID.
    @GetMapping("/stats/{coworkingId}")
    public ResponseEntity<CoworkingStatsForAdminDTO> getStats(@PathVariable("coworkingId") UUID coworkingId) {
        String adminUsername = getAdminUsername();
        return ResponseEntity.ok().body(adminService.getStats(coworkingId, adminUsername));
    }

    // Блокирует или разблокирует учетную запись клиента по его имени пользователя.
    @PostMapping("/block-client/{username}")
    public ResponseEntity<ResponseDTO> manageUserStatus(@PathVariable("username") String username,
                                                        @RequestParam("block") boolean block) {
        String adminUsername = getAdminUsername();
        ResponseDTO response = adminService.blockClient(username, block, adminUsername);
        if (response.getStatus().equals("error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
