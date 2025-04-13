package com.workingspacehub.notification_microservice.deserializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workingspacehub.notification_microservice.events.PasswordRestoreEvent;
import com.workingspacehub.notification_microservice.exceptions.DeserializerException;
import org.apache.kafka.common.serialization.Deserializer;

public class PasswordRestoreEventDeserializer implements Deserializer<PasswordRestoreEvent> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PasswordRestoreEvent deserialize(String topic, byte[] data) {
        try {
            if (data == null || data.length == 0) {
                return null;
            }
            return objectMapper.readValue(data, PasswordRestoreEvent.class);
        } catch (Exception e) {
            throw new DeserializerException("Ошибка при десериализации сообщения");
        }
    }
}
