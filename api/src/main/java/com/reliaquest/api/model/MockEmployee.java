package com.reliaquest.api.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

/**
 * Ideally the contract between services should be central repository to prevent duplication
 * however in this exercise, due to limitations, the response classes are copied across
 */
@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(MockEmployee.PrefixNamingStrategy.class)
public class MockEmployee {

    private UUID id;
    private String name;
    private Integer salary;
    private Integer age;
    private String title;
    private String email;

    public static MockEmployee from(@NonNull String email, @NonNull CreateMockEmployeeInput input) {
        return MockEmployee.builder()
                .id(UUID.randomUUID())
                .email(email)
                .name(input.getName())
                .salary(input.getSalary())
                .age(input.getAge())
                .title(input.getTitle())
                .build();
    }

    static class PrefixNamingStrategy extends PropertyNamingStrategies.NamingBase {

        @Override
        public String translate(String propertyName) {
            if ("id".equals(propertyName)) {
                return propertyName;
            }
            return "employee_" + propertyName;
        }
    }
}
