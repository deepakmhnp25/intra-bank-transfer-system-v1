package com.org.modernbank.transfersystem.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * This pojo holds the transaction related details
 * for each account
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 13-11-2022
 */
@Getter
@Setter
public class TransferRequest {

    private String fromAccountId;
    private String toAccountId;
    private double amount;
    private String currencyCode;
}
