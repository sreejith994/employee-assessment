package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.InvalidInputException;
import com.reliaquest.api.exception.MockEmployeeServiceFailureException;
import com.reliaquest.api.exception.NoEmployeesFoundException;
import com.reliaquest.api.model.CreateMockEmployeeInput;
import com.reliaquest.api.model.EmployeeDto;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerIT {

    @Autowired 
    MockMvc mockMvc;
    
    @Autowired 
    ObjectMapper objectMapper;
    
    @MockBean 
    EmployeeService employeeService;


    @Test
    void getAllEmployees_ok() throws Exception {
        when(employeeService.getAllEmployees())
                .thenReturn(List.of(dto("186d753a-b43a-476a-bcfa-d0f83e8793e9","clark",100),
                        dto("2c5e68c4-587c-4d19-a581-549314f5918f","bruce",120)));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("clark"))
                .andExpect(jsonPath("$[1].name").value("bruce"))
                .andExpect(jsonPath("$[0].salary").value(100))
                .andExpect(jsonPath("$[1].salary").value(120));
    }

    @Test
    void getAllEmployees_error() throws Exception {
        when(employeeService.getAllEmployees())
                .thenThrow(new MockEmployeeServiceFailureException(""));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isInternalServerError());

    }

    @Test
    void getEmployeesByNameSearch_ok() throws Exception {
        when(employeeService.getAllEmployeesBySearch("peter"))
                .thenReturn(List.of(dto("2c5e68c4-587c-4d19-a581-549314f5918f","peter",100)));

        mockMvc.perform(get("/api/v1/employee/search/peter"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("peter"))
                .andExpect(jsonPath("$[0].salary").value(100));
    }

    @Test
    void getEmployeesByNameSearch_invalidInputException() throws Exception {
        when(employeeService.getAllEmployeesBySearch("peter"))
                .thenThrow(new InvalidInputException(""));

        mockMvc.perform(get("/api/v1/employee/search/peter"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getEmployeeById_ok() throws Exception {
        when(employeeService.getEmployeeById("2c5e68c4-587c-4d19-a581-549314f5918f"))
                .thenReturn(dto("2c5e68c4-587c-4d19-a581-549314f5918f","logan",150));

        mockMvc.perform(get("/api/v1/employee/2c5e68c4-587c-4d19-a581-549314f5918f", "2c5e68c4-587c-4d19-a581-549314f5918f"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("2c5e68c4-587c-4d19-a581-549314f5918f"))
                .andExpect(jsonPath("$.name").value("logan"))
                .andExpect(jsonPath("$.salary").value(150));
    }

    @Test
    void getEmployeeById_invalidInputException() throws Exception {
        when(employeeService.getEmployeeById("2c5e68c4-587c-4d19-a581-549314f5918f"))
                .thenThrow(new InvalidInputException(""));

        mockMvc.perform(get("/api/v1/employee/2c5e68c4-587c-4d19-a581-549314f5918f", "2c5e68c4-587c-4d19-a581-549314f5918f"))
                .andExpect(status().isBadRequest());

    }

    @Test
    void getEmployeeById_EmployeeNotFoundException() throws Exception {
        when(employeeService.getEmployeeById("2c5e68c4-587c-4d19-a581-549314f5918f"))
                .thenThrow(new EmployeeNotFoundException(""));

        mockMvc.perform(get("/api/v1/employee/2c5e68c4-587c-4d19-a581-549314f5918f", "2c5e68c4-587c-4d19-a581-549314f5918f"))
                .andExpect(status().isNotFound());

    }

    @Test
    void getEmployeeById_error() throws Exception {
        when(employeeService.getEmployeeById("2c5e68c4-587c-4d19-a581-549314f5918f"))
                .thenThrow(new MockEmployeeServiceFailureException(""));

        mockMvc.perform(get("/api/v1/employee/2c5e68c4-587c-4d19-a581-549314f5918f", "2c5e68c4-587c-4d19-a581-549314f5918f"))
                .andExpect(status().isInternalServerError());

    }

    @Test
    void getHighestSalary_ok() throws Exception {
        when(employeeService.getHighestSalary()).thenReturn(99999);

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("99999"));
    }

    @Test
    void getHighestSalary_noEmployeesFoundException() throws Exception {
        when(employeeService.getHighestSalary()).thenThrow(new NoEmployeesFoundException(""));

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isNotFound());

    }

    @Test
    void getTopTenSalaryNames_ok() throws Exception {
        when(employeeService.getTopTenSalaryEmployees())
                .thenReturn(List.of("jean","xavier"));

        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value("jean"))
                .andExpect(jsonPath("$[1]").value("xavier"));
    }

    @Test
    void getTopTenSalaryNames_noEmployeesFound() throws Exception {
        when(employeeService.getTopTenSalaryEmployees())
                .thenThrow(new NoEmployeesFoundException(""));

        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isNotFound());
        ;
    }

    @Test
    void createEmployee_ok() throws Exception {
        CreateMockEmployeeInput input = CreateMockEmployeeInput.builder()
                                        .name("tony")
                                        .age(32)
                                        .title("mr")
                                        .salary(9999)
                                        .build();
        when(employeeService.createEmployee(any(CreateMockEmployeeInput.class)))
                .thenReturn(dto("2c5e68c4-587c-4d19-a581-549314f5918f","tony",9999));

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("tony"))
                .andExpect(jsonPath("$.salary").value(9999));
    }

    @Test
    void createEmployee_error() throws Exception {
        CreateMockEmployeeInput input = CreateMockEmployeeInput.builder()
                .name("tony")
                .age(32)
                .title("mr")
                .salary(9999)
                .build();
        when(employeeService.createEmployee(any(CreateMockEmployeeInput.class)))
                .thenThrow(new MockEmployeeServiceFailureException(""));

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError());
        ;
    }

    @Test
    void createEmployee_invalid_400() throws Exception {
        CreateMockEmployeeInput bad = CreateMockEmployeeInput.builder().build();

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployee_ok() throws Exception {
        when(employeeService.deleteEmployee("321")).thenReturn("deleted");

        mockMvc.perform(delete("/api/v1/employee/{id}", "321"))
                .andExpect(status().isOk())
                .andExpect(content().string("deleted"));
    }

    @Test
    void deleteEmployee_InvalidInputException() throws Exception {
        when(employeeService.deleteEmployee("321")).thenThrow(new InvalidInputException(""));

        mockMvc.perform(delete("/api/v1/employee/{id}", "321"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployee_MockEmployeeServiceFailureException() throws Exception {
        when(employeeService.deleteEmployee("321")).thenThrow(new MockEmployeeServiceFailureException(""));

        mockMvc.perform(delete("/api/v1/employee/{id}", "321"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteEmployee_EmployeeNotFoundException() throws Exception {
        when(employeeService.deleteEmployee("321")).thenThrow(new EmployeeNotFoundException(""));

        mockMvc.perform(delete("/api/v1/employee/{id}", "321"))
                .andExpect(status().isNotFound());
    }

    private EmployeeDto dto(String id, String name, int salary) {
        return EmployeeDto.builder()
                .id(UUID.fromString(id))
                .name(name)
                .salary(salary)
                .build();
    }


}
