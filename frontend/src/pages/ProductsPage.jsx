import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Search, SlidersHorizontal, X, ChevronDown } from 'lucide-react';
import { productsAPI, categoriesAPI } from '../api/products.api';
import ProductCard from '../components/ProductCard';
import { SkeletonCard } from '../components/Loader';
import { generatePageNumbers, debounce } from '../utils/helpers';
import './ProductsPage.css';

const SORT_OPTIONS = [
  { value: 'id,asc', label: 'Newest' },
  { value: 'price,asc', label: 'Price: Low to High' },
  { value: 'price,desc', label: 'Price: High to Low' },
  { value: 'averageRating,desc', label: 'Top Rated' },
  { value: 'name,asc', label: 'Name A-Z' },
];

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [filtersOpen, setFiltersOpen] = useState(false);

  const page = parseInt(searchParams.get('page') || '0');
  const search = searchParams.get('search') || '';
  const categoryId = searchParams.get('categoryId') || '';
  const sortParam = searchParams.get('sort') || 'id,asc';
  const minPrice = searchParams.get('minPrice') || '';
  const maxPrice = searchParams.get('maxPrice') || '';

  const [searchInput, setSearchInput] = useState(search);

  const [sortBy, sortDir] = sortParam.split(',');

  const updateParam = (key, val) => {
    const next = new URLSearchParams(searchParams);
    if (val) next.set(key, val); else next.delete(key);
    next.set('page', '0');
    setSearchParams(next);
  };

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    try {
      let res;
      const pageParams = { page, size: 12, sortBy, sortDir };

      if (search) {
        res = await productsAPI.search(search, pageParams);
      } else if (categoryId) {
        res = await productsAPI.getByCategory(categoryId, pageParams);
      } else if (minPrice || maxPrice) {
        res = await productsAPI.getByPriceRange(minPrice || 0, maxPrice || 999999, pageParams);
      } else {
        res = await productsAPI.getAll(pageParams);
      }

      const data = res.data;
      setProducts(data?.content || data || []);
      setTotalPages(data?.totalPages || 1);
      setTotalElements(data?.totalElements || (data?.length || 0));
    } catch (err) {
      console.error('Products fetch error:', err);
      setProducts([]);
    } finally {
      setLoading(false);
    }
  }, [page, search, categoryId, sortBy, sortDir, minPrice, maxPrice]);

  useEffect(() => { fetchProducts(); }, [fetchProducts]);

  useEffect(() => {
    categoriesAPI.getAll().then(res => {
      setCategories(res.data?.content || res.data || []);
    }).catch(() => {});
  }, []);

  const handleSearchInput = debounce((val) => {
    updateParam('search', val);
  }, 500);

  const clearFilters = () => {
    setSearchParams(new URLSearchParams({ page: '0' }));
    setSearchInput('');
  };

  const hasFilters = search || categoryId || minPrice || maxPrice;

  return (
    <div className="page-wrapper">
      <div className="container">
        <div className="page-header">
          <div>
            <h1 className="page-title">All Products</h1>
            <p className="text-secondary text-sm" style={{ marginTop: '8px' }}>
              {totalElements > 0 ? `${totalElements.toLocaleString()} products found` : 'Browse our catalog'}
            </p>
          </div>
        </div>

        {/* Search & Filter Bar */}
        <div className="products-toolbar">
          <div className="toolbar-left">
            <div className="input-icon-wrapper products-search">
              <Search size={16} className="input-icon" />
              <input
                id="products-search-input"
                className="input"
                placeholder="Search products…"
                value={searchInput}
                onChange={(e) => {
                  setSearchInput(e.target.value);
                  handleSearchInput(e.target.value);
                }}
              />
              {searchInput && (
                <button
                  className="clear-search-btn"
                  onClick={() => { setSearchInput(''); updateParam('search', ''); }}
                >
                  <X size={14} />
                </button>
              )}
            </div>

            <button
              id="products-filter-btn"
              className={`btn btn-secondary btn-sm ${filtersOpen ? 'active' : ''}`}
              onClick={() => setFiltersOpen(!filtersOpen)}
            >
              <SlidersHorizontal size={14} />
              Filters
              {hasFilters && <span className="filter-dot" />}
            </button>

            {hasFilters && (
              <button id="clear-filters-btn" className="btn btn-ghost btn-sm" onClick={clearFilters}>
                <X size={14} /> Clear
              </button>
            )}
          </div>

          <div className="toolbar-right">
            <div className="input-icon-wrapper" style={{ position: 'relative' }}>
              <select
                id="products-sort-select"
                className="select"
                style={{ minWidth: '180px', paddingRight: '36px' }}
                value={sortParam}
                onChange={(e) => updateParam('sort', e.target.value)}
              >
                {SORT_OPTIONS.map(o => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
              <ChevronDown size={14} style={{
                position: 'absolute', right: '12px', top: '50%',
                transform: 'translateY(-50%)', color: 'var(--text-muted)', pointerEvents: 'none'
              }} />
            </div>
          </div>
        </div>

        {/* Filter Panel */}
        {filtersOpen && (
          <div className="filter-panel card animate-fade-in">
            <div className="filter-section">
              <h4 className="filter-title">Category</h4>
              <div className="filter-chips">
                <button
                  className={`filter-chip ${!categoryId ? 'active' : ''}`}
                  onClick={() => updateParam('categoryId', '')}
                >
                  All
                </button>
                {categories.map(cat => (
                  <button
                    key={cat.id}
                    id={`filter-cat-${cat.id}`}
                    className={`filter-chip ${categoryId === String(cat.id) ? 'active' : ''}`}
                    onClick={() => updateParam('categoryId', cat.id)}
                  >
                    {cat.name}
                  </button>
                ))}
              </div>
            </div>

            <div className="filter-section">
              <h4 className="filter-title">Price Range</h4>
              <div className="price-range-inputs">
                <input
                  id="filter-min-price"
                  className="input"
                  type="number"
                  placeholder="Min ₹"
                  value={minPrice}
                  onChange={(e) => updateParam('minPrice', e.target.value)}
                  style={{ width: '120px' }}
                />
                <span className="text-muted">—</span>
                <input
                  id="filter-max-price"
                  className="input"
                  type="number"
                  placeholder="Max ₹"
                  value={maxPrice}
                  onChange={(e) => updateParam('maxPrice', e.target.value)}
                  style={{ width: '120px' }}
                />
              </div>
            </div>
          </div>
        )}

        {/* Products Grid */}
        {loading ? (
          <div className="grid-4">
            {Array.from({ length: 12 }).map((_, i) => <SkeletonCard key={i} />)}
          </div>
        ) : products.length > 0 ? (
          <div className="grid-4">
            {products.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        ) : (
          <div className="empty-state" style={{ minHeight: '400px' }}>
            <div className="empty-state-icon">
              <Search size={28} />
            </div>
            <h3 className="empty-state-title">No products found</h3>
            <p className="empty-state-desc">Try adjusting your search or filters</p>
            <button className="btn btn-primary" onClick={clearFilters}>Clear Filters</button>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="pagination">
            <button
              id="prev-page-btn"
              className="page-btn"
              disabled={page === 0}
              onClick={() => updateParam('page', String(page - 1))}
            >
              ←
            </button>

            {generatePageNumbers(page + 1, totalPages).map((p, i) => (
              p === '...'
                ? <span key={`dot-${i}`} className="text-muted" style={{ padding: '0 4px' }}>…</span>
                : (
                  <button
                    key={p}
                    id={`page-btn-${p}`}
                    className={`page-btn ${page + 1 === p ? 'active' : ''}`}
                    onClick={() => updateParam('page', String(p - 1))}
                  >
                    {p}
                  </button>
                )
            ))}

            <button
              id="next-page-btn"
              className="page-btn"
              disabled={page >= totalPages - 1}
              onClick={() => updateParam('page', String(page + 1))}
            >
              →
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
