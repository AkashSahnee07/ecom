import { Star } from 'lucide-react';

export default function StarRating({ rating = 0, maxRating = 5, size = 14, showValue = false, count }) {
  const rounded = Math.round(rating * 2) / 2;
  return (
    <div className="stars-wrapper" style={{ gap: '2px' }}>
      {Array.from({ length: maxRating }, (_, i) => {
        const filled = i + 1 <= Math.floor(rounded);
        const half = !filled && i < rounded;
        return (
          <Star
            key={i}
            size={size}
            fill={filled ? '#f59e0b' : 'none'}
            color={filled || half ? '#f59e0b' : '#334155'}
            style={half ? { clipPath: 'inset(0 50% 0 0)', overflow: 'visible' } : {}}
          />
        );
      })}
      {showValue && (
        <span style={{ fontSize: '12px', color: 'var(--text-secondary)', marginLeft: '4px' }}>
          {rating.toFixed(1)}
        </span>
      )}
      {count !== undefined && (
        <span style={{ fontSize: '12px', color: 'var(--text-muted)', marginLeft: '2px' }}>
          ({count})
        </span>
      )}
    </div>
  );
}
