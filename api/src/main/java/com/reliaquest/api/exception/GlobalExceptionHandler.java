package com.reliaquest.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = MockEmployeeServiceFailureException.class)
    public ResponseEntity<ErrorResponse> handleMockEmployeeServiceFailure(MockEmployeeServiceFailureException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .code(ErrorCode.ERROR_CALLING_MOCK_EMPLOYEE_SERVICE.getValue())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .code(ErrorCode.INVALID_INPUT.getValue())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(value = EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFoundException(EmployeeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .code(ErrorCode.EMPLOYEE_NOT_FOUND.getValue())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(value = NoEmployeesFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoEmployeesFoundException(NoEmployeesFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .code(ErrorCode.NO_EMPLOYEES_FOUND.getValue())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(value = MockEmployeeServiceTooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequestsException(MockEmployeeServiceTooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                ErrorResponse.builder()
                        .code(ErrorCode.TOO_MANY_REQUESTS.getValue())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );


    }

    @ExceptionHandler(value = InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputExceptionException(InvalidInputException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .code(ErrorCode.INVALID_INPUT.getValue())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );


    }


}
