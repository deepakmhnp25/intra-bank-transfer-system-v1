package com.org.mastercard.transfersystem;

import com.org.mastercard.transfersystem.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static com.org.mastercard.transfersystem.constants.ErrorMessageConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is the test class to test Account/Transfer Related functionalities
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 12-11-2022
 */
@SpringBootTest(classes = TransferSystem.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferSystemTest {

    private static Logger logger = LoggerFactory.getLogger(TransferSystemTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    /**
     * Acceptance Criteria 1
     * Given valid account details and positive funds available
     * When account-id 111 sends £10 to account-id 222
     * Then account-111's account should be debited with £10
     * And account-222's account should be credited with 10
     */
    @Test
    void testTransferAmount(){
        // Given valid account details and positive funds available
        createAccount("111", "GBP", 20);
        createAccount("222", "GBP", 20);

        // When account-id 111 sends £10 to account-id 222
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setToAccountId("222");
        transferRequest.setFromAccountId("111");
        transferRequest.setAmount(10);
        transferRequest.setCurrencyCode("GBP");
        TransferResponse transferStatus = this.testRestTemplate
                .postForObject("http://localhost:" + port + "/accounts/transfer",
                        new HttpEntity<>(transferRequest), TransferResponse.class);

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
        createAccount("111", "GBP", 20);

        // When account-id 111 sends £10 to account-id 999
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setToAccountId("999");
        transferRequest.setFromAccountId("111");
        transferRequest.setAmount(10);
        ResponseEntity responseEntity = this.testRestTemplate
                .postForEntity("http://localhost:" + port + "/accounts/transfer",
                        new HttpEntity<>(transferRequest), String.class);

        // Then system should reject the transfer and report invalid account details
        assertEquals(HttpStatus.BAD_REQUEST,  responseEntity.getStatusCode());
        assertEquals(INVALID_RECEIVER_ACCOUNT, responseEntity.getBody());
    }

    /**
     * Should not transfer the amount when
     * Invalid sender account details
     */
    @Test
    void invalidSenderAccountDetails(){

        // Given invalid sender account details
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setToAccountId("111");
        transferRequest.setFromAccountId("999");
        transferRequest.setAmount(10);

        // When account-id 111 sends £10 to account-id 999
        ResponseEntity responseEntity = this.testRestTemplate
                .postForEntity("http://localhost:" + port + "/accounts/transfer",
                        new HttpEntity<>(transferRequest), String.class);

        // Then system should reject the transfer and report invalid account details
        assertEquals(HttpStatus.BAD_REQUEST,  responseEntity.getStatusCode());
        assertEquals(INVALID_SENDER_ACCOUNT_DETAILS, responseEntity.getBody());
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
        createAccount("333", "GBP", 0);
        createAccount("222", "GBP", 20);

        // When account-id 333 sends £10 to account-id 222
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setToAccountId("222");
        transferRequest.setFromAccountId("333");
        transferRequest.setAmount(10);
        ResponseEntity responseEntity = this.testRestTemplate
                .postForEntity("http://localhost:" + port + "/accounts/transfer",
                        new HttpEntity<>(transferRequest), String.class);

        // Then system should reject the transfer with error Insufficient
        // funds available
        assertEquals(HttpStatus.BAD_REQUEST,  responseEntity.getStatusCode());
        assertEquals(INSUFFICIENT_ACCOUNT_BALANCE_IN_SENDER_ACCOUNT, responseEntity.getBody());
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
        createAccount("111", "GBP", 20);
        String accountId = "111";

        // When : I call a service to check my account balance
        BalanceResponse balanceResponse = this.testRestTemplate
                .getForObject("http://localhost:" + port + "/accounts/" + accountId + "/balance",
                        BalanceResponse.class);

        // Then : Then system should be able to report my current balance
        assertEquals(20, balanceResponse.getBalance());
        assertEquals("GBP", balanceResponse.getCurrency());
        assertEquals("111", balanceResponse.getAccountId());
    }

    /**
     * Acceptance Criteria : 5
     * Given valid account details
     * When I call mini-statement service
     * Then system should be able to show me last 20 transactions
     */
    @Test
    void getMiniStatement(){
        // Given valid account details
        createAccount("666", "GBP", 20);
        createAccount("777", "GBP", 20);
        // create 21 transactions
        createTransactions("666", "777", 5);
        createTransactions("666", "777", 1);
        createTransactions("777", "666", 1);
        createTransactions("777", "666", 7);
        createTransactions("666", "777", 2);
        createTransactions("666", "777", 9);
        createTransactions("777", "666", 2);
        createTransactions("777", "666", 2);
        createTransactions("777", "666", 2);
        createTransactions("777", "666", 2);
        createTransactions("777", "666", 1);
        createTransactions("666", "777", 9);
        createTransactions("777", "666", 9);
        createTransactions("777", "666", 2);
        createTransactions("777", "666", 2);
        createTransactions("777", "666", 2);
        createTransactions("666", "777", 2);
        createTransactions("666", "777", 2);
        createTransactions("777", "666", 2);
        createTransactions("666", "777", 2);
        createTransactions("666", "777", 2);

        // When I call mini-statement service
        String accountId = "666";
        TransactionResponse transactionResponse = this.testRestTemplate
                .getForObject("http://localhost:" + port + "/accounts/" + accountId + "/statements/mini",
                        TransactionResponse.class);

        // Then system should be able to show me last 20 transactions
        assertEquals(20, transactionResponse.getTransactions().size());

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
        String accountId = "999";

        // When I call a service to check my account balance
        ResponseEntity responseEntity =
                this.testRestTemplate.getForEntity("http://localhost:" + port + "/accounts/" + accountId + "/balance",
                        String.class);

        assertEquals(HttpStatus.BAD_REQUEST,  responseEntity.getStatusCode());
        assertEquals(NO_ACCOUNT_FOUND_BALANCE, responseEntity.getBody());
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
        String accountId = "999";

        // When I call mini statement service
        ResponseEntity responseEntity = this.testRestTemplate
                .getForEntity("http://localhost:" + port + "/accounts/" + accountId + "/statements/mini",
                        String.class);

        // Then system should return error saying invalid account number
        assertEquals(HttpStatus.BAD_REQUEST,  responseEntity.getStatusCode());
        assertEquals(UNABLE_TO_GET_THE_STATEMENT_DUE_TO_INVALID_ACCOUNT_ID, responseEntity.getBody());
    }
    /**
     * Test successful creation of account
     */
    @Test
    void testAccountCreation(){
        // Given valid account details
        Account account = new Account();
        account.setAccountId("555");
        account.setCurrencyCode("GBP");
        account.setBalanceAmount(100.10);

        HttpEntity<Account> request = new HttpEntity<>(account);
        // When trying to create an account with valid account details
        AccountResponse accountResponse
                = this.testRestTemplate.postForObject("http://localhost:" + port + "/accounts/createAccount",
                request, AccountResponse.class);
        // Then : one account should be created
        assertTrue(accountResponse.isStatus());
        assertEquals("555", accountResponse.getAccount().getAccountId());
    }


    @Test
    void testDuplicateAccountCreation(){
        // Given duplicate account details
        createAccount("111", "GBP", 20);

        Account account1 = new Account();
        account1.setAccountId("111");
        account1.setCurrencyCode("GBP");
        account1.setBalanceAmount(100.10);

        HttpEntity<Account> request1 = new HttpEntity<>(account1);

        ResponseEntity responseEntity = this.testRestTemplate.postForEntity("http://localhost:" + port + "/accounts/createAccount",
                request1, String.class);
        // Then : one account should be created
        assertEquals(HttpStatus.BAD_REQUEST,  responseEntity.getStatusCode());
        assertEquals(ACCOUNT_ALREADY_EXISTS_IN_THE_SYSTEM, responseEntity.getBody());
    }

    /**
     * Creates a test account in the system
     * @param accountId account id of the account
     * @param currencyCode currency code of the account
     * @param startingBalance starting balance of the account
     */
    private void createAccount(String accountId, String currencyCode, double startingBalance) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setCurrencyCode(currencyCode);
        account.setBalanceAmount(startingBalance);

        HttpEntity<Account> request = new HttpEntity<>(account);
        try {
            // When trying to create an account with valid account details
            this.testRestTemplate.postForObject("http://localhost:" + port + "/accounts/createAccount",
                    request, AccountResponse.class);
        } catch (RuntimeException ex){
            logger.info(ACCOUNT_ALREADY_EXISTS_IN_THE_SYSTEM);
        }
    }

    /**
     * Creates a transaction/transfer in the account
     * @param fromAccount sender's account id
     * @param toAccount receiver's account id
     * @param amount transfer amount
     */
    private void createTransactions(String fromAccount, String toAccount, double amount){
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setToAccountId(toAccount);
        transferRequest.setFromAccountId(fromAccount);
        transferRequest.setAmount(amount);

        // When account-id 111 sends £10 to account-id 222
        TransferResponse transferStatus = this.testRestTemplate
                .postForObject("http://localhost:" + port + "/accounts/transfer",
                        new HttpEntity<>(transferRequest), TransferResponse.class);
    }

}