# ShowTime Tickets: Ticket Booking Sample App

A small full-stack ticket booking app for movies, concerts, sports and comedy shows.
It has **Spring Boot** on the backend, **React** (Vite) on the frontend, and an in-memory
**H2** database. The React build is served by Spring Boot, so the whole app runs on
**one port (8080)** with one command.

## Pages

| Page | URL | What it does |
|---|---|---|
| **Events** | `/` | Lists upcoming events with date, venue, starting price and seats left. Filter by category. |
| **Book tickets** | `/events/{id}` | Interactive seat map (standard / premium / taken), live price breakdown, name and email form, and a confirmation with a booking reference. |
| **My Bookings** | `/my-bookings` | Look up bookings by email, see status, and cancel with a refund preview. |

## Business rules

All rules are enforced on the **server**. The UI only displays what the API returns.

**Pricing** (`PricingService`)
- Standard seat = event base price; **premium seat** (front rows) = base price × **1.5**
- **Group discount:** 10% off the ticket subtotal for **4 or more** tickets
- **Convenience fee:** ₹30 per ticket

**Booking** (`BookingService`)
- 1 to **6 seats** per booking; duplicate or non-existent seats are rejected
- A seat can't be sold twice. A database unique constraint on (event, seat) guarantees this
  even when two people book at the same moment (the loser gets HTTP 409 and the UI refreshes the map)
- Online booking **closes 1 hour** before the show
- Name and a valid email are required; each booking gets a reference like `TB-7XPPDJ`

**Cancellation and refunds** (`CancellationPolicy`)
- **72+ hours** before the show: 100% of the ticket amount refunded
- **24 to 72 hours** before: 50% refunded
- **Under 24 hours:** cancellation not allowed
- The convenience fee is never refunded. Cancelling releases the seats for others.

Sample data is loaded on startup with dates relative to today, so you can see each refund
tier: a show tomorrow (no cancellation), one in 2 days (50%), and ones further out (100%).
Use **`demo@example.com`** on the My Bookings page to see sample bookings.

## Run it

**Requirements:** Java 11 to 21 (JDK 17 recommended). Node.js is only needed if you change the frontend.

```bash
# Windows
start-app.bat          # or: gradlew.bat bootRun

# macOS / Linux
./gradlew bootRun
```

Open **http://localhost:8080**. The H2 console is at http://localhost:8080/h2-console
(JDBC URL `jdbc:h2:mem:ticketdb`, user `sa`, blank password). Data resets on restart.

### Working on the frontend

```bash
cd frontend
npm install
npm run dev     # hot-reload UI on http://localhost:5173 (proxies /api to the backend on :8080)
npm run build   # rebuilds the UI into src/main/resources/static
```

## REST API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/events` | Upcoming events |
| GET | `/api/events/{id}` | Event details, seat layout and taken seats |
| POST | `/api/bookings/quote` | Price a seat selection: `{"eventId":1,"seats":["A1","D4"]}` |
| POST | `/api/bookings` | Book: `{"eventId":1,"seats":["D4"],"customerName":"Asha","customerEmail":"asha@example.com"}` |
| GET | `/api/bookings?email=` | Bookings for an email |
| POST | `/api/bookings/{reference}/cancel` | Cancel: `{"email":"asha@example.com"}` |

Rule violations return an HTTP 400/404/409 status with `{"message": "..."}`.

## Project structure

```
src/main/java/com/example/ticketbooking
├── model/          Event, Booking, BookedSeat, PriceBreakdown
├── repository/     Spring Data JPA repositories
├── service/        PricingService, BookingService, CancellationPolicy  ← business rules
├── web/            REST controllers, DTOs, error handler, SPA route forwarding
└── config/         Clock bean, sample data seeder
frontend/src
├── pages/          EventsPage, BookingPage, MyBookingsPage
├── components/     SeatMap
└── api.js          fetch wrapper for the REST API
```

## License

MIT
