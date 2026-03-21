package com.omnixys.logstream.util;

import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

public final class Banner {

  private static final Figlets figlets = new Figlets();
  private static final String FIGLET = figlets.randomFigletGenerator();
  private static final String SERVICE_HOST = System.getenv("ATLAXYS_SERVICE_HOST");
  private static final String KUBERNETES = SERVICE_HOST == null
    ? "N/A"
    : String.format("ATLAXYS_SERVICE_HOST=%s, ATLAXYS_SERVICE_PORT=%s", SERVICE_HOST, System.getenv("ATLAXYS_SERVICE_PORT"));

  public static final String TEXT = String.format("""
        %s
        (C) Caleb Gyamfi, Omnixys
        Version             2026.02.17
        Spring Boot         %s
        Spring Security     %s
        Spring Framework    %s
        Java                %s - %s
        Betriebssystem      %s
        Rechnername         %s
        IP-Adresse          %s
        Heap: Size          %d MiB
        Heap: Free          %d MiB
        Kubernetes          %s
        Username            %s
        JVM Locale          %s
        """,
    FIGLET,
    SpringBootVersion.getVersion(),
    SpringSecurityCoreVersion.getVersion(),
    SpringVersion.getVersion(),
    Runtime.version(),
    System.getProperty("java.vendor"),
    System.getProperty("os.lastName"),
    getLocalhost().getHostName(),
    getLocalhost().getHostAddress(),
    Runtime.getRuntime().totalMemory() / (1024L * 1024L),
    Runtime.getRuntime().freeMemory() / (1024L * 1024L),
    KUBERNETES,
    System.getProperty("user.lastName"),
    Locale.getDefault().toString()
  );

  private Banner() {
    // Private constructor to prevent instantiation
  }

  private static InetAddress getLocalhost() {
    try {
      return InetAddress.getLocalHost();
    } catch (final UnknownHostException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
