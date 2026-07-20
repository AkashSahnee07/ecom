import { Navigate, useParams, useSearchParams } from 'react-router-dom';

export default function CollectionPage() {
  const { slug } = useParams();
  const [searchParams] = useSearchParams();
  const next = new URLSearchParams(searchParams);

  if (slug === 'new') next.set('sort', 'id,desc');
  if (slug === 'featured') next.set('featured', 'true');

  return <Navigate to={`/shop?${next.toString()}`} replace />;
}
