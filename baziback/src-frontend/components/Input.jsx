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
        <label className="block text-sm font-medium text-[#cdb79a]">
          {label}
        </label>
      )}
      <input
        className={clsx(
          'w-full rounded-[18px] border px-4 py-3',
          'border-white/10 bg-white/[0.04]',
          'text-[#f4ece1] placeholder-[#8f7b66]',
          'focus:border-transparent focus:outline-none focus:ring-2 focus:ring-[#a34224]/30',
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
        <label className="block text-sm font-medium text-[#cdb79a]">
          {label}
        </label>
      )}
      <textarea
        rows={rows}
        className={clsx(
          'w-full resize-none rounded-[18px] border px-4 py-3',
          'border-white/10 bg-white/[0.04]',
          'text-[#f4ece1] placeholder-[#8f7b66]',
          'focus:border-transparent focus:outline-none focus:ring-2 focus:ring-[#a34224]/30',
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
        <label className="block text-sm font-medium text-[#cdb79a]">
          {label}
        </label>
      )}
      <select
        className={clsx(
          'w-full rounded-[18px] border px-4 py-3',
          'border-white/10 bg-white/[0.04]',
          'text-[#f4ece1]',
          'focus:border-transparent focus:outline-none focus:ring-2 focus:ring-[#a34224]/30',
          'transition-all duration-300',
          error && 'border-red-500',
          className
        )}
        {...props}
      >
        {options.map(({ value, label }) => (
          <option key={value} value={value} className="bg-[#140f0f]">
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
