package io.neif.coworkingplacehub.security;

import io.neif.coworkingplacehub.models.Client;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class ClientDetails implements UserDetails {
    private final Client client;

    public ClientDetails(Client client) {
        this.client = client;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Roles role = client.getRole();
        return Collections.singleton(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return client.getPassword();
    }

    @Override
    public String getUsername() {
        return client.getUsername();
    }

    public String getClientId() {
        return String.valueOf(client.getId());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}