package com.reliaquest.api.client;

import com.reliaquest.api.exception.MockEmployeeServiceTooManyRequestsException;
import com.reliaquest.api.model.CreateMockEmployeeInput;
import com.reliaquest.api.model.DeleteMockEmployeeInput;
import com.reliaquest.api.model.MockEmployee;
import com.reliaquest.api.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

@Service
@Slf4j
public class MockEmployeeClient {

    //TODO: use cache to store cooldown period after 429 error from mockservice, call from this service during cooldown
    // should thrown exception and return 503 service unavailable

    private final RestClient restClient;

    private final String mockEmployeeUrl;

    private static final String TARGET = "mockEmployeeService";


    public MockEmployeeClient(@Value("${com.reliaquest.api.mockemployeeservice.baseurl}") String baseUrl) {
        this.mockEmployeeUrl = baseUrl;
        restClient = RestClient.builder()
                .baseUrl(this.mockEmployeeUrl)
                .build();
    }

    public Response<List<MockEmployee>> getEmployees() {

        return restClient.get()
                .retrieve()
                .onStatus(status -> true, (request, response) -> {
                    logApi(response.getStatusCode().value(),request.getMethod(),request.getURI(),"getAllEmployees");
                })
                .body(new ParameterizedTypeReference<Response<List<MockEmployee>>>() {});
    }

    public Response<MockEmployee> createEmployee(CreateMockEmployeeInput input) {
        return restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(input)
                .retrieve()
                .onStatus(status -> true, (request, response) -> {
                    logApi(response.getStatusCode().value(),request.getMethod(),request.getURI(),"createEmployee");
                })
                .body(new ParameterizedTypeReference<Response<MockEmployee>>() {});
    }

    public Response<MockEmployee> getEmployee(String id) {
        return restClient.get()
                .uri("/{id}", id)   // safer than manual string concat
                .retrieve()
                .onStatus(status -> true, (request, response) -> {
                    logApi(response.getStatusCode().value(),request.getMethod(),request.getURI(),"getEmployee");
                })
                .body(new ParameterizedTypeReference<Response<MockEmployee>>() {});
    }

    public Response<Boolean> deleteEmployee(DeleteMockEmployeeInput deleteRequest) {
        return restClient.method(HttpMethod.DELETE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(deleteRequest)
                .retrieve()
                .onStatus(status -> true, (request, response) -> {
                    logApi(response.getStatusCode().value(),request.getMethod(),request.getURI(),"deleteEmployeeById");
                })
                .body(new ParameterizedTypeReference<Response<Boolean>>() {});
    }

    private void logApi(int status,HttpMethod method, URI uri, String message) {
        log.warn("Downstream call status={} error target={} method={} uri={}", status,TARGET, method, uri);
        if(429 == status) {
            throw new MockEmployeeServiceTooManyRequestsException("Rate limit exceeded calling " + message);
        }
    }



}
