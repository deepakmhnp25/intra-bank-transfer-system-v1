package com.org.mastercard.transfersystem.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transaction {

    private String accountId;
    private double amount;
    private String currency;
    private String type;
    private String transactionDate;

}
