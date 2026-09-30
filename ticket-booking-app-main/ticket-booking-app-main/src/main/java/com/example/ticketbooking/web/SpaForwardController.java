package com.example.ticketbooking.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Sends React routes to index.html so refreshing a page like /events/2 still works. */
@Controller
public class SpaForwardController {
    @GetMapping({"/events/{id}", "/my-bookings"})
    public String forward() {
        return "forward:/index.html";
    }
}
