import { useEffect, useState } from 'react'
import { CheckCircle, XCircle, Info, AlertTriangle, X } from 'lucide-react'
import { getStoredUiLocale, translateUiText } from '../utils/runtimeLocale'

// Toast 管理器
class ToastManager {
  constructor() {
    this.toasts = []
    this.listeners = []
  }

  subscribe(listener) {
    this.listeners.push(listener)
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener)
    }
  }

  notify() {
    this.listeners.forEach(listener => listener([...this.toasts]))
  }

  show(message, type = 'info', duration = 3000) {
    const id = Date.now().toString()
    const safeMessage =
      message == null
        ? ''
        : typeof message === 'string'
          ? message
          : message instanceof Error
            ? (message.message || 'Error')
            : (() => {
                try {
                  return JSON.stringify(message)
                } catch {
                  return String(message)
                }
              })()

    const localizedMessage = translateUiText(safeMessage, getStoredUiLocale())

    this.toasts.push({ id, message: localizedMessage, type, duration })
    this.notify()
    return id
  }

  remove(id) {
    this.toasts = this.toasts.filter(t => t.id !== id)
    this.notify()
  }

  success(message, duration) {
    return this.show(message, 'success', duration)
  }

  error(message, duration) {
    return this.show(message, 'error', duration)
  }

  info(message, duration) {
    return this.show(message, 'info', duration)
  }

  warning(message, duration) {
    return this.show(message, 'warning', duration)
  }
}

export const toast = new ToastManager()

const toastTypes = {
  success: { icon: CheckCircle, color: 'text-green-400 bg-green-400/20 border-green-400/30' },
  error: { icon: XCircle, color: 'text-red-400 bg-red-400/20 border-red-400/30' },
  info: { icon: Info, color: 'text-blue-400 bg-blue-400/20 border-blue-400/30' },
  warning: { icon: AlertTriangle, color: 'text-yellow-400 bg-yellow-400/20 border-yellow-400/30' }
}

export default function Toast({ message, type = 'info', duration = 3000, onClose }) {
  const [isVisible, setIsVisible] = useState(true)
  const Icon = toastTypes[type]?.icon || toastTypes.info.icon
  const colorClass = toastTypes[type]?.color || toastTypes.info.color

  useEffect(() => {
    if (duration > 0) {
      const timer = setTimeout(() => {
        setIsVisible(false)
        setTimeout(() => onClose?.(), 300)
      }, duration)
      return () => clearTimeout(timer)
    }
  }, [duration, onClose])

  if (!isVisible) return null

  return (
    <div className={`animate-slide-in w-full ${colorClass} rounded-lg border p-4 shadow-lg sm:min-w-[300px] sm:max-w-md`}>
      <div className="flex items-start space-x-3">
        <Icon size={20} className="flex-shrink-0 mt-0.5" />
        <div className="flex-1">
          <p className="text-sm font-medium">{message}</p>
        </div>
        <button
          onClick={() => {
            setIsVisible(false)
            setTimeout(() => onClose?.(), 300)
          }}
          className="flex-shrink-0 hover:opacity-70 transition"
        >
          <X size={16} />
        </button>
      </div>
    </div>
  )
}

// Toast 容器组件
export function ToastContainer() {
  const [toasts, setToasts] = useState([])

  useEffect(() => {
    const unsubscribe = toast.subscribe(setToasts)
    return unsubscribe
  }, [])

  return (
    <div className="fixed left-4 right-4 top-4 z-50 space-y-2 sm:left-auto sm:right-4 sm:w-auto">
      {toasts.map(toastItem => (
        <Toast
          key={toastItem.id}
          message={toastItem.message}
          type={toastItem.type}
          duration={toastItem.duration}
          onClose={() => toast.remove(toastItem.id)}
        />
      ))}
    </div>
  )
}
