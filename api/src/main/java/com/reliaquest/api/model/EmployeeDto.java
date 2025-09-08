package com.reliaquest.api.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record EmployeeDto(
        UUID id,
        String name,
        Integer salary,
        Integer age,
        String title,
        String email
) {

}
