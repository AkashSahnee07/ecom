export function Loader({ size = 'md', center = false }) {
  const cls = size === 'lg' ? 'spinner spinner-lg' : 'spinner';
  if (center) {
    return (
      <div className="loader-center">
        <div className={cls} />
      </div>
    );
  }
  return <div className={cls} />;
}

export function PageLoader() {
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '60vh',
      gap: '16px',
    }}>
      <div className="spinner spinner-lg" />
      <p style={{ color: 'var(--text-muted)', fontSize: '14px' }}>Loading…</p>
    </div>
  );
}

export function SkeletonCard() {
  return (
    <div className="card" style={{ overflow: 'hidden' }}>
      <div className="skeleton" style={{ aspectRatio: '1', borderRadius: '0' }} />
      <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
        <div className="skeleton" style={{ height: '12px', width: '40%' }} />
        <div className="skeleton" style={{ height: '16px', width: '80%' }} />
        <div className="skeleton" style={{ height: '12px', width: '60%' }} />
        <div className="skeleton" style={{ height: '18px', width: '50%', marginTop: '4px' }} />
      </div>
    </div>
  );
}

export function SkeletonList({ count = 5 }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="card" style={{ padding: '16px', display: 'flex', gap: '16px', alignItems: 'center' }}>
          <div className="skeleton" style={{ width: '48px', height: '48px', flexShrink: 0, borderRadius: '8px' }} />
          <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <div className="skeleton" style={{ height: '14px', width: '60%' }} />
            <div className="skeleton" style={{ height: '12px', width: '40%' }} />
          </div>
          <div className="skeleton" style={{ height: '14px', width: '80px', flexShrink: 0 }} />
        </div>
      ))}
    </div>
  );
}
