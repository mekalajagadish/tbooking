package com.example.ticketbooking.model;

import javax.persistence.*;

/**
 * One row per seat that is currently taken. The unique constraint guarantees that the
 * same seat can never be sold twice, even if two customers click "Book" at the same time.
 * Rows are deleted when a booking is cancelled, which releases the seat.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "seat_label"}))
public class BookedSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "seat_label", nullable = false)
    private String seatLabel;

    @ManyToOne(optional = false)
    private Booking booking;

    protected BookedSeat() {
    }

    public BookedSeat(Event event, String seatLabel, Booking booking) {
        this.event = event;
        this.seatLabel = seatLabel;
        this.booking = booking;
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public String getSeatLabel() { return seatLabel; }
    public Booking getBooking() { return booking; }
}
