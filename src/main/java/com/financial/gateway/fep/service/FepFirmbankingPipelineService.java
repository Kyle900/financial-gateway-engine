package com.financial.gateway.fep.service;

import com.financial.gateway.fep.dto.FepPacketDto;
import com.financial.gateway.fep.parser.BytePacketConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FepFirmbankingPipelineService {

    private static final Logger log = LoggerFactory.getLogger(FepFirmbankingPipelineService.class);
    private static final String OUR_BANK_CODE = "023"; // SC제일은행 코드

    @Autowired
    private BytePacketConverter byteConverter;

    @Autowired
    private HostTransferService hostTransferService;

    /**
     * FEP 파이프라인 전체 프로세스 실행
     * 1. 전처리: VAN 전문(ASCII) 바이트 수신 및 디코딩
     * 2. 본처리: 당행(Host EBCDIC) / 타행(금융결제원 ASCII) 라우팅 송수신
     * 3. 후처리: 결과 전문 생성 및 마감 정산 데이터 준비
     */
    @Transactional
    public FepPacketDto processPipeline(byte[] vanRawPacket) {
        log.info("========== [FEP Pipeline Start] ==========");

        // 1. 전처리 (ASCII/EUC-KR 바이트 파싱)
        FepPacketDto packetDto = byteConverter.parseVanAsciiPacket(vanRawPacket);
        log.info("[Step 1: 전처리 완료] TxId={}, 계좌번호={}, 요청금액={}원", 
                packetDto.getTxId(), packetDto.getAccountNo(), packetDto.getAmount());

        // 2. 본처리 (라우팅)
        if (OUR_BANK_CODE.equals(packetDto.getBankCode())) {
            log.info("[Step 2: 본처리 라우팅] 당행 이체 식별 -> 메인프레임 Host 연계");
            packetDto = hostTransferService.executeInternalTransfer(packetDto);
        } else {
            log.info("[Step 2: 본처리 라우팅] 타행 이체 식별 (타행코드: {}) -> 금융결제원 연계", packetDto.getBankCode());
            packetDto = hostTransferService.executeExternalKftcTransfer(packetDto);
        }

        // 3. 후처리 (결과 파일 생성 및 일일 정산 준비)
        processPostSettlement(packetDto);
        log.info("========== [FEP Pipeline Completed: TxId={}] ==========", packetDto.getTxId());

        return packetDto;
    }

    private void processPostSettlement(FepPacketDto dto) {
        // 후처리: 결과 파일 생성(NDM 전송용) 및 일일정산 배치 큐 등록 시뮬레이션
        log.info("[Step 3: 후처리 완료] 정산 데이터 FEP DB 적재 준비. Status={}, RespCode={}", 
                dto.getStatus(), dto.getResponseCode());
    }
}