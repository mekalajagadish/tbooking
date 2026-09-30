package com.example.ticketbooking.repository;

import com.example.ticketbooking.model.BookedSeat;
import com.example.ticketbooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface BookedSeatRepository extends JpaRepository<BookedSeat, Long> {

    @Query("select s.seatLabel from BookedSeat s where s.event.id = :eventId")
    List<String> findSeatLabelsByEventId(Long eventId);

    long countByEventId(Long eventId);

    List<BookedSeat> findByEventIdAndSeatLabelIn(Long eventId, Collection<String> seatLabels);

    void deleteByBooking(Booking booking);
}
