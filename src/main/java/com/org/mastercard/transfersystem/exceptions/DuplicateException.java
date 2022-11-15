package com.org.mastercard.transfersystem.exceptions;

/**
 * Exception related to Duplicate creation
 */
public class DuplicateException extends RuntimeException{

    public DuplicateException(final String cause){
        super(cause);
    }
}
