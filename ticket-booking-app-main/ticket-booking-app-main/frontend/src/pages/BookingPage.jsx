import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api.js';
import { CATEGORY_ICON, dateTime, money } from '../format.js';
import SeatMap from '../components/SeatMap.jsx';

export default function BookingPage() {
  const { id } = useParams();
  const [event, setEvent] = useState(null);
  const [selected, setSelected] = useState([]);
  const [quote, setQuote] = useState(null);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [confirmed, setConfirmed] = useState(null);

  const loadEvent = () => api.event(id).then(setEvent).catch((e) => setError(e.message));
  useEffect(() => { loadEvent(); }, [id]);

  // Ask the backend for the price every time the selection changes, so the UI always
  // shows exactly what the server will charge.
  useEffect(() => {
    if (selected.length === 0) { setQuote(null); return; }
    let cancelled = false;
    api.quote(Number(id), selected)
      .then((q) => { if (!cancelled) setQuote(q); })
      .catch((e) => { if (!cancelled) setError(e.message); });
    return () => { cancelled = true; };
  }, [selected, id]);

  const toggleSeat = (label) => {
    setError('');
    setSelected((cur) => {
      if (cur.includes(label)) return cur.filter((s) => s !== label);
      if (cur.length >= event.maxSeatsPerBooking) {
        setError(`You can book at most ${event.maxSeatsPerBooking} seats in one booking.`);
        return cur;
      }
      return [...cur, label];
    });
  };

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const booking = await api.book({ eventId: Number(id), seats: selected, customerName: name, customerEmail: email });
      setConfirmed(booking);
    } catch (err) {
      setError(err.message);
      if (err.status === 409) {
        // Someone else took a seat: refresh the map and drop seats that are no longer free.
        const fresh = await api.event(id);
        setEvent(fresh);
        setSelected((cur) => cur.filter((s) => !fresh.bookedSeats.includes(s)));
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (confirmed) return <Confirmation booking={confirmed} />;
  if (!event) return error ? <div className="alert error">{error}</div> : <p className="muted">Loading…</p>;

  return (
    <>
      <Link to="/" className="back">← All events</Link>
      <div className="event-header">
        <span className="event-icon">{CATEGORY_ICON[event.category]}</span>
        <div>
          <h1>{event.title}</h1>
          <p className="muted">{event.venue}, {event.city} · {dateTime(event.startTime)}</p>
        </div>
      </div>

      <div className="booking-layout">
        <section className="card">
          <h2>1. Choose your seats</h2>
          <p className="muted small">
            Standard {money(event.basePrice)} · Premium (rows A–{String.fromCharCode(64 + event.premiumRows)}) {money(event.premiumPrice)} · up to {event.maxSeatsPerBooking} seats
          </p>
          <SeatMap event={event} selected={selected} onToggle={toggleSeat} />
        </section>

        <aside className="card summary">
          <h2>2. Review &amp; pay</h2>
          {selected.length === 0 ? (
            <p className="muted">Select seats on the map to see the price.</p>
          ) : (
            <>
              <p><strong>Seats:</strong> {[...selected].sort().join(', ')}</p>
              {quote && <PriceTable quote={quote} />}
              {quote && quote.ticketCount < 4 && (
                <p className="hint">Tip: book 4 or more seats to get 10% off.</p>
              )}
            </>
          )}

          <form onSubmit={submit} className="form">
            <label>Full name<input value={name} onChange={(e) => setName(e.target.value)} placeholder="Your name" /></label>
            <label>Email<input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" /></label>
            {error && <div className="alert error">{error}</div>}
            <button className="btn primary block" disabled={selected.length === 0 || submitting}>
              {submitting ? 'Booking…' : quote ? `Pay ${money(quote.total)}` : 'Book'}
            </button>
          </form>

          <details className="rules">
            <summary>Booking &amp; refund rules</summary>
            <ul>
              <li>Premium seats cost 1.5× the standard price.</li>
              <li>10% group discount on 4 or more tickets.</li>
              <li>₹30 convenience fee per ticket (non-refundable).</li>
              <li>Booking closes 1 hour before the show.</li>
              <li>Cancel 72+ hours before: 100% refund. 24–72 hours: 50%. Under 24 hours: no cancellation.</li>
            </ul>
          </details>
        </aside>
      </div>
    </>
  );
}

export function PriceTable({ quote }) {
  return (
    <table className="price">
      <tbody>
        <tr><td>{quote.ticketCount} ticket{quote.ticketCount > 1 ? 's' : ''}{quote.premiumCount ? ` (${quote.premiumCount} premium)` : ''}</td><td>{money(quote.subtotal)}</td></tr>
        {Number(quote.discount) > 0 && (
          <tr className="discount"><td>{quote.discountLabel || 'Discount'}</td><td>−{money(quote.discount)}</td></tr>
        )}
        <tr><td>Convenience fee</td><td>{money(quote.convenienceFee)}</td></tr>
        <tr className="total"><td>Total</td><td>{money(quote.total)}</td></tr>
      </tbody>
    </table>
  );
}

function Confirmation({ booking }) {
  return (
    <div className="card confirm">
      <div className="confirm-icon">✅</div>
      <h1>Booking confirmed!</h1>
      <p className="ref">Booking reference <strong>{booking.reference}</strong></p>
      <p>{booking.eventTitle}<br /><span className="muted">{booking.venue}, {booking.city} · {dateTime(booking.startTime)}</span></p>
      <p><strong>Seats:</strong> {booking.seats.join(', ')}</p>
      <PriceTable quote={{ ...booking, ticketCount: booking.seats.length, premiumCount: 0, discountLabel: 'Group discount' }} />
      <p className="muted small">A confirmation would be sent to {booking.customerEmail}.</p>
      <div className="actions">
        <Link to={`/my-bookings?email=${encodeURIComponent(booking.customerEmail)}`} className="btn primary">View my bookings</Link>
        <Link to="/" className="btn">Book another event</Link>
      </div>
    </div>
  );
}
