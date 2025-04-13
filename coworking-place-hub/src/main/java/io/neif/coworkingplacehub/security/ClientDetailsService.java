package io.neif.coworkingplacehub.security;

import io.neif.coworkingplacehub.models.Client;
import io.neif.coworkingplacehub.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;

    @Autowired
    public ClientDetailsService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Client> clientOptional = clientRepository.findByUsername(username);
        if (clientOptional.isPresent()) {
            return new ClientDetails(clientOptional.get());
        } else {
            throw new UsernameNotFoundException("Пользователь не найден.");
        }
    }
}
