# LUMEN Frontend Rebuild — Design Spec

**Date:** 2026-07-08  
**Status:** Approved for planning  
**Approach:** B — New editorial UI shell + shared API layer  
**Brand:** LUMEN · Fashion / lifestyle luxury · Editorial black & white

---

## 1. Goals

Rebuild the existing React/Vite ecommerce frontend as a **production-ready** storefront and admin for **LUMEN**: a fashion/lifestyle luxury house with an editorial (magazine) black-and-white aesthetic.

Preserve working backend contracts (API clients + Zustand). Rewrite UI structure, visual system, IA, and page compositions. Fix known auth and Vite proxy bugs so write flows work in local dev.

**Non-goals (v1):** wishlist, multi-currency, CMS, TypeScript migration, Tailwind introduction, greenfield app replace.

---

## 2. Decisions (locked)

| Decision | Choice |
|----------|--------|
| Aesthetic | Editorial luxury B&W (fashion magazine) |
| Scope | Full rebuild of storefront **and** admin |
| Persona | Fashion / lifestyle luxury shopper |
| Brand | **LUMEN** (rebrand from ShopSphere) |
| Build approach | Approach B: new UI shell; keep `api/*` + stores |
| URLs | New paths (`/shop`, `/bag`, `/account`) + redirects from old |
| Theme flexibility | CSS custom properties + documented token map; no hard-coded brand colors in JSX |
| CSS approach | Token-driven plain CSS (no Tailwind in v1) |

---

## 3. Brand & visual system

### 3.1 Palette (strict monochrome)

| Token | Default | Role |
|-------|---------|------|
| `--color-ink` | `#0A0A0A` | Primary text, filled CTAs, inverse surfaces |
| `--color-paper` | `#FAFAFA` | Page background |
| `--color-soft` | `#F0F0F0` | Subtle surfaces, zebra rows |
| `--color-mid` | `#8A8A8A` | Secondary text, captions |
| `--color-line` | `#E5E5E5` | Borders, dividers |
| `--color-inverse-text` | `#FAFAFA` | Text on ink surfaces |

Status (success / error / warning) uses **tone + icon + label**, not chromatic accents (e.g. filled black check, outlined warning). Optional future: `--color-status-*` tokens for non-B&W themes without changing components.

### 3.2 Typography

| Token | Default | Use |
|-------|---------|-----|
| `--font-display` | Playfair Display | Heroes, product names, section titles |
| `--font-body` | Inter | Nav, body, forms, prices, admin tables |

Logo: uppercase `LUMEN` with wide tracking; optional thin vertical rule mark.

### 3.3 Shape & motion

- Radius: `--radius` default `2px` (editorial, sharp; overridable via token)
- Motion: restrained fade/slide only; no glassmorphism, ambient orbs, or colored glows
- Product imagery: full-bleed where possible; minimal overlay UI

### 3.4 Theme flexibility

1. All colors, fonts, radii, spacing live in `:root` in `index.css` (or `styles/tokens.css`).
2. Components and pages reference **only** CSS variables / utility classes built from tokens.
3. `src/theme/tokens.js` exports a documented mirror of token names/defaults for future runtime theme swap or design docs — not required for v1 UI logic.
4. Changing LUMEN to another palette later = update tokens (+ fonts if needed), not rewrite pages.

---

## 4. Information architecture

### 4.1 Storefront routes

| Path | Page | Notes |
|------|------|-------|
| `/` | Home | Editorial hero, collections, featured / new |
| `/shop` | Catalog | Filters + lookbook grid |
| `/shop/:id` | Product detail | Gallery + details + reviews + add to bag |
| `/collections/:slug` | Collection landing | v1: slug maps to known presets (`new` → recent, `featured` → featured, numeric/id → categoryId). No separate collections microservice. |
| `/bag` | Cart | Was `/cart` |
| `/checkout` | Checkout | Protected |
| `/orders` | Order list | Protected |
| `/orders/:id` | Order detail | Protected |
| `/account` | Profile | Protected; was `/profile` |
| `/login` | Login | Editorial minimal form |
| `/register` | Register | Editorial minimal form |

### 4.2 Redirects (compatibility)

| From | To |
|------|-----|
| `/products` | `/shop` (preserve query string) |
| `/products/:id` | `/shop/:id` |
| `/cart` | `/bag` |
| `/profile` | `/account` |

### 4.3 Admin routes

| Path | Purpose |
|------|---------|
| `/admin` | Overview (stats, recent orders, low stock) |
| `/admin/products` | Product management (list / create / edit via existing product APIs) |
| `/admin/orders` | Order list + status actions if APIs allow |

Admin uses `AdminLayout` (sidebar). Same tokens; denser tables and controls.

### 4.4 Navigation

**Storefront header:** `LUMEN` · Shop · category links (from categories API when available) · Search · Bag (count) · Account / Login  

**Footer:** brand line, shop links, account links, sparse legal placeholder.

**Admin sidebar:** Overview · Products · Orders · back to storefront.

---

## 5. Architecture

```
frontend/src/
  api/           # KEEP — axios clients (fix endpoints where wrong)
  store/         # KEEP — auth + cart (fix auth field mapping)
  theme/         # NEW — tokens.js documentation / future theme hook
  styles/        # NEW or evolve — tokens.css + global utilities
  layouts/       # NEW — AppLayout, AdminLayout
  components/    # REWRITE — Button, Input, Header, Footer, ProductCard, etc.
  pages/         # REWRITE — all storefront + admin pages under LUMEN IA
  App.jsx        # UPDATE — routes, layouts, toaster theme
```

**Stack (unchanged):** React 19, Vite 8, React Router 7, Zustand, Axios, Lucide, react-hot-toast, Recharts (admin charts in B&W).

### 5.1 Layouts

- `AppLayout`: Header + `<Outlet />` + Footer; used by all storefront routes.
- `AdminLayout`: Sidebar + top bar + `<Outlet />`; admin-only, guarded by `ProtectedRoute adminOnly`.

### 5.2 Shared components (minimum)

| Component | Responsibility |
|-----------|----------------|
| `Header` | Nav, search, bag badge, account menu |
| `Footer` | Editorial footer |
| `ProductCard` | Lookbook card: image, title (serif), price |
| `Button` | Primary (ink fill), secondary (outline), ghost |
| `Input` / `Select` | Forms with token borders/focus rings |
| `EmptyState` | Editorial empty |
| `Loader` / skeletons | Grayscale shimmer |
| `ProtectedRoute` | Auth + admin gate (keep behavior) |
| `StatusBadge` | Monochrome status chips |
| `StarRating` | Monochrome stars |

### 5.3 Data layer fixes (required for production readiness)

1. **Login request:** send `{ usernameOrEmail, password }` (not `{ email, password }`).
2. **Login response:** read `accessToken` (and user payload as returned); persist under existing localStorage keys used by axios interceptor.
3. **Vite proxy:** configure so browser `Origin` on POST/PUT/PATCH does not yield 403 in dev (e.g. strip/rewrite Origin or equivalent proxy option) so writes reach the gateway.
4. **Categories:** align client with platform path `/api/categories` (not `/api/products/categories` if that is wrong); degrade gracefully if empty.
5. Keep all other existing `api/*.js` modules unless a path is proven wrong during implementation.

### 5.4 State

- `auth.store`: session user + token; fix login mapping; role `ADMIN` for admin routes.
- `cart.store`: bag line items via cart API; strip rename UI-only (`bag` routes / copy).

---

## 6. Page compositions

### Home
Full-bleed ink or image hero with `LUMEN` wordmark + primary CTA to Shop. Horizontal or grid collection strips. Featured / new product lookbook. Sparse closing CTA band (paper vs ink contrast).

### Shop
Sticky or top filter rail (category, price, sort, search). Responsive lookbook grid (2–4 columns). Pagination. Empty/loading via shared components.

### Product detail
Large gallery (left / top on mobile). Title (display font), price, brand, stock. Quantity + Add to bag. Reviews section. Related products if API available.

### Bag & Checkout
Quiet tables/lists; clear totals. Checkout: shipping + payment fields as current APIs require; order summary side panel. Success → order detail.

### Orders / Account
Clean lists and forms; no marketplace chrome.

### Admin Overview
Monochrome stat cards; recent orders table; low-stock list; refresh action. Charts (if kept) in grayscale.

### Admin Products / Orders
Tables + drawer or dedicated forms for create/edit using existing product/order APIs. Confirm destructive actions.

---

## 7. Error, empty, loading

- **Loading:** centered spinner or skeleton cards using `--color-soft` shimmer.
- **Empty:** short serif or medium title + one CTA (e.g. “Continue shopping”).
- **Errors:** toast in B&W; inline field errors under inputs; page-level alert using ink/line borders.
- **Auth gate:** redirect to `/login` with return path when possible.

---

## 8. Production readiness checklist

- [ ] Tokenized theme; no indigo/glass leftovers in CSS or JSX
- [ ] Brand copy/logos say LUMEN
- [ ] Routes + redirects as specified
- [ ] Auth login/register works against gateway DTOs
- [ ] Dev proxy allows mutating HTTP methods
- [ ] Responsive: mobile nav, shop grid, PDP stack
- [ ] Accessible basics: focus states, labels, aria on icon buttons
- [ ] Admin overview + products + orders usable
- [ ] `npm run build` succeeds; lint clean on new/changed files where practical

---

## 9. Out of scope (explicit)

- Wishlist, gift cards, CMS, A/B personalization UI beyond existing recommendations API
- TypeScript rewrite
- Tailwind / UI kit adoption
- Backend service or DTO changes (frontend adapts to existing contracts)
- Replacing axios/Zustand

---

## 10. Success criteria

A user can browse an editorial LUMEN storefront in black and white, add to bag, check out (with backend up), view orders/account, and an admin can manage overview/products/orders — all on the existing microservice APIs, with theme tokens swappable for a future retheme without rewriting page structure.
