import { useState, useEffect } from 'react';
import { Link, NavLink, useNavigate, useLocation } from 'react-router-dom';
import {
  ShoppingCart, User, Search, Menu, X, Package, LogOut,
  LayoutDashboard, Bell, ChevronDown, Zap
} from 'lucide-react';
import useAuthStore from '../store/auth.store';
import useCartStore from '../store/cart.store';
import './Navbar.css';

export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const { user, logout, isAdmin } = useAuthStore();
  const { getItemCount } = useCartStore();
  const navigate = useNavigate();
  const location = useLocation();
  const cartCount = getItemCount();

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  useEffect(() => {
    setMenuOpen(false);
    setDropdownOpen(false);
    setSearchOpen(false);
  }, [location]);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/products?search=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
      setSearchOpen(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <>
      <nav className={`navbar ${scrolled ? 'navbar-scrolled' : ''}`}>
        <div className="navbar-container">
          {/* Logo */}
          <Link to="/" className="navbar-logo">
            <div className="navbar-logo-icon">
              <Zap size={18} />
            </div>
            <span className="navbar-logo-text">ShopSphere</span>
          </Link>

          {/* Center Nav */}
          <div className="navbar-links">
            <NavLink to="/" end className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
              Home
            </NavLink>
            <NavLink to="/products" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
              Products
            </NavLink>
            {user && (
              <>
                <NavLink to="/orders" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
                  Orders
                </NavLink>
                {isAdmin() && (
                  <NavLink to="/admin" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
                    Dashboard
                  </NavLink>
                )}
              </>
            )}
          </div>

          {/* Right Actions */}
          <div className="navbar-actions">
            {/* Search */}
            <button
              id="navbar-search-btn"
              className="navbar-icon-btn"
              onClick={() => setSearchOpen(!searchOpen)}
              aria-label="Search"
            >
              <Search size={18} />
            </button>

            {/* Cart */}
            <Link to="/cart" className="navbar-icon-btn cart-btn" id="navbar-cart-btn" aria-label="Cart">
              <ShoppingCart size={18} />
              {cartCount > 0 && (
                <span className="cart-badge">{cartCount > 99 ? '99+' : cartCount}</span>
              )}
            </Link>

            {/* Auth */}
            {user ? (
              <div className="user-dropdown">
                <button
                  id="navbar-user-btn"
                  className="user-avatar-btn"
                  onClick={() => setDropdownOpen(!dropdownOpen)}
                >
                  <div className="user-avatar">
                    {user.firstName?.charAt(0) || user.username?.charAt(0) || 'U'}
                  </div>
                  <span className="user-name">{user.firstName || user.username}</span>
                  <ChevronDown size={14} className={`dropdown-chevron ${dropdownOpen ? 'open' : ''}`} />
                </button>

                {dropdownOpen && (
                  <div className="dropdown-menu animate-scale-in">
                    <div className="dropdown-header">
                      <p className="dropdown-user-name">{user.firstName} {user.lastName}</p>
                      <p className="dropdown-user-email text-muted text-xs">{user.email}</p>
                    </div>
                    <div className="dropdown-divider" />
                    <Link to="/profile" id="nav-profile-link" className="dropdown-item">
                      <User size={15} /> Profile
                    </Link>
                    <Link to="/orders" id="nav-orders-link" className="dropdown-item">
                      <Package size={15} /> My Orders
                    </Link>
                    {isAdmin() && (
                      <Link to="/admin" id="nav-admin-link" className="dropdown-item">
                        <LayoutDashboard size={15} /> Admin Panel
                      </Link>
                    )}
                    <div className="dropdown-divider" />
                    <button id="nav-logout-btn" className="dropdown-item danger" onClick={handleLogout}>
                      <LogOut size={15} /> Logout
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="auth-btns">
                <Link to="/login" id="navbar-login-btn" className="btn btn-ghost btn-sm">Login</Link>
                <Link to="/register" id="navbar-register-btn" className="btn btn-primary btn-sm">
                  Sign Up
                </Link>
              </div>
            )}

            {/* Mobile Menu Toggle */}
            <button
              id="navbar-menu-btn"
              className="navbar-icon-btn mobile-menu-btn"
              onClick={() => setMenuOpen(!menuOpen)}
              aria-label="Menu"
            >
              {menuOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>

        {/* Search Bar */}
        {searchOpen && (
          <div className="search-bar animate-slide-down">
            <div className="navbar-container">
              <form onSubmit={handleSearch} className="search-form">
                <div className="input-icon-wrapper" style={{ flex: 1 }}>
                  <Search size={16} className="input-icon" />
                  <input
                    id="navbar-search-input"
                    className="input"
                    placeholder="Search products, brands…"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    autoFocus
                  />
                </div>
                <button type="submit" className="btn btn-primary btn-sm">Search</button>
              </form>
            </div>
          </div>
        )}
      </nav>

      {/* Mobile Nav */}
      {menuOpen && (
        <div className="mobile-nav animate-slide-down">
          <NavLink to="/" end className="mobile-nav-link" onClick={() => setMenuOpen(false)}>Home</NavLink>
          <NavLink to="/products" className="mobile-nav-link" onClick={() => setMenuOpen(false)}>Products</NavLink>
          {user && (
            <>
              <NavLink to="/orders" className="mobile-nav-link" onClick={() => setMenuOpen(false)}>Orders</NavLink>
              <NavLink to="/profile" className="mobile-nav-link" onClick={() => setMenuOpen(false)}>Profile</NavLink>
              {isAdmin() && (
                <NavLink to="/admin" className="mobile-nav-link" onClick={() => setMenuOpen(false)}>Dashboard</NavLink>
              )}
              <button className="mobile-nav-link danger" onClick={handleLogout}>Logout</button>
            </>
          )}
          {!user && (
            <>
              <NavLink to="/login" className="mobile-nav-link" onClick={() => setMenuOpen(false)}>Login</NavLink>
              <NavLink to="/register" className="mobile-nav-link" onClick={() => setMenuOpen(false)}>Sign Up</NavLink>
            </>
          )}
        </div>
      )}

      {/* Overlay */}
      {(dropdownOpen || menuOpen) && (
        <div
          className="nav-overlay"
          onClick={() => { setDropdownOpen(false); setMenuOpen(false); }}
        />
      )}
    </>
  );
}
