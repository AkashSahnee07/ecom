import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Package, ChevronRight, Search } from 'lucide-react';
import { ordersAPI } from '../api/orders.api';
import { shippingAPI } from '../api/shipping.api';
import StatusBadge from '../components/StatusBadge';
import { SkeletonList } from '../components/Loader';
import useAuthStore from '../store/auth.store';
import { formatCurrency, formatDate, getOrderStatusColor } from '../utils/helpers';
import './OrdersPage.css';

export default function OrdersPage() {
  const { user } = useAuthStore();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');

  useEffect(() => {
    if (!user?.id) return;
    const fetchOrders = async () => {
      setLoading(true);
      try {
        const res = await ordersAPI.getUserOrders(user.id, { page, size: 10 });
        const data = res.data;
        setOrders(data?.content || data || []);
        setTotalPages(data?.totalPages || 1);
      } catch { setOrders([]); }
      finally { setLoading(false); }
    };
    fetchOrders();
  }, [user?.id, page]);

  const filtered = search
    ? orders.filter(o =>
        (o.orderNumber || '').toLowerCase().includes(search.toLowerCase()) ||
        (o.status || '').toLowerCase().includes(search.toLowerCase())
      )
    : orders;

  return (
    <div className="page-wrapper">
      <div className="container">
        <div className="page-header">
          <h1 className="page-title">My Orders</h1>
        </div>

        {/* Search */}
        <div className="input-icon-wrapper" style={{ maxWidth: '400px', marginBottom: '24px' }}>
          <Search size={16} className="input-icon" />
          <input
            id="orders-search"
            className="input"
            placeholder="Search by order # or status…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>

        {loading ? (
          <SkeletonList count={5} />
        ) : filtered.length === 0 ? (
          <div className="empty-state" style={{ minHeight: '50vh' }}>
            <div className="empty-state-icon"><Package size={36} /></div>
            <h3 className="empty-state-title">No orders found</h3>
            <p className="empty-state-desc">
              {orders.length === 0 ? "You haven't placed any orders yet." : 'No orders match your search.'}
            </p>
            <Link to="/products" id="orders-shop-btn" className="btn btn-primary">Start Shopping</Link>
          </div>
        ) : (
          <div className="orders-list animate-fade-in">
            {filtered.map(order => (
              <Link
                key={order.id}
                to={`/orders/${order.id}`}
                id={`order-${order.id}`}
                className="order-card card"
              >
                <div className="order-card-header">
                  <div>
                    <p className="order-number">Order #{order.orderNumber || order.id}</p>
                    <p className="text-muted text-xs">{formatDate(order.createdAt || order.orderDate)}</p>
                  </div>
                  <StatusBadge status={order.status} colorFn={getOrderStatusColor} />
                </div>

                <div className="order-card-items">
                  {(order.items || []).slice(0, 3).map((item, i) => (
                    <span key={i} className="order-item-chip">
                      {item.productName || `Product #${item.productId}`}
                      {item.quantity > 1 && <span className="text-muted"> ×{item.quantity}</span>}
                    </span>
                  ))}
                  {(order.items || []).length > 3 && (
                    <span className="text-muted text-xs">+{order.items.length - 3} more</span>
                  )}
                </div>

                <div className="order-card-footer">
                  <div>
                    <p className="text-xs text-muted">Total</p>
                    <p className="price font-bold">{formatCurrency(order.totalAmount || order.totalPrice)}</p>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <StatusBadge status={order.paymentStatus || order.paymentStatus} colorFn={getOrderStatusColor} />
                    <ChevronRight size={18} style={{ color: 'var(--text-muted)' }} />
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="pagination">
            <button className="page-btn" disabled={page === 0} onClick={() => setPage(p => p - 1)}>←</button>
            {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => (
              <button
                key={i}
                className={`page-btn ${page === i ? 'active' : ''}`}
                onClick={() => setPage(i)}
              >{i + 1}</button>
            ))}
            <button className="page-btn" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>→</button>
          </div>
        )}
      </div>
    </div>
  );
}
