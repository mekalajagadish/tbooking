package com.example.ticketbooking.service;

import org.springframework.http.HttpStatus;

/** A business-rule violation, reported to the customer as a readable message. */
public class BookingException extends RuntimeException {
    private final HttpStatus status;

    public BookingException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static BookingException badRequest(String message) {
        return new BookingException(HttpStatus.BAD_REQUEST, message);
    }

    public static BookingException conflict(String message) {
        return new BookingException(HttpStatus.CONFLICT, message);
    }

    public static BookingException notFound(String message) {
        return new BookingException(HttpStatus.NOT_FOUND, message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
