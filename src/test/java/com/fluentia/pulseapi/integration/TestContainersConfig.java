package com.fluentia.pulseapi.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
public abstract class TestContainersConfig {
  static {
    String jwtSecret = java.util.UUID.randomUUID().toString().replace("-", "")
        + java.util.UUID.randomUUID().toString().replace("-", "");
    System.setProperty("JWT_SECRET", jwtSecret);
    System.setProperty("DOCKER_API_VERSION", "1.53");
    System.setProperty("docker.api.version", "1.53");
    System.setProperty("DOCKER_HOST", "unix:///var/run/docker.sock");
    System.setProperty("docker.host", "unix:///var/run/docker.sock");
    System.setProperty("api.version", "1.53");
  }

  @Container
  protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("pulseapi")
      .withUsername("pulseapi")
      .withPassword("pulseapi");

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
    registry.add("spring.flyway.user", POSTGRES::getUsername);
    registry.add("spring.flyway.password", POSTGRES::getPassword);
  }
}
