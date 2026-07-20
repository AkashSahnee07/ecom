import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { CreditCard, Landmark, Smartphone, CheckCircle } from 'lucide-react';
import useAuthStore from '../store/auth.store';
import useCartStore from '../store/cart.store';
import { ordersAPI } from '../api/orders.api';
import { paymentsAPI } from '../api/payments.api';
import { formatCurrency } from '../utils/helpers';
import toast from 'react-hot-toast';
import './CheckoutPage.css';

const PAYMENT_METHODS = [
  { id: 'CREDIT_CARD',  label: 'Credit Card',   icon: <CreditCard size={18} /> },
  { id: 'DEBIT_CARD',   label: 'Debit Card',    icon: <CreditCard size={18} /> },
  { id: 'UPI',          label: 'UPI',            icon: <Smartphone size={18} /> },
  { id: 'NET_BANKING',  label: 'Net Banking',    icon: <Landmark size={18} /> },
  { id: 'CASH_ON_DELIVERY', label: 'Cash on Delivery', icon: '💵' },
];

export default function CheckoutPage() {
  const { user } = useAuthStore();
  const { cart, fetchCart, clearCart } = useCartStore();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [orderId, setOrderId] = useState(null);

  const [address, setAddress] = useState({
    fullName: `${user?.firstName || ''} ${user?.lastName || ''}`.trim(),
    street: '', city: '', state: '', pincode: '', phone: user?.phone || '',
  });

  const [paymentMethod, setPaymentMethod] = useState('UPI');

  useEffect(() => {
    if (user?.id) fetchCart(user.id);
  }, [user?.id]);

  const items = cart?.items || [];
  const subtotal = cart?.totalAmount || cart?.totalPrice || 0;
  const shipping = subtotal > 999 ? 0 : 99;
  const total = subtotal + shipping;

  const handleAddressSubmit = (e) => {
    e.preventDefault();
    const { street, city, state, pincode } = address;
    if (!street || !city || !state || !pincode) {
      toast.error('Please fill in all address fields');
      return;
    }
    setStep(2);
  };

  const handlePlaceOrder = async () => {
    setLoading(true);
    try {
      const shippingAddress = `${address.fullName}, ${address.street}, ${address.city}, ${address.state} - ${address.pincode}`;

      const orderPayload = {
        userId: String(user.id),
        shippingAddress,
        billingAddress: shippingAddress,
        items: items.map(it => ({
          productId: String(it.productId),
          quantity: it.quantity,
          price: it.price || it.unitPrice,
          productName: it.productName,
        })),
        totalAmount: total,
        currency: 'INR',
        notes: '',
      };

      const orderRes = await ordersAPI.create(orderPayload);
      const createdOrder = orderRes.data;

      // Create payment
      try {
        await paymentsAPI.create({
          orderId: String(createdOrder.id || createdOrder.orderId),
          userId: String(user.id),
          amount: total,
          currency: 'INR',
          paymentMethod: paymentMethod,
        });
      } catch {/* payment creation may fail - order still created */}

      setOrderId(createdOrder.id || createdOrder.orderId);
      setStep(3);
      await clearCart(user.id);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to place order');
    } finally {
      setLoading(false);
    }
  };

  if (step === 3) {
    return (
      <div className="page-wrapper">
        <div className="container">
          <div className="order-success animate-scale-in">
            <div className="success-icon">
              <CheckCircle size={56} style={{ color: 'var(--accent-emerald)' }} />
            </div>
            <h1 style={{ fontSize: '28px', fontWeight: 800, marginBottom: '8px' }}>
              Order Placed!
            </h1>
            <p className="text-secondary" style={{ marginBottom: '8px' }}>
              Your order #{orderId} has been placed successfully.
            </p>
            <p className="text-muted text-sm" style={{ marginBottom: '32px' }}>
              You'll receive a confirmation notification shortly.
            </p>
            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', justifyContent: 'center' }}>
              <button
                id="view-orders-btn"
                className="btn btn-primary btn-lg"
                onClick={() => navigate('/orders')}
              >
                View My Orders
              </button>
              <button
                id="continue-shopping-after-order"
                className="btn btn-secondary"
                onClick={() => navigate('/products')}
              >
                Continue Shopping
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-wrapper">
      <div className="container">
        <div className="page-header">
          <h1 className="page-title">Checkout</h1>
        </div>

        {/* Step Progress */}
        <div className="checkout-steps">
          {['Delivery Address', 'Payment Method', 'Confirm'].map((s, i) => (
            <div key={i} className={`checkout-step ${step > i + 1 ? 'done' : step === i + 1 ? 'active' : ''}`}>
              <div className="step-circle">{step > i + 1 ? '✓' : i + 1}</div>
              <span className="step-label">{s}</span>
            </div>
          ))}
        </div>

        <div className="checkout-grid">
          <div className="checkout-main">
            {/* Step 1: Address */}
            {step === 1 && (
              <div className="checkout-section card animate-fade-in">
                <h2 className="checkout-section-title">Delivery Address</h2>
                <form id="checkout-address-form" onSubmit={handleAddressSubmit}>
                  <div className="form-grid">
                    <div className="form-grid-2">
                      <div className="input-wrapper">
                        <label className="input-label" htmlFor="co-fullname">Full Name</label>
                        <input id="co-fullname" className="input" placeholder="John Doe"
                          value={address.fullName} onChange={e => setAddress({...address, fullName: e.target.value})} required />
                      </div>
                      <div className="input-wrapper">
                        <label className="input-label" htmlFor="co-phone">Phone</label>
                        <input id="co-phone" className="input" placeholder="+91 9876543210"
                          value={address.phone} onChange={e => setAddress({...address, phone: e.target.value})} required />
                      </div>
                    </div>
                    <div className="input-wrapper">
                      <label className="input-label" htmlFor="co-street">Street Address</label>
                      <input id="co-street" className="input" placeholder="123 Main Street, Apt 4B"
                        value={address.street} onChange={e => setAddress({...address, street: e.target.value})} required />
                    </div>
                    <div className="form-grid-2" style={{ gridTemplateColumns: '1fr 1fr 120px' }}>
                      <div className="input-wrapper">
                        <label className="input-label" htmlFor="co-city">City</label>
                        <input id="co-city" className="input" placeholder="Mumbai"
                          value={address.city} onChange={e => setAddress({...address, city: e.target.value})} required />
                      </div>
                      <div className="input-wrapper">
                        <label className="input-label" htmlFor="co-state">State</label>
                        <input id="co-state" className="input" placeholder="Maharashtra"
                          value={address.state} onChange={e => setAddress({...address, state: e.target.value})} required />
                      </div>
                      <div className="input-wrapper">
                        <label className="input-label" htmlFor="co-pincode">Pincode</label>
                        <input id="co-pincode" className="input" placeholder="400001"
                          value={address.pincode} onChange={e => setAddress({...address, pincode: e.target.value})} required maxLength={6} />
                      </div>
                    </div>
                    <button id="checkout-next-btn" type="submit" className="btn btn-primary btn-lg" style={{ alignSelf: 'flex-start' }}>
                      Continue to Payment →
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Step 2: Payment */}
            {step === 2 && (
              <div className="checkout-section card animate-fade-in">
                <h2 className="checkout-section-title">Payment Method</h2>
                <div className="payment-methods">
                  {PAYMENT_METHODS.map(pm => (
                    <label
                      key={pm.id}
                      id={`pm-${pm.id}`}
                      className={`payment-option ${paymentMethod === pm.id ? 'selected' : ''}`}
                    >
                      <input
                        type="radio"
                        name="paymentMethod"
                        value={pm.id}
                        checked={paymentMethod === pm.id}
                        onChange={() => setPaymentMethod(pm.id)}
                      />
                      <div className="payment-icon">{pm.icon}</div>
                      <span className="font-medium text-sm">{pm.label}</span>
                      {paymentMethod === pm.id && (
                        <CheckCircle size={16} style={{ color: 'var(--accent-indigo)', marginLeft: 'auto' }} />
                      )}
                    </label>
                  ))}
                </div>

                <div style={{ display: 'flex', gap: '12px', marginTop: '24px' }}>
                  <button id="checkout-back-btn" className="btn btn-secondary" onClick={() => setStep(1)}>← Back</button>
                  <button id="checkout-confirm-btn" className="btn btn-primary btn-lg" onClick={handlePlaceOrder} disabled={loading}>
                    {loading ? <span className="spinner" style={{ width: '18px', height: '18px', borderWidth: '2px' }} /> : `Place Order · ${formatCurrency(total)}`}
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* Order Summary */}
          <div className="checkout-summary card">
            <h3 className="font-semibold" style={{ marginBottom: '16px' }}>Order Summary</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '16px' }}>
              {items.map((item, i) => (
                <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                  <span className="text-secondary">{item.productName || `Product #${item.productId}`} ×{item.quantity}</span>
                  <span>{formatCurrency((item.price || item.unitPrice) * item.quantity)}</span>
                </div>
              ))}
            </div>
            <div className="divider" />
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                <span className="text-secondary">Subtotal</span>
                <span>{formatCurrency(subtotal)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                <span className="text-secondary">Shipping</span>
                <span style={{ color: shipping === 0 ? 'var(--accent-emerald)' : '' }}>
                  {shipping === 0 ? 'FREE' : formatCurrency(shipping)}
                </span>
              </div>
              <div className="divider" />
              <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '16px' }}>
                <span>Total</span>
                <span>{formatCurrency(total)}</span>
              </div>
            </div>

            {step === 1 && address.street && (
              <div className="checkout-address-preview">
                <p className="text-xs text-muted text-uppercase font-bold" style={{ marginBottom: '4px' }}>Delivering to</p>
                <p className="text-sm text-secondary">
                  {address.street}, {address.city}, {address.state} - {address.pincode}
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
