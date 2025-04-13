package io.neif.coworkingplacehub.controllers;

import io.neif.coworkingplacehub.dto.BookingDTO;
import io.neif.coworkingplacehub.dto.ResponseDTO;
import io.neif.coworkingplacehub.security.ClientDetails;
import io.neif.coworkingplacehub.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/booking")
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Бронирование места в коворкинге для пользователя.
    @PostMapping("/book-spot")
    private ResponseEntity<ResponseDTO> bookWorkingSpot(@AuthenticationPrincipal ClientDetails clientDetails,
                                                        @RequestBody BookingDTO bookingDTO) {
        String clientId = clientDetails.getClientId();
        ResponseDTO response = bookingService.bookWorkingSpot(clientId, bookingDTO);
        if (response.getStatus().equals("error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

}