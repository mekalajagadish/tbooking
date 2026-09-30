package com.example.ticketbooking.model;

import java.math.BigDecimal;

/** Result of pricing a set of seats. Shown to the customer before and after booking. */
public class PriceBreakdown {
    private final int ticketCount;
    private final int premiumCount;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final String discountLabel;
    private final BigDecimal convenienceFee;
    private final BigDecimal total;

    public PriceBreakdown(int ticketCount, int premiumCount, BigDecimal subtotal, BigDecimal discount,
                          String discountLabel, BigDecimal convenienceFee, BigDecimal total) {
        this.ticketCount = ticketCount;
        this.premiumCount = premiumCount;
        this.subtotal = subtotal;
        this.discount = discount;
        this.discountLabel = discountLabel;
        this.convenienceFee = convenienceFee;
        this.total = total;
    }

    public int getTicketCount() { return ticketCount; }
    public int getPremiumCount() { return premiumCount; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public String getDiscountLabel() { return discountLabel; }
    public BigDecimal getConvenienceFee() { return convenienceFee; }
    public BigDecimal getTotal() { return total; }
}
