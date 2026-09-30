package com.example.ticketbooking.web;

import com.example.ticketbooking.model.Booking;
import com.example.ticketbooking.model.PriceBreakdown;
import com.example.ticketbooking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** Live price for the current seat selection (called as the customer picks seats). */
    @PostMapping("/quote")
    public PriceBreakdown quote(@RequestBody Dtos.QuoteRequest req) {
        return bookingService.quote(req.eventId, req.seats);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Dtos.BookingView book(@RequestBody Dtos.BookingRequest req) {
        return view(bookingService.book(req.eventId, req.seats, req.customerName, req.customerEmail));
    }

    @GetMapping
    public List<Dtos.BookingView> byEmail(@RequestParam String email) {
        return bookingService.findByEmail(email).stream().map(this::view).collect(Collectors.toList());
    }

    @PostMapping("/{reference}/cancel")
    public Dtos.BookingView cancel(@PathVariable String reference, @RequestBody Dtos.CancelRequest req) {
        return view(bookingService.cancel(reference, req.email));
    }

    private Dtos.BookingView view(Booking b) {
        Dtos.BookingView v = new Dtos.BookingView();
        v.reference = b.getReference();
        v.status = b.getStatus().name();
        v.eventId = b.getEvent().getId();
        v.eventTitle = b.getEvent().getTitle();
        v.venue = b.getEvent().getVenue();
        v.city = b.getEvent().getCity();
        v.startTime = b.getEvent().getStartTime();
        v.customerName = b.getCustomerName();
        v.customerEmail = b.getCustomerEmail();
        v.seats = b.getSeats();
        v.subtotal = b.getSubtotal();
        v.discount = b.getDiscount();
        v.convenienceFee = b.getConvenienceFee();
        v.total = b.getTotal();
        v.refundAmount = b.getRefundAmount();
        v.createdAt = b.getCreatedAt();
        v.cancelledAt = b.getCancelledAt();
        v.refundPercentIfCancelledNow = bookingService.refundPercentNow(b);
        return v;
    }
}
