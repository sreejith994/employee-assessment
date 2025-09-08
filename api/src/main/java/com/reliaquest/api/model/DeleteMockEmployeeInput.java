package com.reliaquest.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteMockEmployeeInput {

    @NotBlank
    private String name;
}
