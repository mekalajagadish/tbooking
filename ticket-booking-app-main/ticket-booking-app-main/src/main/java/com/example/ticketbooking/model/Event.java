package com.example.ticketbooking.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private EventCategory category;

    private String venue;
    private String city;

    @Column(length = 500)
    private String description;

    private LocalDateTime startTime;

    /** Price of a standard seat. Premium seats cost more (see PricingService). */
    private BigDecimal basePrice;

    /** Seat layout: rows are lettered A, B, C... and seats numbered 1..seatsPerRow. */
    private int seatRows;
    private int seatsPerRow;

    /** The first N rows (A, B, ...) are premium seats. */
    private int premiumRows;

    protected Event() {
    }

    public Event(String title, EventCategory category, String venue, String city, String description,
                 LocalDateTime startTime, BigDecimal basePrice, int seatRows, int seatsPerRow, int premiumRows) {
        this.title = title;
        this.category = category;
        this.venue = venue;
        this.city = city;
        this.description = description;
        this.startTime = startTime;
        this.basePrice = basePrice;
        this.seatRows = seatRows;
        this.seatsPerRow = seatsPerRow;
        this.premiumRows = premiumRows;
    }

    public int getTotalSeats() {
        return seatRows * seatsPerRow;
    }

    /** True if the label (e.g. "C7") refers to a seat that exists in this event's layout. */
    public boolean hasSeat(String label) {
        if (label == null || label.length() < 2) {
            return false;
        }
        int row = label.charAt(0) - 'A';
        try {
            int number = Integer.parseInt(label.substring(1));
            return row >= 0 && row < seatRows && number >= 1 && number <= seatsPerRow;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isPremiumSeat(String label) {
        return label.charAt(0) - 'A' < premiumRows;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public EventCategory getCategory() { return category; }
    public String getVenue() { return venue; }
    public String getCity() { return city; }
    public String getDescription() { return description; }
    public LocalDateTime getStartTime() { return startTime; }
    public BigDecimal getBasePrice() { return basePrice; }
    public int getSeatRows() { return seatRows; }
    public int getSeatsPerRow() { return seatsPerRow; }
    public int getPremiumRows() { return premiumRows; }
}
