package com.fluentia.pulseapi.infrastructure.config;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import com.fluentia.pulseapi.application.service.MonitorCheckService;
import com.fluentia.pulseapi.domain.entity.Monitor;
import com.fluentia.pulseapi.domain.repository.MonitorRepository;

@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class MonitorCheckScheduler {
  private static final Logger logger = LoggerFactory.getLogger(MonitorCheckScheduler.class);

  private final MonitorRepository monitorRepository;
  private final MonitorCheckService monitorCheckService;
  private final TransactionTemplate transactionTemplate;
  private final int claimBatchSize;
  private final int claimLeaseSeconds;
  private final ExecutorService checkExecutor;

  public MonitorCheckScheduler(MonitorRepository monitorRepository,
      MonitorCheckService monitorCheckService,
      TransactionTemplate transactionTemplate,
      @Value("${app.scheduler.claim-batch-size:50}") int claimBatchSize,
      @Value("${app.scheduler.claim-lease-seconds:120}") int claimLeaseSeconds,
      @Value("${app.scheduler.max-parallel-checks:4}") int maxParallelChecks) {
    this.monitorRepository = monitorRepository;
    this.monitorCheckService = monitorCheckService;
    this.transactionTemplate = transactionTemplate;
    this.claimBatchSize = claimBatchSize;
    this.claimLeaseSeconds = claimLeaseSeconds;
    this.checkExecutor = Executors.newFixedThreadPool(Math.max(1, maxParallelChecks));
  }

  @Scheduled(fixedDelay = 30_000)
  public void runScheduler() {
    try {
      OffsetDateTime now = OffsetDateTime.now();
      List<Monitor> eligible = claimEligible(now);
      if (!eligible.isEmpty()) {
        logger.info("Scheduler claim {} monitores elegiveis para check", eligible.size());
      }
      runChecksInParallel(eligible);
    } catch (Exception ex) {
      logger.error("Erro no scheduler de checks", ex);
    }
  }

  private List<Monitor> claimEligible(OffsetDateTime now) {
    OffsetDateTime leaseUntil = now.plusSeconds(claimLeaseSeconds);
    List<Monitor> claimed = transactionTemplate.execute(
        status -> monitorRepository.claimEligibleForCheck(now, leaseUntil, claimBatchSize));
    return claimed == null ? List.of() : claimed;
  }

  private void runChecksInParallel(List<Monitor> monitors) {
    if (monitors.isEmpty()) {
      return;
    }
    List<CompletableFuture<Void>> futures = monitors.stream()
        .map(monitor -> CompletableFuture.runAsync(() -> runSingleCheck(monitor), checkExecutor))
        .toList();
    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
  }

  private void runSingleCheck(Monitor monitor) {
    try {
      monitorCheckService.runCheck(monitor);
    } catch (Exception ex) {
      logger.error("Erro ao executar check do monitor {}", monitor.getId(), ex);
    }
  }

  @PreDestroy
  void shutdownExecutor() {
    checkExecutor.shutdown();
    try {
      if (!checkExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        checkExecutor.shutdownNow();
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      checkExecutor.shutdownNow();
    }
  }
}
