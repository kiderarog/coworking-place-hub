package io.neif.coworkingplacehub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    private String username;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private Double balance;
    private UUID bookedSpotId;

}
