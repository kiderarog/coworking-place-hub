package io.neif.coworkingplacehub.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessEvent {
    private String clientId;
    private int amount;

}