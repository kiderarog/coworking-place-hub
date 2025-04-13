package io.neif.coworkingplacehub.handlers;


import io.neif.coworkingplacehub.events.PaymentSuccessEvent;
import io.neif.coworkingplacehub.services.ClientService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSuccessEventHandler {

    private final ClientService clientService;

    public PaymentSuccessEventHandler(ClientService clientService) {
        this.clientService = clientService;
    }

    @KafkaListener(topics = "payment-success-event-topic", groupId = "monolith-payment-group")
    public void handle(PaymentSuccessEvent paymentSuccessEvent) {
        System.out.print("Получено сообщение из Kafka: " + paymentSuccessEvent.getClientId() + " " + paymentSuccessEvent.getAmount());

        try {
            clientService.creditMoneyToClientVirtualBalance(
                    paymentSuccessEvent.getClientId(),
                    paymentSuccessEvent.getAmount()
            );
            System.out.println("Баланс пользователя " + paymentSuccessEvent.getClientId() + " обновлен.");
        } catch (Exception e) {
            System.out.println("Ошибка при обновлении баланса: " + e.getMessage());
        }
    }
}



