import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { Compass, Star, Calendar, Brain, User, Home, Menu, X, Sparkles, LogOut, Languages, BarChart3, Users, Trophy } from 'lucide-react'
import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { setLanguage } from '../i18n'
import { useAuth } from '../context/AuthContext'
import { useTheme } from '../context/ThemeContext'
import { logger } from '../utils/logger'
import BottomNavigation from './BottomNavigation'

const navItems = [
  { path: '/', icon: Home, key: 'nav.home' },
  { path: '/yijing', icon: Compass, key: 'nav.yijing' },
  { path: '/tarot', icon: Sparkles, key: 'nav.tarot' },
  // { path: '/zodiac', icon: Star, key: 'nav.zodiac' }, // 暂时禁用星座页面
  { path: '/bazi', icon: Calendar, key: 'nav.bazi' },
  { path: '/ai', icon: Brain, key: 'nav.ai' },
  { path: '/dashboard', icon: BarChart3, key: 'nav.dashboard' },
]

export default function Layout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { t, i18n } = useTranslation()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const auth = useAuth()
  const { setTheme } = useTheme()

  useEffect(() => {
    const path = location.pathname.split('/')[1];
    const themeName = ['yijing', 'tarot', /* 'zodiac', */ 'bazi', 'ai'].includes(path) ? path : 'default';
    setTheme(themeName);
  }, [location.pathname, setTheme]);

  const handleLogout = () => {
    auth.logout()
    setMobileMenuOpen(false)
    navigate('/login')
  }

  // 注意：不再在Layout中同步localStorage和AuthContext
  // AuthContext的useEffect已经在初始化时处理了localStorage的读取
  // 在这里再次同步会导致无限循环或状态不稳定
  useEffect(() => {
    logger.debug('Layout mounted, auth state:', { 
      user: auth.user?.username, 
      token: !!auth.token, 
      isLoggedIn: auth.isLoggedIn 
    })
  }, [auth.isLoggedIn, auth.user, auth.token])

  return (
    <div className="min-h-screen flex flex-col">
      {/* 顶部导航 - 移动端简化显示 */}
      {location.pathname !== '/' && (
      <header className="glass-dark sticky top-0 z-50 safe-area-top">
        <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-14 md:h-16">
            {/* Logo - 移动端只显示图标 */}
            <Link to="/" className="flex items-center space-x-2 md:space-x-3">
              <div className="w-9 h-9 md:w-10 md:h-10 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center">
                <span className="text-white text-lg md:text-xl">☯</span>
              </div>
              <span className="hidden sm:inline-block text-lg md:text-xl font-bold bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
                {t('app.name')}
              </span>
            </Link>

            {/* 桌面导航 */}
            <div className="hidden md:flex items-center space-x-1">
              {navItems.map(({ path, icon: Icon, key }) => (
                <Link
                  key={path}
                  to={path}
                  className={`flex items-center space-x-2 px-4 py-2 rounded-lg transition-all duration-300 ${
                    location.pathname === path
                      ? 'bg-purple-500/20 text-purple-300'
                      : 'text-gray-400 hover:text-white hover:bg-white/5'
                  }`}
                >
                  <Icon size={18} />
                  <span>{t(key)}</span>
                </Link>
              ))}
            </div>

            {/* 用户按钮 */}
            <div className="hidden md:flex items-center space-x-4">
              {/* 成就系统 */}
              <Link
                to="/achievement"
                className="flex items-center space-x-2 px-3 py-2 rounded-lg text-gray-300 hover:text-white hover:bg-white/5 transition-all duration-300"
              >
                <Trophy size={18} />
                <span>成就</span>
              </Link>
              {/* 邀请好友 */}
              <Link
                to="/referral"
                className="flex items-center space-x-2 px-3 py-2 rounded-lg text-gray-300 hover:text-white hover:bg-white/5 transition-all duration-300"
              >
                <Users size={18} />
                <span>邀请好友</span>
              </Link>
              {/* 语言切换 */}
              <button
                type="button"
                onClick={() => setLanguage(i18n.language === 'en-US' ? 'zh-CN' : 'en-US')}
                className="flex items-center space-x-2 px-3 py-2 rounded-lg text-gray-300 hover:text-white hover:bg-white/5 transition-all duration-300"
                title={i18n.language === 'en-US' ? t('lang.zh') : t('lang.en')}
              >
                <Languages size={18} />
                <span>{i18n.language === 'en-US' ? t('lang.zh') : t('lang.en')}</span>
              </button>
              {/* 调试：显示auth状态 */}
              {/* <div className="text-xs text-gray-500">
                isLoggedIn: {auth.isLoggedIn.toString()}, user: {auth.user ? 'yes' : 'no'}
              </div> */}
              {auth.isLoggedIn ? (
                <div className="flex items-center space-x-3">
                  <div className="flex items-center space-x-2 text-white">
                    <span className="px-2 py-0.5 rounded-lg bg-purple-600/30 text-sm flex items-center space-x-1">
                      <span className="">💎</span>
                      <span>{auth.credits}</span>
                    </span>
                    <button
                      onClick={() => navigate('/self')}
                      className="flex items-center space-x-2 hover:text-purple-300 transition-colors cursor-pointer"
                    >
                      <User size={18} />
                      <span>{auth.user?.username || '用户'}</span>
                    </button>
                  </div>
                  <button
                    type="button"
                    onClick={handleLogout}
                    className="flex items-center space-x-2 px-3 py-2 rounded-lg text-gray-300 hover:text-white hover:bg-white/5 transition-all duration-300"
                  >
                    <LogOut size={18} />
                    <span>{t('nav.logout')}</span>
                  </button>
                </div>
              ) : (
                <Link
                  to="/login"
                  className="flex items-center space-x-2 px-4 py-2 rounded-lg bg-gradient-to-r from-purple-600 to-pink-600 text-white hover:from-purple-500 hover:to-pink-500 transition-all duration-300"
                >
                  <User size={18} />
                  <span>{t('nav.login')}</span>
                </Link>
              )}
            </div>

            {/* 移动端菜单按钮和用户信息 */}
            <div className="md:hidden flex items-center space-x-2">
              {/* 移动端显示积分（如果已登录） */}
              {auth.isLoggedIn && (
                <div className="flex items-center space-x-1 px-2 py-1 rounded-lg bg-purple-600/30">
                  <span className="text-sm">💎</span>
                  <span className="text-sm text-white font-medium">{auth.credits}</span>
                </div>
              )}
            <button
                className="p-2 rounded-lg hover:bg-white/10 active:bg-white/20 transition-colors"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                aria-label="菜单"
            >
                {mobileMenuOpen ? <X size={22} /> : <Menu size={22} />}
            </button>
            </div>
          </div>

          {/* 移动端菜单 */}
          {mobileMenuOpen && (
            <div className="md:hidden py-4 space-y-2">
              {navItems.map(({ path, icon: Icon, key }) => (
                <Link
                  key={path}
                  to={path}
                  onClick={() => setMobileMenuOpen(false)}
                  className={`flex items-center space-x-3 px-4 py-3 rounded-lg transition-all ${
                    location.pathname === path
                      ? 'bg-purple-500/20 text-purple-300'
                      : 'text-gray-400 hover:text-white hover:bg-white/5'
                  }`}
                >
                  <Icon size={20} />
                  <span>{t(key)}</span>
                </Link>
              ))}
              {auth.isLoggedIn ? (
                <>
                  <Link
                    to="/achievement"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-300 hover:text-white hover:bg-white/5 transition-all"
                  >
                    <Trophy size={20} />
                    <span>成就</span>
                  </Link>
                  <Link
                    to="/referral"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-300 hover:text-white hover:bg-white/5 transition-all"
                  >
                    <Users size={20} />
                    <span>邀请好友</span>
                  </Link>
                  <div className="flex items-center space-x-3 px-4 py-3 rounded-lg bg-purple-500/20 text-purple-300 cursor-pointer hover:bg-purple-500/30 transition-colors" onClick={() => {
                    setMobileMenuOpen(false)
                    navigate('/self')
                  }}>
                    <User size={20} />
                    <span>{auth.user?.username || '用户'}</span>
                  </div>
                  <button
                    type="button"
                    onClick={handleLogout}
                    className="w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-300 hover:text-white hover:bg-white/5 transition-all"
                  >
                    <LogOut size={20} />
                    <span>{t('nav.logoutFull')}</span>
                  </button>
                </>
              ) : (
                <Link
                  to="/login"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center space-x-3 px-4 py-3 rounded-lg bg-gradient-to-r from-purple-600 to-pink-600 text-white"
                >
                  <User size={20} />
                  <span>{t('nav.login')}</span>
                </Link>
              )}
            </div>
          )}
        </nav>
      </header>
      )}

      {/* 主内容区 - 为底部导航栏留出空间，移动端增加更多间距 */}
      <main className="flex-1 pb-20 md:pb-16">
        <Outlet />
      </main>

      {/* 底部导航栏 - 所有页面都显示，登录页除外 */}
      {location.pathname !== '/login' && <BottomNavigation />}

      {/* 底部版权信息 - 仅在非登录页且非首页显示 */}
      {location.pathname !== '/' && location.pathname !== '/login' && (
        <footer className="glass-dark py-6 mt-auto safe-area-bottom hidden md:block">
          <div className="max-w-7xl mx-auto px-4 text-center text-gray-500 text-sm">
            <p>© 2025 {t('app.footerTitle')}</p>
            <p className="mt-1">{t('app.footerNote')}</p>
          </div>
        </footer>
      )}
    </div>
  )
}
