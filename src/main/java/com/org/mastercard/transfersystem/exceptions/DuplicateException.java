package com.org.mastercard.transfersystem.exceptions;

public class DuplicateException extends RuntimeException{

    public DuplicateException(final String cause){
        super(cause);
    }
}
