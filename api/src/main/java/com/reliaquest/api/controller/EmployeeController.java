package com.reliaquest.api.controller;

import com.reliaquest.api.model.CreateMockEmployeeInput;
import com.reliaquest.api.model.EmployeeDto;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<EmployeeDto, CreateMockEmployeeInput> {

    private final EmployeeService employeeService;

    //TODO: Enhancement: If downstream Mockservice implemented a Pageaable interface this would reduce memory requirements
    @Override
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Override
    public ResponseEntity<List<EmployeeDto>> getEmployeesByNameSearch(
            @PathVariable("searchString")  String searchString) {
        return ResponseEntity.ok(employeeService.getAllEmployeesBySearch(searchString));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable("id") String id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return ResponseEntity.ok(employeeService.getHighestSalary());
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(employeeService.getTopTenSalaryEmployees());
    }

    @Override
    public ResponseEntity<EmployeeDto> createEmployee(@RequestBody @Valid CreateMockEmployeeInput employeeInput) {
        return ResponseEntity.ok(employeeService.createEmployee(employeeInput));
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        return ResponseEntity.ok(employeeService.deleteEmployee(id));

    }
}
