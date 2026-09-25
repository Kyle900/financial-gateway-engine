package com.financial.gateway.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class BatchJobExecutionListener implements JobExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(BatchJobExecutionListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("[BATCH-START] Job Name: {}, Params: {}", 
                jobExecution.getJobInstance().getJobName(), 
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("[BATCH-END] Job Status: {}, Exit Code: {}", 
                jobExecution.getStatus(), 
                jobExecution.getExitStatus().getExitCode());
    }
}
