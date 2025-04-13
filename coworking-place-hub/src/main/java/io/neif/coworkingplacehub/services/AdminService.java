package io.neif.coworkingplacehub.services;

import io.neif.coworkingplacehub.dto.AddCoworkingDTO;
import io.neif.coworkingplacehub.dto.CoworkingStatsForAdminDTO;
import io.neif.coworkingplacehub.dto.PriceChangingDTO;
import io.neif.coworkingplacehub.dto.ResponseDTO;
import io.neif.coworkingplacehub.exception.ErrorAddingCoworkingException;
import io.neif.coworkingplacehub.exception.CoworkingNotFoundException;
import io.neif.coworkingplacehub.models.Client;
import io.neif.coworkingplacehub.models.Coworking;
import io.neif.coworkingplacehub.models.Spot;
import io.neif.coworkingplacehub.repositories.ClientRepository;
import io.neif.coworkingplacehub.repositories.CoworkingRepository;
import io.neif.coworkingplacehub.repositories.SpotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
public class AdminService {

    private final CoworkingRepository coworkingRepository;
    private final SpotRepository spotRepository;
    private final ClientRepository clientRepository;

    public AdminService(CoworkingRepository coworkingRepository,
                        SpotRepository spotRepository, ClientRepository clientRepository) {
        this.coworkingRepository = coworkingRepository;
        this.spotRepository = spotRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public ResponseDTO createCoworking(AddCoworkingDTO addCoworkingDTO, String adminUsername) {
        try {
            Coworking coworking = buildCoworking(addCoworkingDTO);
            coworkingRepository.save(coworking);

            List<Spot> spots = IntStream.range(0, addCoworkingDTO.getTotalSpots())
                    .mapToObj(i -> {
                        Spot spot = new Spot();
                        spot.setCoworking(coworking);
                        spot.setActiveBooking(false);
                        return spot;
                    })
                    .toList();
            spotRepository.saveAll(spots);

            log.info("Администратором {} добавлен новый коворкинг. ID: {}", adminUsername, coworking.getId());
            return new ResponseDTO("success", "Коворкинг успешно добавлен.");
        } catch (ErrorAddingCoworkingException e) {
            log.error("Ошибка при создании коворкинга: {}", e.getMessage());
            return new ResponseDTO("error", "Ошибка при создании коворкинга: " + e.getMessage());
        }
    }

    // Приватный метод, который формирует объект Coworking, заполняя его данными из DTO.
    // Вызывается в методе createCoworking.
    private Coworking buildCoworking(AddCoworkingDTO addCoworkingDTO) {
        Coworking coworking = new Coworking();
        coworking.setName(addCoworkingDTO.getName());
        coworking.setLocation(addCoworkingDTO.getLocation());
        coworking.setTotalSpots(addCoworkingDTO.getTotalSpots());
        coworking.setAvailableSpots(addCoworkingDTO.getAvailableSpots());
        coworking.setDailyPrice(addCoworkingDTO.getDailyPrice());
        coworking.setMonthlyPrice(addCoworkingDTO.getMonthlyPrice());
        coworking.setBookedSpots(0);
        return coworking;
    }


    @Transactional
    public ResponseDTO deleteCoworking(UUID coworkingId, String adminUsername) {
        Optional<Coworking> optionalCoworking = coworkingRepository.findById(coworkingId);
        if (optionalCoworking.isEmpty()) {
            log.error("Ошибка удаления: коворкинг с ID {} не найден!", coworkingId);
            return new ResponseDTO("error", "Нет такого коворкинга.");
        }
        boolean b = spotRepository.existsByCoworkingIdAndActiveBookingIsTrue(coworkingId);
        if (b) {
            log.warn("Удаление невозможно: у коворкинга с ID {} есть активные бронирования", coworkingId);
            return new ResponseDTO("error", "Удаление невозможно. В коворкинге имеются активные бронирования.");
        }
        coworkingRepository.deleteById(coworkingId);
        log.info("Администратором {} был удален коворкинг с ID: {}", adminUsername, coworkingId);
        return new ResponseDTO("success", "Коворкинг удален.");
    }


    @Transactional
    public ResponseDTO freezeCoworking(UUID coworkingId, boolean freeze, String adminUsername) {
        Optional<Coworking> optionalCoworking = coworkingRepository.findById(coworkingId);
        if (optionalCoworking.isEmpty()) {
            log.error("Ошибка заморозки: коворкинг с ID {} не найден!", coworkingId);
            return new ResponseDTO("error", "Нет такого коворкинга.");
        }
        boolean activeBookings = spotRepository.existsByCoworkingIdAndActiveBookingIsTrue(coworkingId);
        if (activeBookings) {
            log.warn("Заморозка невозможна: у коворкинга с ID {} есть активные бронирования", coworkingId);

            return new ResponseDTO("error", "Заморозка невозможна. В коворкинге имеются активные бронирования.");
        }
        Coworking coworking = optionalCoworking.get();
        coworking.setFreeze(freeze);
        coworkingRepository.save(coworking);
        log.info("Администратором {} успешно {} коворкинг с ID {}", adminUsername, freeze ? "заморожен" : "разморожен", coworkingId);
        String message = freeze ? "Коворкинг заморожен." : "Коворкинг разморожен.";
        return new ResponseDTO("success", message);
    }


    @Transactional
    public ResponseDTO changePrice(UUID coworkingId, PriceChangingDTO priceChangingDTO, String adminUsername) {
        log.info("Администратор {} выполнил запрос на изменение цены в коворкинге: {}", adminUsername, coworkingId);

        Optional<Coworking> optionalCoworking = coworkingRepository.findById(coworkingId);
        if (optionalCoworking.isEmpty()) {
            log.error("Ошибка изменения цен: коворкинг с ID {} не найден!", coworkingId);
            return new ResponseDTO("error", "Нет такого коворкинга.");
        }
        Coworking coworking = optionalCoworking.get();
        if (priceChangingDTO.getNewDailyPrice() != null) {
            coworking.setDailyPrice(priceChangingDTO.getNewDailyPrice());
        }
        if (priceChangingDTO.getNewMonthlyPrice() != null) {
            coworking.setMonthlyPrice(priceChangingDTO.getNewMonthlyPrice());
        }

        coworkingRepository.save(coworking);
        log.info("Цены для коворкинга с ID {} были изменены администратором {}", coworkingId, adminUsername);
        return new ResponseDTO("success", "Цены успешно изменены.");
    }


    @Transactional(readOnly = true)
    public CoworkingStatsForAdminDTO getStats(UUID coworkingId, String adminUsername) {
        log.info("Администратор {} запросил статистику коворкинга {}", adminUsername, coworkingId);
        Optional<Coworking> optionalCoworking = coworkingRepository.findById(coworkingId);
        if (optionalCoworking.isEmpty()) {
            log.error("Ошибка вывода статистики. Коворкинг с ID {} не найден!", coworkingId);
            throw new CoworkingNotFoundException("Коворкинг не найден.");
        }
        Coworking coworking = optionalCoworking.get();
        return new CoworkingStatsForAdminDTO(
                coworking.getName(),
                coworking.getLocation(),
                coworking.getTotalSpots(),
                coworking.getAvailableSpots(),
                coworking.getBookedSpots(),
                coworking.getDailyPrice(),
                coworking.getMonthlyPrice(),
                coworking.isFreeze());

    }


    @Transactional
    public ResponseDTO blockClient(String username, boolean block, String adminUsername) {
        Optional<Client> optionalClient = clientRepository.findByUsername(username);
        if (optionalClient.isEmpty()) {
            log.error("Ошибка блокировки клиента! Клиент с Username {} не найден!", username);
            return new ResponseDTO("error", "Нет такого пользователя");
        }
        Client client = optionalClient.get();
        if (block && client.getBookedSpotId() != null) {
            Spot spot = spotRepository.findByClientId(client.getId()).orElseThrow();
            spot.setClientId(null);
            spot.setStartBookDate(null);
            spot.setEndBookDate(null);
            spot.setActiveBooking(false);
            client.setBookedSpotId(null);
            spot.getCoworking().setBookedSpots(spot.getCoworking().getBookedSpots() - 1);
            spotRepository.save(spot);
        }
        client.setBlocked(block);
        clientRepository.save(client);
        log.info("Администратор {} успешно {} клиента с ID {}", adminUsername, block ? "заблокировал" : "разблокировал", client.getUsername());
        String message = block ? "Пользователь заблокирован, бронь удалена." : "Пользователь разблокирован.";
        return new ResponseDTO("success", message);
    }
}