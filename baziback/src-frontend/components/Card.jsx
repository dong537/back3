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
        'glass rounded-2xl p-6',
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
    <h3 className={clsx('text-xl font-bold text-white', className)}>
      {children}
    </h3>
  )
}

export function CardDescription({ children, className = '' }) {
  return (
    <p className={clsx('text-gray-400 mt-1', className)}>
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
