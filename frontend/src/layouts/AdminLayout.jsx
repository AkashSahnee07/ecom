import { Link, NavLink, Outlet } from 'react-router-dom';
import './AdminLayout.css';

export default function AdminLayout() {
  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <Link to="/" className="admin-brand">
          LUMEN
        </Link>
        <NavLink end to="/admin" className="admin-side-link">
          Overview
        </NavLink>
        <NavLink to="/admin/products" className="admin-side-link">
          Products
        </NavLink>
        <NavLink to="/admin/orders" className="admin-side-link">
          Orders
        </NavLink>
        <Link to="/shop" className="admin-side-link">
          Back to Storefront
        </Link>
      </aside>
      <section className="admin-main">
        <Outlet />
      </section>
    </div>
  );
}
