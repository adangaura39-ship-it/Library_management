package com.library.exception;

/**
 * Thrown when a requested record (book, member, or loan) does not
 * exist in the database.
 */
public class RecordNotFoundException extends Exception {

    public RecordNotFoundException(String message) {
        super(message);
    }
}
