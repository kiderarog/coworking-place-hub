package io.neif.coworkingplacehub.services;

import io.neif.coworkingplacehub.dto.AuthClientDTO;
import io.neif.coworkingplacehub.dto.ResponseDTO;
import io.neif.coworkingplacehub.models.Client;
import io.neif.coworkingplacehub.repositories.ClientRepository;
import io.neif.coworkingplacehub.security.JWTUtil;
import io.neif.coworkingplacehub.security.Roles;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class AuthService {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JWTUtil jwtUtil;
    private final PyrusService pyrusService;



    public AuthService(ClientRepository clientRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper,
                       JWTUtil jwtUtil, PyrusService pyrusService) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.jwtUtil = jwtUtil;
        this.pyrusService = pyrusService;
    }

    @Transactional
    public void saveClient(AuthClientDTO authClientDTO) {
        Client client = modelMapper.map(authClientDTO, Client.class);
        client.setPassword(passwordEncoder.encode(client.getPassword()));
        client.setRole(Roles.ROLE_USER);
        client.setBalance(0.0);
        client.setBlocked(false);
        clientRepository.save(client);
        pyrusService.addClientCRM(client);
        log.info("Зарегистрирован новый пользователь {}. Информация отправлена в CRM.", client.getUsername());

    }

    // Метод для получения JWT-токена для авторизации пользователя.
    @Transactional(readOnly = true)
    public ResponseDTO getAuthorizationToken(AuthClientDTO authClientDTO) {
        Optional<Client> optionalClient =
                clientRepository.findByUsername(authClientDTO.getUsername());
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            if (passwordEncoder.matches(authClientDTO.getPassword(), client.getPassword())) {
                log.info("Пользователь {} запросил токен аутентификации.", client.getUsername());
                return new ResponseDTO("token", jwtUtil.generateToken(client.getUsername(),
                        client.getRole().name(), client.getId()));
            }
        }
        log.error("Пользователь {} запросил токен аутентификации с неверными" +
                " аутентификационными данными.", authClientDTO.getUsername());
        return new ResponseDTO("error", "Неверные имя пользователя или пароль.");
    }
}

