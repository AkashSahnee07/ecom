const FALLBACK_IMAGE =
  "data:image/svg+xml;utf8," +
  encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="800" height="1000" viewBox="0 0 800 1000">
  <rect width="800" height="1000" fill="#111111"/>
  <g fill="none" stroke="#f5f5f5" stroke-width="10">
    <ellipse cx="400" cy="520" rx="160" ry="230"/>
    <path d="M290 390c30-70 190-70 220 0"/>
    <path d="M250 750h300"/>
  </g>
  <text x="400" y="850" text-anchor="middle" fill="#f5f5f5" font-family="Arial" font-size="38" letter-spacing="2">
    BLACK POTTERY
  </text>
</svg>
`);

export const getSafeImage = (src) => src || FALLBACK_IMAGE;
export const getFallbackImage = () => FALLBACK_IMAGE;
