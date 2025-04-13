package com.workingspacehub.notification_microservice.exceptions;

public class DeserializerException extends RuntimeException {
    public DeserializerException(String message) {
        super(message);
    }
}
