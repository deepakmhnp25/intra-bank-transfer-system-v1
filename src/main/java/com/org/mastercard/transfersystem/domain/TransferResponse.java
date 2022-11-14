package com.org.mastercard.transfersystem.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TransferResponse {

    private boolean status;
    private List<Account> updatedAccountDetails;

}
