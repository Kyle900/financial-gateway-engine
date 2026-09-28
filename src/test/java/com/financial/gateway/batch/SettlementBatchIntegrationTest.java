package com.financial.gateway.batch;

import com.financial.gateway.FinancialGatewayApplication;
import com.financial.gateway.mapper.SettlementMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

@SpringBatchTest
@SpringBootTest(classes = FinancialGatewayApplication.class)
@ActiveProfiles("daemon")
public class SettlementBatchIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("job_FB_CM_004_SD_02003")
    private Job settlementJob;

    @Autowired
    private SettlementMapper settlementMapper;

    @Test
    @DisplayName("금융 거래 인입 후 정산 배치(Tasklet) 실행 정상 정합성 검증")
    void testSettlementJobExecution() throws Exception {
        settlementMapper.insertTransaction("TX_TEST_001", "ENC_ACC_123", new BigDecimal("50000.00"), "PENDING", "KLID_TAX");
        settlementMapper.insertTransaction("TX_TEST_002", "ENC_ACC_456", new BigDecimal("120000.00"), "PENDING", "KLID_TAX");

        jobLauncherTestUtils.setJob(settlementJob);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        int pendingRemaining = settlementMapper.countPendingTransactions();
        Assertions.assertEquals(0, pendingRemaining);
    }
}
