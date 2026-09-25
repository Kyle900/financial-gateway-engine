package com.financial.gateway.batch.tasklet;

import com.financial.gateway.mapper.SettlementMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class SettlementTasklet implements Tasklet {
    private static final Logger log = LoggerFactory.getLogger(SettlementTasklet.class);
    private final SettlementMapper settlementMapper;

    public SettlementTasklet(SettlementMapper settlementMapper) {
        this.settlementMapper = settlementMapper;
    }

//    @Override
//    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//        int targetCount = settlementMapper.countPendingTransactions();
//        log.info("[SETTLEMENT] 대상 거래 건수: {}건", targetCount);
//
//        if (targetCount > 0) {
//            settlementMapper.updateSettledStatus();
//        }
//
//        String targetDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
//        settlementMapper.insertSettlementLog("job_FB_CM_004_SD_02003", targetDate, targetCount, "SUCCESS");
//
//        return RepeatStatus.FINISHED;
//    }
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        long startTime = System.currentTimeMillis();
        
        // 1. DB 내부 일괄 전이 (Direct Bulk UPDATE / INSERT INTO ... SELECT)
        int updatedRows = settlementMapper.bulkUpdatePendingToSettled("KLID_TAX");
        
        long endTime = System.currentTimeMillis();
        log.info("[BULK-TASKLET] {}건 일괄 정산 처리 완료, 소요시간: {}ms", updatedRows, (endTime - startTime));
        
        // 2. 정산 로그 기록 (설계서 006)
        settlementMapper.insertSettlementLog("job_FB_CM_004_SD_02003", "20260915", updatedRows, "SUCCESS");
        
        return RepeatStatus.FINISHED;
    }
}
