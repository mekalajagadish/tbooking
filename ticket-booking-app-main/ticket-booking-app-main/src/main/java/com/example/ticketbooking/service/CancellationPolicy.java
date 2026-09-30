package com.example.ticketbooking.service;

import com.example.ticketbooking.model.Booking;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Refund rules, based on how long before the show the customer cancels:
 * <ul>
 *   <li>72 hours or more before: 100% of the ticket amount refunded</li>
 *   <li>24 to 72 hours before: 50% refunded</li>
 *   <li>Less than 24 hours before: cancellation not allowed</li>
 * </ul>
 * The convenience fee is never refunded.
 */
@Component
public class CancellationPolicy {

    public static final long FULL_REFUND_HOURS = 72;
    public static final long MIN_CANCEL_HOURS = 24;

    /** Percentage of the ticket amount that would be refunded if cancelled at {@code now}; 0 means not allowed. */
    public int refundPercent(LocalDateTime showTime, LocalDateTime now) {
        long minutesLeft = Duration.between(now, showTime).toMinutes();
        if (minutesLeft >= FULL_REFUND_HOURS * 60) {
            return 100;
        }
        if (minutesLeft >= MIN_CANCEL_HOURS * 60) {
            return 50;
        }
        return 0;
    }

    public BigDecimal refundAmount(Booking booking, LocalDateTime now) {
        int percent = refundPercent(booking.getEvent().getStartTime(), now);
        return PricingService.money(booking.getTicketAmount()
                .multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100)));
    }
}
