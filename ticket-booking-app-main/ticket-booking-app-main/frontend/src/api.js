// Thin wrapper around fetch. Errors from the backend come back as {"message": "..."}.
async function request(path, options = {}) {
  const res = await fetch('/api' + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  const body = await res.json().catch(() => null);
  if (!res.ok) {
    const err = new Error((body && body.message) || `Request failed (${res.status})`);
    err.status = res.status;
    throw err;
  }
  return body;
}

export const api = {
  events: () => request('/events'),
  event: (id) => request(`/events/${id}`),
  quote: (eventId, seats) => request('/bookings/quote', { method: 'POST', body: JSON.stringify({ eventId, seats }) }),
  book: (payload) => request('/bookings', { method: 'POST', body: JSON.stringify(payload) }),
  bookingsByEmail: (email) => request(`/bookings?email=${encodeURIComponent(email)}`),
  cancel: (reference, email) =>
    request(`/bookings/${reference}/cancel`, { method: 'POST', body: JSON.stringify({ email }) }),
};
