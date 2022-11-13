package com.org.mastercard.transfersystem.controllers;


import com.org.mastercard.transfersystem.TransferSystem;
import com.org.mastercard.transfersystem.domain.Account;
import com.org.mastercard.transfersystem.domain.Transactions;
import com.org.mastercard.transfersystem.domain.TransferStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is the test class to test Account Related functionalities
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 12-11-2022
 */
@SpringBootTest(classes = TransferSystem.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    /**
     * Acceptence Criteria 1
     * Given valid account details and positive funds available
     * When account-id 111 sends £10 to account-id 222
     * Then account-111's account should be debited with £10
     * And account-222's account should be credited with 10
     */
    void testTransferAmount(){
        // Given valid account details and positive funds available

        // When account-id 111 sends £10 to account-id 222
        Transactions transferRequest = new Transactions();
        transferRequest.setToAccountId("222");
        transferRequest.setFromAccountId("111");
        TransferStatus transferStatus = this.testRestTemplate.postForObject("http://localhost:" + port + "/accounts/transfer", Transactions.class, TransferStatus.class);

        assertTrue(transferStatus.isStatus());

        // Then account-111's account should be debited with £10
        Optional<Account> fromAccount = transferStatus.getUpdatedAccountDetails().stream().filter(account -> account.getAccountId().equals("111")).findFirst();
        assertTrue(fromAccount.isPresent());
        assertEquals(10 , fromAccount.get().getBalanceAmount());

        // And account-222's account should be credited with 10
        Optional<Account> toAccount = transferStatus.getUpdatedAccountDetails().stream().filter(account -> account.getAccountId().equals("222")).findFirst();
        assertTrue(toAccount.isPresent());
        assertEquals(30 , toAccount.get().getBalanceAmount());

    }

    /**
     * Get account balance of the specified valid account id
     * Acceptance criteria no 4 : When I call a service to check my account balance
     * Then system should be able to report my current balance
     */
    @Test
    void testAccountBalance(){

        // Given : Valid account detail
        String accountId = "111";

        // When : I call a service to check my account balance
        Account accountResponse = this.testRestTemplate.getForObject("http://localhost:" + port + "/accounts/" + accountId + "/balance", Account.class);

        // Then : Then system should be able to report my current balance
        assertEquals(100.10, accountResponse.getBalanceAmount());
        assertEquals("GBP", accountResponse.getCurrencyCode());
        assertEquals("111", accountResponse.getAccountId());
    }

}