import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api.js';
import { dateTime, money } from '../format.js';

export default function MyBookingsPage() {
  const [params, setParams] = useSearchParams();
  const [email, setEmail] = useState(params.get('email') || '');
  const [bookings, setBookings] = useState(null);
  const [error, setError] = useState('');

  const load = (addr) =>
    api.bookingsByEmail(addr).then((b) => { setBookings(b); setError(''); }).catch((e) => setError(e.message));

  useEffect(() => {
    const q = params.get('email');
    if (q) load(q);
  }, [params]);

  const search = (e) => {
    e.preventDefault();
    setParams({ email: email.trim() });
  };

  const replace = (updated) => setBookings((list) => list.map((b) => (b.reference === updated.reference ? updated : b)));

  return (
    <>
      <h1>My bookings</h1>
      <form onSubmit={search} className="search">
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="Email used for booking" required />
        <button className="btn primary">Find bookings</button>
      </form>
      <p className="muted small">Try <button className="linklike" onClick={() => { setEmail('demo@example.com'); setParams({ email: 'demo@example.com' }); }}>demo@example.com</button> to see sample bookings.</p>

      {error && <div className="alert error">{error}</div>}
      {bookings && bookings.length === 0 && <p className="muted">No bookings found for this email.</p>}
      <div className="booking-list">
        {bookings && bookings.map((b) => <BookingCard key={b.reference} booking={b} onUpdated={replace} />)}
      </div>
    </>
  );
}

function BookingCard({ booking: b, onUpdated }) {
  const [confirming, setConfirming] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const ticketAmount = Number(b.subtotal) - Number(b.discount);
  const refundNow = (ticketAmount * b.refundPercentIfCancelledNow) / 100;
  const cancelled = b.status === 'CANCELLED';

  const doCancel = async () => {
    setBusy(true);
    setError('');
    try {
      onUpdated(await api.cancel(b.reference, b.customerEmail));
      setConfirming(false);
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <article className={'card booking' + (cancelled ? ' is-cancelled' : '')}>
      <div className="booking-top">
        <div>
          <h2>{b.eventTitle}</h2>
          <p className="muted">{b.venue}, {b.city} · {dateTime(b.startTime)}</p>
        </div>
        <span className={'badge ' + (cancelled ? 'grey' : 'ok')}>{b.status}</span>
      </div>
      <div className="booking-facts">
        <div><span className="muted small">Reference</span><strong>{b.reference}</strong></div>
        <div><span className="muted small">Seats</span><strong>{b.seats.join(', ')}</strong></div>
        <div><span className="muted small">Paid</span><strong>{money(b.total)}</strong></div>
        {cancelled && <div><span className="muted small">Refunded</span><strong>{money(b.refundAmount)}</strong></div>}
      </div>

      {!cancelled && (
        <div className="cancel-area">
          {b.refundPercentIfCancelledNow === 0 ? (
            <p className="muted small">Cancellation closed: less than 24 hours to the show.</p>
          ) : confirming ? (
            <div className="confirm-cancel">
              <span>Cancel and get <strong>{money(refundNow)}</strong> back ({b.refundPercentIfCancelledNow}% of ticket price; fee not refunded)?</span>
              <button className="btn danger" onClick={doCancel} disabled={busy}>{busy ? 'Cancelling…' : 'Yes, cancel'}</button>
              <button className="btn" onClick={() => setConfirming(false)} disabled={busy}>Keep booking</button>
            </div>
          ) : (
            <button className="btn danger-outline" onClick={() => setConfirming(true)}>
              Cancel booking · {b.refundPercentIfCancelledNow}% refund
            </button>
          )}
          {error && <div className="alert error">{error}</div>}
        </div>
      )}
    </article>
  );
}
