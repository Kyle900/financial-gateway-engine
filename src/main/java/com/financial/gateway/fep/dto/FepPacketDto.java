package com.financial.gateway.fep.dto;

import java.io.Serializable;

public class FepPacketDto implements Serializable {
    private static final long serialVersionUID = 1L;

    // 공통 헤더 (40 bytes)
    private String txId;            // 전문관리번호 (20 bytes)
    private String telegramType;    // 전문구분코드 (4 bytes: 0200=이체요청, 0210=이체응답)
    private String bankCode;        // 은행코드 (3 bytes: 당행=023, 타행=기타)
    private String sendTime;        // 송신시각 (14 bytes: yyyyMMddHHmmss)

    // 개별 바디 (60 bytes)
    private String accountNo;       // 계좌번호 (16 bytes)
    private Long amount;            // 거래금액 (13 bytes)
    private String responseCode;    // 응답코드 (4 bytes: 0000=정상)
    private String status;          // 처리상태 (PRE_PROCESSED, HOST_SUCCESS, KFTC_SUCCESS, FAILED)

    public FepPacketDto() {}

    public String getTxId() { return txId; }
    public void setTxId(String txId) { this.txId = txId; }

    public String getTelegramType() { return telegramType; }
    public void setTelegramType(String telegramType) { this.telegramType = telegramType; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getSendTime() { return sendTime; }
    public void setSendTime(String sendTime) { this.sendTime = sendTime; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "FepPacketDto{" +
                "txId='" + txId + '\'' +
                ", telegramType='" + telegramType + '\'' +
                ", bankCode='" + bankCode + '\'' +
                ", sendTime='" + sendTime + '\'' +
                ", accountNo='" + accountNo + '\'' +
                ", amount=" + amount +
                ", responseCode='" + responseCode + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}