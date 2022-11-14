package com.org.mastercard.transfersystem.services;

import com.org.mastercard.transfersystem.domain.Account;
import com.org.mastercard.transfersystem.domain.Transaction;
import com.org.mastercard.transfersystem.domain.TransferResponse;
import com.org.mastercard.transfersystem.exceptions.AccountException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This service returns the account related information
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 13-11-2022
 */
@Service
@Qualifier(("accountService"))
public class AccountService {

    /**
     * Returns account details using and accountId
     * @param accountId for the account to be fetched
     * @return Account details for the specific account id
     */
    public Account getAccount(String accountId){


        // since the requirement says to keep all the data in an in memory data structure,
        // reading all the account test data from a file and creating it as a List<Accounts>



        return new Account();
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
                throw new AccountException("Invalid receiver account details");
            }
        } else {
            // Invalid Sender account
            throw new AccountException("Invalid sender account details");
        }

    }

    private static void updateBalances(double amount, Optional<Account> fromAccount, Optional<Account> toAccount) {
        double accountBalanceAfterTransfer = fromAccount.get().getBalanceAmount() - amount;
        if(accountBalanceAfterTransfer < 0){
            throw new AccountException("Insufficient Account Balance in sender account");
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
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String transactionDate = dateFormat.format(date);

        Transaction debitTransaction = createTransaction(toAccountid, amount, currencyCode,
                transactionDate, "DEBIT");
        Transaction creditTransaction =  createTransaction(fromAccountId, amount, currencyCode,
                transactionDate, "CREDIT");

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
                                                 String transactionDate, String type) {
        Transaction debitTransaction = new Transaction();
        debitTransaction.setAccountId(accountId);
        debitTransaction.setAmount(amount);
        debitTransaction.setCurrency(currencyCode);
        debitTransaction.setType(type);
        debitTransaction.setTransactionDate(transactionDate);
        return debitTransaction;
    }

    /**
     * Create an account in the system
     * @param newAccount new account details
     * @param allAccounts in memory store for all accounts
     */
    public void createAccount(Account newAccount, List<Account> allAccounts){
        newAccount.setTransactions(new ArrayList());
        allAccounts.add(newAccount);
    }
}
