import { useState, useEffect } from 'react';
import {
  ShoppingBag, DollarSign, Package, AlertTriangle,
  TrendingUp, RefreshCw, Clock
} from 'lucide-react';
import { ordersAPI } from '../api/orders.api';
import { inventoryAPI } from '../api/inventory.api';
import { shippingAPI } from '../api/shipping.api';
import { productsAPI } from '../api/products.api';
import StatusBadge from '../components/StatusBadge';
import { SkeletonList } from '../components/Loader';
import { formatCurrency, formatDate, getOrderStatusColor, getShipmentStatusColor } from '../utils/helpers';
import { getSampleProducts } from '../data/sampleProducts';
import toast from 'react-hot-toast';
import './AdminDashboard.css';

const SAMPLE_ORDERS = [
  { id: 'ORD-1001', orderNumber: 'ORD-1001', createdAt: new Date().toISOString(), status: 'CONFIRMED', totalAmount: 4297 },
  { id: 'ORD-1002', orderNumber: 'ORD-1002', createdAt: new Date(Date.now() - 86400000).toISOString(), status: 'SHIPPED', totalAmount: 1899 },
  { id: 'ORD-1003', orderNumber: 'ORD-1003', createdAt: new Date(Date.now() - 172800000).toISOString(), status: 'PENDING', totalAmount: 2598 },
  { id: 'ORD-1004', orderNumber: 'ORD-1004', createdAt: new Date(Date.now() - 259200000).toISOString(), status: 'DELIVERED', totalAmount: 999 },
];

export default function AdminDashboard() {
  const [stats, setStats] = useState({ orders: 0, revenue: 0, products: 0, lowStock: 0 });
  const [recentOrders, setRecentOrders] = useState([]);
  const [lowStock, setLowStock] = useState([]);
  const [overdueShipments, setOverdueShipments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [usingSample, setUsingSample] = useState(false);

  const applySampleData = () => {
    const samples = getSampleProducts();
    setRecentOrders(SAMPLE_ORDERS);
    setLowStock(
      samples.slice(0, 4).map((p) => ({
        productId: p.name,
        warehouseId: 'WH-MAIN',
        availableQuantity: Math.max(1, Math.floor((p.stockQuantity || 8) / 2)),
        minimumStockLevel: 10,
      }))
    );
    setOverdueShipments([]);
    setStats({
      orders: SAMPLE_ORDERS.length,
      revenue: SAMPLE_ORDERS.reduce((sum, o) => sum + o.totalAmount, 0),
      products: samples.length,
      lowStock: 4,
    });
    setUsingSample(true);
  };

  const fetchAll = async () => {
    setLoading(true);
    try {
      const results = await Promise.allSettled([
        ordersAPI.getAll({ page: 0, size: 5 }),
        inventoryAPI.getLowStock(),
        shippingAPI.getOverdue(),
        productsAPI.getAll({ size: 1 }),
      ]);

      const ordersData = results[0].status === 'fulfilled'
        ? (results[0].value.data?.content || results[0].value.data || [])
        : [];
      const productsTotal = results[3].status === 'fulfilled'
        ? (results[3].value.data?.totalElements || results[3].value.data?.length || 0)
        : 0;

      if (!ordersData.length && !productsTotal) {
        applySampleData();
        return;
      }

      setUsingSample(false);
      setRecentOrders(ordersData);
      setStats((prev) => ({
        ...prev,
        orders: results[0].value?.data?.totalElements || ordersData.length,
        revenue: ordersData.reduce((sum, o) => sum + (o.totalAmount || 0), 0),
        products: productsTotal || getSampleProducts().length,
      }));

      if (results[1].status === 'fulfilled') {
        const items = results[1].value.data || [];
        setLowStock(items);
        setStats((prev) => ({ ...prev, lowStock: items.length }));
      } else {
        setLowStock([]);
      }

      if (results[2].status === 'fulfilled') {
        setOverdueShipments(results[2].value.data || []);
      } else {
        setOverdueShipments([]);
      }
    } catch {
      toast.error('Using sample admin data');
      applySampleData();
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAll(); }, []);

  const STAT_CARDS = [
    {
      icon: <ShoppingBag size={20} />, label: 'Total Orders',
      value: stats.orders.toLocaleString(), id: 'stat-orders'
    },
    {
      icon: <DollarSign size={20} />, label: 'Revenue (Recent)',
      value: formatCurrency(stats.revenue), id: 'stat-revenue'
    },
    {
      icon: <Package size={20} />, label: 'Products',
      value: stats.products.toLocaleString(), id: 'stat-products'
    },
    {
      icon: <AlertTriangle size={20} />, label: 'Low Stock Alerts',
      value: stats.lowStock, id: 'stat-low-stock'
    },
  ];

  return (
    <div className="admin-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Overview</h1>
          {usingSample && (
            <p className="text-muted text-xs" style={{ marginTop: 8 }}>
              Showing sample admin metrics while live services are unavailable.
            </p>
          )}
        </div>
        <button
          id="admin-refresh-btn"
          className="btn btn-secondary btn-sm"
          onClick={fetchAll}
          disabled={loading}
        >
          <RefreshCw size={14} className={loading ? 'spin-anim' : ''} />
          Refresh
        </button>
      </div>

      <div className="admin-stats-grid">
        {STAT_CARDS.map((s) => (
          <div key={s.id} id={s.id} className="stat-card">
            <div className="stat-icon">{s.icon}</div>
            <div>
              <p className="stat-value">{s.value}</p>
              <p className="stat-label">{s.label}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="admin-content-grid">
        <div className="admin-section">
          <div className="admin-section-header">
            <h3><Clock size={15} /> Recent Orders</h3>
          </div>
          {loading ? <SkeletonList count={4} /> : (
            <div className="admin-list">
              {recentOrders.length === 0 ? (
                <p className="admin-empty-note">No orders found</p>
              ) : recentOrders.map((order) => (
                <div key={order.id} id={`admin-order-${order.id}`} className="admin-list-item">
                  <div>
                    <p className="font-semibold text-sm">#{order.orderNumber || order.id}</p>
                    <p className="text-muted text-xs">{formatDate(order.createdAt)}</p>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <StatusBadge status={order.status} colorFn={getOrderStatusColor} />
                    <span className="font-semibold text-sm">{formatCurrency(order.totalAmount)}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="admin-section">
          <div className="admin-section-header">
            <h3><AlertTriangle size={15} /> Low Stock Alerts</h3>
          </div>
          {loading ? <SkeletonList count={4} /> : (
            <div className="admin-list">
              {lowStock.length === 0 ? (
                <p className="admin-empty-note">All stock levels are healthy</p>
              ) : lowStock.map((item, i) => (
                <div key={i} id={`low-stock-${item.productId}`} className="admin-list-item">
                  <div>
                    <p className="font-semibold text-sm">{item.productId}</p>
                    <p className="text-muted text-xs">Warehouse: {item.warehouseId}</p>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <p className="font-bold text-sm">{item.availableQuantity} left</p>
                    <p className="text-muted text-xs">Min: {item.minimumStockLevel}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="admin-section">
          <div className="admin-section-header">
            <h3><TrendingUp size={15} /> Overdue Shipments</h3>
          </div>
          {loading ? <SkeletonList count={4} /> : (
            <div className="admin-list">
              {overdueShipments.length === 0 ? (
                <p className="admin-empty-note">No overdue shipments</p>
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
  );
}
