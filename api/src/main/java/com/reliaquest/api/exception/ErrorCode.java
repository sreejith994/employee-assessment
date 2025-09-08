package com.reliaquest.api.exception;

public enum ErrorCode {
    ERROR_CALLING_MOCK_EMPLOYEE_SERVICE("ERROR_CALLING_MOCK_EMPLOYEE_SERVICE"),
    INVALID_INPUT("INVALID_INPUT"),
    EMPLOYEE_NOT_FOUND("EMPLOYEE_NOT_FOUND"),
    NO_EMPLOYEES_FOUND("NO_EMPLOYEES_FOUND"),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS"),;

    private final String value;
    ErrorCode(String value) { this.value = value; }
    public String getValue() { return value; }
}
