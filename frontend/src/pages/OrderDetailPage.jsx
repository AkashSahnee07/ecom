import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ChevronLeft, Package, Truck, MapPin, CreditCard, XCircle } from 'lucide-react';
import { ordersAPI } from '../api/orders.api';
import { shippingAPI } from '../api/shipping.api';
import { paymentsAPI } from '../api/payments.api';
import StatusBadge from '../components/StatusBadge';
import { PageLoader } from '../components/Loader';
import { formatCurrency, formatDateTime, getOrderStatusColor, getPaymentStatusColor, getShipmentStatusColor } from '../utils/helpers';
import useAuthStore from '../store/auth.store';
import toast from 'react-hot-toast';
import './OrderDetailPage.css';

export default function OrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [order, setOrder] = useState(null);
  const [shipment, setShipment] = useState(null);
  const [payment, setPayment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    const fetchAll = async () => {
      setLoading(true);
      try {
        const orderRes = await ordersAPI.getById(id);
        setOrder(orderRes.data);

        await Promise.allSettled([
          shippingAPI.getByOrderId(id).then(r => setShipment(r.data)).catch(() => {}),
          paymentsAPI.getByOrderId(id).then(r => setPayment(r.data)).catch(() => {}),
        ]);
      } catch {
        toast.error('Failed to load order');
        navigate('/orders');
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, [id]);

  const handleCancel = async () => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return;
    setCancelling(true);
    try {
      await ordersAPI.cancel(id, 'Cancelled by user');
      toast.success('Order cancelled successfully');
      const res = await ordersAPI.getById(id);
      setOrder(res.data);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to cancel order');
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <div className="page-wrapper"><PageLoader /></div>;
  if (!order) return null;

  const canCancel = ['PENDING', 'PROCESSING'].includes(order.status);

  return (
    <div className="page-wrapper">
      <div className="container">
        {/* Header */}
        <div className="page-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <button className="btn btn-ghost btn-sm" onClick={() => navigate('/orders')}>
              <ChevronLeft size={16} /> Back
            </button>
            <div>
              <h1 className="page-title">Order #{order.orderNumber || order.id}</h1>
              <p className="text-muted text-sm">{formatDateTime(order.createdAt || order.orderDate)}</p>
            </div>
          </div>
          {canCancel && (
            <button
              id="cancel-order-btn"
              className="btn btn-danger"
              onClick={handleCancel}
              disabled={cancelling}
            >
              <XCircle size={16} />
              {cancelling ? 'Cancelling…' : 'Cancel Order'}
            </button>
          )}
        </div>

        <div className="order-detail-grid">
          <div className="order-detail-main">
            {/* Status Card */}
            <div className="card order-status-card">
              <div className="order-status-row">
                <div>
                  <p className="text-xs text-muted font-bold uppercase" style={{ marginBottom: '6px' }}>Order Status</p>
                  <StatusBadge status={order.status} colorFn={getOrderStatusColor} />
                </div>
                <div>
                  <p className="text-xs text-muted font-bold uppercase" style={{ marginBottom: '6px' }}>Payment Status</p>
                  <StatusBadge status={order.paymentStatus} colorFn={getPaymentStatusColor} />
                </div>
                <div>
                  <p className="text-xs text-muted font-bold uppercase" style={{ marginBottom: '6px' }}>Total</p>
                  <p className="price" style={{ fontSize: '20px' }}>{formatCurrency(order.totalAmount)}</p>
                </div>
              </div>
            </div>

            {/* Items */}
            <div className="card" style={{ padding: '24px' }}>
              <h3 className="font-semibold" style={{ marginBottom: '16px' }}>
                <Package size={16} style={{ display: 'inline', marginRight: '8px' }} />
                Order Items ({(order.items || []).length})
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {(order.items || []).map((item, i) => (
                  <div key={i} className="order-detail-item">
                    <div className="order-item-img">
                      {item.imageUrl ? <img src={item.imageUrl} alt={item.productName} /> : '📦'}
                    </div>
                    <div style={{ flex: 1 }}>
                      <Link to={`/products/${item.productId}`} className="order-item-name">
                        {item.productName || `Product #${item.productId}`}
                      </Link>
                      <p className="text-xs text-muted">Qty: {item.quantity}</p>
                    </div>
                    <p className="font-semibold">{formatCurrency(item.price * item.quantity)}</p>
                  </div>
                ))}
              </div>
            </div>

            {/* Shipment Tracking */}
            {shipment && (
              <div className="card" style={{ padding: '24px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                  <h3 className="font-semibold">
                    <Truck size={16} style={{ display: 'inline', marginRight: '8px' }} />
                    Shipment Tracking
                  </h3>
                  <StatusBadge status={shipment.status} colorFn={getShipmentStatusColor} />
                </div>
                {shipment.trackingNumber && (
                  <p className="text-sm text-secondary" style={{ marginBottom: '12px' }}>
                    Tracking #: <span className="font-semibold text-primary">{shipment.trackingNumber}</span>
                  </p>
                )}
                {shipment.carrier && (
                  <p className="text-sm text-secondary">Carrier: {shipment.carrier}</p>
                )}
                {shipment.estimatedDelivery && (
                  <p className="text-sm text-secondary">
                    Est. Delivery: {formatDateTime(shipment.estimatedDelivery)}
                  </p>
                )}
              </div>
            )}
          </div>

          {/* Sidebar */}
          <div className="order-detail-sidebar">
            {/* Shipping Address */}
            <div className="card" style={{ padding: '20px' }}>
              <h4 className="font-semibold text-sm" style={{ marginBottom: '12px' }}>
                <MapPin size={14} style={{ display: 'inline', marginRight: '6px' }} />
                Shipping Address
              </h4>
              <p className="text-secondary text-sm" style={{ lineHeight: '1.6' }}>
                {order.shippingAddress || 'N/A'}
              </p>
            </div>

            {/* Payment Info */}
            {payment && (
              <div className="card" style={{ padding: '20px' }}>
                <h4 className="font-semibold text-sm" style={{ marginBottom: '12px' }}>
                  <CreditCard size={14} style={{ display: 'inline', marginRight: '6px' }} />
                  Payment Details
                </h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                    <span className="text-secondary">Method</span>
                    <span>{payment.paymentMethod?.replace(/_/g, ' ')}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                    <span className="text-secondary">Amount</span>
                    <span className="font-semibold">{formatCurrency(payment.amount)}</span>
                  </div>
                  <StatusBadge status={payment.status} colorFn={getPaymentStatusColor} />
                </div>
              </div>
            )}

            {/* Order Summary */}
            <div className="card" style={{ padding: '20px' }}>
              <h4 className="font-semibold text-sm" style={{ marginBottom: '12px' }}>Price Breakdown</h4>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                  <span className="text-secondary">Subtotal</span>
                  <span>{formatCurrency(order.totalAmount)}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                  <span className="text-secondary">Shipping</span>
                  <span style={{ color: 'var(--accent-emerald)' }}>FREE</span>
                </div>
                <div className="divider" />
                <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700 }}>
                  <span>Total</span>
                  <span>{formatCurrency(order.totalAmount)}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
