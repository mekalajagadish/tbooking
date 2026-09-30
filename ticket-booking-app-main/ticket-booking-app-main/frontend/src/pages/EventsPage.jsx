import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';
import { CATEGORY_ICON, dateTime, money } from '../format.js';

const FILTERS = ['ALL', 'MOVIE', 'CONCERT', 'SPORTS', 'COMEDY'];

export default function EventsPage() {
  const [events, setEvents] = useState(null);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    api.events().then(setEvents).catch((e) => setError(e.message));
  }, []);

  if (error) return <div className="alert error">{error}</div>;
  if (!events) return <p className="muted">Loading events…</p>;

  const shown = events.filter((e) => filter === 'ALL' || e.category === filter);

  return (
    <>
      <h1>Upcoming events</h1>
      <div className="chips">
        {FILTERS.map((f) => (
          <button key={f} className={'chip' + (f === filter ? ' active' : '')} onClick={() => setFilter(f)}>
            {f === 'ALL' ? 'All' : `${CATEGORY_ICON[f]} ${f.charAt(0) + f.slice(1).toLowerCase()}`}
          </button>
        ))}
      </div>
      <div className="grid">
        {shown.map((e) => {
          const pctLeft = e.seatsLeft / e.totalSeats;
          return (
            <article key={e.id} className="card event-card">
              <div className="event-icon">{CATEGORY_ICON[e.category]}</div>
              <h2>{e.title}</h2>
              <p className="muted">{e.venue}, {e.city}</p>
              <p className="when">{dateTime(e.startTime)}</p>
              <p className="desc">{e.description}</p>
              <div className="event-meta">
                <span>From <strong>{money(e.basePrice)}</strong></span>
                <span className={pctLeft < 0.25 ? 'badge warn' : 'badge'}>
                  {e.seatsLeft === 0 ? 'Sold out' : `${e.seatsLeft} seats left`}
                </span>
              </div>
              {e.bookingOpen && e.seatsLeft > 0 ? (
                <Link to={`/events/${e.id}`} className="btn primary block">Book tickets</Link>
              ) : (
                <button className="btn block" disabled>{e.seatsLeft === 0 ? 'Sold out' : 'Booking closed'}</button>
              )}
            </article>
          );
        })}
        {shown.length === 0 && <p className="muted">No events in this category.</p>}
      </div>
    </>
  );
}
