import { useState, useEffect, useMemo } from 'react'
import { Users, Gift, Copy, Share2, Award } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card, { CardHeader, CardTitle, CardContent } from '../components/Card'
import Button from '../components/Button'
import { referralApi, creditApi } from '../api'
import { toast } from '../components/Toast'
import { logger } from '../utils/logger'
import {
  resolvePageLocale,
  safeArray,
  safeNumber,
  safeText,
  formatLocaleDateTime,
} from '../utils/displayText'

const DEFAULT_STATS = {
  total: 0,
  registered: 0,
  divined: 0,
  completed: 0,
  pending: 0,
}

const REFERRAL_COPY = {
  'zh-CN': {
    title: '邀请好友',
    subtitle: '分享给好友，双方都能获得丰厚奖励。',
    currentPoints: '当前积分',
    totalReward: '累计获得',
    myCode: '我的邀请码',
    copy: '复制',
    shareLink: '分享邀请链接',
    noCode: '邀请码生成中，请稍后再试。',
    codeCopied: '邀请码已复制',
    linkCopied: '分享链接已复制',
    copyCodeFailed: '复制邀请码失败，请稍后重试。',
    copyLinkFailed: '复制分享链接失败，请稍后重试。',
    shareTitle: '邀请好友一起占卜',
    shareSuccess: '分享成功',
    loadFailed: '加载邀请数据失败',
    rewardRules: '奖励规则',
    friendSignup: '好友注册',
    firstDivination: '好友首次占卜',
    inviteMaster: '邀请达人',
    signupDesc: '好友使用你的邀请码注册',
    firstDivinationDesc: '好友完成第一次占卜',
    inviteMasterDesc: '成功邀请 3 位好友',
    statsTitle: '邀请统计',
    totalInvites: '总邀请数',
    completed: '已完成',
    pending: '待完成',
    registered: '已注册',
    divined: '已占卜',
    recordsTitle: '邀请记录',
    emptyTitle: '还没有邀请记录',
    emptyDesc: '分享邀请码给好友，开始获得奖励吧。',
    codeLabel: '邀请码：',
    missingCode: '暂无邀请码',
    usageTitle: '使用说明',
    usageSteps: [
      '复制你的邀请码，或直接把邀请链接分享给好友。',
      '好友使用你的邀请码注册后，你立即获得 20 积分。',
      '好友完成首次占卜后，你再获得 30 积分。',
      '积分可用于免费占卜次数和高级功能兑换。',
    ],
    loading: '加载中...',
    shareMessage: (code, url) =>
      `我发现了一个很准的占卜应用，使用我的邀请码 ${code} 注册，我们都能获得奖励。\n${url}`,
  },
  'en-US': {
    title: 'Invite Friends',
    subtitle: 'Share with friends and both of you can earn generous rewards.',
    currentPoints: 'Current Credits',
    totalReward: 'Total Earned',
    myCode: 'My Invite Code',
    copy: 'Copy',
    shareLink: 'Share Invite Link',
    noCode:
      'Your invite code is still being generated. Please try again shortly.',
    codeCopied: 'Invite code copied',
    linkCopied: 'Invite link copied',
    copyCodeFailed: 'Failed to copy invite code. Please try again later.',
    copyLinkFailed: 'Failed to copy invite link. Please try again later.',
    shareTitle: 'Invite friends to explore together',
    shareSuccess: 'Shared successfully!',
    loadFailed: 'Failed to load invite data',
    rewardRules: 'Reward Rules',
    friendSignup: 'Friend Signs Up',
    firstDivination: 'Friend First Reading',
    inviteMaster: 'Invite Master',
    signupDesc: 'A friend signs up with your invite code',
    firstDivinationDesc: 'A friend completes their first reading',
    inviteMasterDesc: 'Successfully invite 3 friends',
    statsTitle: 'Invite Stats',
    totalInvites: 'Total Invites',
    completed: 'Completed',
    pending: 'Pending',
    registered: 'Registered',
    divined: 'First Reading Done',
    recordsTitle: 'Invite Records',
    emptyTitle: 'No invite records yet',
    emptyDesc: 'Share your invite code with friends and start earning rewards.',
    codeLabel: 'Code: ',
    missingCode: 'Unavailable',
    usageTitle: 'How It Works',
    usageSteps: [
      'Copy your invite code or share your invite link with friends.',
      'Once a friend signs up with your code, you immediately earn 20 credits.',
      'After that friend completes their first reading, you earn another 30 credits.',
      'Credits can be used for free readings and premium features.',
    ],
    loading: 'Loading...',
    shareMessage: (code, url) =>
      `I found a surprisingly accurate divination app! Sign up with my invite code ${code} and we can both earn rewards.\n${url}`,
  },
}

function buildStats(rawStats) {
  const next = { ...DEFAULT_STATS }
  next.total = safeNumber(rawStats?.total, 0)
  next.registered = safeNumber(rawStats?.registered, 0)
  next.divined = safeNumber(rawStats?.divined, 0)
  next.completed = safeNumber(rawStats?.completed, 0)
  next.pending = safeNumber(
    rawStats?.pending,
    Math.max(next.total - next.completed, 0)
  )
  return next
}

function getInviteStatus(inviteStatus, copy) {
  const status = safeNumber(inviteStatus, 0)
  if (status >= 3) return copy.completed
  if (status >= 2) return copy.divined
  if (status >= 1) return copy.registered
  return copy.pending
}

function getInviteStatusClass(inviteStatus) {
  const status = safeNumber(inviteStatus, 0)
  if (status >= 3) return 'bg-green-500/20 text-green-400'
  if (status >= 2) return 'bg-blue-500/20 text-blue-400'
  if (status >= 1) return 'bg-yellow-500/20 text-yellow-400'
  return 'bg-gray-500/20 text-gray-400'
}

export default function ReferralPage() {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = REFERRAL_COPY[locale]
  const [referralCode, setReferralCode] = useState('')
  const [stats, setStats] = useState(DEFAULT_STATS)
  const [userPoints, setUserPoints] = useState(0)
  const [invites, setInvites] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [codeRes, statsRes, balanceRes, recordsRes] = await Promise.all([
        referralApi.getReferralCode(),
        referralApi.getStats(),
        creditApi.getBalance(),
        referralApi.getRecords(),
      ])

      if (codeRes.data?.code === 200) {
        setReferralCode(safeText(codeRes.data?.data))
      }
      if (statsRes.data?.code === 200) {
        setStats(buildStats(statsRes.data?.data))
      } else {
        setStats(DEFAULT_STATS)
      }
      if (balanceRes.data?.code === 200) {
        setUserPoints(safeNumber(balanceRes.data?.data?.balance, 0))
      } else {
        setUserPoints(0)
      }
      if (recordsRes.data?.code === 200) {
        setInvites(safeArray(recordsRes.data?.data))
      } else {
        setInvites([])
      }
    } catch (error) {
      logger.error('Failed to load referral data:', error)
      toast.error(copy.loadFailed)
      setStats(DEFAULT_STATS)
      setInvites([])
    } finally {
      setLoading(false)
    }
  }

  const safeReferralCode = safeText(referralCode)
  const shareUrl = useMemo(() => {
    if (!safeReferralCode) return ''
    return `${window.location.origin}?ref=${encodeURIComponent(safeReferralCode)}`
  }, [safeReferralCode])
  const shareText = useMemo(() => {
    if (!safeReferralCode || !shareUrl) return ''
    return copy.shareMessage(safeReferralCode, shareUrl)
  }, [copy, safeReferralCode, shareUrl])

  const handleCopyCode = async () => {
    if (!safeReferralCode) {
      toast.warn(copy.noCode)
      return
    }
    try {
      await navigator.clipboard.writeText(safeReferralCode)
      toast.success(copy.codeCopied)
    } catch (error) {
      logger.error('Failed to copy referral code:', error)
      toast.error(copy.copyCodeFailed)
    }
  }

  const handleCopyLink = async () => {
    if (!shareText) {
      toast.warn(copy.noCode)
      return
    }
    try {
      await navigator.clipboard.writeText(shareText)
      toast.success(copy.linkCopied)
    } catch (error) {
      logger.error('Failed to copy referral link:', error)
      toast.error(copy.copyLinkFailed)
    }
  }

  const handleShare = async () => {
    if (!shareText || !shareUrl) {
      toast.warn(copy.noCode)
      return
    }

    if (navigator.share) {
      try {
        await navigator.share({
          title: copy.shareTitle,
          text: shareText,
          url: shareUrl,
        })
        toast.success(copy.shareSuccess)
      } catch (error) {
        if (error?.name !== 'AbortError') {
          await handleCopyLink()
        }
      }
    } else {
      await handleCopyLink()
    }
  }

  const totalReward = safeNumber(stats.completed, 0) * 50

  return (
    <div className="page-shell" data-theme="default">
      <div className="page-hero">
        <div className="page-hero-inner">
          <div className="page-badge">
            <Users className="text-theme h-4 w-4" />
            <span>{copy.recordsTitle}</span>
          </div>
          <h1 className="page-title font-serif-title text-white">{copy.title}</h1>
          <p className="page-subtitle">{copy.subtitle}</p>
        </div>
      </div>

      <div className="mx-auto max-w-4xl px-4 pb-8">

        <Card className="mb-6">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="mb-1 text-sm text-[#bdaa94]">
                  {copy.currentPoints}
                </p>
                <p className="text-3xl font-bold text-[#dcb86f]">
                  {safeNumber(userPoints, 0)}
                </p>
              </div>
              <div className="text-right">
                <p className="mb-1 text-sm text-[#bdaa94]">{copy.totalReward}</p>
                <p className="text-2xl font-bold text-[#f4ece1]">{totalReward}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="mb-6">
          <CardHeader>
            <CardTitle>{copy.myCode}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="mb-4 flex items-center space-x-3">
              <input
                type="text"
                value={safeReferralCode || copy.missingCode}
                readOnly
                className="mystic-input flex-1 py-3 text-center font-mono text-xl font-bold"
              />
              <Button
                onClick={handleCopyCode}
                variant="secondary"
                disabled={!safeReferralCode}
              >
                <Copy size={18} />
                <span>{copy.copy}</span>
              </Button>
            </div>
            <Button
              onClick={handleShare}
              className="w-full"
              disabled={!safeReferralCode}
            >
              <Share2 size={18} />
              <span>{copy.shareLink}</span>
            </Button>
          </CardContent>
        </Card>

        <div className="mb-6 grid gap-6 md:grid-cols-3">
          <Card>
            <CardContent className="p-6 text-center">
              <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-[#7a3218]/25">
                <Users className="text-[#dcb86f]" size={24} />
              </div>
              <h3 className="mb-2 font-bold">{copy.friendSignup}</h3>
              <p className="mb-1 text-2xl font-bold text-[#dcb86f]">+20</p>
              <p className="text-xs text-[#bdaa94]">{copy.signupDesc}</p>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 text-center">
              <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-[#6c341f]/25">
                <Gift className="text-[#f0d9a5]" size={24} />
              </div>
              <h3 className="mb-2 font-bold">{copy.firstDivination}</h3>
              <p className="mb-1 text-2xl font-bold text-[#dcb86f]">+30</p>
              <p className="text-xs text-[#bdaa94]">
                {copy.firstDivinationDesc}
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6 text-center">
              <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-[#8d4a22]/25">
                <Award className="text-[#dcb86f]" size={24} />
              </div>
              <h3 className="mb-2 font-bold">{copy.inviteMaster}</h3>
              <p className="mb-1 text-2xl font-bold text-[#dcb86f]">+100</p>
              <p className="text-xs text-[#bdaa94]">{copy.inviteMasterDesc}</p>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>{copy.statsTitle}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="mb-6 grid grid-cols-3 gap-4">
              <div className="text-center">
                <p className="mb-1 text-3xl font-bold text-[#dcb86f]">
                  {safeNumber(stats.total, 0)}
                </p>
                <p className="text-sm text-[#bdaa94]">{copy.totalInvites}</p>
              </div>
              <div className="text-center">
                <p className="mb-1 text-3xl font-bold text-green-400">
                  {safeNumber(stats.completed, 0)}
                </p>
                <p className="text-sm text-[#bdaa94]">{copy.completed}</p>
              </div>
              <div className="text-center">
                <p className="mb-1 text-3xl font-bold text-yellow-400">
                  {safeNumber(stats.pending, 0)}
                </p>
                <p className="text-sm text-[#bdaa94]">{copy.pending}</p>
              </div>
            </div>

            {loading ? (
              <div className="py-8 text-center text-[#bdaa94]">
                {copy.loading}
              </div>
            ) : invites.length > 0 ? (
              <div className="space-y-2">
                <h3 className="mb-3 text-sm font-medium">
                  {copy.recordsTitle}
                </h3>
                {invites.map((invite, index) => {
                  const code = safeText(invite?.referralCode, copy.missingCode)
                  const createTime = formatLocaleDateTime(
                    invite?.createTime,
                    locale
                  )
                  return (
                    <div
                      key={safeText(invite?.id, `${code}-${index}`)}
                    className="mystic-muted-box flex items-center justify-between"
                  >
                      <div>
                        <p className="font-medium">
                          {copy.codeLabel}
                          {code}
                        </p>
                        <p className="text-xs text-[#bdaa94]">{createTime}</p>
                      </div>
                      <span
                        className={`rounded-full px-3 py-1 text-xs ${getInviteStatusClass(
                          invite?.inviteStatus
                        )}`}
                      >
                        {getInviteStatus(invite?.inviteStatus, copy)}
                      </span>
                    </div>
                  )
                })}
              </div>
            ) : (
              <div className="py-8 text-center text-[#bdaa94]">
                <Users size={48} className="mx-auto mb-4 opacity-50" />
                <p>{copy.emptyTitle}</p>
                <p className="mt-2 text-sm">{copy.emptyDesc}</p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="mt-6">
          <CardHeader>
            <CardTitle>{copy.usageTitle}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 text-sm text-[#bdaa94]">
              {copy.usageSteps.map((step, index) => (
                <div key={step} className="flex items-start space-x-3">
                  <span className="font-bold text-[#dcb86f]">
                    {index + 1}.
                  </span>
                  <p>{step}</p>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
