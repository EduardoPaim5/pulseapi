package com.fluentia.pulseapi.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthFlowIntegrationTest extends TestContainersConfig {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void shouldRegisterLoginAndAccessMe() {
    String baseUrl = "http://localhost:" + port;

    Map<String, String> register = Map.of("email", "auth@pulseapi.dev", "password", "secret123");
    ResponseEntity<Map> registerResponse = restTemplate.postForEntity(baseUrl + "/api/v1/auth/register", register, Map.class);
    assertThat(registerResponse.getStatusCode().value()).isEqualTo(201);
    assertThat(registerResponse.getBody()).containsKey("accessToken");

    Map<String, String> login = Map.of("email", "auth@pulseapi.dev", "password", "secret123");
    ResponseEntity<Map> loginResponse = restTemplate.postForEntity(baseUrl + "/api/v1/auth/login", login, Map.class);
    assertThat(loginResponse.getStatusCode().value()).isEqualTo(200);
    String token = (String) loginResponse.getBody().get("accessToken");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);

    HttpEntity<Void> entity = new HttpEntity<>(headers);
    ResponseEntity<Map> meResponse = restTemplate.exchange(baseUrl + "/api/v1/auth/me", HttpMethod.GET, entity, Map.class);
    assertThat(meResponse.getStatusCode().value()).isEqualTo(200);
    assertThat(meResponse.getBody()).containsEntry("email", "auth@pulseapi.dev");
  }
}
