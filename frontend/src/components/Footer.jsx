import { Link } from 'react-router-dom';
import { Zap } from 'lucide-react';
import './Footer.css';

export default function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="site-footer">
      <div className="container footer-container">
        <div className="footer-brand">
          <Link to="/" className="footer-logo">
            <div className="footer-logo-icon"><Zap size={16} /></div>
            <span>ShopSphere</span>
          </Link>
          <p className="footer-tagline text-secondary text-sm">
            Your premium ecommerce destination with AI-powered recommendations.
          </p>
        </div>

        <div className="footer-links">
          <div className="footer-col">
            <h4 className="footer-col-title">Shop</h4>
            <Link to="/products">All Products</Link>
            <Link to="/products?featured=true">Featured</Link>
            <Link to="/products?sort=averageRating,desc">Top Rated</Link>
          </div>
          <div className="footer-col">
            <h4 className="footer-col-title">Account</h4>
            <Link to="/login">Login</Link>
            <Link to="/register">Sign Up</Link>
            <Link to="/orders">My Orders</Link>
            <Link to="/profile">Profile</Link>
          </div>
          <div className="footer-col">
            <h4 className="footer-col-title">Support</h4>
            <Link to="/cart">Cart</Link>
            <a href="mailto:support@shopsphere.com">Contact Us</a>
          </div>
        </div>
      </div>

      <div className="footer-bottom">
        <div className="container">
          <p className="text-muted text-xs">© {year} ShopSphere. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}
