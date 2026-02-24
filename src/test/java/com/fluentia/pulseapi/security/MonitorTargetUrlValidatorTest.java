package com.fluentia.pulseapi.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.fluentia.pulseapi.infrastructure.security.MonitorTargetUrlValidator;

class MonitorTargetUrlValidatorTest {

  @Test
  void shouldBlockLocalhostWhenPrivateTargetsDisabled() {
    MonitorTargetUrlValidator validator = new MonitorTargetUrlValidator(false);
    assertThrows(IllegalArgumentException.class,
        () -> validator.assertAllowedForMonitorConfig("http://localhost:8080/health"));
  }

  @Test
  void shouldBlockPrivateIpWhenPrivateTargetsDisabled() {
    MonitorTargetUrlValidator validator = new MonitorTargetUrlValidator(false);
    assertThrows(IllegalArgumentException.class,
        () -> validator.assertAllowedForMonitorConfig("http://192.168.1.10/health"));
  }

  @Test
  void shouldAllowPublicIpWhenPrivateTargetsDisabled() {
    MonitorTargetUrlValidator validator = new MonitorTargetUrlValidator(false);
    assertDoesNotThrow(() -> validator.assertAllowedForExecution("https://1.1.1.1"));
  }

  @Test
  void shouldAllowLocalhostWhenPrivateTargetsEnabled() {
    MonitorTargetUrlValidator validator = new MonitorTargetUrlValidator(true);
    assertDoesNotThrow(() -> validator.assertAllowedForExecution("http://localhost:8080/health"));
  }
}
