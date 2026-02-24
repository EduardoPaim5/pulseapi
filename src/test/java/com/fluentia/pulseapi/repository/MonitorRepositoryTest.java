package com.fluentia.pulseapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import com.fluentia.pulseapi.domain.entity.Monitor;
import com.fluentia.pulseapi.domain.entity.User;
import com.fluentia.pulseapi.domain.entity.UserRole;
import com.fluentia.pulseapi.domain.repository.MonitorRepository;
import com.fluentia.pulseapi.domain.repository.UserRepository;
import com.fluentia.pulseapi.integration.TestContainersConfig;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MonitorRepositoryTest extends TestContainersConfig {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private MonitorRepository monitorRepository;

  @Test
  void shouldSaveAndLoadMonitor() {
    User user = new User(UUID.randomUUID(), "repo@pulseapi.dev", "hash", UserRole.USER);
    userRepository.save(user);

    Monitor monitor = new Monitor(UUID.randomUUID(), user, "Site", "http://localhost/health", 60, 2000, true);
    monitorRepository.save(monitor);

    Optional<Monitor> loaded = monitorRepository.findById(monitor.getId());
    assertThat(loaded).isPresent();
    assertThat(loaded.get().getOwner().getId()).isEqualTo(user.getId());
  }
}
