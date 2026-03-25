import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

export default function DashboardPage() {
  const navigate = useNavigate()

  useEffect(() => {
    // 重定向到个人中心
    navigate('/self', { replace: true })
  }, [navigate])

  return null
}
