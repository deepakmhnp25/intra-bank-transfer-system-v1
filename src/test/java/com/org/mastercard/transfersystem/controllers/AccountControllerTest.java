package com.org.mastercard.transfersystem.controllers;

import com.org.mastercard.transfersystem.TransferSystem;
import com.org.mastercard.transfersystem.domain.Account;
import com.org.mastercard.transfersystem.domain.TransactionResponse;
import com.org.mastercard.transfersystem.domain.TransactionRequest;
import com.org.mastercard.transfersystem.domain.TransferStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is the test class to test Account Related functionalities
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 12-11-2022
 */
@SpringBootTest(classes = TransferSystem.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeAll
    void setUp() throws IOException {
        createAccount("111", "GBP", 20);
        createAccount("222", "GBP", 20);
        createAccount("333", "GBP", 0);
    }

    /**
     * Acceptance Criteria 1
     * Given valid account details and positive funds available
     * When account-id 111 sends £10 to account-id 222
     * Then account-111's account should be debited with £10
     * And account-222's account should be credited with 10
     */
    void testTransferAmount(){
        // Given valid account details and positive funds available
        TransactionRequest transferRequest = new TransactionRequest();
        transferRequest.setToAccountId("222");
        transferRequest.setFromAccountId("111");
        transferRequest.setAmount(10);

        // When account-id 111 sends £10 to account-id 222
        TransferStatus transferStatus = this.testRestTemplate
                .postForObject("http://localhost:" + port + "/accounts/transfer",
                        new HttpEntity<>(transferRequest), TransferStatus.class);

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
     * Acceptance Criteria 2
     * Given invalid receiver account details and positive funds available
     * When account-id 111 sends £10 to account-id 999
     * Then system should reject the transfer and report invalid account details
     */
    @Test
    void invalidAccountDetailsForFundTransfer(){
        // Given valid account details and positive funds available
        TransactionRequest transferRequest = new TransactionRequest();
        transferRequest.setToAccountId("999");
        transferRequest.setFromAccountId("111");
        transferRequest.setAmount(10);

        // When account-id 111 sends £10 to account-id 999
        ResponseEntity responseEntity = this.testRestTemplate
                .postForEntity("http://localhost:" + port + "/accounts/transfer",
                        new HttpEntity<>(transferRequest), String.class);

        // Then system should reject the transfer and report invalid account details
        assertEquals(400,  responseEntity.getStatusCode());
        assertEquals("Invalid receiver account details", responseEntity.getBody());

    }

    /**
     * Acceptance criteria 3
     * Given valid account details and no funds available (fO)
     * When account-id 333 sends £10 to account-id 222
     * Then system should reject the transfer with error Insufficient
     * funds available
     */
    @Test
    void testFundTransferNoFundAvailable(){
        // Given valid account details and no funds available (fO)
        TransactionRequest transferRequest = new TransactionRequest();
        transferRequest.setToAccountId("222");
        transferRequest.setFromAccountId("333");
        transferRequest.setAmount(10);

        // When account-id 333 sends £10 to account-id 222
        ResponseEntity responseEntity = this.testRestTemplate
                .postForEntity("http://localhost:" + port + "/accounts/transfer",
                        new HttpEntity<>(transferRequest), String.class);

        // Then system should reject the transfer with error Insufficient
        // funds available
        assertEquals(400,  responseEntity.getStatusCode());
        assertEquals("Insufficient Account Balance in Account 333", responseEntity.getBody());
    }

    /**
     * Acceptance criteria no 4
     * Given valid account details
     * When I call a service to check my account balance
     * Then system should be able to report my current balance
     */
    @Test
    void testAccountBalance(){

        // Given : Valid account detail
        String accountId = "111";

        // When : I call a service to check my account balance
        Account accountResponse = this.testRestTemplate.getForObject("http://localhost:" + port + "/accounts/" + accountId + "/balance", Account.class);

        // Then : Then system should be able to report my current balance
        assertEquals(10, accountResponse.getBalanceAmount());
        assertEquals("GBP", accountResponse.getCurrencyCode());
        assertEquals("111", accountResponse.getAccountId());
    }

    /**
     * Acceptance Criteria : 5
     * Given valid account details
     * When I call mini-statement service
     * Then system should be able to show me last 20 transactions
     */
    @Test
    void getMiniStatement(){

        createTransactions("111", "222", 5);
        createTransactions("111", "222", 1);
        createTransactions("222", "111", 1);
        createTransactions("222", "111", 7);
        createTransactions("111", "222", 2);
        createTransactions("111", "222", 9);
        createTransactions("222", "111", 2);
        createTransactions("222", "111", 2);
        createTransactions("222", "111", 2);
        createTransactions("222", "111", 2);
        createTransactions("222", "111", 1);
        createTransactions("111", "222", 9);


        // Given valid account details
        String accountId = "111";

        // When I call mini-statement service
        TransactionResponse transactionResponse = this.testRestTemplate
                .getForObject("http://localhost:" + port + "/accounts/" + accountId + "/statements/mini",
                        TransactionResponse.class);

        // Then system should be able to show me last 20 transactions
        assertEquals(12, transactionResponse.getTransactions().size());

    }

    /**
     * Acceptance Criteria : 6
     * Given invalid account details
     * When I call a service to check my account balance
     * Then system should return error saying invalid account number
     */
    @Test
    void checkBalanceWithInvalidAccountNo(){
        // Given : invalid account details
        String accountId = "111";

        // When I call a service to check my account balance
        ResponseEntity responseEntity = this.testRestTemplate.getForEntity("http://localhost:" + port + "/accounts/" + accountId + "/balance", String.class);

        assertEquals(400,  responseEntity.getStatusCode());
        assertEquals("Unable to check the balance due to invalid account id ", responseEntity.getBody());
    }

    /**
     * Acceptance criteria : 7
     * Given invalid account details
     * When I call mini statement service
     * Then system should return error saying invalid account number
     */
    @Test
    void getMiniStatementWithInvalidAccountNo(){

        // Given invalid account details
        String accountId = "111";

        // When I call mini statement service
        ResponseEntity responseEntity = this.testRestTemplate
                .getForEntity("http://localhost:" + port + "/accounts/" + accountId + "/statements/mini",
                        String.class);

        // Then system should return error saying invalid account number
        assertEquals(400,  responseEntity.getStatusCode());
        assertEquals("Unable to get the statement due to invalid account id ", responseEntity.getBody());
    }
    /**
     * Test successful creation of account
     */
    @Test
    void testAccountCreation(){
        // Given valid account details
        Account account = new Account();
        account.setAccountId("111");
        account.setCurrencyCode("GBP");
        account.setBalanceAmount(100.10);

        HttpEntity<Account> request = new HttpEntity<>(account);
        // When trying to create an account with valid account details
        ArrayList allAccounts
                = this.testRestTemplate.postForObject("http://localhost:" + port + "/accounts/createAccount",
                request, ArrayList.class);
        // Then : one account should be created
        assertEquals(1,  allAccounts.size());
    }


    @Test
    void testDuplicateAccountCreation(){
        // Given duplicate account details
        Account account1 = new Account();
        account1.setAccountId("111");
        account1.setCurrencyCode("GBP");
        account1.setBalanceAmount(100.10);

        HttpEntity<Account> request1 = new HttpEntity<>(account1);

        Account account2 = new Account();
        account2.setAccountId("111");
        account2.setCurrencyCode("GBP");
        account2.setBalanceAmount(100.10);

        HttpEntity<Account> request2 = new HttpEntity<>(account2);
        // When trying to create accounts with same id twice
        ArrayList allAccounts
                = this.testRestTemplate.postForObject("http://localhost:" + port + "/accounts/createAccount",
                request1, ArrayList.class);
        ResponseEntity responseEntity = this.testRestTemplate.postForEntity("http://localhost:" + port + "/accounts/createAccount",
                request1, String.class);
        // Then : one account should be created
        assertEquals(400,  responseEntity.getStatusCode());
        assertEquals("Account already exists in the system", responseEntity.getBody());
    }

    private void createAccount(String accountId, String currencyCode, double startingBalance) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setCurrencyCode(currencyCode);
        account.setBalanceAmount(startingBalance);

        HttpEntity<Account> request = new HttpEntity<>(account);
        // When trying to create an account with valid account details
        this.testRestTemplate.postForObject("http://localhost:" + port + "/accounts/createAccount",
                request, ArrayList.class);
    }

    private void createTransactions(String fromAccount, String toAccount, double amount){
        TransactionRequest transferRequest = new TransactionRequest();
        transferRequest.setToAccountId(toAccount);
        transferRequest.setFromAccountId(fromAccount);
        transferRequest.setAmount(amount);

        // When account-id 111 sends £10 to account-id 222
        TransferStatus transferStatus = this.testRestTemplate
                .postForObject("http://localhost:" + port + "/accounts/transfer",
                        new HttpEntity<>(transferRequest), TransferStatus.class);
    }

}