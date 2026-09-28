package com.financial.gateway.fep.service;

import com.financial.gateway.fep.dto.FepPacketDto;
import com.financial.gateway.fep.parser.BytePacketConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

@Service
public class HostTransferService {

    private static final Logger log = LoggerFactory.getLogger(HostTransferService.class);
    private static final Charset EBCDIC_CHARSET = Charset.forName("CP933");

    @Autowired
    private BytePacketConverter converter;

    /**
     * 당행 이체 본처리: COBOL 기반 호스트 엔진(IBM 메인프레임) EBCDIC 전문 송수신
     */
    public FepPacketDto executeInternalTransfer(FepPacketDto dto) {
        byte[] ebcdicPayload = converter.convertToHostEbcdic(dto);
        log.info("[FEP-HOST] 당행 Host EBCDIC 전문 송신. TxId={}, PayloadSize={} bytes", dto.getTxId(), ebcdicPayload.length);

        // 가상 호스트 코어 응답 시뮬레이션
        dto.setResponseCode("0000");
        dto.setStatus("HOST_SUCCESS");
        log.info("[FEP-HOST] 당행 이체 Host 승인 완료. TxId={}, RespCode={}", dto.getTxId(), dto.getResponseCode());
        return dto;
    }

    /**
     * 타행 이체 본처리: 금융결제원 중계망 연계 (ASCII 전문 통신)
     */
    public FepPacketDto executeExternalKftcTransfer(FepPacketDto dto) {
        byte[] asciiPayload = converter.convertToKftcAscii(dto);
        log.info("[FEP-KFTC] 타행 결제원 ASCII 전문 송신. BankCode={}, TxId={}, PayloadSize={} bytes", 
                dto.getBankCode(), dto.getTxId(), asciiPayload.length);

        // 가상 금융결제원 응답 시뮬레이션
        dto.setResponseCode("0000");
        dto.setStatus("KFTC_SUCCESS");
        log.info("[FEP-KFTC] 타행 이체 금융결제원 중계 승인 완료. TxId={}, RespCode={}", dto.getTxId(), dto.getResponseCode());
        return dto;
    }
}