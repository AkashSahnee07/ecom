import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { Menu, Search, ShoppingBag, User, X } from 'lucide-react';
import useAuthStore from '../store/auth.store';
import useCartStore from '../store/cart.store';
import './Header.css';

export default function Header() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [query, setQuery] = useState('');
  const { user, logout, isAdmin } = useAuthStore();
  const { getItemCount } = useCartStore();
  const navigate = useNavigate();
  const bagCount = getItemCount();

  const onSearch = (e) => {
    e.preventDefault();
    const keyword = query.trim();
    if (!keyword) return;
    navigate(`/shop?search=${encodeURIComponent(keyword)}`);
    setQuery('');
    setMenuOpen(false);
  };

  return (
    <header className="lumen-header">
      <div className="lumen-header-inner">
        <button
          className="lumen-icon-btn mobile-only"
          onClick={() => setMenuOpen((prev) => !prev)}
          aria-label="Toggle menu"
        >
          {menuOpen ? <X size={18} /> : <Menu size={18} />}
        </button>

        <Link to="/" className="lumen-logo">
          LUMEN
        </Link>

        <nav className="lumen-nav desktop-only">
          <NavLink to="/" end className="lumen-nav-link">
            Home
          </NavLink>
          <NavLink to="/shop" className="lumen-nav-link">
            Shop
          </NavLink>
          <NavLink to="/collections/new" className="lumen-nav-link">
            New
          </NavLink>
        </nav>

        <div className="lumen-actions">
          <form onSubmit={onSearch} className="lumen-search desktop-only">
            <Search size={14} />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search"
              aria-label="Search products"
            />
          </form>
          <Link to="/bag" className="lumen-icon-btn" aria-label="Bag">
            <ShoppingBag size={16} />
            {bagCount > 0 ? <span className="lumen-badge">{bagCount}</span> : null}
          </Link>
          {user ? (
            <>
              <Link to="/account" className="lumen-icon-btn" aria-label="Account">
                <User size={16} />
              </Link>
              {isAdmin() ? (
                <Link to="/admin" className="lumen-admin-link desktop-only">
                  Admin
                </Link>
              ) : null}
              <button
                className="lumen-admin-link desktop-only"
                onClick={() => {
                  logout();
                  navigate('/');
                }}
              >
                Logout
              </button>
            </>
          ) : (
            <Link to="/login" className="lumen-admin-link">
              Login
            </Link>
          )}
        </div>
      </div>

      {menuOpen ? (
        <div className="lumen-mobile-menu">
          <form onSubmit={onSearch} className="lumen-search">
            <Search size={14} />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search products"
              aria-label="Search products"
            />
          </form>
          <NavLink onClick={() => setMenuOpen(false)} to="/" end className="lumen-mobile-link">
            Home
          </NavLink>
          <NavLink onClick={() => setMenuOpen(false)} to="/shop" className="lumen-mobile-link">
            Shop
          </NavLink>
          <NavLink
            onClick={() => setMenuOpen(false)}
            to="/collections/new"
            className="lumen-mobile-link"
          >
            New Collection
          </NavLink>
          {user ? (
            <>
              <NavLink onClick={() => setMenuOpen(false)} to="/account" className="lumen-mobile-link">
                Account
              </NavLink>
              {isAdmin() ? (
                <NavLink onClick={() => setMenuOpen(false)} to="/admin" className="lumen-mobile-link">
                  Admin
                </NavLink>
              ) : null}
            </>
          ) : null}
        </div>
      ) : null}
    </header>
  );
}
