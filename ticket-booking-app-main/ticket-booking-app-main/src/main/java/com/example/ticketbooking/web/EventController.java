package com.example.ticketbooking.web;

import com.example.ticketbooking.model.Event;
import com.example.ticketbooking.repository.BookedSeatRepository;
import com.example.ticketbooking.repository.EventRepository;
import com.example.ticketbooking.service.BookingService;
import com.example.ticketbooking.service.PricingService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository events;
    private final BookedSeatRepository bookedSeats;
    private final BookingService bookingService;
    private final PricingService pricing;

    public EventController(EventRepository events, BookedSeatRepository bookedSeats,
                           BookingService bookingService, PricingService pricing) {
        this.events = events;
        this.bookedSeats = bookedSeats;
        this.bookingService = bookingService;
        this.pricing = pricing;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<Dtos.EventSummary> list() {
        return events.findAllByOrderByStartTimeAsc().stream()
                .filter(e -> e.getStartTime().isAfter(bookingService.now()))
                .map(e -> fill(new Dtos.EventSummary(), e))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public Dtos.EventDetail get(@PathVariable Long id) {
        Event e = bookingService.getEvent(id);
        Dtos.EventDetail d = fill(new Dtos.EventDetail(), e);
        d.seatRows = e.getSeatRows();
        d.seatsPerRow = e.getSeatsPerRow();
        d.premiumRows = e.getPremiumRows();
        d.bookedSeats = bookedSeats.findSeatLabelsByEventId(id);
        d.maxSeatsPerBooking = BookingService.MAX_SEATS_PER_BOOKING;
        return d;
    }

    private <T extends Dtos.EventSummary> T fill(T s, Event e) {
        s.id = e.getId();
        s.title = e.getTitle();
        s.category = e.getCategory().name();
        s.venue = e.getVenue();
        s.city = e.getCity();
        s.description = e.getDescription();
        s.startTime = e.getStartTime();
        s.basePrice = e.getBasePrice();
        s.premiumPrice = pricing.premiumPrice(e);
        s.totalSeats = e.getTotalSeats();
        s.seatsLeft = e.getTotalSeats() - (int) bookedSeats.countByEventId(e.getId());
        s.bookingOpen = bookingService.isBookingOpen(e);
        return s;
    }
}
