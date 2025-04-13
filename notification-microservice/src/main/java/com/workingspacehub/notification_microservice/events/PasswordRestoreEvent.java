package com.workingspacehub.notification_microservice.events;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PasswordRestoreEvent {
    private String email;
    private Integer otpCode;
    private String name;
}
