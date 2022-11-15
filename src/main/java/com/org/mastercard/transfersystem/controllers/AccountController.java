package com.org.mastercard.transfersystem.controllers;

import com.org.mastercard.transfersystem.domain.*;
import com.org.mastercard.transfersystem.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for the rest api mapping related to Accounts
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 13-11-2022
 */
@RequestMapping("/accounts")
@RestController
public class AccountController {

    // Assignment Guideline : Use in-memory data-structure to store all the data
    List<Account> allAccounts = new ArrayList<>();

    @Autowired
    private AccountService accountService;

    /**
     * Service to get the account balance
     * @param accountId account id for balance
     * @return account balance
     */
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountId){
        BalanceResponse balance = accountService.getBalance(accountId, allAccounts);
        return ResponseEntity.ok(balance);
    }

    /**
     * Service to transfer the amount
     * @param transferRequest amount transfer request
     * @return transfer status
     */
    @PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransferResponse> transferAmount(@RequestBody TransferRequest transferRequest){

        String fromAccountId = transferRequest.getFromAccountId();
        String toAccountId = transferRequest.getToAccountId();
        double amount = transferRequest.getAmount();
        String currencyCode = transferRequest.getCurrencyCode();

        TransferResponse transferResponse = accountService.transferAmount(fromAccountId,
                toAccountId, amount, currencyCode, allAccounts);
        return ResponseEntity.ok(transferResponse);
    }

    /**
     * Retrieves the mini statement for the account
     * @param accountId account id for mini statement
     * @return mini statement
     */
    @GetMapping("/{accountId}/statements/mini")
    public ResponseEntity<TransactionResponse> getMiniStatement(@PathVariable String accountId){
        TransactionResponse transactionResponse = accountService.getMiniStatement(accountId, allAccounts);
        return ResponseEntity.ok(transactionResponse);
    }

    /**
     * Service to create an account in the system
     * @param account account details for the new account
     * @return
     */
    @PostMapping(value = "/createAccount", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody Account account) {
        AccountResponse accountResponse = accountService.createAccount(account, allAccounts);
        return ResponseEntity.ok(accountResponse);
    }

}
