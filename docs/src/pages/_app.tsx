import type { AppProps } from 'next/app'
import { Inter, JetBrains_Mono } from 'next/font/google'
import '@/styles/globals.css'
import Layout from '@/components/Layout'
import { useRouter } from 'next/router'
import { useEffect } from 'react'

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-inter',
})

const jetbrainsMono = JetBrains_Mono({
  subsets: ['latin'],
  variable: '--font-jetbrains-mono',
})

export default function App({ Component, pageProps }: AppProps) {
  const router = useRouter()

  useEffect(() => {
    // Add any global analytics or tracking here
    const handleRouteChange = (url: string) => {
      // Track page views
      console.log('Page view:', url)
    }

    router.events.on('routeChangeComplete', handleRouteChange)
    return () => {
      router.events.off('routeChangeComplete', handleRouteChange)
    }
  }, [router.events])

  return (
    <div className={`${inter.variable} ${jetbrainsMono.variable} font-sans`}>
      <Layout>
        <Component {...pageProps} />
      </Layout>
    </div>
  )
}