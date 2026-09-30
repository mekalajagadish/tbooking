package com.example.ticketbooking.config;

import com.example.ticketbooking.model.Event;
import com.example.ticketbooking.model.EventCategory;
import com.example.ticketbooking.repository.EventRepository;
import com.example.ticketbooking.service.BookingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

/**
 * Loads sample events on startup. Dates are relative to "now" so the demo always has
 * upcoming shows, including one 2 days away (50% refund) and one tomorrow (no cancellation).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final EventRepository events;
    private final BookingService bookingService;

    public DataSeeder(EventRepository events, BookingService bookingService) {
        this.events = events;
        this.bookingService = bookingService;
    }

    @Override
    public void run(String... args) {
        LocalDateTime today = bookingService.now().truncatedTo(ChronoUnit.DAYS);

        Event concert = events.save(new Event("Midnight Melodies Live", EventCategory.CONCERT,
                "Gachibowli Stadium", "Hyderabad",
                "An evening of Bollywood and indie hits with a full live band and light show.",
                today.plusDays(12).withHour(19), new BigDecimal("1500"), 8, 12, 2));
        Event movie = events.save(new Event("The Last Orbit (IMAX 3D)", EventCategory.MOVIE,
                "Prasads IMAX, Screen 1", "Hyderabad",
                "A sci-fi thriller about the final crew of a failing space station. 2h 28m, UA.",
                today.plusDays(2).withHour(21), new BigDecimal("350"), 10, 14, 3));
        Event match = events.save(new Event("Hyderabad Hawks vs Chennai Chargers", EventCategory.SPORTS,
                "City Cricket Ground", "Hyderabad",
                "T20 league match. Gates open 90 minutes before the first ball.",
                today.plusDays(6).withHour(19).withMinute(30), new BigDecimal("800"), 9, 12, 2));
        Event comedy = events.save(new Event("Laugh Riot: Stand-up Night", EventCategory.COMEDY,
                "The Humming Tree", "Hyderabad",
                "Four comics, one mic, zero filters. Recommended for ages 16+.",
                bookingService.now().plusHours(20).truncatedTo(ChronoUnit.HOURS), new BigDecimal("499"), 6, 10, 1));

        // A few existing bookings so the seat maps look realistic and "My Bookings" has demo data.
        bookingService.book(concert.getId(), Arrays.asList("A5", "A6", "C3", "C4", "C5"), "Priya Sharma", "priya@example.com");
        bookingService.book(concert.getId(), Arrays.asList("B7", "B8"), "Demo User", "demo@example.com");
        bookingService.book(movie.getId(), Arrays.asList("E7", "E8", "E9"), "Demo User", "demo@example.com");
        bookingService.book(movie.getId(), Arrays.asList("F1", "F2", "G10", "G11"), "Rahul Verma", "rahul@example.com");
        bookingService.book(match.getId(), Arrays.asList("D6", "D7"), "Anita Rao", "anita@example.com");
        bookingService.book(comedy.getId(), Arrays.asList("B4", "B5"), "Demo User", "demo@example.com");
    }
}
