package com.org.mastercard.transfersystem.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Response Entity class for mini statement request
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 13-11-2022
 */
@Getter
@Setter
public class TransactionResponse {

    private List<Transaction> transactions;

}
