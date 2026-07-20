import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, ShoppingBag, Zap, Star, TrendingUp, Package } from 'lucide-react';
import { productsAPI } from '../api/products.api';
import { recommendationsAPI } from '../api/recommendations.api';
import ProductCard from '../components/ProductCard';
import { SkeletonCard } from '../components/Loader';
import useAuthStore from '../store/auth.store';
import './HomePage.css';

export default function HomePage() {
  const { user } = useAuthStore();
  const [featured, setFeatured] = useState([]);
  const [topRated, setTopRated] = useState([]);
  const [recent, setRecent] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [featRes, topRes, recRes] = await Promise.allSettled([
          productsAPI.getFeatured({ size: 8 }),
          productsAPI.getTopRated({ size: 4 }),
          productsAPI.getRecent({ size: 4 }),
        ]);

        if (featRes.status === 'fulfilled') {
          setFeatured(featRes.value.data?.content || featRes.value.data || []);
        }
        if (topRes.status === 'fulfilled') {
          setTopRated(topRes.value.data?.content || topRes.value.data || []);
        }
        if (recRes.status === 'fulfilled') {
          setRecent(recRes.value.data?.content || recRes.value.data || []);
        }

        // Load personalized recommendations if logged in
        if (user?.id) {
          try {
            const recsRes = await recommendationsAPI.getForUser(user.id, { limit: 8 });
            setRecommendations(recsRes.data || []);
          } catch {/* no recs available */}
        }
      } catch (err) {
        console.error('Home page fetch error:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [user?.id]);

  const CATEGORIES = [
    { id: 1, name: 'Electronics', icon: '⚡', color: '#6366f1' },
    { id: 2, name: 'Fashion',     icon: '👗', color: '#ec4899' },
    { id: 3, name: 'Home',        icon: '🏠', color: '#f59e0b' },
    { id: 4, name: 'Sports',      icon: '⚽', color: '#10b981' },
    { id: 5, name: 'Books',       icon: '📚', color: '#22d3ee' },
    { id: 6, name: 'Beauty',      icon: '💄', color: '#f43f5e' },
  ];

  const STATS = [
    { icon: <ShoppingBag size={22} />, value: '50K+', label: 'Products',     color: '#6366f1' },
    { icon: <Star size={22} />,        value: '4.8★', label: 'Avg Rating',   color: '#f59e0b' },
    { icon: <Package size={22} />,     value: '2M+',  label: 'Orders',       color: '#10b981' },
    { icon: <TrendingUp size={22} />,  value: '99%',  label: 'Satisfaction', color: '#22d3ee' },
  ];

  return (
    <div className="page-wrapper">
      {/* ── Hero ─────────────────────────────────────────────── */}
      <section className="hero">
        <div className="ambient-orb orb-1" />
        <div className="ambient-orb orb-2" />
        <div className="hero-gradient-ball hero-ball-1" />
        <div className="hero-gradient-ball hero-ball-2" />

        <div className="container">
          <div className="hero-content animate-fade-in">
            <div className="hero-badge">
              <Zap size={12} />
              <span>AI-Powered Recommendations</span>
            </div>

            <h1 className="hero-title">
              Shop Smarter,<br />
              <span className="hero-title-accent">Live Better</span>
            </h1>

            <p className="hero-subtitle">
              Discover millions of products curated just for you. From electronics to fashion,
              enjoy a seamless shopping experience powered by intelligent recommendations.
            </p>

            <div className="hero-cta">
              <Link to="/products" id="hero-shop-now-btn" className="btn btn-primary btn-lg hero-btn-primary">
                Start Shopping <ArrowRight size={18} />
              </Link>
              {!user && (
                <Link to="/register" id="hero-signup-btn" className="btn btn-secondary btn-lg">
                  Create Account
                </Link>
              )}
            </div>

            {/* Stats */}
            <div className="hero-stats">
              {STATS.map((s, i) => (
                <div key={i} className="hero-stat">
                  <p className="hero-stat-value" style={{ color: s.color }}>{s.value}</p>
                  <p className="hero-stat-label">{s.label}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* ── Categories ───────────────────────────────────────── */}
      <section className="section">
        <div className="container">
          <div className="section-header">
            <div>
              <h2 className="section-title">Shop by Category</h2>
              <p className="section-subtitle">Explore our wide range of categories</p>
            </div>
            <Link to="/products" className="btn btn-ghost btn-sm" id="home-view-all-cats">
              View All <ArrowRight size={14} />
            </Link>
          </div>

          <div className="categories-grid">
            {CATEGORIES.map((cat) => (
              <Link
                key={cat.id}
                to={`/products?categoryId=${cat.id}`}
                id={`category-${cat.id}`}
                className="category-card card"
                style={{ '--cat-color': cat.color }}
              >
                <div className="category-icon">{cat.icon}</div>
                <p className="category-name">{cat.name}</p>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* ── Featured Products ─────────────────────────────────── */}
      <section className="section" style={{ background: 'rgba(99,102,241,0.02)', borderTop: '1px solid var(--border)', borderBottom: '1px solid var(--border)' }}>
        <div className="container">
          <div className="section-header">
            <div>
              <h2 className="section-title">Featured Products</h2>
              <p className="section-subtitle">Handpicked products just for you</p>
            </div>
            <Link to="/products?featured=true" id="home-view-all-featured" className="btn btn-ghost btn-sm">
              View All <ArrowRight size={14} />
            </Link>
          </div>

          {loading ? (
            <div className="grid-4">
              {Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)}
            </div>
          ) : featured.length > 0 ? (
            <div className="grid-4">
              {featured.map(p => <ProductCard key={p.id} product={p} />)}
            </div>
          ) : (
            <div className="empty-state">
              <div className="empty-state-icon"><ShoppingBag size={28} /></div>
              <p className="empty-state-title">No featured products yet</p>
              <p className="empty-state-desc">Check back soon for featured items</p>
            </div>
          )}
        </div>
      </section>

      {/* ── Personalized Recommendations ─────────────────────── */}
      {user && (
        <section className="section">
          <div className="container">
            <div className="section-header">
              <div>
                <h2 className="section-title">Recommended for You</h2>
                <p className="section-subtitle">Personalized picks based on your preferences</p>
              </div>
            </div>

            {recommendations.length > 0 ? (
              <div className="grid-4">
                {recommendations.slice(0, 8).map((rec, i) => (
                  <ProductCard
                    key={rec.productId || i}
                    product={{ id: rec.productId, name: rec.productName, price: rec.price, ...rec }}
                  />
                ))}
              </div>
            ) : (
              <div className="recs-empty">
                <Zap size={28} style={{ color: 'var(--accent-indigo)' }} />
                <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
                  Keep browsing to get personalized recommendations!
                </p>
              </div>
            )}
          </div>
        </section>
      )}

      {/* ── Top Rated ────────────────────────────────────────── */}
      {topRated.length > 0 && (
        <section className="section" style={{ borderTop: '1px solid var(--border)' }}>
          <div className="container">
            <div className="section-header">
              <div>
                <h2 className="section-title">Top Rated</h2>
                <p className="section-subtitle">Products loved by our customers</p>
              </div>
              <Link to="/products?sort=top-rated" id="home-top-rated-link" className="btn btn-ghost btn-sm">
                View All <ArrowRight size={14} />
              </Link>
            </div>
            <div className="grid-4">
              {topRated.map(p => <ProductCard key={p.id} product={p} />)}
            </div>
          </div>
        </section>
      )}

      {/* ── Banner CTA ────────────────────────────────────────── */}
      <section className="section">
        <div className="container">
          <div className="cta-banner">
            <div className="cta-content">
              <h2 className="cta-title">Ready to start shopping?</h2>
              <p className="cta-subtitle">
                Join millions of happy customers and discover the best deals every day.
              </p>
              <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
                <Link to="/products" id="home-cta-shop-btn" className="btn btn-primary btn-lg">
                  Browse Products <ArrowRight size={16} />
                </Link>
                {!user && (
                  <Link to="/register" id="home-cta-signup-btn" className="btn btn-secondary btn-lg">
                    Sign Up Free
                  </Link>
                )}
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
