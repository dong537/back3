import { Link, useLocation } from 'react-router-dom'
import { Home, Heart, Zap, Trophy, User } from 'lucide-react'
import { useTranslation } from 'react-i18next'

const bottomNavItems = [
  { 
    path: '/', 
    icon: Home, 
    key: 'nav.home',
    label: '首页'
  },
  { 
    path: '/favorites', 
    icon: Heart, 
    key: 'nav.favorites',
    label: '我的收藏'
  },
  { 
    path: '/ai', 
    icon: Zap, 
    key: 'nav.ask',
    label: '问'
  },
  { 
    path: '/achievement', 
    icon: Trophy, 
    key: 'nav.achievement',
    label: '我的成就'
  },
  { 
    path: '/self', 
    icon: User, 
    key: 'nav.self',
    label: '我的'
  },
]

export default function BottomNavigation() {
  const location = useLocation()
  const { t } = useTranslation()

  // 判断当前路径是否匹配（支持子路径）
  const isActive = (path) => {
    if (path === '/') {
      return location.pathname === '/'
    }
    // 对于其他路径，检查是否以该路径开头
    return location.pathname.startsWith(path)
  }

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-[100] bg-white border-t border-gray-200 safe-area-bottom shadow-lg md:shadow-xl">
      <div className="max-w-7xl mx-auto">
        <div className="flex items-center justify-around h-16 md:h-14 px-1 md:px-2">
          {bottomNavItems.map(({ path, icon: Icon, key, label }) => {
            const active = isActive(path)
            return (
              <Link
                key={path}
                to={path}
                className={`
                  flex flex-col items-center justify-center 
                  flex-1 h-full
                  transition-all duration-200
                  ${active 
                    ? 'text-purple-600' 
                    : 'text-gray-500'
                  }
                `}
              >
                {/* 图标容器 */}
                <div className={`
                  w-10 h-10 md:w-9 md:h-9 rounded-lg
                  flex items-center justify-center
                  transition-all duration-200 mb-0.5 md:mb-1
                  ${active 
                    ? 'bg-purple-600' 
                    : 'bg-transparent'
                  }
                `}>
                  {/* 首页：紫色背景 + 白色圆点 */}
                  {active && path === '/' ? (
                    <div className="w-2 h-2 rounded-full bg-white"></div>
                  ) : active && path === '/ai' ? (
                    /* AI/问：紫色背景 + 白色闪电图标 */
                    <Icon size={18} className="text-white" strokeWidth={2.5} />
                  ) : (
                    /* 其他：灰色图标 */
                    <Icon 
                      size={20} 
                      className={active ? 'text-purple-600' : 'text-gray-500'} 
                      strokeWidth={active ? 2.5 : 2}
                    />
                  )}
                </div>
                {/* 文字标签 */}
                <span className={`
                  text-xs md:text-[11px]
                  ${active ? 'font-medium text-purple-600' : 'font-normal text-gray-500'}
                `}>
                  {label}
                </span>
              </Link>
            )
          })}
        </div>
      </div>
    </nav>
  )
}
