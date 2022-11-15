package com.org.modernbank.transfersystem.services;

import com.org.modernbank.transfersystem.domain.*;
import com.org.modernbank.transfersystem.exceptions.AccountException;
import com.org.modernbank.transfersystem.exceptions.DuplicateException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.org.modernbank.transfersystem.constants.ErrorMessageConstants.*;

/**
 * This service returns the account/transaction related information
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 13-11-2022
 */
@Service
@Qualifier(("accountService"))
public class AccountService {

    /**
     * get the account balance for the account id
     * @param accountId account id for the account
     * @param allAccounts all accounts in the system
     * @return Balance response
     */
    public BalanceResponse getBalance(String accountId, List<Account> allAccounts){
        Optional<Account> accountOptional = allAccounts.stream().filter(account -> account.getAccountId().equals(accountId)).findFirst();
        if(accountOptional.isPresent()){
            // build response
            Account account = accountOptional.get();
            BalanceResponse balanceResponse = new BalanceResponse(account.getAccountId(), account.getBalanceAmount(),
                    account.getCurrencyCode());
            return balanceResponse;
        } else {
            // Account does not exist in the system
            throw new AccountException(NO_ACCOUNT_FOUND_BALANCE);
        }
    }
    /**
     * Debits one account and Credit the amount to another account
     * @param fromAccountId debit account
     * @param toAccountid credit account
     * @param amount amount to be transferred
     * @param currencyCode currency of the transaction
     * @param allAccounts all accounts in the system
     */
    public TransferResponse transferAmount(String fromAccountId, String toAccountid, double amount,
                                           String currencyCode, List<Account> allAccounts){

        // Get the accounts using account id
        Optional<Account> fromAccount = allAccounts.stream().filter(account -> account.getAccountId().equals(fromAccountId)).findFirst();
        Optional<Account> toAccount = allAccounts.stream().filter(account -> account.getAccountId().equals(toAccountid)).findFirst();

        if(fromAccount.isPresent()){
            if(toAccount.isPresent()){
                // debit and credit accounts
                updateBalances(amount, fromAccount, toAccount);

                // create transaction
                transact(fromAccountId, toAccountid, amount, currencyCode, fromAccount, toAccount);

                return buildResponse(fromAccount, toAccount);
            } else {
                // Invalid Receiver account
                throw new AccountException(INVALID_RECEIVER_ACCOUNT);
            }
        } else {
            // Invalid Sender account
            throw new AccountException(INVALID_SENDER_ACCOUNT_DETAILS);
        }
    }

    /**
     * Get mini statement for the account id
     * @param accountId account id for mini statement
     * @param allAccounts all accounts in the system
     * @return mini statement
     */
    public TransactionResponse getMiniStatement(String accountId, List<Account> allAccounts){
        Optional<Account> accountOptional = allAccounts.stream().filter(account -> account.getAccountId().equals(accountId)).findFirst();
        if(accountOptional.isPresent()){
            // get latest 20 transactions
            return getLatest20Transactions(accountOptional.get());
        } else {
            // Account does not exist in the system
            throw new AccountException(UNABLE_TO_GET_THE_STATEMENT_DUE_TO_INVALID_ACCOUNT_ID);
        }
    }

    /**
     * Create an account in the system
     * @param newAccount new account details
     * @param allAccounts in memory store for all accounts
     */
    public AccountResponse createAccount(Account newAccount, List<Account> allAccounts){
        if(allAccounts.stream().anyMatch(accountObj -> accountObj.getAccountId().equals(newAccount.getAccountId()))){
            throw new DuplicateException(ACCOUNT_ALREADY_EXISTS_IN_THE_SYSTEM);
        }
        newAccount.setTransactions(new ArrayList());
        allAccounts.add(newAccount);

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setStatus(true);
        accountResponse.setAccount(newAccount);
        return accountResponse;

    }
    private static void updateBalances(double amount, Optional<Account> fromAccount, Optional<Account> toAccount) {
        double accountBalanceAfterTransfer = fromAccount.get().getBalanceAmount() - amount;
        if(accountBalanceAfterTransfer < 0){
            throw new AccountException(INSUFFICIENT_ACCOUNT_BALANCE_IN_SENDER_ACCOUNT);
        }
        fromAccount.get().setBalanceAmount(accountBalanceAfterTransfer);
        toAccount.get().setBalanceAmount(toAccount.get().getBalanceAmount() + amount);
    }

    /**
     * Builds the final response for the transaction status
     * @param fromAccount debit account
     * @param toAccount credit account
     * @return transfer response
     */
    private static TransferResponse buildResponse(Optional<Account> fromAccount, Optional<Account> toAccount) {
        List<Account> updatedAccounts = new ArrayList<>();
        updatedAccounts.add(fromAccount.get());
        updatedAccounts.add(toAccount.get());
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setUpdatedAccountDetails(updatedAccounts);
        transferResponse.setStatus(true);
        return transferResponse;
    }

    /**
     * Performs the transaction
     * @param fromAccountId debit account id
     * @param toAccountid credit account id
     * @param amount amount to be transferred
     * @param currencyCode
     * @param fromAccount debit account
     * @param toAccount credit account
     */
    private static void transact(String fromAccountId, String toAccountid, double amount, String currencyCode, Optional<Account> fromAccount, Optional<Account> toAccount) {

        Transaction debitTransaction = createTransaction(toAccountid, amount, currencyCode,
                new Date(), "DEBIT");
        Transaction creditTransaction =  createTransaction(fromAccountId, amount, currencyCode,
                new Date(), "CREDIT");

        fromAccount.get().getTransactions().add(debitTransaction);
        toAccount.get().getTransactions().add(creditTransaction);
    }

    /**
     * Creates a transaction
     * @param accountId account id of the transaction
     * @param amount amount to be transferred
     * @param currencyCode currency of the amount
     * @param transactionDate date of transfer
     * @param type type of transfer
     * @return transaction
     */
    private static Transaction createTransaction(String accountId, double amount, String currencyCode,
                                                 Date transactionDate, String type) {
        Transaction debitTransaction = new Transaction();
        debitTransaction.setAccountId(accountId);
        debitTransaction.setAmount(amount);
        debitTransaction.setCurrency(currencyCode);
        debitTransaction.setType(type);
        debitTransaction.setTransactionDate(transactionDate);
        return debitTransaction;
    }

    /**
     * Get latest 20 transactions
     * @param account account
     * @return transactions
     */
    private static TransactionResponse getLatest20Transactions(Account account) {

        // comparator to compare transaction dates
        Comparator<Transaction> comparator = (c1, c2) -> {
            return Long.valueOf(c1.getTransactionDate().getTime()).compareTo(c2.getTransactionDate().getTime());
        };

        // sorting the transactions
        List<Transaction> sortedTransactions = account.getTransactions().stream()
                .sorted(comparator.reversed())
                .collect(Collectors.toList());

        // limit latest 20 transactions
        TransactionResponse transactionResponse
                = new TransactionResponse();
        transactionResponse.setTransactions(sortedTransactions
                .subList(0,sortedTransactions.size() >= 20 ? 20: sortedTransactions.size()));
        return transactionResponse;
    }
}
