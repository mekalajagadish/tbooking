import { NavLink, Route, Routes } from 'react-router-dom';
import EventsPage from './pages/EventsPage.jsx';
import BookingPage from './pages/BookingPage.jsx';
import MyBookingsPage from './pages/MyBookingsPage.jsx';

export default function App() {
  return (
    <>
      <header className="topbar">
        <div className="container topbar-inner">
          <NavLink to="/" className="brand">🎟️ ShowTime Tickets</NavLink>
          <nav>
            <NavLink to="/" end>Events</NavLink>
            <NavLink to="/my-bookings">My Bookings</NavLink>
          </nav>
        </div>
      </header>
      <main className="container">
        <Routes>
          <Route path="/" element={<EventsPage />} />
          <Route path="/events/:id" element={<BookingPage />} />
          <Route path="/my-bookings" element={<MyBookingsPage />} />
          <Route path="*" element={<p className="muted">Page not found.</p>} />
        </Routes>
      </main>
      <footer className="container footer muted">Sample app · Spring Boot + React · data resets on restart</footer>
    </>
  );
}
