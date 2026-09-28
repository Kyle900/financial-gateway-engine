package com.financial.gateway.batch;

import com.financial.gateway.FinancialGatewayApplication;
import com.financial.gateway.mapper.SettlementMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBatchTest
@SpringBootTest(classes = FinancialGatewayApplication.class)
@ActiveProfiles("daemon")
public class SettlementBulkBatchIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(SettlementBulkBatchIntegrationTest.class);

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("job_FB_CM_004_SD_02003")
    private Job settlementJob;

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Chunk 없이 10만 건 Set-based Tasklet 정산 및 무결성 검증")
    void testBulkSettlementTaskletExecution() throws Exception {
        log.info(">>> [대량 테스트] 10만 건 더미 거래 데이터 적재 시작...");

        // H2 In-memory DB의 SYSTEM_RANGE를 활용해 10만 건 생성
        jdbcTemplate.execute(
            "INSERT INTO TB_FIN_TRANSACTION (TX_ID, ACC_NO_ENC, AMOUNT, STATUS, BIZ_CODE, CREATED_AT) " +
            "SELECT 'TX_BULK_' || LPAD(X, 8, '0'), 'ENC_ACC_' || X, 15000.00, 'PENDING', 'KLID_TAX', CURRENT_TIMESTAMP " +
            "FROM SYSTEM_RANGE(1, 100000)"
        );

        int initialPending = settlementMapper.countPendingTransactions();
        log.info(">>> [대량 테스트] 원장 적재 완료: 총 {}건 대기 상태 확인", initialPending);
        Assertions.assertTrue(initialPending >= 100000);

        jobLauncherTestUtils.setJob(settlementJob);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        long startTime = System.currentTimeMillis();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        long duration = System.currentTimeMillis() - startTime;

        Assertions.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        int remainingPending = settlementMapper.countPendingTransactions();
        Assertions.assertEquals(0, remainingPending);

        log.info(">>> [대량 테스트 성공] 10만 건 Tasklet 정산 완료 | 총 소요시간: {}ms | 잔여 PENDING: {}건", duration, remainingPending);
    }
}
