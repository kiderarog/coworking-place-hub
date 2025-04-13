package com.workingspacehub.notification_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordRestoreDTO {

    private String email;
    private Integer otpCode;
    private String name;
}
