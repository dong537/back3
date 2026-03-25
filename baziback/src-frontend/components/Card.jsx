import { clsx } from 'clsx'

export default function Card({ 
  children, 
  className = '', 
  hover = true,
  glow = false,
  ...props 
}) {
  return (
    <div 
      className={clsx(
        'glass rounded-[28px] p-6 text-[#f4ece1]',
        hover && 'card-hover',
        glow && 'animate-glow',
        className
      )}
      {...props}
    >
      {children}
    </div>
  )
}

export function CardHeader({ children, className = '' }) {
  return (
    <div className={clsx('mb-4', className)}>
      {children}
    </div>
  )
}

export function CardTitle({ children, className = '' }) {
  return (
    <h3 className={clsx('text-xl font-semibold tracking-[0.02em] text-[#f8eee2]', className)}>
      {children}
    </h3>
  )
}

export function CardDescription({ children, className = '' }) {
  return (
    <p className={clsx('mt-1 text-sm leading-6 text-[#bdaa94]', className)}>
      {children}
    </p>
  )
}

export function CardContent({ children, className = '' }) {
  return (
    <div className={clsx(className)}>
      {children}
    </div>
  )
}
