// User Types
export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  profilePicture?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  dateOfBirth?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
}

// Product Types
export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  originalPrice?: number;
  discountPrice?: number;
  discount?: number;
  sku: string;
  brand: string;
  categoryId: number;
  categoryName: string;
  imageUrl: string;
  images: string[];
  stock: number;
  stockQuantity: number;
  isActive: boolean;
  weight?: number;
  dimensions?: string;
  tags: string[];
  rating: number;
  averageRating: number;
  reviewCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
  imageUrl?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductFilter {
  categoryId?: number;
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
  inStock?: boolean;
  search?: string;
  sortBy?: 'name' | 'price' | 'rating' | 'createdAt';
  sortDirection?: 'asc' | 'desc';
}

// Cart Types
export interface CartItem {
  productId: number;
  productName: string;
  productSku: string;
  price: number;
  quantity: number;
  imageUrl: string;
  brand: string;
  categoryId: number;
  categoryName: string;
}

export interface Cart {
  id: string;
  userId: number;
  items: CartItem[];
  totalAmount: number;
  totalItems: number;
  createdAt: string;
  updatedAt: string;
}

export interface AddToCartRequest {
  productId: number;
  productName: string;
  productSku: string;
  price: number;
  quantity: number;
  imageUrl: string;
  brand: string;
  categoryId: number;
  categoryName: string;
}

export interface CartSummary {
  totalItems: number;
  totalAmount: number;
  subtotal: number;
  tax: number;
  shipping: number;
  discount: number;
  currency: string;
}

// Order Types
export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  PROCESSING = 'PROCESSING',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

export interface Address {
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export interface OrderItem {
  productId: number;
  productName: string;
  productSku: string;
  price: number;
  quantity: number;
  imageUrl: string;
}

export interface Order {
  id: number;
  orderNumber: string;
  userId: string;
  items: OrderItem[];
  totalAmount: number;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  paymentMethod: string;
  shippingAddress: Address;
  billingAddress: Address;
  estimatedDeliveryDate?: string;
  actualDeliveryDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOrderRequest {
  userId: string;
  items: OrderItem[];
  shippingAddress: Address;
  billingAddress: Address;
  paymentMethod: string;
}

// Payment Types
export enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  PAYPAL = 'PAYPAL',
  BANK_TRANSFER = 'BANK_TRANSFER'
}

export interface Payment {
  id: number;
  paymentId: string;
  orderId: string;
  userId: string;
  amount: number;
  currency: string;
  status: PaymentStatus;
  paymentMethod: PaymentMethod;
  paymentGateway: string;
  transactionId?: string;
  failureReason?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePaymentRequest {
  orderId: string;
  userId: string;
  amount: number;
  currency: string;
  paymentMethod: PaymentMethod;
}

// API Response Types
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ErrorResponse {
  error: string;
  message: string;
  status: number;
  timestamp: string;
}

// UI State Types
export interface LoadingState {
  isLoading: boolean;
  error?: string;
}

export interface AuthState extends LoadingState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
}

export interface CartState extends LoadingState {
  cart: Cart | null;
  itemCount: number;
}

export interface ProductState extends LoadingState {
  products: Product[];
  categories: Category[];
  currentProduct: Product | null;
  filters: ProductFilter;
  pagination: {
    page: number;
    size: number;
    totalPages: number;
    totalElements: number;
  };
}

export interface OrderState extends LoadingState {
  orders: Order[];
  currentOrder: Order | null;
  pagination: {
    page: number;
    size: number;
    totalPages: number;
    totalElements: number;
  };
}