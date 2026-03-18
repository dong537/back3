import React, { useState } from 'react'

/**
 * 响应式图片组件
 * 支持多种格式、懒加载、响应式尺寸
 */
const ResponsiveImage = React.memo(({ 
  src, 
  alt = '图片', 
  width = 'auto', 
  height = 'auto',
  className = '',
  onLoad,
  onError
}) => {
  const [isLoading, setIsLoading] = useState(true)
  const [hasError, setHasError] = useState(false)

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
        className={`bg-gray-200 flex items-center justify-center ${className}`}
        style={{ width, height }}
      >
        <span className="text-gray-500 text-sm">图片加载失败</span>
      </div>
    )
  }

  return (
    <div className={`relative overflow-hidden ${className}`} style={{ width, height }}>
      {isLoading && (
        <div className="absolute inset-0 bg-gray-200 animate-pulse" />
      )}
      
      <picture>
        {/* WebP 格式 - 最优压缩 */}
        <source 
          srcSet={`${src}?w=400&h=300&q=80&fm=webp 400w, ${src}?w=800&h=600&q=80&fm=webp 800w, ${src}?w=1200&h=900&q=80&fm=webp 1200w`}
          type="image/webp"
        />
        
        {/* JPEG 格式 - 兼容性好 */}
        <source 
          srcSet={`${src}?w=400&h=300&q=80 400w, ${src}?w=800&h=600&q=80 800w, ${src}?w=1200&h=900&q=80 1200w`}
          type="image/jpeg"
        />
        
        {/* 原始格式 - 备选 */}
        <img 
          src={src}
          alt={alt}
          width={width}
          height={height}
          loading="lazy"
          decoding="async"
          onLoad={handleLoad}
          onError={handleError}
          className={`w-full h-full object-cover transition-opacity duration-300 ${
            isLoading ? 'opacity-0' : 'opacity-100'
          }`}
        />
      </picture>
    </div>
  )
})

ResponsiveImage.displayName = 'ResponsiveImage'

export default ResponsiveImage
