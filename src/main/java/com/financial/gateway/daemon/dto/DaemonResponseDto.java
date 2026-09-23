package com.financial.gateway.daemon.dto;

public class DaemonResponseDto {
    private String code;
    private String message;
    private String txId;

    public DaemonResponseDto(String code, String message, String txId) {
        this.code = code;
        this.message = message;
        this.txId = txId;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getTxId() { return txId; }
}
