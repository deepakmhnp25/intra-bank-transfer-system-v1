package com.org.modernbank.transfersystem.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Response entity class for Balance service
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 13-11-2022
 */
@Getter
@Setter
public class BalanceResponse {

    private String accountId;
    private double balance;
    private String currency;

    public BalanceResponse(String accountId, double balance, String currency) {
        this.accountId = accountId;
        this.balance = balance;
        this.currency = currency;
    }
}
