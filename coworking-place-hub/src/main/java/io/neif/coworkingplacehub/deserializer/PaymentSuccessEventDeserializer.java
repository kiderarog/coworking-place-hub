package io.neif.coworkingplacehub.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.neif.coworkingplacehub.events.PaymentSuccessEvent;
import io.neif.coworkingplacehub.exception.DeserializerException;
import org.apache.kafka.common.serialization.Deserializer;

public class PaymentSuccessEventDeserializer implements Deserializer<PaymentSuccessEvent> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PaymentSuccessEvent deserialize(String topic, byte[] data) {
        try {
            if (data == null || data.length == 0) {
                return null;
            }
            return objectMapper.readValue(data, PaymentSuccessEvent.class);
        } catch (Exception e) {
            throw new DeserializerException("Ошибка при десериализации сообщения");
        }
    }
}
