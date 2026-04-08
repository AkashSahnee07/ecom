import { useState, useEffect } from 'react';
import { User, Bell, ShoppingBag, Save } from 'lucide-react';
import { userAPI } from '../api/auth.api';
import { ordersAPI } from '../api/orders.api';
import { notificationsAPI } from '../api/notifications.api';
import useAuthStore from '../store/auth.store';
import { formatRelative, formatDate } from '../utils/helpers';
import { SkeletonList } from '../components/Loader';
import toast from 'react-hot-toast';
import './ProfilePage.css';

export default function ProfilePage() {
  const { user, updateUser } = useAuthStore();
  const [tab, setTab] = useState('profile');
  const [form, setForm] = useState({
    firstName: user?.firstName || '',
    lastName:  user?.lastName  || '',
    phone:     user?.phone     || '',
    email:     user?.email     || '',
  });
  const [saving, setSaving] = useState(false);
  const [orders, setOrders] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [loadingNotifs, setLoadingNotifs] = useState(false);
  const [orderSummary, setOrderSummary] = useState(null);

  useEffect(() => {
    if (!user?.id) return;
    ordersAPI.getUserSummary(user.id).then(r => setOrderSummary(r.data)).catch(() => {});
    ordersAPI.getUserOrders(user.id, { page: 0, size: 5 }).then(r => {
      setOrders(r.data?.content || r.data || []);
    }).catch(() => {});
  }, [user?.id]);

  useEffect(() => {
    if (tab !== 'notifications' || !user?.id) return;
    setLoadingNotifs(true);
    notificationsAPI.getByRecipient(String(user.id), { size: 20 })
      .then(r => setNotifications(r.data?.content || r.data || []))
      .catch(() => {})
      .finally(() => setLoadingNotifs(false));
  }, [tab, user?.id]);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const res = await userAPI.update(user.id, form);
      updateUser({ ...user, ...res.data });
      toast.success('Profile updated!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Update failed');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page-wrapper">
      <div className="container">
        <div className="page-header">
          <h1 className="page-title">My Profile</h1>
        </div>

        {/* Profile Hero */}
        <div className="profile-hero card">
          <div className="profile-avatar-large">
            {(user?.firstName || user?.username || 'U').charAt(0).toUpperCase()}
          </div>
          <div>
            <h2 className="font-bold" style={{ fontSize: '22px', marginBottom: '4px' }}>
              {user?.firstName} {user?.lastName}
            </h2>
            <p className="text-secondary text-sm">{user?.email}</p>
            <p className="text-muted text-xs" style={{ marginTop: '4px' }}>
              Member since {formatDate(user?.createdAt)}
            </p>
          </div>

          {orderSummary && (
            <div className="profile-stats">
              <div className="profile-stat">
                <p className="profile-stat-value">{orderSummary.totalOrders || 0}</p>
                <p className="text-xs text-muted">Orders</p>
              </div>
              <div className="profile-stat-divider" />
              <div className="profile-stat">
                <p className="profile-stat-value">{orderSummary.completedOrders || 0}</p>
                <p className="text-xs text-muted">Completed</p>
              </div>
            </div>
          )}
        </div>

        {/* Tabs */}
        <div className="tab-nav" style={{ margin: '24px 0 0' }}>
          {[
            { key: 'profile', label: 'Profile', icon: <User size={14} /> },
            { key: 'orders', label: 'Recent Orders', icon: <ShoppingBag size={14} /> },
            { key: 'notifications', label: 'Notifications', icon: <Bell size={14} /> },
          ].map(t => (
            <button
              key={t.key}
              id={`profile-tab-${t.key}`}
              className={`tab-btn ${tab === t.key ? 'active' : ''}`}
              onClick={() => setTab(t.key)}
              style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              {t.icon} {t.label}
            </button>
          ))}
        </div>

        <div className="profile-tab-content animate-fade-in" key={tab}>
          {tab === 'profile' && (
            <div className="profile-form-card card">
              <form id="profile-form" onSubmit={handleSave}>
                <div className="form-grid">
                  <div className="form-grid-2">
                    <div className="input-wrapper">
                      <label className="input-label" htmlFor="profile-first-name">First Name</label>
                      <input id="profile-first-name" className="input" value={form.firstName}
                        onChange={e => setForm({...form, firstName: e.target.value})} />
                    </div>
                    <div className="input-wrapper">
                      <label className="input-label" htmlFor="profile-last-name">Last Name</label>
                      <input id="profile-last-name" className="input" value={form.lastName}
                        onChange={e => setForm({...form, lastName: e.target.value})} />
                    </div>
                  </div>
                  <div className="input-wrapper">
                    <label className="input-label" htmlFor="profile-email">Email</label>
                    <input id="profile-email" className="input" type="email" value={form.email}
                      onChange={e => setForm({...form, email: e.target.value})} />
                  </div>
                  <div className="input-wrapper">
                    <label className="input-label" htmlFor="profile-phone">Phone</label>
                    <input id="profile-phone" className="input" value={form.phone}
                      onChange={e => setForm({...form, phone: e.target.value})} />
                  </div>
                  <button id="profile-save-btn" type="submit" className="btn btn-primary" style={{ alignSelf: 'flex-start' }} disabled={saving}>
                    {saving ? <span className="spinner" style={{ width: '16px', height: '16px', borderWidth: '2px' }} /> : <><Save size={14} /> Save Changes</>}
                  </button>
                </div>
              </form>
            </div>
          )}

          {tab === 'orders' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '16px' }}>
              {orders.length === 0 ? (
                <p className="text-secondary">No orders yet.</p>
              ) : orders.map(o => (
                <div key={o.id} className="card" style={{ padding: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <p className="font-semibold text-sm">Order #{o.orderNumber || o.id}</p>
                    <p className="text-muted text-xs">{formatDate(o.createdAt)}</p>
                  </div>
                  <span className="badge badge-indigo">{o.status}</span>
                </div>
              ))}
            </div>
          )}

          {tab === 'notifications' && (
            <div style={{ marginTop: '16px' }}>
              {loadingNotifs ? <SkeletonList count={4} /> : notifications.length === 0 ? (
                <p className="text-secondary">No notifications yet.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {notifications.map((n, i) => (
                    <div key={n.id || i} className="card notification-item">
                      <div className="notif-dot" style={{
                        background: n.status === 'DELIVERED' ? 'var(--accent-emerald)' : 'var(--accent-indigo)'
                      }} />
                      <div>
                        <p className="font-semibold text-sm">{n.subject || 'Notification'}</p>
                        <p className="text-secondary text-sm" style={{ marginTop: '2px' }}>{n.content}</p>
                        <p className="text-muted text-xs" style={{ marginTop: '4px' }}>{formatRelative(n.createdAt)}</p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
