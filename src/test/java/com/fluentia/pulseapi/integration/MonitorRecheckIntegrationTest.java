package com.fluentia.pulseapi.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fluentia.pulseapi.domain.entity.Monitor;
import com.fluentia.pulseapi.domain.entity.User;
import com.fluentia.pulseapi.domain.repository.MonitorRepository;
import com.fluentia.pulseapi.domain.repository.UserRepository;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MonitorRecheckIntegrationTest extends TestContainersConfig {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MonitorRepository monitorRepository;

  private HttpServer server;

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  void shouldRecheckMonitor() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/health", exchange -> {
      byte[] body = "ok".getBytes();
      exchange.sendResponseHeaders(200, body.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(body);
      }
    });
    server.start();

    String targetUrl = "http://localhost:" + server.getAddress().getPort() + "/health";

    String email = "recheck@pulseapi.dev";
    String token = registerAndLogin(email);
    User user = userRepository.findByEmail(email).orElseThrow();

    Monitor monitor = monitorRepository.save(new Monitor(UUID.randomUUID(), user, "Health", targetUrl, 60, 2000, true));

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);

    ResponseEntity<Map> response = restTemplate.exchange(
        url("/api/v1/monitors/" + monitor.getId() + "/recheck"),
        HttpMethod.POST,
        new HttpEntity<>(headers),
        Map.class);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).containsEntry("success", true);
    assertThat(response.getBody()).containsKey("checkedAt");
  }

  private String registerAndLogin(String email) {
    String baseUrl = "http://localhost:" + port;
    Map<String, String> register = Map.of("email", email, "password", "secret123");
    restTemplate.postForEntity(baseUrl + "/api/v1/auth/register", register, Map.class);

    Map<String, String> login = Map.of("email", email, "password", "secret123");
    ResponseEntity<Map> loginResponse = restTemplate.postForEntity(baseUrl + "/api/v1/auth/login", login, Map.class);
    return (String) loginResponse.getBody().get("accessToken");
  }

  private String url(String path) {
    return "http://localhost:" + port + path;
  }
}
