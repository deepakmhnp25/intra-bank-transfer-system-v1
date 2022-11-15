package com.org.mastercard.transfersystem.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Exception handler for service related exceptions
 *
 * @author Deepak Mohan
 * @version 1.0
 * @since 15-11-2022
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DuplicateException.class})
    public ResponseEntity handleException(DuplicateException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler({AccountException.class})
    public ResponseEntity handleException(AccountException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
