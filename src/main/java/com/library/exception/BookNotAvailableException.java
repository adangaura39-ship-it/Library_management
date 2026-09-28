package com.library.exception;

/**
 * Thrown when an attempt is made to issue a book that has zero
 * available copies.
 */
public class BookNotAvailableException extends Exception {

    public BookNotAvailableException(String message) {
        super(message);
    }
}
