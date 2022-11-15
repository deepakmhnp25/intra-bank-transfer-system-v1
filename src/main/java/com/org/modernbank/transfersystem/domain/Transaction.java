package com.org.modernbank.transfersystem.domain;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * Transaction info of an account
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 13-11-2022
 */
@Getter
@Setter
public class Transaction {

    private String accountId;
    private double amount;
    private String currency;
    private String type;
    private Date transactionDate;

}
