package com.fluentia.pulseapi.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import com.fluentia.pulseapi.domain.entity.Alert;
import com.fluentia.pulseapi.domain.entity.CheckRun;
import com.fluentia.pulseapi.domain.entity.Incident;
import com.fluentia.pulseapi.domain.entity.Monitor;
import com.fluentia.pulseapi.domain.entity.User;
import com.fluentia.pulseapi.domain.repository.AlertRepository;
import com.fluentia.pulseapi.domain.repository.CheckRunRepository;
import com.fluentia.pulseapi.domain.repository.IncidentRepository;
import com.fluentia.pulseapi.domain.repository.MonitorRepository;
import com.fluentia.pulseapi.domain.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DashboardMetricsIntegrationTest extends TestContainersConfig {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MonitorRepository monitorRepository;

  @Autowired
  private CheckRunRepository checkRunRepository;

  @Autowired
  private IncidentRepository incidentRepository;

  @Autowired
  private AlertRepository alertRepository;

  @Test
  void shouldReturnMonitorSummary() {
    AuthContext auth = registerAndLogin("summary@pulseapi.dev");
    User user = auth.user();
    Monitor monitor = monitorRepository.save(new Monitor(UUID.randomUUID(), user, "API", "http://localhost/health", 60, 2000, true));

    OffsetDateTime now = OffsetDateTime.now();
    checkRunRepository.saveAll(List.of(
        buildCheckRun(monitor, now.minusHours(1), true, 120),
        buildCheckRun(monitor, now.minusHours(2), true, 240),
        buildCheckRun(monitor, now.minusHours(3), false, 500),
        buildCheckRun(monitor, now.minusHours(4), true, 80)
    ));

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(auth.token());

    ResponseEntity<Map> response = restTemplate.exchange(
        url("/api/v1/monitors/" + monitor.getId() + "/checks/summary?window=24h"),
        HttpMethod.GET,
        new HttpEntity<>(headers),
        Map.class);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).containsEntry("totalChecks", 4);
    assertThat(response.getBody()).containsEntry("failures", 1);
  }

  @Test
  void shouldReturnDashboardOverview() {
    AuthContext auth = registerAndLogin("overview@pulseapi.dev");
    User user = auth.user();
    Monitor monitorA = monitorRepository.save(new Monitor(UUID.randomUUID(), user, "API A", "https://a.com", 60, 2000, true));
    Monitor monitorB = monitorRepository.save(new Monitor(UUID.randomUUID(), user, "API B", "https://b.com", 60, 2000, true));
    monitorA.setLastStatus("DOWN");
    monitorB.setLastStatus("UP");
    monitorRepository.save(monitorA);
    monitorRepository.save(monitorB);

    OffsetDateTime now = OffsetDateTime.now();
    checkRunRepository.saveAll(List.of(
        buildCheckRun(monitorA, now.minusHours(1), false, 300),
        buildCheckRun(monitorA, now.minusHours(2), false, 400),
        buildCheckRun(monitorB, now.minusHours(1), true, 120),
        buildCheckRun(monitorB, now.minusHours(2), true, 100)
    ));

    Incident incident = incidentRepository.save(new Incident(UUID.randomUUID(), monitorA, now.minusHours(1), "OPEN"));
    alertRepository.save(new Alert(UUID.randomUUID(), incident, "SYSTEM", "UNACKED", "DOWN", now.minusMinutes(30)));

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(auth.token());

    ResponseEntity<Map> response = restTemplate.exchange(
        url("/api/v1/dashboard/overview?window=7d"),
        HttpMethod.GET,
        new HttpEntity<>(headers),
        Map.class);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    Map totals = (Map) response.getBody().get("totals");
    assertThat(totals.get("monitorsTotal")).isEqualTo(2);
    assertThat(totals.get("incidentsOpen")).isEqualTo(1);
  }

  private CheckRun buildCheckRun(Monitor monitor, OffsetDateTime startedAt, boolean success, int latencyMs) {
    CheckRun checkRun = new CheckRun(UUID.randomUUID(), monitor, startedAt, success ? "UP" : "DOWN");
    checkRun.setSuccess(success);
    checkRun.setLatencyMs(latencyMs);
    return checkRun;
  }

  private AuthContext registerAndLogin(String email) {
    String baseUrl = "http://localhost:" + port;
    Map<String, String> register = Map.of("email", email, "password", "secret123");
    restTemplate.postForEntity(baseUrl + "/api/v1/auth/register", register, Map.class);

    Map<String, String> login = Map.of("email", email, "password", "secret123");
    ResponseEntity<Map> loginResponse = restTemplate.postForEntity(baseUrl + "/api/v1/auth/login", login, Map.class);
    String token = (String) loginResponse.getBody().get("accessToken");
    User user = userRepository.findByEmail(email).orElseThrow();
    return new AuthContext(user, token);
  }

  private String url(String path) {
    return "http://localhost:" + port + path;
  }

  private record AuthContext(User user, String token) {}
}
