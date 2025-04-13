package com.workingspacehub.payment_microservice.controllers;


import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.workingspacehub.payment_microservice.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestParam String clientId, @RequestParam int amount) {
        return paymentService.createCheckoutSession(clientId, amount);
    }

    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam String sessionId) {
        try {
            // 🔥 Загружаем данные из Checkout Session
            Session session = Session.retrieve(sessionId);
            String clientId = session.getClientReferenceId();
            int amount = Integer.parseInt(session.getMetadata().get("amount"));

            System.out.println("✅ Оплата успешна! userId: " + clientId + ", amount: " + amount);
            paymentService.sendPaymentSuccess(clientId, amount); // ✅ Отправляем данные в Kafka

            return ResponseEntity.ok("Оплата завершена успешно!");
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обработке платежа: " + e.getMessage());
        }
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> paymentCancel() {
        return ResponseEntity.ok("Оплата отменена.");
    }
}
