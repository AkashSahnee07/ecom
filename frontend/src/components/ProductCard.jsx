import { Link } from 'react-router-dom';
import { ShoppingCart, Star, Eye } from 'lucide-react';
import { formatCurrency, truncate } from '../utils/helpers';
import useAuthStore from '../store/auth.store';
import useCartStore from '../store/cart.store';
import toast from 'react-hot-toast';
import './ProductCard.css';

export default function ProductCard({ product }) {
  const { user } = useAuthStore();
  const { addItem } = useCartStore();

  if (!product) return null;

  const {
    id, name, price, originalPrice, imageUrl, averageRating,
    reviewCount, stockQuantity, brand, category, featured
  } = product;

  const discount = originalPrice && price < originalPrice
    ? Math.round(((originalPrice - price) / originalPrice) * 100)
    : null;

  const inStock = stockQuantity === undefined || stockQuantity > 0;

  const handleAddToCart = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!user) {
      toast.error('Please login to add items to cart');
      return;
    }
    const result = await addItem(user.id || user.userId, id, 1);
    if (result?.success) {
      toast.success(`${name} added to cart!`);
    } else {
      toast.error(result?.error || 'Failed to add to cart');
    }
  };

  return (
    <Link to={`/products/${id}`} className="product-card card" id={`product-card-${id}`}>
      {/* Image */}
      <div className="product-img-wrapper">
        {imageUrl ? (
          <img src={imageUrl} alt={name} className="product-img" loading="lazy" />
        ) : (
          <div className="product-img-placeholder">
            <Eye size={28} />
          </div>
        )}

        {/* Badges */}
        <div className="product-badges">
          {featured && <span className="badge badge-indigo">Featured</span>}
          {discount && <span className="badge badge-rose">-{discount}%</span>}
          {!inStock && <span className="badge badge-slate">Out of Stock</span>}
        </div>

        {/* Quick actions overlay */}
        <div className="product-actions-overlay">
          <button
            id={`add-to-cart-${id}`}
            className="btn btn-primary btn-sm"
            onClick={handleAddToCart}
            disabled={!inStock}
          >
            <ShoppingCart size={14} />
            {inStock ? 'Add to Cart' : 'Out of Stock'}
          </button>
        </div>
      </div>

      {/* Info */}
      <div className="product-info">
        {brand && <p className="product-brand">{brand}</p>}
        <h3 className="product-name" title={name}>{truncate(name, 60)}</h3>

        {/* Rating */}
        {averageRating !== undefined && (
          <div className="product-rating">
            <div className="stars-wrapper">
              {[1,2,3,4,5].map(i => (
                <Star
                  key={i}
                  size={12}
                  fill={i <= Math.round(averageRating) ? '#f59e0b' : 'none'}
                  color={i <= Math.round(averageRating) ? '#f59e0b' : '#475569'}
                />
              ))}
            </div>
            <span className="rating-count text-muted text-xs">
              ({reviewCount || 0})
            </span>
          </div>
        )}

        {/* Price */}
        <div className="product-price-row">
          <span className="price">{formatCurrency(price)}</span>
          {originalPrice && price < originalPrice && (
            <span className="price-old">{formatCurrency(originalPrice)}</span>
          )}
        </div>
      </div>
    </Link>
  );
}
