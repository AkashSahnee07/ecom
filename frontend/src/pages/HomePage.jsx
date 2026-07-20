import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { productsAPI } from '../api/products.api';
import ProductCard from '../components/ProductCard';
import { SkeletonCard } from '../components/Loader';
import { getFeaturedSampleProducts, getRecentSampleProducts } from '../data/sampleProducts';
import './HomePage.css';

export default function HomePage() {
  const [featured, setFeatured] = useState([]);
  const [recent, setRecent] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [featRes, recRes] = await Promise.allSettled([
          productsAPI.getFeatured({ size: 8 }),
          productsAPI.getRecent({ size: 4 }),
        ]);

        if (featRes.status === 'fulfilled') {
          const featuredData = featRes.value.data?.content || featRes.value.data || [];
          setFeatured(featuredData.length ? featuredData : getFeaturedSampleProducts());
        } else {
          setFeatured(getFeaturedSampleProducts());
        }
        if (recRes.status === 'fulfilled') {
          const recentData = recRes.value.data?.content || recRes.value.data || [];
          setRecent(recentData.length ? recentData : getRecentSampleProducts().slice(0, 4));
        } else {
          setRecent(getRecentSampleProducts().slice(0, 4));
        }
      } catch (err) {
        console.error('Home page fetch error:', err);
        setFeatured(getFeaturedSampleProducts());
        setRecent(getRecentSampleProducts().slice(0, 4));
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  return (
    <div className="page-wrapper lumen-home">
      <section className="lumen-hero">
        <div className="container">
          <div className="lumen-hero-grid">
            <div>
              <p className="lumen-kicker">LUMEN COLLECTION 2026</p>
              <h1 className="lumen-hero-title">
                Editorial fashion,
                <br />
                curated in black and white.
              </h1>
              <p className="lumen-hero-copy">
                A minimalist storefront for timeless silhouettes, crafted textures, and everyday luxury.
              </p>
              <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                <Link to="/shop" className="btn btn-lg">
                  Shop Now <ArrowRight size={16} />
                </Link>
                <Link to="/collections/new" className="btn btn-secondary btn-lg">
                  New Collection
                </Link>
              </div>
            </div>
            <div className="lumen-hero-panel">
              <p>LOOK 01</p>
              <span>Monochrome tailoring and modern essentials.</span>
            </div>
          </div>
          <div className="lumen-banner-grid">
            <article className="lumen-banner-card">
              <p className="lumen-banner-kicker">Editorial Drop</p>
              <h3>Minimal Outerwear</h3>
              <Link to="/collections/featured" className="btn btn-sm">View Drop</Link>
            </article>
            <article className="lumen-banner-card">
              <p className="lumen-banner-kicker">Curated Picks</p>
              <h3>Accessories in Focus</h3>
              <Link to="/shop?categoryId=5" className="btn btn-sm">Shop Accessories</Link>
            </article>
            <article className="lumen-banner-card">
              <p className="lumen-banner-kicker">New Story</p>
              <h3>Quiet Luxury Edit</h3>
              <Link to="/collections/new" className="btn btn-sm">Explore Now</Link>
            </article>
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="section-header">
            <div>
              <h2 className="section-title">Featured Pieces</h2>
              <p className="section-subtitle">Hand-selected essentials for this season.</p>
            </div>
            <Link to="/shop?featured=true" className="btn btn-ghost btn-sm" id="home-view-all-featured">
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
              <p className="empty-state-title">No featured products yet</p>
              <p className="empty-state-desc">Check back soon for featured items</p>
            </div>
          )}
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="section-header">
            <div>
              <h2 className="section-title">New Arrivals</h2>
              <p className="section-subtitle">Just landed in the atelier.</p>
            </div>
            <Link to="/shop?sort=id,desc" className="btn btn-ghost btn-sm">
              See Latest
            </Link>
          </div>
          <div className="grid-4">
            {recent.map((item) => (
              <ProductCard key={item.id} product={item} />
            ))}
            {!recent.length && !loading ? (
              <div className="empty-state">
                <p className="empty-state-title">No recent arrivals yet.</p>
              </div>
            ) : null}
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="lumen-cta">
            <h2 className="section-title">Build your monochrome wardrobe.</h2>
            <p className="section-subtitle">From statement pieces to everyday essentials.</p>
            <div style={{ marginTop: '14px' }}>
              <Link to="/shop" className="btn">
                Explore Shop <ArrowRight size={16} />
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
