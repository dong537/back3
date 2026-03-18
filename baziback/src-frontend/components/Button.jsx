import { clsx } from 'clsx'

export default function Button({ 
  children, 
  variant = 'primary', 
  size = 'md',
  loading = false,
  disabled = false,
  className = '',
  ...props 
}) {
  const variants = {
    primary: 'bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-500 hover:to-pink-500 text-white',
    secondary: 'bg-white/10 hover:bg-white/20 text-white',
    outline: 'border border-purple-500 text-purple-400 hover:bg-purple-500/10',
    ghost: 'hover:bg-white/10 text-gray-300',
  }

  const sizes = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2',
    lg: 'px-6 py-3 text-lg',
  }

  return (
    <button
      disabled={disabled || loading}
      className={clsx(
        'rounded-lg font-medium transition-all duration-300 flex items-center justify-center space-x-2',
        'disabled:opacity-50 disabled:cursor-not-allowed',
        'btn-glow tap-highlight',
        'active:scale-95',
        'min-h-[44px] md:min-h-0', // 移动端最小触摸目标
        variants[variant],
        sizes[size],
        className
      )}
      {...props}
    >
      {loading && (
        <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
        </svg>
      )}
      {children}
    </button>
  )
}
