export const formatCurrency = (amount, currency = 'INR') => {
  if (amount === null || amount === undefined) return '—';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency,
    maximumFractionDigits: 2,
  }).format(amount);
};

export const formatDate = (dateStr) => {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('en-IN', {
    year: 'numeric', month: 'short', day: 'numeric',
  });
};

export const formatDateTime = (dateStr) => {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleString('en-IN', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
};

export const formatRelative = (dateStr) => {
  if (!dateStr) return '';
  const now = new Date();
  const diff = now - new Date(dateStr);
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'Just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  if (days < 30) return `${days}d ago`;
  return formatDate(dateStr);
};

export const truncate = (str, length = 80) => {
  if (!str) return '';
  return str.length > length ? str.slice(0, length) + '…' : str;
};

export const debounce = (fn, delay = 400) => {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), delay);
  };
};

export const getOrderStatusColor = (status) => {
  const map = {
    PENDING:    'badge-amber',
    PROCESSING: 'badge-indigo',
    CONFIRMED:  'badge-indigo',
    SHIPPED:    'badge-cyan',
    DELIVERED:  'badge-emerald',
    CANCELLED:  'badge-rose',
    REFUNDED:   'badge-slate',
    FAILED:     'badge-rose',
  };
  return map[status] || 'badge-slate';
};

export const getPaymentStatusColor = (status) => {
  const map = {
    PENDING:    'badge-amber',
    PROCESSING: 'badge-indigo',
    COMPLETED:  'badge-emerald',
    FAILED:     'badge-rose',
    REFUNDED:   'badge-cyan',
    CANCELLED:  'badge-rose',
    EXPIRED:    'badge-slate',
  };
  return map[status] || 'badge-slate';
};

export const getShipmentStatusColor = (status) => {
  const map = {
    CREATED:     'badge-slate',
    PICKED_UP:   'badge-indigo',
    IN_TRANSIT:  'badge-cyan',
    OUT_FOR_DELIVERY: 'badge-amber',
    DELIVERED:   'badge-emerald',
    FAILED:      'badge-rose',
    RETURNED:    'badge-amber',
    CANCELLED:   'badge-rose',
  };
  return map[status] || 'badge-slate';
};

export const generatePageNumbers = (current, total, delta = 2) => {
  const range = [];
  const rangeWithDots = [];
  for (let i = Math.max(2, current - delta); i <= Math.min(total - 1, current + delta); i++) {
    range.push(i);
  }
  if (current - delta > 2) range.unshift('...');
  if (current + delta < total - 1) range.push('...');
  range.unshift(1);
  if (total > 1) range.push(total);
  return [...new Set(range)];
};
