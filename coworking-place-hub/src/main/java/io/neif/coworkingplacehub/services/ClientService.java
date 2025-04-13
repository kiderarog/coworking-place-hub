package io.neif.coworkingplacehub.services;

import io.neif.coworkingplacehub.dto.ClientDTO;
import io.neif.coworkingplacehub.dto.EditProfileDTO;
import io.neif.coworkingplacehub.dto.ResponseDTO;
import io.neif.coworkingplacehub.models.Client;
import io.neif.coworkingplacehub.repositories.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ClientService {
    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // Метод для отдачи личной информации пользователя для отображения его профиля в приложении.
    @Transactional(readOnly = true)
    public ClientDTO showProfile(String userName) {
        Optional<Client> optionalClient = clientRepository.findByUsername(userName);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            return new ClientDTO(
                    client.getUsername(),
                    client.getName(),
                    client.getSurname(),
                    client.getEmail(),
                    client.getPhone(),
                    client.getBalance(),
                    client.getBookedSpotId()
            );
        }
        throw new UsernameNotFoundException("Пользователь не найден.");
    }

    // Метод для изменения личной информации в профиле пользователя.
    @Transactional
    public ResponseDTO editProfile(String clientName, EditProfileDTO editProfileDTO) {
        Optional<Client> optionalClient = clientRepository.findByUsername(clientName);
        if (optionalClient.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь не найден.");
        }
        Client client = optionalClient.get();
        if (editProfileDTO.getNewName() != null) {
            client.setName(editProfileDTO.getNewName());
        }
        if (editProfileDTO.getNewSurname() != null) {
            client.setSurname(editProfileDTO.getNewSurname());
        }
        clientRepository.save(client);
        log.info("Пользователь {} осуществил изменения в профиле.", clientName);
        return new ResponseDTO("success", "Профиль пользователя успешно обновлен.");
    }


    @Transactional
    public ResponseDTO deleteProfile(String clientName) {
        Client client = clientRepository.findByUsername(clientName)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден."));
        log.error("Попытка удаления несуществующего профиля {}", clientName);
        if (client.getBookedSpotId() != null) {
            log.warn("Попытка удаления профиля пользователя {} с активной бронью.", clientName);
            return new ResponseDTO("error", "У Вас имеется активная бронь.");
        }
        clientRepository.delete(client);
        log.info("Удаление профиля пользователя {} из личного кабинета", clientName);
        return new ResponseDTO("success", "Ваш аккаунт, " +
                client.getName() + " успешно удален.");
    }

    // Метод для списания денежных средств с виртуального баланса пользователя в приложении.
    @Transactional
    public void writeOffMoneyFromUserVirtualBalance(Client client, Integer penaltyMoneyToWriteOff) {
        client.setBalance(client.getBalance() - penaltyMoneyToWriteOff);
        clientRepository.save(client);
        log.info("С виртуального баланса пользователя {} списана сумма {} за бронирование",
                client.getId(), penaltyMoneyToWriteOff);
    }


    // Метод для пополнения виртуального баланса пользователя.
    // Метод срабатывает в случае успешного пополнения баланса через платежную систему Stripe +
    // После получения сообщения от Payment-микросервиса через Apache Kafka (PaymentSuccessEventHandler)
    @Transactional
    public void creditMoneyToClientVirtualBalance(String clientId, Integer amountOfMoney) {
        Optional<Client> optionalClient = clientRepository.findById(UUID.fromString(clientId));

        if (optionalClient.isEmpty()) {
            log.error("Попытка зачисления денежных средств на счет несуществующего пользователя.");
            throw new UsernameNotFoundException("Нет такого пользователя.");
        }
        Client client = optionalClient.get();
        client.setBalance(client.getBalance() + amountOfMoney);
        clientRepository.save(client);
        log.info("Пользователь {} пополнил баланс на {}", client.getId(), amountOfMoney);
    }
}