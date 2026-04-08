import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Trash2, Minus, Plus, ShoppingCart, ArrowRight, Tag } from 'lucide-react';
import useAuthStore from '../store/auth.store';
import useCartStore from '../store/cart.store';
import { formatCurrency } from '../utils/helpers';
import { recommendationsAPI } from '../api/recommendations.api';
import ProductCard from '../components/ProductCard';
import { PageLoader } from '../components/Loader';
import toast from 'react-hot-toast';
import './CartPage.css';

export default function CartPage() {
  const { user } = useAuthStore();
  const { cart, loading, fetchCart, updateQuantity, removeItem, clearCart } = useCartStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (user?.id) fetchCart(user.id);
  }, [user?.id]);

  if (!user) {
    return (
      <div className="page-wrapper">
        <div className="container">
          <div className="empty-state" style={{ minHeight: '60vh' }}>
            <div className="empty-state-icon"><ShoppingCart size={36} /></div>
            <h3 className="empty-state-title">Please sign in</h3>
            <p className="empty-state-desc">Sign in to view your shopping cart</p>
            <Link to="/login" className="btn btn-primary">Sign In</Link>
          </div>
        </div>
      </div>
    );
  }

  if (loading) return <div className="page-wrapper"><PageLoader /></div>;

  const items = cart?.items || [];
  const subtotal = cart?.totalAmount || cart?.totalPrice || 0;
  const shipping = subtotal > 999 ? 0 : 99;
  const total = subtotal + shipping;

  const handleUpdateQty = async (productId, qty) => {
    if (qty < 1) return;
    await updateQuantity(user.id, productId, qty);
  };

  const handleRemove = async (productId) => {
    await removeItem(user.id, productId);
    toast.success('Item removed from cart');
  };

  const handleClear = async () => {
    await clearCart(user.id);
    toast.success('Cart cleared');
  };

  if (items.length === 0) {
    return (
      <div className="page-wrapper">
        <div className="container">
          <div className="page-header">
            <h1 className="page-title">Shopping Cart</h1>
          </div>
          <div className="empty-state" style={{ minHeight: '50vh' }}>
            <div className="empty-state-icon"><ShoppingCart size={36} /></div>
            <h3 className="empty-state-title">Your cart is empty</h3>
            <p className="empty-state-desc">Add products to your cart to continue shopping</p>
            <Link to="/products" id="cart-browse-btn" className="btn btn-primary">Browse Products</Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-wrapper">
      <div className="container">
        <div className="page-header">
          <h1 className="page-title">Shopping Cart</h1>
          <button id="clear-cart-btn" className="btn btn-ghost btn-sm" onClick={handleClear}>
            <Trash2 size={14} /> Clear all
          </button>
        </div>

        <div className="cart-grid">
          {/* Items List */}
          <div className="cart-items animate-fade-in">
            {items.map((item, i) => (
              <div key={item.productId || i} id={`cart-item-${item.productId}`} className="cart-item card">
                {/* Image */}
                <div className="cart-item-image">
                  {item.imageUrl ? (
                    <img src={item.imageUrl} alt={item.productName || 'Product'} />
                  ) : (
                    <div className="cart-img-placeholder">🛍️</div>
                  )}
                </div>

                {/* Info */}
                <div className="cart-item-info">
                  <Link
                    to={`/products/${item.productId}`}
                    className="cart-item-name font-semibold"
                  >
                    {item.productName || `Product #${item.productId}`}
                  </Link>
                  {item.brand && <p className="text-xs text-indigo">{item.brand}</p>}
                  <p className="cart-item-price">{formatCurrency(item.price || item.unitPrice)}</p>
                </div>

                {/* Qty */}
                <div className="cart-item-qty">
                  <div className="qty-selector">
                    <button
                      id={`decrease-qty-${item.productId}`}
                      className="qty-btn"
                      onClick={() => handleUpdateQty(item.productId, item.quantity - 1)}
                      disabled={item.quantity <= 1}
                    >
                      <Minus size={14} />
                    </button>
                    <span className="qty-value">{item.quantity}</span>
                    <button
                      id={`increase-qty-${item.productId}`}
                      className="qty-btn"
                      onClick={() => handleUpdateQty(item.productId, item.quantity + 1)}
                    >
                      <Plus size={14} />
                    </button>
                  </div>
                </div>

                {/* Subtotal */}
                <div className="cart-item-subtotal">
                  <p className="price font-bold">{formatCurrency((item.price || item.unitPrice) * item.quantity)}</p>
                  <button
                    id={`remove-${item.productId}`}
                    className="btn btn-icon btn-danger btn-sm"
                    onClick={() => handleRemove(item.productId)}
                    title="Remove"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Order Summary */}
          <div className="cart-summary animate-fade-in">
            <div className="cart-summary-card card">
              <h2 className="font-bold" style={{ fontSize: '18px', marginBottom: '20px' }}>
                Order Summary
              </h2>

              <div className="summary-rows">
                <div className="summary-row">
                  <span className="text-secondary">Subtotal ({items.length} items)</span>
                  <span className="font-semibold">{formatCurrency(subtotal)}</span>
                </div>
                <div className="summary-row">
                  <span className="text-secondary">Shipping</span>
                  <span className={shipping === 0 ? 'text-emerald font-semibold' : 'font-semibold'}>
                    {shipping === 0 ? 'FREE' : formatCurrency(shipping)}
                  </span>
                </div>
                {shipping > 0 && (
                  <div className="free-shipping-notice">
                    <Tag size={12} />
                    Add {formatCurrency(999 - subtotal)} more for free shipping
                  </div>
                )}
                <div className="divider" />
                <div className="summary-row summary-total">
                  <span>Total</span>
                  <span className="price price-large">{formatCurrency(total)}</span>
                </div>
              </div>

              <button
                id="proceed-checkout-btn"
                className="btn btn-primary btn-full btn-lg"
                style={{ marginTop: '20px' }}
                onClick={() => navigate('/checkout')}
              >
                Proceed to Checkout <ArrowRight size={16} />
              </button>

              <Link
                to="/products"
                id="cart-continue-shopping"
                className="btn btn-ghost btn-full"
                style={{ marginTop: '10px' }}
              >
                Continue Shopping
              </Link>
            </div>

            {/* Trust Info */}
            <div className="cart-trust-info">
              <p className="text-xs text-muted text-center">
                🔒 Secure checkout • 30-day returns • Free support
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
