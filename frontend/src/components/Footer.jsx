import { Link } from 'react-router-dom';
import './Footer.css';

export default function Footer() {
  return (
    <footer className="lumen-footer">
      <div className="lumen-footer-inner">
        <div>
          <p className="lumen-footer-brand">LUMEN</p>
          <p className="lumen-footer-copy">Editorial fashion & lifestyle essentials.</p>
        </div>
        <div className="lumen-footer-links">
          <Link to="/shop">Shop</Link>
          <Link to="/account">Account</Link>
          <Link to="/orders">Orders</Link>
        </div>
      </div>
    </footer>
  );
}
