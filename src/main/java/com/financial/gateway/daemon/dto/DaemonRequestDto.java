package com.financial.gateway.daemon.dto;

import java.math.BigDecimal;

public class DaemonRequestDto {
    private String txId;
    private String accountNumber;
    private BigDecimal amount;
    private String bizCode;

    public String getTxId() { return txId; }
    public void setTxId(String txId) { this.txId = txId; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getBizCode() { return bizCode; }
    public void setBizCode(String bizCode) { this.bizCode = bizCode; }
}
