import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * 路由守卫：未登录时跳转到 /login，并携带回跳地址
 */
export default function PrivateRoute({ children }) {
  const { isLoggedIn, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    // 可根据需要返回全局 Loading 组件
    return null
  }

  if (!isLoggedIn) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return children
}
