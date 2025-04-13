package com.workingspacehub.payment_microservice.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.workingspacehub.payment_microservice.events.PaymentSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PaymentService {

    private final KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate;

    @Autowired
    public PaymentService(KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    public ResponseEntity<Map<String, String>> createCheckoutSession(String clientId, int amount) {
        Stripe.apiKey = stripeApiKey;

        try {
            Session session = Session.create(Map.of(
                    "payment_method_types", List.of("card"),
                    "line_items", List.of(Map.of(
                            "price_data", Map.of(
                                    "currency", "usd",
                                    "product_data", Map.of("name", "Пополнение баланса в Coworking-Place-Hub"),
                                    "unit_amount", amount * 100
                            ),
                            "quantity", 1
                    )),
                    "mode", "payment",
                    "success_url", "http://localhost:8082/payment/success?sessionId={CHECKOUT_SESSION_ID}",
                    "cancel_url", "http://localhost:8082/payment/cancel",
                    "client_reference_id", clientId,
                    "metadata", Map.of("amount", String.valueOf(amount))
            ));

            log.info("Создана check-out сессия {}", session.getUrl());
            return ResponseEntity.ok(Map.of("checkoutUrl", session.getUrl()));
        } catch (StripeException e) {
            log.error("Ошибка при создании check-out-сессии {}", e.getMessage(), e.getCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signature) {
        try {
            String webhookSecret = stripeApiKey;
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            log.info("Webhook успешно проверен. Тип события: {}", event.getType());
            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getData().getObject();
                String clientId = session.getClientReferenceId();

                Long totalAmount = session.getAmountTotal();
                if (totalAmount == null) {
                    log.warn("Ошибка обработки платежа: сумма пополнения отсутствует. userId: {}", clientId);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка при указании суммы пополнения.");
                }
                int amount = totalAmount.intValue();
                log.info("Оплата завершена! userId: {}, сумма: {}", clientId, amount);
                sendPaymentSuccess(clientId, amount); // Отправка события в Kafka
            }
            return ResponseEntity.ok("Тестовый webhook успешно отправлен.");
        } catch (Exception e) {
            log.error("Ошибка обработки вебхука: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Непредвиденная ошибка: " + e.getMessage());
        }
    }

    public void sendPaymentSuccess(String clientId, int amount) {
        PaymentSuccessEvent event = new PaymentSuccessEvent(clientId, amount);
        kafkaTemplate.send("payment-success-event-topic", String.valueOf(clientId), event);
        log.info("Событие успешного платежа отправлено в Kafka. userId: {}, сумма: {}", clientId, amount);    }
}
