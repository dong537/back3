import React, { useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { resolvePageLocale } from '../utils/displayText'

const IMAGE_COPY = {
  'zh-CN': {
    alt: '图片',
    loadFailed: '图片加载失败',
  },
  'en-US': {
    alt: 'Image',
    loadFailed: 'Failed to load image',
  },
}

const ResponsiveImage = React.memo(
  ({
    src,
    alt,
    width = 'auto',
    height = 'auto',
    className = '',
    onLoad,
    onError,
  }) => {
    const { i18n } = useTranslation()
    const locale = resolvePageLocale(i18n.language)
    const copy = IMAGE_COPY[locale]
    const [isLoading, setIsLoading] = useState(true)
    const [hasError, setHasError] = useState(false)

    const resolvedAlt = useMemo(() => alt || copy.alt, [alt, copy.alt])

    const handleLoad = () => {
      setIsLoading(false)
      onLoad?.()
    }

    const handleError = () => {
      setIsLoading(false)
      setHasError(true)
      onError?.()
    }

    if (hasError) {
      return (
        <div
          className={`flex items-center justify-center bg-gray-200 ${className}`}
          style={{ width, height }}
        >
          <span className="text-sm text-gray-500">{copy.loadFailed}</span>
        </div>
      )
    }

    return (
      <div
        className={`relative overflow-hidden ${className}`}
        style={{ width, height }}
      >
        {isLoading && (
          <div className="absolute inset-0 animate-pulse bg-gray-200" />
        )}

        <picture>
          <source
            srcSet={`${src}?w=400&h=300&q=80&fm=webp 400w, ${src}?w=800&h=600&q=80&fm=webp 800w, ${src}?w=1200&h=900&q=80&fm=webp 1200w`}
            type="image/webp"
          />

          <source
            srcSet={`${src}?w=400&h=300&q=80 400w, ${src}?w=800&h=600&q=80 800w, ${src}?w=1200&h=900&q=80 1200w`}
            type="image/jpeg"
          />

          <img
            src={src}
            alt={resolvedAlt}
            width={width}
            height={height}
            loading="lazy"
            decoding="async"
            onLoad={handleLoad}
            onError={handleError}
            className={`h-full w-full object-cover transition-opacity duration-300 ${
              isLoading ? 'opacity-0' : 'opacity-100'
            }`}
          />
        </picture>
      </div>
    )
  }
)

ResponsiveImage.displayName = 'ResponsiveImage'

export default ResponsiveImage
