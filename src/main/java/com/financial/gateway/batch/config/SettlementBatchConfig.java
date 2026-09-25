package com.financial.gateway.batch.config;

import com.financial.gateway.batch.listener.BatchJobExecutionListener;
import com.financial.gateway.batch.tasklet.SettlementTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SettlementBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager primaryTransactionManager;
    private final SettlementTasklet settlementTasklet;
    private final BatchJobExecutionListener listener;

    public SettlementBatchConfig(
            JobRepository jobRepository,
            @Qualifier("primaryTransactionManager") PlatformTransactionManager primaryTransactionManager,
            SettlementTasklet settlementTasklet,
            BatchJobExecutionListener listener) {
        this.jobRepository = jobRepository;
        this.primaryTransactionManager = primaryTransactionManager;
        this.settlementTasklet = settlementTasklet;
        this.listener = listener;
    }

    @Bean(name = "job_FB_CM_004_SD_02003")
    public Job settlementJob() {
        return new JobBuilder("job_FB_CM_004_SD_02003", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(settlementStep())
                .build();
    }

    @Bean
    public Step settlementStep() {
        return new StepBuilder("settlementStep", jobRepository)
                .tasklet(settlementTasklet, primaryTransactionManager)
                .build();
    }
}
