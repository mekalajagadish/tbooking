const inr = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 });

export const money = (n) => inr.format(Number(n || 0));

export const dateTime = (iso) =>
  new Date(iso).toLocaleString('en-IN', {
    weekday: 'short', day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit',
  });

export const CATEGORY_ICON = { MOVIE: '🎬', CONCERT: '🎤', SPORTS: '🏏', COMEDY: '😂' };
