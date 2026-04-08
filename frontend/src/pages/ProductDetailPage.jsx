import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ShoppingCart, Star, Heart, Package, Truck, Shield, ChevronLeft, Minus, Plus } from 'lucide-react';
import { productsAPI } from '../api/products.api';
import { inventoryAPI } from '../api/inventory.api';
import { reviewsAPI } from '../api/reviews.api';
import { recommendationsAPI } from '../api/recommendations.api';
import ProductCard from '../components/ProductCard';
import StarRating from '../components/StarRating';
import { PageLoader } from '../components/Loader';
import useAuthStore from '../store/auth.store';
import useCartStore from '../store/cart.store';
import { formatCurrency, formatRelative, truncate } from '../utils/helpers';
import toast from 'react-hot-toast';
import './ProductDetailPage.css';

export default function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { addItem } = useCartStore();

  const [product, setProduct] = useState(null);
  const [inventory, setInventory] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [reviewStats, setReviewStats] = useState(null);
  const [similar, setSimilar] = useState([]);
  const [reviewForm, setReviewForm] = useState({ rating: 5, title: '', content: '' });
  const [submittingReview, setSubmittingReview] = useState(false);
  const [loading, setLoading] = useState(true);
  const [qty, setQty] = useState(1);
  const [tab, setTab] = useState('overview');
  const [addingToCart, setAddingToCart] = useState(false);

  useEffect(() => {
    const fetchAll = async () => {
      setLoading(true);
      try {
        const prodRes = await productsAPI.getById(id);
        setProduct(prodRes.data);

        // track behavior
        if (user?.id) {
          recommendationsAPI.trackBehavior({
            userId: String(user.id),
            productId: String(id),
            actionType: 'VIEW',
          }).catch(() => {});
        }

        // parallel fetches
        await Promise.allSettled([
          inventoryAPI.checkAvailability(id, 1).then(r => setInventory(r.data)).catch(() => {}),
          reviewsAPI.getByProduct(id, { size: 10 }).then(r => {
            setReviews(r.data?.content || r.data || []);
          }).catch(() => {}),
          reviewsAPI.getProductStats(id).then(r => setReviewStats(r.data)).catch(() => {}),
          recommendationsAPI.getSimilarProducts(id, { limit: 6 }).then(r => {
            setSimilar(r.data || []);
          }).catch(() => {}),
        ]);
      } catch (err) {
        console.error('Product detail fetch error:', err);
        navigate('/products');
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, [id, user?.id]);

  if (loading || !product) return <div className="page-wrapper"><PageLoader /></div>;

  const inStock = inventory?.available !== false && (product.stockQuantity === undefined || product.stockQuantity > 0);
  const discount = product.originalPrice && product.price < product.originalPrice
    ? Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)
    : null;

  const handleAddToCart = async () => {
    if (!user) { toast.error('Please login first'); return; }
    setAddingToCart(true);
    try {
      const result = await addItem(user.id || user.userId, product.id, qty);
      if (result?.success) {
        toast.success(`${product.name} x${qty} added to cart!`);
        // track add-to-cart
        recommendationsAPI.trackBehavior({
          userId: String(user.id),
          productId: String(id),
          actionType: 'ADD_TO_CART',
          quantity: qty,
        }).catch(() => {});
      } else {
        toast.error(result?.error || 'Failed to add to cart');
      }
    } finally {
      setAddingToCart(false);
    }
  };

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (!user) { toast.error('Please login to write a review'); return; }
    if (!reviewForm.title || !reviewForm.content) { toast.error('Please fill in all fields'); return; }
    setSubmittingReview(true);
    try {
      await reviewsAPI.create({
        productId: String(id),
        userId: String(user.id),
        title: reviewForm.title,
        content: reviewForm.content,
        rating: reviewForm.rating,
      });
      toast.success('Review submitted!');
      setReviewForm({ rating: 5, title: '', content: '' });
      // Reload reviews
      const reviewsRes = await reviewsAPI.getByProduct(id, { size: 10 });
      setReviews(reviewsRes.data?.content || reviewsRes.data || []);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit review');
    } finally {
      setSubmittingReview(false);
    }
  };

  return (
    <div className="page-wrapper">
      <div className="container">
        {/* Breadcrumb */}
        <div className="breadcrumb">
          <button className="breadcrumb-back" onClick={() => navigate(-1)}>
            <ChevronLeft size={16} /> Back
          </button>
          <span className="text-muted">/</span>
          <span className="text-secondary">{product.category?.name || 'Products'}</span>
          <span className="text-muted">/</span>
          <span className="text-primary font-medium">{truncate(product.name, 40)}</span>
        </div>

        {/* Main Product Section */}
        <div className="product-detail-grid animate-fade-in">
          {/* Image */}
          <div className="product-detail-image-section">
            <div className="product-detail-image-wrapper card">
              {product.imageUrl ? (
                <img src={product.imageUrl} alt={product.name} className="product-detail-img" />
              ) : (
                <div className="product-detail-img-placeholder">
                  <Package size={60} style={{ color: 'var(--text-muted)' }} />
                </div>
              )}
              <div className="product-detail-badges">
                {product.featured && <span className="badge badge-indigo">Featured</span>}
                {discount && <span className="badge badge-rose">-{discount}% OFF</span>}
                {!inStock && <span className="badge badge-slate">Out of Stock</span>}
              </div>
            </div>
          </div>

          {/* Info */}
          <div className="product-detail-info">
            {product.brand && <p className="product-detail-brand">{product.brand}</p>}
            <h1 className="product-detail-name">{product.name}</h1>

            {/* Rating */}
            <div className="product-detail-rating">
              <StarRating rating={product.averageRating || 0} showValue size={18} />
              <span className="text-secondary text-sm">
                {reviewStats?.totalReviews || reviews.length || 0} reviews
              </span>
            </div>

            {/* Price */}
            <div className="product-detail-price-section">
              <div className="price price-large">{formatCurrency(product.price)}</div>
              {product.originalPrice && product.price < product.originalPrice && (
                <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                  <span className="price-old">{formatCurrency(product.originalPrice)}</span>
                  <span className="price-discount">Save {discount}%</span>
                </div>
              )}
            </div>

            {/* Stock */}
            <div className={`stock-indicator ${inStock ? 'in-stock' : 'out-stock'}`}>
              <span className="stock-dot" />
              {inStock ? `In Stock ${inventory?.totalAvailableQuantity ? `(${inventory.totalAvailableQuantity} units)` : ''}` : 'Out of Stock'}
            </div>

            {/* Qty + Cart */}
            <div className="product-detail-actions">
              <div className="qty-selector">
                <button
                  id="qty-decrease-btn"
                  className="qty-btn"
                  onClick={() => setQty(Math.max(1, qty - 1))}
                  disabled={qty <= 1}
                >
                  <Minus size={16} />
                </button>
                <span className="qty-value">{qty}</span>
                <button
                  id="qty-increase-btn"
                  className="qty-btn"
                  onClick={() => setQty(qty + 1)}
                  disabled={!inStock}
                >
                  <Plus size={16} />
                </button>
              </div>

              <button
                id="add-to-cart-detail-btn"
                className="btn btn-primary btn-lg"
                style={{ flex: 1 }}
                onClick={handleAddToCart}
                disabled={!inStock || addingToCart}
              >
                {addingToCart ? (
                  <span className="spinner" style={{ width: '18px', height: '18px', borderWidth: '2px' }} />
                ) : (
                  <><ShoppingCart size={18} /> Add to Cart</>
                )}
              </button>
            </div>

            {/* Trust Badges */}
            <div className="trust-badges">
              <div className="trust-badge">
                <Truck size={16} style={{ color: 'var(--accent-cyan)' }} />
                <span>Free shipping over ₹999</span>
              </div>
              <div className="trust-badge">
                <Shield size={16} style={{ color: 'var(--accent-emerald)' }} />
                <span>30-day return policy</span>
              </div>
              <div className="trust-badge">
                <Package size={16} style={{ color: 'var(--accent-indigo)' }} />
                <span>Secure packaging</span>
              </div>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="product-tabs">
          <div className="tab-nav">
            {['overview', 'specs', 'reviews'].map(t => (
              <button
                key={t}
                id={`tab-${t}`}
                className={`tab-btn ${tab === t ? 'active' : ''}`}
                onClick={() => setTab(t)}
              >
                {t === 'overview' && 'Overview'}
                {t === 'specs' && 'Specifications'}
                {t === 'reviews' && `Reviews (${reviews.length})`}
              </button>
            ))}
          </div>

          <div className="tab-content animate-fade-in" key={tab}>
            {tab === 'overview' && (
              <div className="overview-tab">
                {product.description ? (
                  <p className="product-description">{product.description}</p>
                ) : (
                  <p className="text-secondary">No description available.</p>
                )}
                {product.sku && (
                  <div className="product-meta">
                    <div className="meta-row"><span>SKU</span><span>{product.sku}</span></div>
                    {product.brand && <div className="meta-row"><span>Brand</span><span>{product.brand}</span></div>}
                    {product.category?.name && <div className="meta-row"><span>Category</span><span>{product.category.name}</span></div>}
                  </div>
                )}
              </div>
            )}

            {tab === 'specs' && (
              <div>
                {product.specifications ? (
                  <div className="product-meta">
                    {Object.entries(product.specifications).map(([k, v]) => (
                      <div className="meta-row" key={k}><span>{k}</span><span>{v}</span></div>
                    ))}
                  </div>
                ) : (
                  <p className="text-secondary">No specifications available.</p>
                )}
              </div>
            )}

            {tab === 'reviews' && (
              <div className="reviews-tab">
                {/* Rating Summary */}
                {reviewStats && (
                  <div className="review-summary card">
                    <div className="review-summary-avg">
                      <p className="review-avg-score">
                        {(reviewStats.averageRating || 0).toFixed(1)}
                      </p>
                      <StarRating rating={reviewStats.averageRating || 0} size={20} />
                      <p className="text-muted text-sm">{reviewStats.totalReviews || 0} reviews</p>
                    </div>
                    <div className="review-bars">
                      {[5, 4, 3, 2, 1].map(star => {
                        const count = reviewStats.ratingDistribution?.[star] || 0;
                        const pct = reviewStats.totalReviews ? (count / reviewStats.totalReviews) * 100 : 0;
                        return (
                          <div key={star} className="review-bar-row">
                            <span className="text-sm text-secondary">{star}★</span>
                            <div className="review-bar-track">
                              <div className="review-bar-fill" style={{ width: `${pct}%` }} />
                            </div>
                            <span className="text-xs text-muted">{count}</span>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* Review List */}
                <div className="reviews-list">
                  {reviews.length > 0 ? reviews.map((r, i) => (
                    <div key={r.id || i} className="review-item card">
                      <div className="review-header">
                        <div className="review-author-info">
                          <div className="review-avatar">
                            {(r.userId || 'A').toString().charAt(0).toUpperCase()}
                          </div>
                          <div>
                            <p className="font-semibold text-sm">{r.userId || 'Anonymous'}</p>
                            <p className="text-xs text-muted">{formatRelative(r.createdAt)}</p>
                          </div>
                        </div>
                        <StarRating rating={r.rating || 0} size={14} />
                      </div>
                      <h4 className="review-title-text">{r.title}</h4>
                      <p className="review-content text-secondary text-sm">{r.content}</p>
                    </div>
                  )) : (
                    <p className="text-secondary text-sm">No reviews yet. Be the first to review!</p>
                  )}
                </div>

                {/* Write Review */}
                {user && (
                  <div className="write-review card">
                    <h3 className="font-semibold" style={{ marginBottom: '16px' }}>Write a Review</h3>
                    <form id="review-form" onSubmit={handleSubmitReview}>
                      <div className="form-grid">
                        <div className="input-wrapper">
                          <label className="input-label">Your Rating</label>
                          <div className="star-selector">
                            {[1,2,3,4,5].map(s => (
                              <button
                                key={s}
                                type="button"
                                id={`star-${s}`}
                                onClick={() => setReviewForm({ ...reviewForm, rating: s })}
                              >
                                <Star
                                  size={24}
                                  fill={s <= reviewForm.rating ? '#f59e0b' : 'none'}
                                  color={s <= reviewForm.rating ? '#f59e0b' : '#475569'}
                                />
                              </button>
                            ))}
                          </div>
                        </div>
                        <div className="input-wrapper">
                          <label className="input-label" htmlFor="review-title">Title</label>
                          <input
                            id="review-title"
                            className="input"
                            placeholder="Review title…"
                            value={reviewForm.title}
                            onChange={(e) => setReviewForm({ ...reviewForm, title: e.target.value })}
                            required
                          />
                        </div>
                        <div className="input-wrapper">
                          <label className="input-label" htmlFor="review-content">Review</label>
                          <textarea
                            id="review-content"
                            className="input"
                            placeholder="Share your experience with this product…"
                            rows={4}
                            value={reviewForm.content}
                            onChange={(e) => setReviewForm({ ...reviewForm, content: e.target.value })}
                            required
                            style={{ resize: 'vertical' }}
                          />
                        </div>
                        <button
                          id="submit-review-btn"
                          type="submit"
                          className="btn btn-primary"
                          disabled={submittingReview}
                          style={{ alignSelf: 'flex-start' }}
                        >
                          {submittingReview ? <span className="spinner" style={{ width: '16px', height: '16px', borderWidth: '2px' }} /> : 'Submit Review'}
                        </button>
                      </div>
                    </form>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Similar Products */}
        {similar.length > 0 && (
          <div className="section">
            <div className="section-header">
              <h2 className="section-title">Similar Products</h2>
            </div>
            <div className="grid-4">
              {similar.slice(0, 4).map((rec, i) => (
                <ProductCard key={rec.productId || i}
                  product={{ id: rec.productId, name: rec.productName, price: rec.price, ...rec }} />
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
