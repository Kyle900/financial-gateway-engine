package com.financial.gateway.fep.controller;

import com.financial.gateway.fep.dto.FepPacketDto;
import com.financial.gateway.fep.service.FepFirmbankingPipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/fep")
public class FepPipelineTestController {

    @Autowired
    private FepFirmbankingPipelineService pipelineService;

    /**
     * 당행 이체 테스트 실행 API
     * (100바이트 고정길이 전문 자동 조립 후 파이프라인 실행)
     */
    @PostMapping("/test/transfer-internal")
    public ResponseEntity<FepPacketDto> testInternalTransfer() {
        // 전문 100 bytes 샘플: TxId(20) + Type(4) + Bank(3) + Date(14) + Account(16) + Amount(13) + Resp(4) + Pad(26)
        String samplePacket = String.format("%-20s%-4s%-3s%-14s%-16s%013d%-4s%-26s",
                "TX20260925000000001",
                "0200",
                "023", // 당행코드
                "20260925160000",
                "1234567890123456",
                500000,
                "    ",
                ""
        );

        byte[] rawBytes = samplePacket.getBytes(StandardCharsets.US_ASCII);
        FepPacketDto result = pipelineService.processPipeline(rawBytes);
        return ResponseEntity.ok(result);
    }

    /**
     * 타행 이체 테스트 실행 API (국민은행 004 연계 테스트)
     */
    @PostMapping("/test/transfer-external")
    public ResponseEntity<FepPacketDto> testExternalTransfer() {
        String samplePacket = String.format("%-20s%-4s%-3s%-14s%-16s%013d%-4s%-26s",
                "TX20260925000000002",
                "0200",
                "004", // 국민은행 코드 (타행)
                "20260925160000",
                "9876543210987654",
                1200000,
                "    ",
                ""
        );

        byte[] rawBytes = samplePacket.getBytes(StandardCharsets.US_ASCII);
        FepPacketDto result = pipelineService.processPipeline(rawBytes);
        return ResponseEntity.ok(result);
    }
}