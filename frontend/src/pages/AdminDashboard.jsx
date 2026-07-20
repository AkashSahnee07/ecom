import { useState, useEffect } from 'react';
import {
  ShoppingBag, DollarSign, Package, Users, AlertTriangle,
  TrendingUp, RefreshCw, Clock
} from 'lucide-react';
import { ordersAPI } from '../api/orders.api';
import { inventoryAPI } from '../api/inventory.api';
import { paymentsAPI } from '../api/payments.api';
import { shippingAPI } from '../api/shipping.api';
import { productsAPI } from '../api/products.api';
import StatusBadge from '../components/StatusBadge';
import { SkeletonList } from '../components/Loader';
import { formatCurrency, formatDate, getOrderStatusColor, getShipmentStatusColor } from '../utils/helpers';
import toast from 'react-hot-toast';
import './AdminDashboard.css';

export default function AdminDashboard() {
  const [stats, setStats] = useState({ orders: 0, revenue: 0, products: 0, lowStock: 0 });
  const [recentOrders, setRecentOrders] = useState([]);
  const [lowStock, setLowStock] = useState([]);
  const [overdueShipments, setOverdueShipments] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchAll = async () => {
    setLoading(true);
    try {
      const results = await Promise.allSettled([
        ordersAPI.getAll({ page: 0, size: 5 }),
        inventoryAPI.getLowStock(),
        shippingAPI.getOverdue(),
        productsAPI.getAll({ size: 1 }),
      ]);

      if (results[0].status === 'fulfilled') {
        const data = results[0].value.data;
        setRecentOrders(data?.content || data || []);
        setStats(prev => ({
          ...prev,
          orders: data?.totalElements || (data?.length || 0),
          revenue: (data?.content || data || []).reduce((sum, o) => sum + (o.totalAmount || 0), 0),
        }));
      }
      if (results[1].status === 'fulfilled') {
        const items = results[1].value.data || [];
        setLowStock(items);
        setStats(prev => ({ ...prev, lowStock: items.length }));
      }
      if (results[2].status === 'fulfilled') {
        setOverdueShipments(results[2].value.data || []);
      }
      if (results[3].status === 'fulfilled') {
        const data = results[3].value.data;
        setStats(prev => ({ ...prev, products: data?.totalElements || (data?.length || 0) }));
      }
    } catch (err) {
      toast.error('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAll(); }, []);

  const STAT_CARDS = [
    {
      icon: <ShoppingBag size={22} />, label: 'Total Orders',
      value: stats.orders.toLocaleString(), color: '#6366f1',
      bg: 'rgba(99,102,241,0.1)', id: 'stat-orders'
    },
    {
      icon: <DollarSign size={22} />, label: 'Revenue (Recent)',
      value: formatCurrency(stats.revenue), color: '#10b981',
      bg: 'rgba(16,185,129,0.1)', id: 'stat-revenue'
    },
    {
      icon: <Package size={22} />, label: 'Products',
      value: stats.products.toLocaleString(), color: '#22d3ee',
      bg: 'rgba(34,211,238,0.1)', id: 'stat-products'
    },
    {
      icon: <AlertTriangle size={22} />, label: 'Low Stock Alerts',
      value: stats.lowStock, color: '#f43f5e',
      bg: 'rgba(244,63,94,0.1)', id: 'stat-low-stock'
    },
  ];

  return (
    <div className="page-wrapper">
      <div className="container">
        <div className="page-header">
          <h1 className="page-title">Admin Dashboard</h1>
          <button id="admin-refresh-btn" className="btn btn-secondary btn-sm" onClick={fetchAll} disabled={loading}>
            <RefreshCw size={14} className={loading ? 'spin-anim' : ''} />
            Refresh
          </button>
        </div>

        {/* Stat Cards */}
        <div className="admin-stats-grid">
          {STAT_CARDS.map((s, i) => (
            <div key={i} id={s.id} className="card stat-card">
              <div className="stat-icon" style={{ background: s.bg, color: s.color }}>
                {s.icon}
              </div>
              <div>
                <p className="stat-value">{s.value}</p>
                <p className="stat-label">{s.label}</p>
              </div>
            </div>
          ))}
        </div>

        {/* Content Grid */}
        <div className="admin-content-grid">
          {/* Recent Orders */}
          <div className="admin-section card">
            <div className="admin-section-header">
              <h3 className="font-semibold">
                <Clock size={15} style={{ display: 'inline', marginRight: '8px' }} />
                Recent Orders
              </h3>
            </div>
            {loading ? <SkeletonList count={5} /> : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {recentOrders.length === 0 ? (
                  <p className="text-secondary text-sm">No orders found</p>
                ) : recentOrders.map(order => (
                  <div key={order.id} id={`admin-order-${order.id}`} className="admin-list-item">
                    <div>
                      <p className="font-semibold text-sm">#{order.orderNumber || order.id}</p>
                      <p className="text-muted text-xs">{formatDate(order.createdAt)}</p>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <StatusBadge status={order.status} colorFn={getOrderStatusColor} />
                      <span className="font-semibold text-sm">{formatCurrency(order.totalAmount)}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Low Stock Alerts */}
          <div className="admin-section card">
            <div className="admin-section-header">
              <h3 className="font-semibold">
                <AlertTriangle size={15} style={{ display: 'inline', marginRight: '8px', color: 'var(--accent-rose)' }} />
                Low Stock Alerts
              </h3>
            </div>
            {loading ? <SkeletonList count={4} /> : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {lowStock.length === 0 ? (
                  <p className="text-secondary text-sm" style={{ color: 'var(--accent-emerald)' }}>
                    ✓ All stock levels are healthy
                  </p>
                ) : lowStock.map((item, i) => (
                  <div key={i} id={`low-stock-${item.productId}`} className="admin-list-item">
                    <div>
                      <p className="font-semibold text-sm">{item.productId}</p>
                      <p className="text-muted text-xs">
                        Warehouse: {item.warehouseId}
                      </p>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <p className="text-rose font-bold text-sm">{item.availableQuantity} left</p>
                      <p className="text-muted text-xs">Min: {item.minimumStockLevel}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Overdue Shipments */}
          <div className="admin-section card">
            <div className="admin-section-header">
              <h3 className="font-semibold">
                <TrendingUp size={15} style={{ display: 'inline', marginRight: '8px' }} />
                Overdue Shipments
              </h3>
            </div>
            {loading ? <SkeletonList count={4} /> : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {overdueShipments.length === 0 ? (
                  <p className="text-secondary text-sm" style={{ color: 'var(--accent-emerald)' }}>
                    ✓ No overdue shipments
                  </p>
                ) : overdueShipments.map((s, i) => (
                  <div key={s.id || i} className="admin-list-item">
                    <div>
                      <p className="font-semibold text-sm">#{s.id}</p>
                      <p className="text-muted text-xs">Order: {s.orderId}</p>
                    </div>
                    <StatusBadge status={s.status} colorFn={getShipmentStatusColor} />
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
