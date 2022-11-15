package com.org.modernbank.transfersystem.exceptions;

/**
 * Runtime account related exceptions
 */
public class AccountException extends RuntimeException{

    public AccountException(String cause){
        super(cause);
    }
}
