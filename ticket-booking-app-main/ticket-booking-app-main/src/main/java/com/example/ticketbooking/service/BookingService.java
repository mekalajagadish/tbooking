package com.example.ticketbooking.service;

import com.example.ticketbooking.model.*;
import com.example.ticketbooking.repository.BookedSeatRepository;
import com.example.ticketbooking.repository.BookingRepository;
import com.example.ticketbooking.repository.EventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Booking rules:
 * <ul>
 *   <li>1 to 6 seats per booking, no duplicates, and every seat must exist in the venue layout</li>
 *   <li>A seat that is already taken cannot be booked again</li>
 *   <li>Online booking closes 1 hour before the show starts</li>
 *   <li>Name and a valid email are required</li>
 * </ul>
 */
@Service
public class BookingService {

    public static final int MAX_SEATS_PER_BOOKING = 6;
    public static final long BOOKING_CLOSES_MINUTES_BEFORE = 60;

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String REF_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final EventRepository events;
    private final BookingRepository bookings;
    private final BookedSeatRepository bookedSeats;
    private final PricingService pricing;
    private final CancellationPolicy cancellationPolicy;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public BookingService(EventRepository events, BookingRepository bookings, BookedSeatRepository bookedSeats,
                          PricingService pricing, CancellationPolicy cancellationPolicy, Clock clock) {
        this.events = events;
        this.bookings = bookings;
        this.bookedSeats = bookedSeats;
        this.pricing = pricing;
        this.cancellationPolicy = cancellationPolicy;
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    public Event getEvent(Long eventId) {
        return events.findById(eventId)
                .orElseThrow(() -> BookingException.notFound("Event " + eventId + " was not found."));
    }

    public boolean isBookingOpen(Event event) {
        return now().isBefore(event.getStartTime().minusMinutes(BOOKING_CLOSES_MINUTES_BEFORE));
    }

    /** Price a seat selection without booking it. */
    @Transactional(readOnly = true)
    public PriceBreakdown quote(Long eventId, List<String> seats) {
        Event event = getEvent(eventId);
        List<String> cleaned = validateSeats(event, seats);
        return pricing.quote(event, cleaned);
    }

    @Transactional
    public synchronized Booking book(Long eventId, List<String> seats, String name, String email) {
        Event event = getEvent(eventId);
        if (!isBookingOpen(event)) {
            throw BookingException.badRequest("Booking for this event has closed. Online booking closes "
                    + BOOKING_CLOSES_MINUTES_BEFORE + " minutes before the show.");
        }
        String customerName = name == null ? "" : name.trim();
        String customerEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (customerName.isEmpty()) {
            throw BookingException.badRequest("Please enter your name.");
        }
        if (!EMAIL.matcher(customerEmail).matches()) {
            throw BookingException.badRequest("Please enter a valid email address.");
        }

        List<String> cleaned = validateSeats(event, seats);
        List<String> taken = bookedSeats.findByEventIdAndSeatLabelIn(eventId, cleaned).stream()
                .map(BookedSeat::getSeatLabel).sorted().collect(Collectors.toList());
        if (!taken.isEmpty()) {
            throw BookingException.conflict("Sorry, these seats were just taken: " + String.join(", ", taken)
                    + ". Please choose different seats.");
        }

        PriceBreakdown price = pricing.quote(event, cleaned);
        Booking booking = bookings.save(new Booking(newReference(), event, customerName, customerEmail,
                cleaned, price, now()));
        try {
            for (String seat : cleaned) {
                bookedSeats.save(new BookedSeat(event, seat, booking));
            }
            bookedSeats.flush();
        } catch (DataIntegrityViolationException e) {
            throw BookingException.conflict("Sorry, one of your seats was just taken. Please choose again.");
        }
        return booking;
    }

    @Transactional(readOnly = true)
    public List<Booking> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw BookingException.badRequest("Please enter the email you booked with.");
        }
        return bookings.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(email.trim());
    }

    @Transactional
    public Booking cancel(String reference, String email) {
        Booking booking = bookings.findByReference(reference == null ? "" : reference.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> BookingException.notFound("Booking " + reference + " was not found."));
        if (email == null || !booking.getCustomerEmail().equalsIgnoreCase(email.trim())) {
            throw BookingException.badRequest("The email does not match this booking.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw BookingException.badRequest("This booking is already cancelled.");
        }
        LocalDateTime now = now();
        if (cancellationPolicy.refundPercent(booking.getEvent().getStartTime(), now) == 0) {
            throw BookingException.badRequest("Bookings cannot be cancelled less than "
                    + CancellationPolicy.MIN_CANCEL_HOURS + " hours before the show.");
        }
        booking.cancel(cancellationPolicy.refundAmount(booking, now), now);
        bookedSeats.deleteByBooking(booking);
        return booking;
    }

    public int refundPercentNow(Booking booking) {
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            return 0;
        }
        return cancellationPolicy.refundPercent(booking.getEvent().getStartTime(), now());
    }

    private List<String> validateSeats(Event event, List<String> seats) {
        if (seats == null || seats.isEmpty()) {
            throw BookingException.badRequest("Please select at least one seat.");
        }
        List<String> cleaned = seats.stream()
                .map(s -> s == null ? "" : s.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toList());
        if (new HashSet<>(cleaned).size() != cleaned.size()) {
            throw BookingException.badRequest("The same seat was selected more than once.");
        }
        if (cleaned.size() > MAX_SEATS_PER_BOOKING) {
            throw BookingException.badRequest("You can book at most " + MAX_SEATS_PER_BOOKING
                    + " seats in one booking.");
        }
        for (String seat : cleaned) {
            if (!event.hasSeat(seat)) {
                throw BookingException.badRequest("Seat " + seat + " does not exist at this venue.");
            }
        }
        cleaned.sort(Comparator.comparing((String s) -> s.charAt(0))
                .thenComparingInt(s -> Integer.parseInt(s.substring(1))));
        return cleaned;
    }

    private String newReference() {
        String ref;
        do {
            StringBuilder sb = new StringBuilder("TB-");
            for (int i = 0; i < 6; i++) {
                sb.append(REF_CHARS.charAt(random.nextInt(REF_CHARS.length())));
            }
            ref = sb.toString();
        } while (bookings.existsByReference(ref));
        return ref;
    }
}
