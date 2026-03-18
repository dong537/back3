import { Component } from 'react'
import { AlertTriangle } from 'lucide-react'
import Button from './Button'
import { logger } from '../utils/logger'

class ErrorBoundary extends Component {
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
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center p-4">
          <div className="max-w-md w-full glass rounded-xl p-8 text-center">
            <AlertTriangle className="w-16 h-16 text-red-400 mx-auto mb-4" />
            <h2 className="text-2xl font-bold mb-2">出现错误</h2>
            <p className="text-gray-400 mb-6">
              {this.state.error?.message || '发生了未知错误'}
            </p>
            <div className="flex flex-col space-y-3">
              <Button onClick={this.handleReset} className="w-full">
                重试
              </Button>
              <Button
                onClick={() => window.location.reload()}
                variant="secondary"
                className="w-full"
              >
                刷新页面
              </Button>
            </div>
            {process.env.NODE_ENV === 'development' && (
              <details className="mt-6 text-left">
                <summary className="cursor-pointer text-sm text-gray-400 mb-2">
                  错误详情
                </summary>
                <pre className="text-xs bg-black/20 p-4 rounded overflow-auto max-h-48">
                  {this.state.error?.stack}
                </pre>
              </details>
            )}
          </div>
        </div>
      )
    }

    return this.props.children
  }
}

export default ErrorBoundary
