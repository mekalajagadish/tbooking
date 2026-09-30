package com.example.ticketbooking.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference;

    @ManyToOne(optional = false)
    private Event event;

    private String customerName;
    private String customerEmail;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    private List<String> seats = new ArrayList<>();

    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal convenienceFee;
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private BigDecimal refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    protected Booking() {
    }

    public Booking(String reference, Event event, String customerName, String customerEmail, List<String> seats,
                   PriceBreakdown price, LocalDateTime createdAt) {
        this.reference = reference;
        this.event = event;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.seats = new ArrayList<>(seats);
        this.subtotal = price.getSubtotal();
        this.discount = price.getDiscount();
        this.convenienceFee = price.getConvenienceFee();
        this.total = price.getTotal();
        this.status = BookingStatus.CONFIRMED;
        this.createdAt = createdAt;
    }

    public void cancel(BigDecimal refund, LocalDateTime when) {
        this.status = BookingStatus.CANCELLED;
        this.refundAmount = refund;
        this.cancelledAt = when;
    }

    /** Amount paid for the tickets themselves (after discount), excluding the convenience fee. */
    public BigDecimal getTicketAmount() {
        return subtotal.subtract(discount);
    }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public Event getEvent() { return event; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public List<String> getSeats() { return seats; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getConvenienceFee() { return convenienceFee; }
    public BigDecimal getTotal() { return total; }
    public BookingStatus getStatus() { return status; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
}
