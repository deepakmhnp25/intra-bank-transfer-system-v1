package com.org.mastercard.transfersystem.controllers;

import com.org.mastercard.transfersystem.domain.Account;
import com.org.mastercard.transfersystem.domain.TransferRequest;
import com.org.mastercard.transfersystem.domain.TransferResponse;
import com.org.mastercard.transfersystem.exceptions.DuplicateException;
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
     * Service to create an account in the system
     * @param account account details for the new account
     * @return
     */
    @PostMapping(value = "/createAccount", consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createAccount(@Valid @RequestBody Account account) {
        if(allAccounts.stream().anyMatch(accountObj -> accountObj.getAccountId().equals(account.getAccountId()))){
            throw new DuplicateException("Account already exists in the system");
        }
        accountService.createAccount(account, allAccounts);
        return ResponseEntity.ok(allAccounts);
    }

    @GetMapping("/{accountId}/balance")
    public List<Account> getAccount(@PathVariable String accountId){
        return allAccounts;
    }

    @PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransferResponse> transferAmount(@RequestBody TransferRequest transferRequest){

        String fromAccountId = transferRequest.getFromAccountId();
        String toAccountId = transferRequest.getToAccountId();
        double amount = transferRequest.getAmount();
        String currencyCode = transferRequest.getCurrencyCode();

        TransferResponse transferResponse = accountService.transferAmount(fromAccountId, toAccountId, amount, currencyCode, allAccounts);
        return ResponseEntity.ok(transferResponse);
    }

}
