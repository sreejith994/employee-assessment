package com.reliaquest.api.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.reliaquest.api.exception.MockEmployeeServiceTooManyRequestsException;
import com.reliaquest.api.model.CreateMockEmployeeInput;
import com.reliaquest.api.model.DeleteMockEmployeeInput;
import com.reliaquest.api.model.MockEmployee;
import com.reliaquest.api.model.Response;
import org.junit.jupiter.api.*;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

class MockEmployeeClientTest {

    static WireMockServer wiremock;
    MockEmployeeClient client;

    @BeforeAll
    static void startUpWireMockServer() {
        wiremock = new WireMockServer(wireMockConfig()
                .dynamicPort()
                .http2PlainDisabled(true));
        wiremock.start();
    }

    @AfterAll
    static void stopWireMockServer() {
        wiremock.stop();
    }

    @BeforeEach
    void setup() {
        wiremock.resetAll();
        String baseUrl = "http://localhost:" + wiremock.port() + "/api/v1/employee";
        client = new MockEmployeeClient(baseUrl);
    }

    @Test
    void getEmployees_success() {
        wiremock.stubFor(get(urlEqualTo("/api/v1/employee"))
                .willReturn(okJson("""
                {
                  "data": [
                    { "id":"186d753a-b43a-476a-bcfa-d0f83e8793e9", "name":"john", "salary":100 },
                    { "id":"2c5e68c4-587c-4d19-a581-549314f5918f", "name":"smith",   "salary":200 }
                  ],
                  "error": null
                }
            """)));

        Response<List<MockEmployee>> resp = client.getEmployees();

        assertNotNull(resp);
        assertNotNull(resp.data());
        assertEquals(2, resp.data().size());
        assertEquals(resp.data().get(0).getId().toString(), "186d753a-b43a-476a-bcfa-d0f83e8793e9");
        assertEquals(resp.data().get(1).getId().toString(), "2c5e68c4-587c-4d19-a581-549314f5918f");
        assertNull(resp.error());
        wiremock.verify(getRequestedFor(urlEqualTo("/api/v1/employee")));
    }

    @Test
    void getEmployees_Status429_throwsMockEmployeeServiceTooManyRequests() {
        wiremock.stubFor(get(urlEqualTo("/api/v1/employee"))
                .willReturn(aResponse().withStatus(429).withBody("{\"error\":\"too many\"}")));

        assertThrows(MockEmployeeServiceTooManyRequestsException.class, () -> client.getEmployees());
        wiremock.verify(getRequestedFor(urlEqualTo("/api/v1/employee")));
    }

    @Test
    void getEmployee_success() {
        wiremock.stubFor(get(urlEqualTo("/api/v1/employee/2c5e68c4-587c-4d19-a581-549314f5918f"))
                .willReturn(okJson("""
                { "data": { "id":"2c5e68c4-587c-4d19-a581-549314f5918f", "name":"Charlie", "salary":150 }, "error": null }
            """)));

        Response<MockEmployee> resp = client.getEmployee("2c5e68c4-587c-4d19-a581-549314f5918f");

        assertNotNull(resp);
        assertNotNull(resp.data());
        assertEquals("2c5e68c4-587c-4d19-a581-549314f5918f", resp.data().getId().toString()); // adjust getter/record accessor as per your model
        wiremock.verify(getRequestedFor(urlEqualTo("/api/v1/employee/2c5e68c4-587c-4d19-a581-549314f5918f")));
    }

    @Test
    void createEmployee_success() {
        wiremock.stubFor(post(urlEqualTo("/api/v1/employee"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.name", equalTo("king")))
                .withRequestBody(matchingJsonPath("$[?(@.salary == 180)]"))
                .willReturn(okJson("""
          { "data": { "id":"2c5e68c4-587c-4d19-a581-549314f5918f", "name":"king", "salary":180 }, "error": null }
        """)));

        var input = CreateMockEmployeeInput.builder()
                .name("king").age(44).title("mr").salary(180).build();

        Response<MockEmployee> resp = client.createEmployee(input);

        assertNotNull(resp);
        assertNull(resp.error());
        assertEquals("king", resp.data().getName());
        wiremock.verify(postRequestedFor(urlEqualTo("/api/v1/employee")));
    }

    @Test
    void createEmployee_status429_throwsMockEmployeeServiceTooManyRequests() {
        wiremock.stubFor(post(urlEqualTo("/api/v1/employee"))
                .willReturn(aResponse().withStatus(429).withBody("{\"error\":\"rate limit\"}")));

        assertThrows(MockEmployeeServiceTooManyRequestsException.class,
                () -> client.createEmployee(
                        CreateMockEmployeeInput.builder()
                                .name("king")
                                .age(44)
                                .title("mr")
                                .salary(180)
                                .build()
                ));
    }

    @Test
    void deleteEmployee_success() {
        wiremock.stubFor(delete(urlEqualTo("/api/v1/employee"))
                .withRequestBody(matchingJsonPath("$.name", equalTo("brock")))
                .willReturn(okJson("{\"data\": true, \"error\": null}")));

        DeleteMockEmployeeInput req = DeleteMockEmployeeInput.builder()
                                        .name("brock")
                                        .build();
        Response<Boolean> resp = client.deleteEmployee(req);

        assertNotNull(resp);
        assertTrue(Boolean.TRUE.equals(resp.data()));
        wiremock.verify(deleteRequestedFor(urlEqualTo("/api/v1/employee")));
    }

    @Test
    void deleteEmployee_status429_throwsMockEmployeeServiceTooManyRequests() {
        wiremock.stubFor(delete(urlEqualTo("/api/v1/employee"))
                .willReturn(aResponse().withStatus(429)));

        assertThrows(MockEmployeeServiceTooManyRequestsException.class,
                () -> client.deleteEmployee(
                        DeleteMockEmployeeInput.builder()
                                .name("brock")
                                .build()
                ));
    }
}
