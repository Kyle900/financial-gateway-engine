package com.financial.gateway.daemon.service;

import com.financial.gateway.daemon.dto.DaemonRequestDto;
import com.financial.gateway.global.crypto.CryptoService;
import com.financial.gateway.mapper.SettlementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class DaemonTransactionService {

    private final SettlementMapper settlementMapper;
    private final CryptoService cryptoService;

    public DaemonTransactionService(SettlementMapper settlementMapper, CryptoService cryptoService) {
        this.settlementMapper = settlementMapper;
        this.cryptoService = cryptoService;
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public void processInboundTransaction(DaemonRequestDto request) {
        String encryptedAcc = cryptoService.encrypt(request.getAccountNumber());
        
        // 부하 테스트 중복 PK 충돌 방지를 위한 유니크 거래 ID 치환
        String finalTxId = request.getTxId().equals("TX_STRESS_") 
                ? "TX_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16)
                : request.getTxId();

        settlementMapper.insertTransaction(
                finalTxId,
                encryptedAcc,
                request.getAmount(),
                "PENDING",
                request.getBizCode()
        );
    }
}
