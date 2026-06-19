# ShopSphere Frontend

React + Vite storefront for the e-commerce microservices platform.

## Prerequisites

- Node.js 18+
- Backend API Gateway running on `http://localhost:8080`

## Quick Start

```bash
cd frontend
npm install
npm run dev
```

The app runs at [http://localhost:5173](http://localhost:5173).

API requests are proxied to the gateway via Vite (`/api` → `http://localhost:8080`).

## Backend Setup

Start infrastructure and microservices before using the frontend:

```bash
# From repo root — start infrastructure (Postgres, Redis, Kafka, etc.)
docker-compose -f docker-compose-zipkin.yml up -d

# Start services (Eureka → Config → Gateway → business services)
# See ../README.md for full startup order
```

The API Gateway must be reachable at port **8080**.

## Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start dev server with HMR |
| `npm run build` | Production build to `dist/` |
| `npm run preview` | Preview production build |
| `npm run lint` | Run ESLint |

## Features

- Product catalog with search, filters, and sorting
- Shopping cart with live badge sync
- Checkout with payment processing
- Order history and tracking
- User authentication (login / register)
- Personalized recommendations
- Admin dashboard (read-only)

## Project Structure

```
src/
  api/          # Axios API clients per microservice
  components/   # Shared UI components
  pages/        # Route pages
  store/        # Zustand state (auth, cart)
  utils/        # Helpers and formatters
```

## Environment Notes

- Dev: Vite proxy handles `/api` — no CORS issues
- Production preview: API Gateway CORS allows `http://localhost:5173`
- For custom API URL, update `frontend/vite.config.js` proxy target
