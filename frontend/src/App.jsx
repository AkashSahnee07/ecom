import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';

import HomePage         from './pages/HomePage';
import LoginPage        from './pages/LoginPage';
import RegisterPage     from './pages/RegisterPage';
import ProductsPage     from './pages/ProductsPage';
import ProductDetailPage from './pages/ProductDetailPage';
import CartPage         from './pages/CartPage';
import CheckoutPage     from './pages/CheckoutPage';
import OrdersPage       from './pages/OrdersPage';
import OrderDetailPage  from './pages/OrderDetailPage';
import ProfilePage      from './pages/ProfilePage';
import AdminDashboard   from './pages/AdminDashboard';

export default function App() {
  return (
    <BrowserRouter>
      <Navbar />

      {/* Toast notifications */}
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 3000,
          style: {
            background: '#0f1120',
            color: '#f1f5f9',
            border: '1px solid rgba(255,255,255,0.08)',
            borderRadius: '12px',
            fontSize: '14px',
            boxShadow: '0 8px 32px rgba(0,0,0,0.6)',
          },
          success: { iconTheme: { primary: '#10b981', secondary: '#0f1120' } },
          error:   { iconTheme: { primary: '#f43f5e', secondary: '#0f1120' } },
        }}
      />

      <Routes>
        {/* Public routes */}
        <Route path="/"         element={<HomePage />} />
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />

        {/* Protected routes */}
        <Route path="/cart" element={
          <ProtectedRoute><CartPage /></ProtectedRoute>
        } />
        <Route path="/checkout" element={
          <ProtectedRoute><CheckoutPage /></ProtectedRoute>
        } />
        <Route path="/orders" element={
          <ProtectedRoute><OrdersPage /></ProtectedRoute>
        } />
        <Route path="/orders/:id" element={
          <ProtectedRoute><OrderDetailPage /></ProtectedRoute>
        } />
        <Route path="/profile" element={
          <ProtectedRoute><ProfilePage /></ProtectedRoute>
        } />

        {/* Admin */}
        <Route path="/admin" element={
          <ProtectedRoute adminOnly><AdminDashboard /></ProtectedRoute>
        } />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
