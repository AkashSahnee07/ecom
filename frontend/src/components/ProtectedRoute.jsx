import { Navigate, useLocation } from 'react-router-dom';
import useAuthStore from '../store/auth.store';

export default function ProtectedRoute({ children, adminOnly = false }) {
  const { user, token } = useAuthStore();
  const location = useLocation();

  if (!token) {
    const redirect = encodeURIComponent(location.pathname + location.search);
    return <Navigate to={`/login?redirect=${redirect}`} replace />;
  }
  if (adminOnly && user?.role !== 'ADMIN') return <Navigate to="/" replace />;

  return children;
}
