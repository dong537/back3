import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import {
  Compass,
  Calendar,
  Brain,
  User,
  Home,
  Menu,
  X,
  Sparkles,
  LogOut,
  BarChart3,
  Users,
  Trophy,
} from 'lucide-react'
import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useTheme } from '../context/ThemeContext'
import { logger } from '../utils/logger'
import BottomNavigation from './BottomNavigation'
import useAppLocale from '../hooks/useAppLocale'
import LanguageToggleButton from './LanguageToggleButton'

const LAYOUT_COPY = {
  'zh-CN': {
    appName: '天机明理',
    nav: {
      home: '首页',
      yijing: '易经',
      tarot: '塔罗',
      bazi: '八字',
      ai: 'AI',
      dashboard: '工作台',
      achievement: '成就',
      invite: '邀请好友',
      login: '登录',
      logout: '退出',
      logoutFull: '退出登录',
      user: '用户',
    },
    footerTitle: '天机明理 · 易经 · 星座 · 八字',
    footerNote: '内容仅供娱乐与文化参考，请理性看待。',
    menuAria: '菜单',
  },
  'en-US': {
    appName: 'Tianji Mingli',
    nav: {
      home: 'Home',
      yijing: 'Yijing',
      tarot: 'Tarot',
      bazi: 'Bazi',
      ai: 'AI',
      dashboard: 'Workspace',
      achievement: 'Achievements',
      invite: 'Invite Friends',
      login: 'Login',
      logout: 'Logout',
      logoutFull: 'Sign out',
      user: 'User',
    },
    footerTitle: 'Tianji Mingli · Yijing · Zodiac · Bazi',
    footerNote: 'For entertainment and cultural reference only.',
    menuAria: 'Menu',
  },
}

const navItems = [
  { path: '/', icon: Home, id: 'home' },
  { path: '/yijing', icon: Compass, id: 'yijing' },
  { path: '/tarot', icon: Sparkles, id: 'tarot' },
  { path: '/bazi', icon: Calendar, id: 'bazi' },
  { path: '/ai', icon: Brain, id: 'ai' },
  { path: '/dashboard', icon: BarChart3, id: 'dashboard' },
]

export default function Layout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { locale } = useAppLocale()
  const copy = LAYOUT_COPY[locale]
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const auth = useAuth()
  const { setTheme } = useTheme()

  useEffect(() => {
    const path = location.pathname.split('/')[1]
    const themeName = ['yijing', 'tarot', 'bazi', 'ai'].includes(path)
      ? path
      : 'default'
    setTheme(themeName)
  }, [location.pathname, setTheme])

  useEffect(() => {
    logger.debug('Layout mounted, auth state:', {
      user: auth.user?.username,
      token: !!auth.token,
      isLoggedIn: auth.isLoggedIn,
    })
  }, [auth.isLoggedIn, auth.user, auth.token])

  const handleLogout = () => {
    auth.logout()
    setMobileMenuOpen(false)
    navigate('/login')
  }

  return (
    <div className="flex min-h-screen flex-col overflow-x-hidden">
      {location.pathname !== '/' && (
        <header className="glass-dark safe-area-top sticky top-0 z-50">
          <nav className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <div className="flex h-14 items-center justify-between md:h-16">
              <Link to="/" className="flex items-center space-x-2 md:space-x-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] md:h-10 md:w-10">
                  <span className="text-lg text-white md:text-xl">
                    {'\u262F'}
                  </span>
                </div>
                <span className="hidden bg-[linear-gradient(135deg,#f6e7cf_0%,#dcb86f_52%,#e19a84_100%)] bg-clip-text text-lg font-bold text-transparent sm:inline-block md:text-xl">
                  {copy.appName}
                </span>
              </Link>

              <div className="hidden items-center space-x-1 md:flex">
                {navItems.map(({ path, icon: Icon, id }) => (
                  <Link
                    key={path}
                    to={path}
                    className={`flex items-center space-x-2 rounded-lg px-4 py-2 transition-all duration-300 ${
                      location.pathname === path
                        ? 'bg-[#7a3218]/18 text-[#f0d9a5]'
                        : 'text-[#8f7b66] hover:bg-white/5 hover:text-white'
                    }`}
                  >
                    <Icon size={18} />
                    <span>{copy.nav[id]}</span>
                  </Link>
                ))}
              </div>

              <div className="hidden items-center space-x-4 md:flex">
                <Link
                  to="/achievement"
                  className="flex items-center space-x-2 rounded-lg px-3 py-2 text-[#bdaa94] transition-all duration-300 hover:bg-white/5 hover:text-white"
                >
                  <Trophy size={18} />
                  <span>{copy.nav.achievement}</span>
                </Link>
                <Link
                  to="/referral"
                  className="flex items-center space-x-2 rounded-lg px-3 py-2 text-[#bdaa94] transition-all duration-300 hover:bg-white/5 hover:text-white"
                >
                  <Users size={18} />
                  <span>{copy.nav.invite}</span>
                </Link>
                <LanguageToggleButton className="flex items-center space-x-2 rounded-lg px-3 py-2 text-[#bdaa94] transition-all duration-300 hover:bg-white/5 hover:text-white"></LanguageToggleButton>
                {auth.isLoggedIn ? (
                  <div className="flex items-center space-x-3">
                    <div className="flex items-center space-x-2 text-white">
                      <span className="flex items-center space-x-1 rounded-lg bg-[#7a3218]/18 px-2 py-0.5 text-sm text-[#f0d9a5]">
                        <span>{'\u{1F48E}'}</span>
                        <span>{auth.credits}</span>
                      </span>
                      <button
                        onClick={() => navigate('/self')}
                        className="flex cursor-pointer items-center space-x-2 transition-colors hover:text-[#f0d9a5]"
                      >
                        <User size={18} />
                        <span>{auth.user?.username || copy.nav.user}</span>
                      </button>
                    </div>
                    <button
                      type="button"
                      onClick={handleLogout}
                      className="flex items-center space-x-2 rounded-lg px-3 py-2 text-[#bdaa94] transition-all duration-300 hover:bg-white/5 hover:text-white"
                    >
                      <LogOut size={18} />
                      <span>{copy.nav.logout}</span>
                    </button>
                  </div>
                ) : (
                  <Link
                    to="/login"
                    className="flex items-center space-x-2 rounded-lg bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] px-4 py-2 text-white transition-all duration-300 hover:brightness-105"
                  >
                    <User size={18} />
                    <span>{copy.nav.login}</span>
                  </Link>
                )}
              </div>

              <div className="flex items-center space-x-2 md:hidden">
                {auth.isLoggedIn && (
                  <div className="flex items-center space-x-1 rounded-lg bg-[#7a3218]/18 px-2 py-1">
                    <span className="text-sm">{'\u{1F48E}'}</span>
                    <span className="text-sm font-medium text-white">
                      {auth.credits}
                    </span>
                  </div>
                )}
                <button
                  className="rounded-lg p-2 transition-colors hover:bg-white/10 active:bg-white/20"
                  onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                  aria-label={copy.menuAria}
                >
                  {mobileMenuOpen ? <X size={22} /> : <Menu size={22} />}
                </button>
              </div>
            </div>

            {mobileMenuOpen && (
              <div className="space-y-2 py-4 md:hidden">
                {navItems.map(({ path, icon: Icon, id }) => (
                  <Link
                    key={path}
                    to={path}
                    onClick={() => setMobileMenuOpen(false)}
                    className={`flex items-center space-x-3 rounded-lg px-4 py-3 transition-all ${
                      location.pathname === path
                        ? 'bg-[#7a3218]/18 text-[#f0d9a5]'
                        : 'text-[#8f7b66] hover:bg-white/5 hover:text-white'
                    }`}
                  >
                    <Icon size={20} />
                    <span>{copy.nav[id]}</span>
                  </Link>
                ))}
                <LanguageToggleButton
                  onAfterToggle={() => setMobileMenuOpen(false)}
                  className="flex w-full items-center space-x-3 rounded-lg px-4 py-3 text-[#bdaa94] transition-all hover:bg-white/5 hover:text-white"
                ></LanguageToggleButton>
                {auth.isLoggedIn ? (
                  <>
                    <Link
                      to="/achievement"
                      onClick={() => setMobileMenuOpen(false)}
                      className="flex items-center space-x-3 rounded-lg px-4 py-3 text-[#bdaa94] transition-all hover:bg-white/5 hover:text-white"
                    >
                      <Trophy size={20} />
                      <span>{copy.nav.achievement}</span>
                    </Link>
                    <Link
                      to="/referral"
                      onClick={() => setMobileMenuOpen(false)}
                      className="flex items-center space-x-3 rounded-lg px-4 py-3 text-[#bdaa94] transition-all hover:bg-white/5 hover:text-white"
                    >
                      <Users size={20} />
                      <span>{copy.nav.invite}</span>
                    </Link>
                    <div
                      className="flex cursor-pointer items-center space-x-3 rounded-lg bg-[#7a3218]/18 px-4 py-3 text-[#f0d9a5] transition-colors hover:bg-[#7a3218]/28"
                      onClick={() => {
                        setMobileMenuOpen(false)
                        navigate('/self')
                      }}
                    >
                      <User size={20} />
                      <span>{auth.user?.username || copy.nav.user}</span>
                    </div>
                    <button
                      type="button"
                      onClick={handleLogout}
                      className="flex w-full items-center space-x-3 rounded-lg px-4 py-3 text-[#bdaa94] transition-all hover:bg-white/5 hover:text-white"
                    >
                      <LogOut size={20} />
                      <span>{copy.nav.logoutFull}</span>
                    </button>
                  </>
                ) : (
                  <Link
                    to="/login"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center space-x-3 rounded-lg bg-[linear-gradient(135deg,#a34224_0%,#cd7840_52%,#e3bf73_100%)] px-4 py-3 text-white"
                  >
                    <User size={20} />
                    <span>{copy.nav.login}</span>
                  </Link>
                )}
              </div>
            )}
          </nav>
        </header>
      )}

      <main className="flex-1 pb-20 md:pb-0">
        <Outlet />
      </main>

      {location.pathname !== '/login' && <BottomNavigation />}

      {location.pathname !== '/' && location.pathname !== '/login' && (
        <footer className="glass-dark safe-area-bottom mt-auto hidden py-6 md:block">
          <div className="mx-auto max-w-7xl px-4 text-center text-sm text-[#8f7b66]">
            <p>{copy.footerTitle}</p>
            <p className="mt-1">{copy.footerNote}</p>
          </div>
        </footer>
      )}
    </div>
  )
}
