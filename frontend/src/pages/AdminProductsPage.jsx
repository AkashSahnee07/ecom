import { useEffect, useState } from 'react';
import { productsAPI } from '../api/products.api';
import { formatCurrency } from '../utils/helpers';
import { getSampleProducts } from '../data/sampleProducts';
import '../layouts/AdminLayout.css';

export default function AdminProductsPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [usingSample, setUsingSample] = useState(false);

  useEffect(() => {
    productsAPI
      .getAll({ page: 0, size: 20, sortBy: 'id', sortDir: 'desc' })
      .then((res) => {
        const apiProducts = res.data?.content || res.data || [];
        if (apiProducts.length) {
          setProducts(apiProducts);
          setUsingSample(false);
        } else {
          setProducts(getSampleProducts());
          setUsingSample(true);
        }
      })
      .catch(() => {
        setProducts(getSampleProducts());
        setUsingSample(true);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="admin-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Products</h1>
          {usingSample && (
            <p className="text-muted text-xs" style={{ marginTop: 8 }}>
              Showing sample black pottery catalog.
            </p>
          )}
        </div>
      </div>

      <div className="admin-panel">
        {loading ? (
          <p className="text-secondary">Loading products...</p>
        ) : products.length === 0 ? (
          <p className="text-secondary">No products found.</p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table className="lumen-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Brand</th>
                  <th>Price</th>
                  <th>Stock</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id}>
                    <td>{product.id}</td>
                    <td>{product.name}</td>
                    <td>{product.brand || '-'}</td>
                    <td>{formatCurrency(product.price)}</td>
                    <td>{product.stockQuantity ?? '-'}</td>
                    <td>{product.active === false ? 'Inactive' : 'Active'}</td>
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
