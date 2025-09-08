package com.reliaquest.api.mapper;

import com.reliaquest.api.model.EmployeeDto;
import com.reliaquest.api.model.MockEmployee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeDto map(MockEmployee mockEmployeeResponse) {
        return new EmployeeDto(
                mockEmployeeResponse.getId(),
                mockEmployeeResponse.getName(),
                mockEmployeeResponse.getSalary(),
                mockEmployeeResponse.getAge(),
                mockEmployeeResponse.getTitle(),
                mockEmployeeResponse.getEmail()
        );
    }
}
