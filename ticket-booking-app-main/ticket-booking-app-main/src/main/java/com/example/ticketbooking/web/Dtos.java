package com.example.ticketbooking.web;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Request and response shapes for the REST API. */
public final class Dtos {

    private Dtos() {
    }

    public static class EventSummary {
        public Long id;
        public String title;
        public String category;
        public String venue;
        public String city;
        public String description;
        public LocalDateTime startTime;
        public BigDecimal basePrice;
        public BigDecimal premiumPrice;
        public int totalSeats;
        public int seatsLeft;
        public boolean bookingOpen;
    }

    public static class EventDetail extends EventSummary {
        public int seatRows;
        public int seatsPerRow;
        public int premiumRows;
        public List<String> bookedSeats;
        public int maxSeatsPerBooking;
    }

    public static class QuoteRequest {
        public Long eventId;
        public List<String> seats;
    }

    public static class BookingRequest {
        public Long eventId;
        public List<String> seats;
        public String customerName;
        public String customerEmail;
    }

    public static class CancelRequest {
        public String email;
    }

    public static class BookingView {
        public String reference;
        public String status;
        public Long eventId;
        public String eventTitle;
        public String venue;
        public String city;
        public LocalDateTime startTime;
        public String customerName;
        public String customerEmail;
        public List<String> seats;
        public BigDecimal subtotal;
        public BigDecimal discount;
        public BigDecimal convenienceFee;
        public BigDecimal total;
        public BigDecimal refundAmount;
        public LocalDateTime createdAt;
        public LocalDateTime cancelledAt;
        /** Refund % if cancelled right now (100, 50, or 0 = cannot cancel). */
        public int refundPercentIfCancelledNow;
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

}
