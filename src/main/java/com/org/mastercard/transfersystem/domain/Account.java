package com.org.mastercard.transfersystem.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This pojo is for holding the Account details
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 13-11-2022
 */
@Getter
@Setter
public class Account {

    private String accountId;
    private double balanceAmount;
    private String currencyCode;
    private List<Transactions> transactions;
}
