package io.neif.coworkingplacehub.services;

import io.neif.coworkingplacehub.models.Client;
import io.neif.coworkingplacehub.models.Spot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PyrusService {
    private final WebClient webClient;

    @Value("${pyrus.auth-url}")
    private String authUrl;

    @Value("${pyrus.add-task-url}")
    private String taskUrl;

    @Value("${pyrus.login}")
    private String login;

    @Value("${pyrus.secret-key}")
    private String securityKey;

    @Autowired
    public PyrusService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // Метод для получения токена для взаимодействия с Pyrus CRM API.
    public String getAccessToken() {
        return webClient.post()
                .uri(authUrl)
                .bodyValue(Map.of(
                        "login", login,
                        "security_key", securityKey
                )).retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .block();

    }

    // Метод для добавления пользователя в CRM при регистрации.
    public void addClientCRM(Client client) {
        log.info("Отправка зарегистрированного клиента с ID {} в CRM.", client.getId());

        String token = getAccessToken();
        Map<String, Object> requestBody = Map.of(
                "form_id", 2291890,
                "fields", List.of(
                        Map.of("id", 1, "value", client.getId()),
                        Map.of("id", 6, "value", client.getUsername()),
                        Map.of("id", 3, "value", java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))),
                        Map.of("id", 4, "value", client.getPhone()),
                        Map.of("id", 5, "value", client.getEmail())
                )
        );

        webClient.post()
                .uri(taskUrl)
                .header("Authorization", "Bearer " + token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .toBodilessEntity()
                .block();

        log.info("Пользователь с ID {} успешно отравлен в CRM.", client.getId());

    }

    // Метод для добавления бронирования в CRM при успешном осуществлении бронирования.
    public void addBookingCRM(Spot spot) {
        log.info("Отправка бронирования {} в CRM для коворкинга {}.", spot.getId(), spot.getCoworking().getId());
        String token = getAccessToken();
        Map<String, Object> requestBody = Map.of(
                "form_id", 2291887,
                "fields", List.of(
                        Map.of("id", 1, "value", spot.getId()),
                        Map.of("id", 3, "value", spot.getCoworking().getId()),
                        Map.of("id", 4, "value", spot.getClientId()),
                        Map.of("id", 5, "value", spot.getStartBookDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))),
                        Map.of("id", 6, "value",
                                (spot.getEndBookDate() != null
                                        ? spot.getEndBookDate()
                                        : spot.getStartBookDate().plusDays(1)
                                ).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        )
                )
        );


        webClient.post()
                .uri(taskUrl)
                .header("Authorization", "Bearer " + token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .toBodilessEntity()
                .block();

        log.info("Бронирование {} успешно отправлено в CRM.", spot.getId());
    }
}