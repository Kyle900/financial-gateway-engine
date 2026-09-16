package com.financial.gateway.global.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("batch")
public class GatewayScheduler {
    private static final Logger log = LoggerFactory.getLogger(GatewayScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job settlementJob;

    public GatewayScheduler(
            JobLauncher jobLauncher,
            @Qualifier("job_FB_CM_004_SD_02003") Job settlementJob) {
        this.jobLauncher = jobLauncher;
        this.settlementJob = settlementJob;
    }

    @Scheduled(fixedRateString = "${gateway.scheduler.fixed-rate:120000}")
    public void runSettlementSchedule() {
        try {
            log.info("[SCHEDULER] 주기적 정산 트리거 가동 시작");
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(settlementJob, params);
        } catch (Exception e) {
            log.error("[SCHEDULER-ERROR] 정산 배치 실행 실패", e);
        }
    }
}
