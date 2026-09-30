package com.example.ticketbooking.web;

import com.example.ticketbooking.service.BookingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns business-rule errors into JSON like {"message": "..."} that the UI shows to the user. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<Dtos.ErrorResponse> handle(BookingException e) {
        return ResponseEntity.status(e.getStatus()).body(new Dtos.ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<Dtos.ErrorResponse> badInput(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Dtos.ErrorResponse("Invalid request."));
    }
}
