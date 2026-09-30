package com.example.ticketbooking.service;

import com.example.ticketbooking.model.Event;
import com.example.ticketbooking.model.PriceBreakdown;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Pricing rules:
 * <ul>
 *   <li>Standard seat = event base price; premium seat (front rows) = base price x 1.5</li>
 *   <li>Group discount: 10% off the ticket subtotal when booking 4 or more seats</li>
 *   <li>Convenience fee: Rs 30 per ticket (not discounted, not refundable)</li>
 * </ul>
 */
@Service
public class PricingService {

    public static final BigDecimal PREMIUM_MULTIPLIER = new BigDecimal("1.5");
    public static final int GROUP_DISCOUNT_MIN_TICKETS = 4;
    public static final BigDecimal GROUP_DISCOUNT_RATE = new BigDecimal("0.10");
    public static final BigDecimal CONVENIENCE_FEE_PER_TICKET = new BigDecimal("30");

    public BigDecimal premiumPrice(Event event) {
        return money(event.getBasePrice().multiply(PREMIUM_MULTIPLIER));
    }

    public BigDecimal seatPrice(Event event, String seatLabel) {
        return event.isPremiumSeat(seatLabel) ? premiumPrice(event) : money(event.getBasePrice());
    }

    public PriceBreakdown quote(Event event, List<String> seats) {
        int premiumCount = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        for (String seat : seats) {
            if (event.isPremiumSeat(seat)) {
                premiumCount++;
            }
            subtotal = subtotal.add(seatPrice(event, seat));
        }

        BigDecimal discount = money(BigDecimal.ZERO);
        String discountLabel = null;
        if (seats.size() >= GROUP_DISCOUNT_MIN_TICKETS) {
            discount = money(subtotal.multiply(GROUP_DISCOUNT_RATE));
            discountLabel = "Group discount (10% off " + GROUP_DISCOUNT_MIN_TICKETS + "+ tickets)";
        }

        BigDecimal fee = money(CONVENIENCE_FEE_PER_TICKET.multiply(BigDecimal.valueOf(seats.size())));
        BigDecimal total = money(subtotal.subtract(discount).add(fee));
        return new PriceBreakdown(seats.size(), premiumCount, money(subtotal), discount, discountLabel, fee, total);
    }

    static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
