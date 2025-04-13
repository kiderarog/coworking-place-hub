package io.neif.coworkingplacehub.services;

import io.neif.coworkingplacehub.dto.BookingDTO;
import io.neif.coworkingplacehub.dto.ResponseDTO;
import io.neif.coworkingplacehub.exception.ErrorWorkspaceBookingException;
import io.neif.coworkingplacehub.models.Client;
import io.neif.coworkingplacehub.models.Coworking;
import io.neif.coworkingplacehub.models.Spot;
import io.neif.coworkingplacehub.repositories.ClientRepository;
import io.neif.coworkingplacehub.repositories.CoworkingRepository;
import io.neif.coworkingplacehub.repositories.SpotRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class BookingService {
    private final ClientRepository clientRepository;
    private final SpotRepository spotRepository;
    private final CoworkingRepository coworkingRepository;
    private final PyrusService pyrusService;
    private final ClientService clientService;

    public BookingService(ClientRepository clientRepository, SpotRepository spotRepository, CoworkingRepository coworkingRepository, PyrusService pyrusService, ClientService clientService) {
        this.clientRepository = clientRepository;
        this.spotRepository = spotRepository;
        this.coworkingRepository = coworkingRepository;
        this.pyrusService = pyrusService;
        this.clientService = clientService;
    }

    // Метод для бронирования рабочего места в коворкинге, проверяя доступность и статус клиента.
    @Transactional
    public ResponseDTO bookWorkingSpot(String clientIdString, BookingDTO bookingDTO) {
        UUID clientId = UUID.fromString(clientIdString);
        log.info("Пользователь {} запросил бронирование места в коворкинге {}.", clientId, bookingDTO.getCoworkingId());

        Spot randomSpot = spotRepository.findFirstByCoworkingIdAndActiveBookingIsFalse(bookingDTO.getCoworkingId())
                .orElseThrow(() -> {
                    log.error("Ошибка бронирования: В коворкинге {} нет свободных мест.", bookingDTO.getCoworkingId());
                    return new RuntimeException("В этом коворкинге нет свободных мест.");
                });

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> {
                    log.error("Ошибка бронирования: клиент {} не найден.", clientId);
                    return new EntityNotFoundException("Нет такого пользователя.");
                });

        Coworking coworking = coworkingRepository.findById(bookingDTO.getCoworkingId())
                .orElseThrow(() -> {
                    log.error("Ошибка бронирования: коворкинг {} не найден.", bookingDTO.getCoworkingId());
                    return new EntityNotFoundException("Коворкинг не найден.");
                });

        if (coworking.isFreeze()) {
            log.warn("Попытка бронирования в замороженном коворкинге {} пользователем {}.", bookingDTO.getCoworkingId(), clientId);
            throw new ErrorWorkspaceBookingException("Коворкинг заморожен! Бронирования недоступны.");
        }
        if (client.getBookedSpotId() != null) {
            log.warn("Ошибка бронирования: клиент {} уже имеет активное бронирование.", clientId);
            return new ResponseDTO("error", "У пользователя уже имеется активное бронирование");
        }
        if (client.isBlocked()) {
            log.warn("Ошибка бронирования: клиент {} заблокирован, бронирование невозможно.", clientId);
            return new ResponseDTO("error", "Пользователь заблокирован. Бронирование невозможно.");
        }

        ResponseDTO response = bookingDTO.isMonth()
                ? bookForMonth(coworking, randomSpot, client, bookingDTO)
                : bookingDTO.getStartDate() != null && bookingDTO.getEndDate() == null
                ? bookSpot(coworking, randomSpot, client)
                : bookingDTO.getStartDate() != null && bookingDTO.getEndDate() != null
                ? bookForSomeDays(coworking, randomSpot, client, bookingDTO)
                : new ResponseDTO("error", "Непредвиденная ошибка при бронировании.");

        if ("success".equals(response.getStatus())) {
            client.setBookedSpotId(randomSpot.getId());
            spotRepository.save(randomSpot);
            clientRepository.save(client);
            coworkingRepository.save(coworking);
            pyrusService.addBookingCRM(randomSpot);

            log.info("Успешное бронирование: клиент {} забронировал место {} в коворкинге {} на {}.",
                    clientId, randomSpot.getId(), bookingDTO.getCoworkingId(),
                    bookingDTO.isMonth() ? "1 месяц" : bookingDTO.getStartDate() + " - " + bookingDTO.getEndDate());
        } else {
            log.error("Ошибка при бронировании: клиент {} не смог забронировать место.", clientId);
        }

        return response;
    }



    private ResponseDTO bookSpot(Coworking coworking, Spot spot, Client client) {
        if (client.getBalance() >= coworking.getDailyPrice()) {
            spot.setClientId(client.getId());
            spot.setStartBookDate(LocalDateTime.now());
            spot.setEndBookDate(spot.getStartBookDate().plusDays(1).toLocalDate().atStartOfDay());
            spot.setActiveBooking(true);
            clientService.writeOffMoneyFromUserVirtualBalance(client, coworking.getDailyPrice());
            coworking.setBookedSpots(coworking.getBookedSpots() + 1);
            spot.setActiveBooking(true);


            return new ResponseDTO("success", "Место успешно забронировано.");
        }
        return new ResponseDTO("error", "Недостаточно средств для бронирования.");
    }

    private ResponseDTO bookForSomeDays(Coworking coworking, Spot spot, Client client, BookingDTO bookingDTO) {
        long days = ChronoUnit.DAYS.between(bookingDTO.getStartDate(), bookingDTO.getEndDate());

        if (client.getBalance() >= coworking.getDailyPrice() * days) {
            spot.setClientId(client.getId());
            spot.setStartBookDate(bookingDTO.getStartDate().toLocalDate().atStartOfDay());
            spot.setEndBookDate(bookingDTO.getEndDate().toLocalDate().atStartOfDay());
            clientService.writeOffMoneyFromUserVirtualBalance(client, (int) (coworking.getDailyPrice() * days));
            coworking.setBookedSpots(coworking.getBookedSpots() + 1);
            spot.setActiveBooking(true);

            return new ResponseDTO("success", "Место забронировано на " + days + " дней.");
        }
        return new ResponseDTO("error", "Недостаточно средств для бронирования на выбранный период.");
    }

    private ResponseDTO bookForMonth(Coworking coworking, Spot spot, Client client, BookingDTO bookingDTO) {
        if (client.getBalance() >= coworking.getMonthlyPrice()) {
            spot.setClientId(client.getId());
            spot.setStartBookDate(bookingDTO.getStartDate().toLocalDate().atStartOfDay());
            spot.setEndBookDate(spot.getStartBookDate().plusDays(30));
            spot.setActiveBooking(true);
            clientService.writeOffMoneyFromUserVirtualBalance(client, coworking.getMonthlyPrice());
            coworking.setBookedSpots(coworking.getBookedSpots() + 1);
            spot.setActiveBooking(true);
            System.out.println("startDate: " + spot.getStartBookDate());
            System.out.println("endDate: " + spot.getEndBookDate());


            return new ResponseDTO("success", "Место в коворкинге забронировано на 30 дней.");
        }
        return new ResponseDTO("error", "Недостаточно средств для бронирования коворкинга на месяц.");
    }

    // Метод, который срабатывает ежедневно в 00:01 и удаляет из системы истекшие бронирования и освобождает места.
    @Scheduled(cron = "${cron.expression}")
    @Transactional
    public void releaseSpots() {
        List<Spot> allSpotList = spotRepository.findAll();
        List<Spot> spotsForRelease = new ArrayList<>();
        List<Client> clientsForRelease = new ArrayList<>();
        for (Spot spot : allSpotList) {
            if (spot.getEndBookDate() != null && spot.getEndBookDate().isBefore(LocalDateTime.now())) {
                UUID clientId = spot.getClientId();
                spot.setClientId(null);
                spot.setStartBookDate(null);
                spot.setEndBookDate(null);
                spot.setActiveBooking(false);
                spotsForRelease.add(spot);
                spot.getCoworking().setBookedSpots(spot.getCoworking().getBookedSpots() - 1);
                if (clientId != null) {
                    Client client = clientRepository.findById(clientId).orElseThrow();
                    client.setBookedSpotId(null);
                    clientsForRelease.add(client);
                }
            }
        }
        if (!clientsForRelease.isEmpty()) {
            clientRepository.saveAll(clientsForRelease);
        }
        if (!spotsForRelease.isEmpty()) {
            spotRepository.saveAll(spotsForRelease);
            log.info("Освобождено {} мест после истечения срока бронирования.", spotsForRelease.size());
        } else {
            log.info("Ни одно место не было освобождено — активных бронирований нет или срок брони не истек.");
        }
    }
}