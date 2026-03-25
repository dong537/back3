/**
 * 移动端优化组件
 * 用于包装需要移动端优化的内容
 */

export function MobileContainer({ children, className = '' }) {
  return (
    <div className={`w-full max-w-full mx-auto px-4 sm:px-6 lg:px-8 ${className}`}>
      {children}
    </div>
  )
}

export function MobileCard({ children, className = '' }) {
  return (
    <div className={`
      bg-white rounded-lg shadow-sm
      p-4 sm:p-6
      ${className}
    `}>
      {children}
    </div>
  )
}

export function MobileButton({ children, className = '', ...props }) {
  return (
    <button
      className={`
        min-h-[44px] min-w-[44px]
        px-4 py-2 sm:px-6 sm:py-3
        text-sm sm:text-base
        ${className}
      `}
      {...props}
    >
      {children}
    </button>
  )
}

export function MobileText({ children, className = '', size = 'base' }) {
  const sizeClasses = {
    xs: 'text-xs sm:text-sm',
    sm: 'text-sm sm:text-base',
    base: 'text-base sm:text-lg',
    lg: 'text-lg sm:text-xl',
    xl: 'text-xl sm:text-2xl',
  }
  
  return (
    <div className={`${sizeClasses[size]} ${className}`}>
      {children}
    </div>
  )
}
