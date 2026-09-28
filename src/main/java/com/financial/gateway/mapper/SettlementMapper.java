package com.financial.gateway.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;

@Mapper
public interface SettlementMapper {
    int insertTransaction(@Param("txId") String txId, 
                          @Param("accNoEnc") String accNoEnc, 
                          @Param("amount") BigDecimal amount, 
                          @Param("status") String status, 
                          @Param("bizCode") String bizCode);

    int countPendingTransactions();

    int updateSettledStatus();

    // Tasklet Set-based 대량 일괄 전이 메서드 (컴파일 에러 해결)
    int bulkUpdatePendingToSettled(@Param("bizCode") String bizCode);

    int insertSettlementLog(@Param("jobId") String jobId, 
                            @Param("targetDate") String targetDate, 
                            @Param("processedCount") int processedCount, 
                            @Param("status") String status);
}
