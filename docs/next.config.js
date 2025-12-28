/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  images: {
    unoptimized: true,
  },
  trailingSlash: true,
  output: 'export',
  distDir: 'out',
  basePath: process.env.NODE_ENV === 'production' ? '/ecommerce-docs' : '',
  assetPrefix: process.env.NODE_ENV === 'production' ? '/ecommerce-docs/' : '',
}

module.exports = nextConfig