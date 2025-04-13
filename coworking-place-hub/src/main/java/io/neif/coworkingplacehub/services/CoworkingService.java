package io.neif.coworkingplacehub.services;

import io.neif.coworkingplacehub.dto.BookedSpotDTO;
import io.neif.coworkingplacehub.dto.CoworkingInfoDTO;
import io.neif.coworkingplacehub.exception.CoworkingNotFoundException;
import io.neif.coworkingplacehub.models.Coworking;
import io.neif.coworkingplacehub.models.Spot;
import io.neif.coworkingplacehub.repositories.CoworkingRepository;
import io.neif.coworkingplacehub.repositories.SpotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CoworkingService {
    private final CoworkingRepository coworkingRepository;
    private final SpotRepository spotRepository;

    @Autowired
    public CoworkingService(CoworkingRepository coworkingRepository, SpotRepository spotRepository) {
        this.coworkingRepository = coworkingRepository;
        this.spotRepository = spotRepository;
    }

    @Transactional(readOnly = true)
    public List<CoworkingInfoDTO> getAllCoworkingSpaces() {
        List<Coworking> coworkingList = coworkingRepository.findAll();
        return coworkingList.stream()
                .map(p -> new CoworkingInfoDTO(
                        p.getName(),
                        p.getLocation(),
                        p.getTotalSpots(),
                        p.getAvailableSpots(),
                        p.getDailyPrice(),
                        p.getMonthlyPrice(),
                        p.isFreeze()
                ))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<BookedSpotDTO> getAllBookedSpots(UUID coworkingId, String adminUsername) {
        Optional<Coworking> optionalCoworking = coworkingRepository.findById(coworkingId);
        if (optionalCoworking.isEmpty()) {
            log.error("Запрос администратором {} статистики несуществующего коворкинга.", adminUsername);
            throw new CoworkingNotFoundException("Нет такого коворкинга.");
        }
        List<Spot> spotList = spotRepository.findSpotsByCoworkingAndActiveBookingIsTrue(optionalCoworking.get());
        if (spotList.isEmpty()) {
            return List.of();
        }
        return spotList.stream()
                .map(spot -> new BookedSpotDTO(
                        spot.getId(),
                        spot.getClientId(),
                        spot.getStartBookDate(),
                        spot.getEndBookDate()))
                .toList();
    }
}