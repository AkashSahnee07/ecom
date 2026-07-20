import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, User, Eye, EyeOff, Phone, Zap, ArrowRight } from 'lucide-react';
import { userAPI } from '../api/auth.api';
import toast from 'react-hot-toast';
import './AuthPages.css';

export default function RegisterPage() {
  const [form, setForm] = useState({
    firstName: '', lastName: '', username: '',
    email: '', password: '', confirmPassword: '', phone: ''
  });
  const [showPass, setShowPass] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.password !== form.confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }
    if (form.password.length < 6) {
      toast.error('Password must be at least 6 characters');
      return;
    }
    setLoading(true);
    try {
      await userAPI.register({
        firstName: form.firstName,
        lastName: form.lastName,
        username: form.username,
        email: form.email,
        password: form.password,
        phone: form.phone,
      });
      toast.success('Account created! Please sign in.');
      navigate('/login');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="ambient-orb orb-1" />
      <div className="ambient-orb orb-2" />

      <div className="auth-card auth-card-wide animate-fade-in">
        <div className="auth-logo">
          <div className="navbar-logo-icon"><Zap size={20} /></div>
          <span className="auth-logo-text">ShopSphere</span>
        </div>

        <h1 className="auth-title">Create your account</h1>
        <p className="auth-subtitle">Join thousands of shoppers on ShopSphere</p>

        <form id="register-form" className="auth-form" onSubmit={handleSubmit}>
          <div className="form-grid-2">
            <div className="input-wrapper">
              <label className="input-label" htmlFor="reg-first-name">First Name</label>
              <div className="input-icon-wrapper">
                <User size={16} className="input-icon" />
                <input id="reg-first-name" name="firstName" className="input"
                  placeholder="John" value={form.firstName} onChange={handleChange} required />
              </div>
            </div>
            <div className="input-wrapper">
              <label className="input-label" htmlFor="reg-last-name">Last Name</label>
              <div className="input-icon-wrapper">
                <User size={16} className="input-icon" />
                <input id="reg-last-name" name="lastName" className="input"
                  placeholder="Doe" value={form.lastName} onChange={handleChange} required />
              </div>
            </div>
          </div>

          <div className="input-wrapper">
            <label className="input-label" htmlFor="reg-username">Username</label>
            <div className="input-icon-wrapper">
              <User size={16} className="input-icon" />
              <input id="reg-username" name="username" className="input"
                placeholder="johndoe123" value={form.username} onChange={handleChange} required />
            </div>
          </div>

          <div className="input-wrapper">
            <label className="input-label" htmlFor="reg-email">Email address</label>
            <div className="input-icon-wrapper">
              <Mail size={16} className="input-icon" />
              <input id="reg-email" name="email" type="email" className="input"
                placeholder="you@example.com" value={form.email} onChange={handleChange} required />
            </div>
          </div>

          <div className="input-wrapper">
            <label className="input-label" htmlFor="reg-phone">Phone (optional)</label>
            <div className="input-icon-wrapper">
              <Phone size={16} className="input-icon" />
              <input id="reg-phone" name="phone" type="tel" className="input"
                placeholder="+91 9876543210" value={form.phone} onChange={handleChange} />
            </div>
          </div>

          <div className="form-grid-2">
            <div className="input-wrapper">
              <label className="input-label" htmlFor="reg-password">Password</label>
              <div className="input-icon-wrapper">
                <Lock size={16} className="input-icon" />
                <input id="reg-password" name="password" type={showPass ? 'text' : 'password'}
                  className="input" placeholder="Min 6 chars" value={form.password}
                  onChange={handleChange} required style={{ paddingRight: '44px' }} />
                <button type="button" className="password-toggle" onClick={() => setShowPass(!showPass)} tabIndex={-1}>
                  {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>
            <div className="input-wrapper">
              <label className="input-label" htmlFor="reg-confirm-password">Confirm Password</label>
              <div className="input-icon-wrapper">
                <Lock size={16} className="input-icon" />
                <input id="reg-confirm-password" name="confirmPassword" type="password" className="input"
                  placeholder="••••••••" value={form.confirmPassword} onChange={handleChange} required />
              </div>
            </div>
          </div>

          <button id="register-submit-btn" type="submit"
            className="btn btn-primary btn-full btn-lg" disabled={loading}>
            {loading
              ? <span className="spinner" style={{ width: '18px', height: '18px', borderWidth: '2px' }} />
              : <> Create Account <ArrowRight size={16} /> </>}
          </button>
        </form>

        <p className="auth-footer-text">
          Already have an account?{' '}
          <Link to="/login" className="auth-link" id="register-login-link">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
