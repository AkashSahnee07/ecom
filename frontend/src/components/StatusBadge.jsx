export default function StatusBadge({ status, colorFn }) {
  const className = colorFn ? colorFn(status) : 'badge-slate';
  return (
    <span className={`badge ${className}`}>
      {status?.replace(/_/g, ' ')}
    </span>
  );
}
