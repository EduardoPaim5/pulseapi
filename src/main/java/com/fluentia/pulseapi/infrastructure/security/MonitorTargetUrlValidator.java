package com.fluentia.pulseapi.infrastructure.security;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MonitorTargetUrlValidator {
  private final boolean allowPrivateTargets;

  public MonitorTargetUrlValidator(@Value("${app.security.allow-private-targets:false}") boolean allowPrivateTargets) {
    this.allowPrivateTargets = allowPrivateTargets;
  }

  public void assertAllowedForMonitorConfig(String rawUrl) {
    URI uri = parseAndValidateBase(rawUrl);
    if (allowPrivateTargets) {
      return;
    }
    String host = normalizeHost(uri.getHost());
    if (isBlockedHostLabel(host)) {
      throw new IllegalArgumentException("Host de monitor não permitido por segurança");
    }
    if (isLiteralIp(host)) {
      InetAddress address = parseLiteralIp(host);
      if (isBlockedAddress(address)) {
        throw new IllegalArgumentException("IP de monitor não permitido por segurança");
      }
    }
  }

  public void assertAllowedForExecution(String rawUrl) {
    URI uri = parseAndValidateBase(rawUrl);
    if (allowPrivateTargets) {
      return;
    }
    String host = normalizeHost(uri.getHost());
    if (isBlockedHostLabel(host)) {
      throw new IllegalArgumentException("Host de monitor não permitido por segurança");
    }
    try {
      InetAddress[] addresses = InetAddress.getAllByName(host);
      if (addresses.length == 0) {
        throw new IllegalArgumentException("Host do monitor não pôde ser resolvido");
      }
      for (InetAddress address : addresses) {
        if (isBlockedAddress(address)) {
          throw new IllegalArgumentException("Destino do monitor não permitido por segurança");
        }
      }
    } catch (UnknownHostException ex) {
      throw new IllegalArgumentException("Host do monitor não pôde ser resolvido");
    }
  }

  private URI parseAndValidateBase(String rawUrl) {
    try {
      URI uri = new URI(rawUrl);
      String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
      if (!"http".equals(scheme) && !"https".equals(scheme)) {
        throw new IllegalArgumentException("URL deve usar protocolo http ou https");
      }
      if (uri.getHost() == null || uri.getHost().isBlank()) {
        throw new IllegalArgumentException("URL precisa de host válido");
      }
      if (uri.getUserInfo() != null) {
        throw new IllegalArgumentException("URL com credenciais embutidas não é permitida");
      }
      return uri;
    } catch (URISyntaxException ex) {
      throw new IllegalArgumentException("URL inválida");
    }
  }

  private String normalizeHost(String host) {
    String normalized = host.toLowerCase(Locale.ROOT);
    if (normalized.startsWith("[") && normalized.endsWith("]")) {
      return normalized.substring(1, normalized.length() - 1);
    }
    return normalized;
  }

  private boolean isLiteralIp(String host) {
    return host.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$") || host.contains(":");
  }

  private InetAddress parseLiteralIp(String host) {
    try {
      return InetAddress.getByName(host);
    } catch (UnknownHostException ex) {
      throw new IllegalArgumentException("IP inválido");
    }
  }

  private boolean isBlockedHostLabel(String host) {
    if ("localhost".equals(host) || host.endsWith(".localhost")) {
      return true;
    }
    return "metadata.google.internal".equals(host)
        || "metadata".equals(host)
        || "instance-data".equals(host);
  }

  private boolean isBlockedAddress(InetAddress address) {
    if (address.isAnyLocalAddress()
        || address.isLoopbackAddress()
        || address.isLinkLocalAddress()
        || address.isSiteLocalAddress()
        || address.isMulticastAddress()) {
      return true;
    }
    byte[] bytes = address.getAddress();
    if (address instanceof Inet4Address && bytes.length == 4) {
      int b0 = bytes[0] & 0xFF;
      int b1 = bytes[1] & 0xFF;
      if (b0 == 0 || b0 == 10 || b0 == 127) {
        return true;
      }
      if (b0 == 100 && b1 >= 64 && b1 <= 127) {
        return true;
      }
      if (b0 == 169 && b1 == 254) {
        return true;
      }
      if (b0 == 172 && b1 >= 16 && b1 <= 31) {
        return true;
      }
      if (b0 == 192 && b1 == 168) {
        return true;
      }
      if (b0 >= 224) {
        return true;
      }
    }
    return false;
  }
}
