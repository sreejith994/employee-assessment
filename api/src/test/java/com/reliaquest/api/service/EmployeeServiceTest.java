package com.reliaquest.api.service;

import com.reliaquest.api.client.MockEmployeeClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.InvalidInputException;
import com.reliaquest.api.exception.MockEmployeeServiceFailureException;
import com.reliaquest.api.exception.NoEmployeesFoundException;
import com.reliaquest.api.mapper.EmployeeMapper;
import com.reliaquest.api.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {

    private MockEmployeeClient client;
    private EmployeeMapper mapper;
    private EmployeeService service;
    MockEmployee m1;
    MockEmployee m2;

    @BeforeEach
    void setUp() {
        client = mock(MockEmployeeClient.class);
        mapper = mock(EmployeeMapper.class);
        service = new EmployeeService(client, mapper);
        m1 = getMockEmployee("mike",100, null);
        m2 = getMockEmployee("tyson",200,null);
    }

    @Test
    void getAllEmployees_success() {

        when(client.getEmployees()).thenReturn(new Response<>(List.of(m1, m2), Response.Status.HANDLED,null));

        List<EmployeeDto> out = service.getAllEmployees();

        assertEquals(2, out.size());
        assertEquals("mike", out.get(0).name());
        assertEquals("tyson", out.get(1).name());
        assertEquals(100, out.get(0).salary());
        assertEquals(200, out.get(1).salary());
        verify(mapper, times(1)).map(m1);
        verify(mapper, times(1)).map(m2);
        verify(client, times(1)).getEmployees();
    }

    @Test
    void getAllEmployeesBySearch_CaseInsensitiveCheck() {
        when(client.getEmployees()).thenReturn(new Response<>(List.of(m1, m2), Response.Status.HANDLED,null));

        List<EmployeeDto> out = service.getAllEmployeesBySearch("MIKE");

        assertEquals(1, out.size());
        assertEquals("mike", out.get(0).name());
    }

    @Test
    void getEmployeeById_invalidUUID_throwsBeforeMapping() {
        String invalidUUID = "1234";
        when(client.getEmployee(invalidUUID)).thenReturn(new Response<>(null, null,null)); // won’t be used ideally

        assertThrows(InvalidInputException.class, () -> service.getEmployeeById(invalidUUID));
        verify(client, times(0)).getEmployee(invalidUUID);
        verify(mapper,times(0)).map(any(MockEmployee.class));
    }

    @Test
    void getAllEmployees_errorFromMockService_throwsMockEmployeeServiceFailureException() {
        when(client.getEmployees()).thenReturn(new Response<>(List.of(m1, m2), Response.Status.HANDLED,"error"));

        assertThrows(MockEmployeeServiceFailureException.class, () -> service.getAllEmployees());

        verify(mapper, times(0)).map(m1);
        verify(mapper, times(0)).map(m2);
        verify(client, times(1)).getEmployees();
    }

    @Test
    void getEmployeeById_notFound_throwsEmployeeNotFoundException() {
        String id = UUID.randomUUID().toString();
        when(client.getEmployee(id)).thenReturn(new Response<>(null,null, null));
        assertThrows(EmployeeNotFoundException.class, () -> service.getEmployeeById(id));

        verify(client, times(1)).getEmployee(id);
    }

    @Test
    void getEmployeeById_mockServiceReturnsNoData_throwsEmployeeNotFoundException() {
        String id = UUID.randomUUID().toString();
        when(client.getEmployee(id)).thenReturn(new Response<>(null,null, "error"));

        assertThrows(EmployeeNotFoundException.class, () -> service.getEmployeeById(id));
    }

    @Test
    void getEmployeeById_mockServiceReturnsError_throwsMockEmployeeServiceFailureException() {
        String id = UUID.randomUUID().toString();
        MockEmployee m1 = getMockEmployee("mike",100,null);
        when(client.getEmployee(id)).thenReturn(new Response<>(m1,null, "error"));

        assertThrows(MockEmployeeServiceFailureException.class, () -> service.getEmployeeById(id));
    }

    @Test
    void getEmployeeById_success() {
        UUID id = UUID.randomUUID();
        MockEmployee m = getMockEmployee( "ash", 150,id);
        when(client.getEmployee(id.toString())).thenReturn(new Response<>(m, null,null));

        EmployeeDto out = service.getEmployeeById(id.toString());

        assertEquals(id.toString(), out.id().toString());
        assertEquals("ash", out.name());
        assertEquals(150, out.salary());
    }

    @Test
    void getHighestSalary_success() {
        MockEmployee m1 = getMockEmployee("mike",100,null);
        MockEmployee m2 = getMockEmployee("tyson",200,null);
        when(client.getEmployees()).thenReturn(new Response<>(List.of(m1, m2), Response.Status.HANDLED,null));

        int max = service.getHighestSalary();

        assertEquals(200, max);
    }

    @Test
    void getHighestSalary_noEmployees_throwsNoEmployeesFoundException() {
        when(client.getEmployees()).thenReturn(new Response<>(new ArrayList<>(),null, null));

        assertThrows(NoEmployeesFoundException.class, () -> service.getHighestSalary());
    }

    @Test
    void getTopTenSalaryEmployees_moreThan10Values_ok() {
        var list = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(i -> getMockEmployee(String.valueOf(i),  i * 10,null))
                .toList();
        when(client.getEmployees()).thenReturn(new Response<>(list, null,null));

        List<String> names = service.getTopTenSalaryEmployees();

        assertEquals(10, names.size());
        assertEquals("12", names.get(0));
        assertEquals("11", names.get(1));
        assertEquals("3", names.get(9));
    }

    @Test
    void getTopTenSalaryEmployees_empty_throwsEmployeeNotFoundException() {
        when(client.getEmployees()).thenReturn(new Response<>(new ArrayList<>(), null,null));

        assertThrows(EmployeeNotFoundException.class, () -> service.getTopTenSalaryEmployees());
    }

    @Test
    void createEmployee_ok() {
        CreateMockEmployeeInput in = CreateMockEmployeeInput.builder()
                .name("mike")
                .salary(100)
                .title("mr")
                .age(59)
                .build();
        MockEmployee employee = getMockEmployee("mike", 9999, null);
        when(client.createEmployee(in)).thenReturn(new Response<>(employee, Response.Status.HANDLED,null));

        EmployeeDto dto = service.createEmployee(in);

        assertEquals("mike", dto.name());
        assertEquals(9999, dto.salary());
    }

    @Test
    void createEmployee_errorFromCreateMockService_throwsMockEmployeeServiceFailureException() {
        CreateMockEmployeeInput createMockEmployeeInput = CreateMockEmployeeInput.builder()
                .name("mike")
                .salary(100)
                .title("mr")
                .age(59)
                .build();
        when(client.createEmployee(createMockEmployeeInput)).thenReturn(new Response<>(null, Response.Status.ERROR,"error"));

        assertThrows(MockEmployeeServiceFailureException.class, () -> service.createEmployee(createMockEmployeeInput));
    }

    @Test
    void deleteEmployee_invalidUuid_throwsInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> service.deleteEmployee("bad-uuid"));
        verifyNoInteractions(client);
    }

    @Test
    void deleteEmployee_success() {
        String id = UUID.randomUUID().toString();
        MockEmployee existing = getMockEmployee("mike", 99, null);
        when(client.getEmployee(id)).thenReturn(new Response<>(existing, null,null));

        Response<Boolean> delResp = new Response<>(Boolean.TRUE, null,null);
        when(client.deleteEmployee(any(DeleteMockEmployeeInput.class))).thenReturn(delResp);

        String name = service.deleteEmployee(id);

        assertEquals("mike", name);

        var captor = ArgumentCaptor.forClass(DeleteMockEmployeeInput.class);
        verify(client).deleteEmployee(captor.capture());
        assertEquals("mike", captor.getValue().getName());
    }

    @Test
    void deleteEmployee_MockServiceReturnsFalse_throwsEmployeeNotFoundException() {
        String id = UUID.randomUUID().toString();
        MockEmployee existing = getMockEmployee("bill", 99, null);
        when(client.getEmployee(id)).thenReturn(new Response<>(existing, null,null));
        when(client.deleteEmployee(any(DeleteMockEmployeeInput.class))).thenReturn(new Response<>(Boolean.FALSE, null,null));

        assertThrows(EmployeeNotFoundException.class, () -> service.deleteEmployee(id));
    }

    @Test
    void deleteEmployee_MockServiceReturnsError_throwsMockEmployeeServiceFailureException() {
        String id = UUID.randomUUID().toString();
        MockEmployee existing = getMockEmployee("jill", 987, null);
        when(client.getEmployee(id)).thenReturn(new Response<>(existing, null,null));
        when(client.deleteEmployee(any(DeleteMockEmployeeInput.class))).thenReturn(new Response<>(null, Response.Status.ERROR,"error"));

        assertThrows(MockEmployeeServiceFailureException.class, () -> service.deleteEmployee(id));
    }

    private MockEmployee getMockEmployee( String name, int salary, UUID id) {
        MockEmployee m = mock(MockEmployee.class);
        Random rand = new Random();

        when(mapper.map(m)).thenReturn(new EmployeeDto(id==null ? UUID.randomUUID(): id,
                name,
                salary,
            rand.nextInt(60) + 16,
            "mr",
            name+"@test.com")
                );
        return m;
    }
}
