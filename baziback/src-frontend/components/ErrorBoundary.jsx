import { Component } from 'react'
import { AlertTriangle } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Button from './Button'
import { resolvePageLocale } from '../utils/displayText'
import { logger } from '../utils/logger'

const ERROR_BOUNDARY_COPY = {
  'zh-CN': {
    title: '页面出现错误',
    unknownError: '发生了未知错误',
    retry: '重试',
    refresh: '刷新页面',
    details: '错误详情',
  },
  'en-US': {
    title: 'Something went wrong',
    unknownError: 'An unknown error occurred',
    retry: 'Try again',
    refresh: 'Refresh page',
    details: 'Error details',
  },
}

class LocalizedErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error }
  }

  componentDidCatch(error, errorInfo) {
    logger.error('Error caught by boundary:', error, errorInfo)
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null })
  }

  render() {
    const { children, copy } = this.props

    if (!this.state.hasError) {
      return children
    }

    return (
      <div className="flex min-h-screen items-center justify-center p-4">
        <div className="glass w-full max-w-md rounded-xl p-8 text-center">
          <AlertTriangle className="mx-auto mb-4 h-16 w-16 text-red-400" />
          <h2 className="mb-2 text-2xl font-bold">{copy.title}</h2>
          <p className="mb-6 text-gray-400">
            {this.state.error?.message || copy.unknownError}
          </p>
          <div className="flex flex-col space-y-3">
            <Button onClick={this.handleReset} className="w-full">
              {copy.retry}
            </Button>
            <Button
              onClick={() => window.location.reload()}
              variant="secondary"
              className="w-full"
            >
              {copy.refresh}
            </Button>
          </div>
          {process.env.NODE_ENV === 'development' && (
            <details className="mt-6 text-left">
              <summary className="mb-2 cursor-pointer text-sm text-gray-400">
                {copy.details}
              </summary>
              <pre className="max-h-48 overflow-auto rounded bg-black/20 p-4 text-xs">
                {this.state.error?.stack}
              </pre>
            </details>
          )}
        </div>
      </div>
    )
  }
}

export default function ErrorBoundary(props) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = ERROR_BOUNDARY_COPY[locale]

  return <LocalizedErrorBoundary {...props} copy={copy} />
}
