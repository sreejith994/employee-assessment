package com.reliaquest.api.exception;

public class NoEmployeesFoundException extends RuntimeException {
    public NoEmployeesFoundException(String message) {
        super(message);
    }
}
