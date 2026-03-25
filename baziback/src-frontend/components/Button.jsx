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
    primary:
      'border border-[#f3d8a8]/10 bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] text-[#fff7eb] shadow-[0_18px_40px_rgba(163,66,36,0.26)] hover:brightness-105',
    secondary:
      'border border-white/10 bg-white/[0.04] text-[#f4ece1] hover:border-[#d0a85b]/25 hover:bg-white/[0.07]',
    outline:
      'border border-[#d0a85b]/40 text-[#dcb86f] hover:bg-[#d0a85b]/10',
    ghost: 'border border-transparent text-[#c7b6a2] hover:bg-white/[0.05] hover:text-[#f4ece1]',
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
        'flex items-center justify-center space-x-2 rounded-[18px] font-medium tracking-[0.02em] transition-all duration-300',
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
