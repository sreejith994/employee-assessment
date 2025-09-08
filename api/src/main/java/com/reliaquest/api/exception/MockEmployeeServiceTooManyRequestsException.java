package com.reliaquest.api.exception;

public class MockEmployeeServiceTooManyRequestsException extends RuntimeException {
    public MockEmployeeServiceTooManyRequestsException(String message) {
        super(message);
    }
}
