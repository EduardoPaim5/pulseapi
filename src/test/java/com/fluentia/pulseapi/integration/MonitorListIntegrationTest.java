package com.fluentia.pulseapi.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
class MonitorListIntegrationTest extends TestContainersConfig {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void shouldReturnMonitorWithStatusFields() {
    String baseUrl = "http://localhost:" + port;

    Map<String, String> register = Map.of("email", "list@pulseapi.dev", "password", "secret123");
    ResponseEntity<Map> registerResponse = restTemplate.postForEntity(baseUrl + "/api/v1/auth/register", register, Map.class);
    assertThat(registerResponse.getStatusCode().value()).isEqualTo(201);
    String token = (String) registerResponse.getBody().get("accessToken");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);

    Map<String, Object> create = Map.of(
        "name", "API Main",
        "url", "http://localhost/health",
        "intervalSec", 60,
        "timeoutMs", 1000,
        "enabled", true
    );
    ResponseEntity<Map> createResponse = restTemplate.postForEntity(baseUrl + "/api/v1/monitors", new HttpEntity<>(create, headers), Map.class);
    assertThat(createResponse.getStatusCode().value()).isEqualTo(201);

    ResponseEntity<List> listResponse = restTemplate.exchange(
        baseUrl + "/api/v1/monitors",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        List.class
    );
    assertThat(listResponse.getStatusCode().value()).isEqualTo(200);
    List<Map<String, Object>> monitors = listResponse.getBody();
    assertThat(monitors).isNotEmpty();

    Map<String, Object> monitor = monitors.get(0);
    assertThat(monitor).containsKeys(
        "lastStatus",
        "lastLatencyMs",
        "lastCheckedAt",
        "nextCheckAt",
        "enabled"
    );
  }
}
