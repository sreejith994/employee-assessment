package com.reliaquest.api.service;

import com.reliaquest.api.client.MockEmployeeClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.InvalidInputException;
import com.reliaquest.api.exception.MockEmployeeServiceFailureException;
import com.reliaquest.api.exception.NoEmployeesFoundException;
import com.reliaquest.api.mapper.EmployeeMapper;
import com.reliaquest.api.model.*;
import com.reliaquest.api.validator.UUIDValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    //TODO: Enhancement use redis caching to store findAll/getEmployee etc results with appropriate TTL

    private final MockEmployeeClient employeeClient;
    private final EmployeeMapper employeeMapper;

    @Cacheable(value = "employees", sync = true)
    public List<EmployeeDto> getAllEmployees() {
        log.info("CACHE MISSED employees -> calling actual service");
        Response<List<MockEmployee>> getEmployeesResponse = employeeClient.getEmployees();
        if(getEmployeesResponse.error() != null) {
            throw new MockEmployeeServiceFailureException(getEmployeesResponse.error());
        }
        return getEmployeesResponse.data().stream()
                .map(employeeMapper::map)
                .toList();

    }

    @Cacheable(value = "employeesBySearch", key = "T(org.springframework.util.StringUtils).trimAllWhitespace(#search)?.toLowerCase()", unless = "#result == null || #result.isEmpty()", sync = true)
    public List<EmployeeDto> getAllEmployeesBySearch(String search) {
        log.info("CACHE MISSED employeesBySearch -> calling actual service");
        if(search == null || search.isBlank()) {
            throw new InvalidInputException("Search cannot be null or empty");
        }
        List<EmployeeDto> allEmployees = getAllEmployees();
        return allEmployees.stream()
                .filter(e -> e.name().toLowerCase().contains(search.toLowerCase()))
                .toList();

    }

    @CacheEvict(value = { "employees", "employeesBySearch" }, allEntries = true)
    public EmployeeDto getEmployeeById(String id) {

        if(UUIDValidator.parseUUID(id).isEmpty()){
            throw new InvalidInputException("id: % is not valid UUID");
        }
        Response<MockEmployee> employeeResponse = employeeClient.getEmployee(id);
        if(employeeResponse.data() == null) {
            throw new EmployeeNotFoundException("Employee with id: %s  not found".formatted(id));
        }
        if(employeeResponse.error() != null) {
            throw new MockEmployeeServiceFailureException(employeeResponse.error());
        }
        return employeeMapper.map(employeeResponse.data());
    }

    public int getHighestSalary(){
        List<EmployeeDto> allEmployees = getAllEmployees();

        return allEmployees.stream()
                .mapToInt(EmployeeDto::salary)
                .max()
                .orElseThrow(() -> new NoEmployeesFoundException("Unable to calculate highest salary as no employees found"));

    }

    //TODO: enhancement, if this logic should be offloaded to the downstream Mockservice it would be more efficient
    // as the calcualtion could be done via DB(JDBC/hibernate) where DBMS are highly optimsed for aggregate cals
    public List<String> getTopTenSalaryEmployees() {
        List<EmployeeDto> allEmployees = getAllEmployees();
        if(allEmployees.isEmpty()) {
            throw new EmployeeNotFoundException("Unable to calculate top 10 highest salary as no employees found");
        }
        return allEmployees.stream()
                .sorted(Comparator.comparing(EmployeeDto::salary).reversed())
                .limit(10)
                .map(EmployeeDto::name)
                .toList();
    }

    public EmployeeDto createEmployee(CreateMockEmployeeInput request) {
        Response<MockEmployee> createEmployeeResponse = employeeClient.createEmployee(request);
        if(createEmployeeResponse.error() != null) {
            throw new MockEmployeeServiceFailureException(createEmployeeResponse.error());
        }
        return employeeMapper.map(createEmployeeResponse.data());
    }

    @CacheEvict(value = { "employees", "employeesBySearch" }, allEntries = true)
    public String deleteEmployee(String id) {
        if(UUIDValidator.parseUUID(id).isEmpty()){
            throw new InvalidInputException("id: % is not valid UUID");
        }
        EmployeeDto employee = getEmployeeById(id);
        DeleteMockEmployeeInput deleteRequest = DeleteMockEmployeeInput.builder()
                                                .name(employee.name())
                                                .build();
        Response<Boolean> deleteEmployeeResponse = employeeClient.deleteEmployee(deleteRequest);
        if(deleteEmployeeResponse.error() != null) {
            throw new MockEmployeeServiceFailureException(deleteEmployeeResponse.error());
        }
        if(!deleteEmployeeResponse.data().booleanValue()) {
            throw new EmployeeNotFoundException("Unable to delete employee");
        }
        return deleteRequest.getName();
    }

    }
