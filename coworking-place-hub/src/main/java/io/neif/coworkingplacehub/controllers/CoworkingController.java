package io.neif.coworkingplacehub.controllers;

import io.neif.coworkingplacehub.dto.BookedSpotDTO;
import io.neif.coworkingplacehub.dto.CoworkingInfoDTO;
import io.neif.coworkingplacehub.services.CoworkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/coworking")
public class CoworkingController {
    private final CoworkingService coworkingService;

    public CoworkingController(CoworkingService coworkingService) {
        this.coworkingService = coworkingService;
    }


    @GetMapping("/show-all")
    public ResponseEntity<List<CoworkingInfoDTO>> showAllCoworkingSpaces() {
        return ResponseEntity.ok(coworkingService.getAllCoworkingSpaces());

    }

    @GetMapping("/bookings/{coworkingId}")
    public ResponseEntity<List<BookedSpotDTO>> showAllBookedSpotsByCoworking(@PathVariable("coworkingId") String coworkingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(coworkingService.getAllBookedSpots(UUID.fromString(coworkingId), authentication.getName()));
    }
}