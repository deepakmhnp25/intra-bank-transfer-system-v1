package com.org.modernbank.transfersystem.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Response Entity for Transfer service request
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 13-11-2022
 */
@Getter
@Setter
public class TransferResponse {

    private boolean status;
    private List<Account> updatedAccountDetails;

}
