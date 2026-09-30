// Renders the venue as a grid. Rows are lettered from the front (A) and the first
// `premiumRows` rows are premium. Booked seats can't be clicked.
export default function SeatMap({ event, selected, onToggle }) {
  const booked = new Set(event.bookedSeats);
  const rows = Array.from({ length: event.seatRows }, (_, i) => String.fromCharCode(65 + i));
  const numbers = Array.from({ length: event.seatsPerRow }, (_, i) => i + 1);

  return (
    <div className="seatmap">
      <div className="screen">{event.category === 'MOVIE' ? 'SCREEN' : 'STAGE'}</div>
      <div className="seat-rows">
        {rows.map((row, idx) => (
          <div key={row} className="seat-row">
            <span className="row-label">{row}</span>
            {numbers.map((n) => {
              const label = `${row}${n}`;
              const isBooked = booked.has(label);
              const isSelected = selected.includes(label);
              const cls = ['seat', idx < event.premiumRows ? 'premium' : '', isBooked ? 'booked' : '', isSelected ? 'selected' : '']
                .join(' ');
              return (
                <button
                  key={label}
                  className={cls}
                  disabled={isBooked}
                  title={isBooked ? `${label} (taken)` : label}
                  aria-label={`Seat ${label}${isBooked ? ' taken' : ''}`}
                  aria-pressed={isSelected}
                  onClick={() => onToggle(label)}
                >
                  {n}
                </button>
              );
            })}
            <span className="row-label">{row}</span>
          </div>
        ))}
      </div>
      <div className="legend">
        <span><i className="seat sample" /> Standard</span>
        <span><i className="seat sample premium" /> Premium</span>
        <span><i className="seat sample selected" /> Selected</span>
        <span><i className="seat sample booked" /> Taken</span>
      </div>
    </div>
  );
}
