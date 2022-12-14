package com.org.modernbank.transfersystem.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Response entity class for account creation
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 13-11-2022
 */
@Getter
@Setter
public class AccountResponse {

    private boolean status;
    private Account account;
}
