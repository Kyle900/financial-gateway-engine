package com.financial.gateway.fep.parser;

import com.financial.gateway.fep.dto.FepPacketDto;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class BytePacketConverter {

    private static final Charset EBCDIC_CHARSET = Charset.forName("CP933");
    private static final Charset EUC_KR_CHARSET = Charset.forName("EUC-KR");

    /**
     * VAN사 수신 ASCII 바이트 배열 -> FepPacketDto 파싱 (전처리 로직)
     * Total Length: 100 Bytes Fixed-Length Flat Packet
     */
    public FepPacketDto parseVanAsciiPacket(byte[] rawBytes) {
        if (rawBytes == null || rawBytes.length < 100) {
            throw new IllegalArgumentException("Invalid VAN Packet Length. Expected: 100 bytes, Actual: " + (rawBytes != null ? rawBytes.length : 0));
        }

        // EUC-KR 디코딩 처리
        String rawPacket = new String(rawBytes, 0, 100, EUC_KR_CHARSET);

        FepPacketDto dto = new FepPacketDto();
        dto.setTxId(rawPacket.substring(0, 20).trim());
        dto.setTelegramType(rawPacket.substring(20, 24).trim());
        dto.setBankCode(rawPacket.substring(24, 27).trim());
        dto.setSendTime(rawPacket.substring(27, 41).trim());
        dto.setAccountNo(rawPacket.substring(41, 57).trim());
        dto.setAmount(Long.parseLong(rawPacket.substring(57, 70).trim()));
        dto.setResponseCode(rawPacket.substring(70, 74).trim());
        dto.setStatus("PRE_PROCESSED");

        return dto;
    }

    /**
     * Host 송신용 EBCDIC 변환 (당행 이체 시 IBM 메인프레임 규격)
     */
    public byte[] convertToHostEbcdic(FepPacketDto dto) {
        String fixedString = String.format("%-20s%-4s%-3s%-14s%-16s%013d%-4s%-26s",
                dto.getTxId(),
                dto.getTelegramType(),
                dto.getBankCode(),
                dto.getSendTime(),
                dto.getAccountNo(),
                dto.getAmount(),
                dto.getResponseCode() != null ? dto.getResponseCode() : "    ",
                ""
        );
        return fixedString.getBytes(EBCDIC_CHARSET);
    }

    /**
     * 타행 결제원 송신용 ASCII 바이트 변환
     */
    public byte[] convertToKftcAscii(FepPacketDto dto) {
        String fixedString = String.format("%-20s%-4s%-3s%-14s%-16s%013d%-4s%-26s",
                dto.getTxId(),
                dto.getTelegramType(),
                dto.getBankCode(),
                dto.getSendTime(),
                dto.getAccountNo(),
                dto.getAmount(),
                dto.getResponseCode() != null ? dto.getResponseCode() : "    ",
                ""
        );
        return fixedString.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * EBCDIC 바이트 스트림을 ASCII 바이트로 변환 (수신 Batch Interface)
     */
    public byte[] ebcdicToAscii(byte[] ebcdicBytes) {
        String decoded = new String(ebcdicBytes, EBCDIC_CHARSET);
        return decoded.getBytes(StandardCharsets.US_ASCII);
    }
}