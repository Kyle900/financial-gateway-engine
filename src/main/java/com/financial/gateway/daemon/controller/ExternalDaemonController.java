package com.financial.gateway.daemon.controller;

import com.financial.gateway.daemon.dto.DaemonRequestDto;
import com.financial.gateway.daemon.dto.DaemonResponseDto;
import com.financial.gateway.daemon.service.DaemonTransactionService;
import com.financial.gateway.global.crypto.CryptoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inbound")
@Profile("daemon")
public class ExternalDaemonController {

    @Value("${gateway.security.api-key:default-batch-dummy-key}")
    private String expectedApiKey;

    private final DaemonTransactionService daemonService;
    private final CryptoService cryptoService;

    public ExternalDaemonController(DaemonTransactionService daemonService, CryptoService cryptoService) {
        this.daemonService = daemonService;
        this.cryptoService = cryptoService;
    }

    @PostMapping("/transaction")
    public ResponseEntity<DaemonResponseDto> receiveExternalData(
            @RequestHeader(value = "X-API-KEY", required = false) String apiKey,
            @RequestBody DaemonRequestDto requestDto) {

        if (!cryptoService.validateApiKey(apiKey, expectedApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new DaemonResponseDto("401", "인증 실패: 유효하지 않은 API Key", requestDto.getTxId()));
        }

        daemonService.processInboundTransaction(requestDto);

        return ResponseEntity.ok(new DaemonResponseDto("0000", "정상 수신 완료", requestDto.getTxId()));
    }

    @GetMapping("/health")
    public ResponseEntity<String> watchdogHealthCheck() {
        return ResponseEntity.ok("ALIVE");
    }
}
