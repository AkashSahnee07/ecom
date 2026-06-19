import { Link } from 'react-router-dom';
import { Home, Search } from 'lucide-react';

export default function NotFoundPage() {
  return (
    <div className="page-wrapper">
      <div className="container">
        <div className="empty-state" style={{ minHeight: '60vh' }}>
          <div className="empty-state-icon">
            <Search size={36} />
          </div>
          <h1 className="empty-state-title" style={{ fontSize: '48px', fontWeight: 800 }}>404</h1>
          <h3 className="empty-state-title">Page not found</h3>
          <p className="empty-state-desc">
            The page you're looking for doesn't exist or has been moved.
          </p>
          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', justifyContent: 'center' }}>
            <Link to="/" className="btn btn-primary">
              <Home size={16} /> Go Home
            </Link>
            <Link to="/products" className="btn btn-secondary">
              Browse Products
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
