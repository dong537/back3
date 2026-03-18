import { clsx } from 'clsx'

export default function Input({ 
  label,
  error,
  className = '',
  ...props 
}) {
  return (
    <div className="space-y-2">
      {label && (
        <label className="block text-sm font-medium text-gray-300">
          {label}
        </label>
      )}
      <input
        className={clsx(
          'w-full px-4 py-3 rounded-lg',
          'bg-white/5 border border-white/10',
          'text-white placeholder-gray-500',
          'focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent',
          'transition-all duration-300',
          error && 'border-red-500',
          className
        )}
        {...props}
      />
      {error && (
        <p className="text-sm text-red-400">{error}</p>
      )}
    </div>
  )
}

export function Textarea({ 
  label,
  error,
  className = '',
  rows = 4,
  ...props 
}) {
  return (
    <div className="space-y-2">
      {label && (
        <label className="block text-sm font-medium text-gray-300">
          {label}
        </label>
      )}
      <textarea
        rows={rows}
        className={clsx(
          'w-full px-4 py-3 rounded-lg resize-none',
          'bg-white/5 border border-white/10',
          'text-white placeholder-gray-500',
          'focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent',
          'transition-all duration-300',
          error && 'border-red-500',
          className
        )}
        {...props}
      />
      {error && (
        <p className="text-sm text-red-400">{error}</p>
      )}
    </div>
  )
}

export function Select({ 
  label,
  error,
  options = [],
  className = '',
  ...props 
}) {
  return (
    <div className="space-y-2">
      {label && (
        <label className="block text-sm font-medium text-gray-300">
          {label}
        </label>
      )}
      <select
        className={clsx(
          'w-full px-4 py-3 rounded-lg',
          'bg-white/5 border border-white/10',
          'text-white',
          'focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent',
          'transition-all duration-300',
          error && 'border-red-500',
          className
        )}
        {...props}
      >
        {options.map(({ value, label }) => (
          <option key={value} value={value} className="bg-gray-800">
            {label}
          </option>
        ))}
      </select>
      {error && (
        <p className="text-sm text-red-400">{error}</p>
      )}
    </div>
  )
}
