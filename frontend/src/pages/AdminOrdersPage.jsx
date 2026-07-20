import { useEffect, useState } from 'react';
import { ordersAPI } from '../api/orders.api';
import { formatCurrency, formatDate } from '../utils/helpers';
import '../layouts/AdminLayout.css';

const SAMPLE_ORDERS = [
  { id: 'ORD-1001', orderNumber: 'ORD-1001', createdAt: new Date().toISOString(), status: 'CONFIRMED', paymentStatus: 'COMPLETED', totalAmount: 4297 },
  { id: 'ORD-1002', orderNumber: 'ORD-1002', createdAt: new Date(Date.now() - 86400000).toISOString(), status: 'SHIPPED', paymentStatus: 'COMPLETED', totalAmount: 1899 },
  { id: 'ORD-1003', orderNumber: 'ORD-1003', createdAt: new Date(Date.now() - 172800000).toISOString(), status: 'PENDING', paymentStatus: 'PENDING', totalAmount: 2598 },
  { id: 'ORD-1004', orderNumber: 'ORD-1004', createdAt: new Date(Date.now() - 259200000).toISOString(), status: 'DELIVERED', paymentStatus: 'COMPLETED', totalAmount: 999 },
  { id: 'ORD-1005', orderNumber: 'ORD-1005', createdAt: new Date(Date.now() - 345600000).toISOString(), status: 'CANCELLED', paymentStatus: 'REFUNDED', totalAmount: 1499 },
];

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [usingSample, setUsingSample] = useState(false);

  useEffect(() => {
    ordersAPI
      .getAll({ page: 0, size: 20, sortBy: 'id', sortDir: 'desc' })
      .then((res) => {
        const apiOrders = res.data?.content || res.data || [];
        if (apiOrders.length) {
          setOrders(apiOrders);
          setUsingSample(false);
        } else {
          setOrders(SAMPLE_ORDERS);
          setUsingSample(true);
        }
      })
      .catch(() => {
        setOrders(SAMPLE_ORDERS);
        setUsingSample(true);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="admin-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Orders</h1>
          {usingSample && (
            <p className="text-muted text-xs" style={{ marginTop: 8 }}>
              Showing sample order records.
            </p>
          )}
        </div>
      </div>

      <div className="admin-panel">
        {loading ? (
          <p className="text-secondary">Loading orders...</p>
        ) : orders.length === 0 ? (
          <p className="text-secondary">No orders found.</p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table className="lumen-table">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Date</th>
                  <th>Status</th>
                  <th>Payment</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr key={order.id}>
                    <td>#{order.orderNumber || order.id}</td>
                    <td>{formatDate(order.createdAt)}</td>
                    <td>{order.status || '-'}</td>
                    <td>{order.paymentStatus || '-'}</td>
                    <td>{formatCurrency(order.totalAmount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
