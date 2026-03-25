import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Download, Share2, Star } from 'lucide-react'
import Card, { CardContent, CardHeader, CardTitle } from './Card'
import Button from './Button'
import { toast } from './Toast'
import { resolvePageLocale } from '../utils/displayText'

const YAO_POSITION_LABELS = {
  1: '初',
  2: '二',
  3: '三',
  4: '四',
  5: '五',
  6: '上',
}

const OVERALL_JUDGMENT_LABELS = {
  'zh-CN': {
    大吉: '大吉',
    吉: '吉',
    中平: '中平',
    平: '平',
    凶: '凶',
    大凶: '大凶',
  },
  'en-US': {
    大吉: 'Excellent',
    吉: 'Auspicious',
    中平: 'Neutral',
    平: 'Balanced',
    凶: 'Inauspicious',
    大凶: 'Adverse',
    excellent: 'Excellent',
    auspicious: 'Auspicious',
    neutral: 'Neutral',
    balanced: 'Balanced',
    inauspicious: 'Inauspicious',
    adverse: 'Adverse',
  },
}

function getLocalizedYaoLabel(position, yaoType, locale) {
  const isEn = locale === 'en-US'
  const safePosition = Number(position) || position
  if (isEn) {
    return `Line ${safePosition} ${yaoType === '阳' ? 'Yang' : 'Yin'}`
  }

  const positionLabel = YAO_POSITION_LABELS[safePosition] || String(safePosition)
  const yaoTypeLabel = yaoType === '阳' ? '九' : '六'
  if (safePosition === 1 || safePosition === 6) {
    return `${positionLabel}${yaoTypeLabel}`
  }
  return `${yaoTypeLabel}${positionLabel}`
}

function getHexagramTitle(name, locale, fallback) {
  const finalName = name || fallback
  if (locale === 'en-US') return `${finalName} Hexagram`
  return `${finalName}卦`
}

function getOverallJudgmentText(value, locale, fallback) {
  const raw = String(value || '').trim()
  if (!raw) return fallback
  const labels = OVERALL_JUDGMENT_LABELS[locale] || OVERALL_JUDGMENT_LABELS['zh-CN']
  return labels[raw] || labels[raw.toLowerCase()] || raw
}

export default function LiuYaoReport({ reportData, onFavorite, onShare }) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const isEn = locale === 'en-US'
  const t = (zh, en) => (isEn ? en : zh)
  const [expandedSections, setExpandedSections] = useState({
    zhuangGua: true,
    yongShen: true,
    wangShuai: true,
    dongBian: true,
  })

  if (!reportData) return null

  const toggleSection = (section) => {
    setExpandedSections((prev) => ({
      ...prev,
      [section]: !prev[section],
    }))
  }

  const zhuangGua = reportData.zhuang_gua || {}
  const yaos = Array.isArray(zhuangGua.yaos)
    ? [...zhuangGua.yaos].sort((a, b) => b.yao_position - a.yao_position)
    : []
  const yongShen = reportData.yong_shen || {}
  const wangShuai = reportData.wang_shuai || {}
  const dongBian = reportData.dong_bian || {}
  const judgment = reportData.overall_judgment || {}
  const listSeparator = isEn ? ', ' : '、'

  const overallText = getOverallJudgmentText(
    judgment.overall,
    locale,
    t('中平', 'Neutral')
  )
  const overallClassName =
    ['大吉', 'Excellent'].includes(overallText)
      ? 'text-green-400'
      : ['吉', 'Auspicious'].includes(overallText)
        ? 'text-blue-400'
        : ['凶', 'Inauspicious', 'Adverse'].includes(overallText)
          ? 'text-red-400'
          : 'text-yellow-400'

  return (
    <div className="space-y-6">
      <Card className="panel gilded-border" glow>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-2xl">
              {getHexagramTitle(
                reportData.original?.chinese,
                locale,
                t('未知', 'Unknown')
              )}
              {reportData.changed &&
                ` → ${getHexagramTitle(
                  reportData.changed.chinese,
                  locale,
                  t('未知', 'Unknown')
                )}`}
            </CardTitle>
            <div className="flex space-x-2">
              <Button
                onClick={onFavorite}
                variant="secondary"
                size="sm"
                title={t('收藏', 'Favorite')}
                aria-label={t('收藏', 'Favorite')}
              >
                <Star size={16} />
              </Button>
              <Button
                onClick={onShare}
                variant="secondary"
                size="sm"
                title={t('分享', 'Share')}
                aria-label={t('分享', 'Share')}
              >
                <Share2 size={16} />
              </Button>
              <Button
                onClick={() =>
                  toast.info(
                    t(
                      '报告下载功能开发中',
                      'Report download is coming soon'
                    )
                  )
                }
                variant="secondary"
                size="sm"
                title={t('下载', 'Download')}
                aria-label={t('下载', 'Download')}
              >
                <Download size={16} />
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 text-sm md:grid-cols-2">
            <div>
              <span className="text-[#8f7b66]">{t('占问：', 'Question: ')}</span>
              <span>{reportData.question}</span>
            </div>
            <div>
              <span className="text-[#8f7b66]">{t('类别：', 'Category: ')}</span>
              <span>{reportData.category}</span>
            </div>
            <div>
              <span className="text-[#8f7b66]">
                {t('占卜日期：', 'Divination Date: ')}
              </span>
              <span>{reportData.divination_date}</span>
            </div>
            <div>
              <span className="text-[#8f7b66]">{t('综合判断：', 'Overall: ')}</span>
              <span className={`font-bold ${overallClassName}`}>{overallText}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className="panel">
        <CardHeader>
          <CardTitle
            className="flex cursor-pointer items-center justify-between"
            onClick={() => toggleSection('zhuangGua')}
          >
            <span>{t('装卦结果', 'Casting Layout')}</span>
            <span className="text-sm text-[#8f7b66]">
              {zhuangGua.palace_nature || '-'} | {t('月建', 'Month')}:{' '}
              {zhuangGua.yue_jian || '-'} | {t('日辰', 'Day')}:{' '}
              {zhuangGua.ri_chen || '-'}
            </span>
          </CardTitle>
        </CardHeader>
        {expandedSections.zhuangGua && (
          <CardContent>
            <div className="space-y-2">
              {yaos.map((yao) => (
                <div
                  key={yao.yao_position}
                  className={`rounded-lg border p-3 ${
                    yao.is_kong_wang
                      ? 'border-red-500/30 bg-red-500/10'
                      : 'border-white/10 bg-white/5'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <span className="font-bold text-[#dcb86f]">
                        {getLocalizedYaoLabel(
                          yao.yao_position,
                          yao.yao_type,
                          locale
                        )}
                      </span>
                      <span
                        className={`rounded px-2 py-1 text-xs ${
                          yao.yao_type === '阳'
                            ? 'bg-yellow-500/20 text-yellow-400'
                            : 'bg-blue-500/20 text-blue-400'
                        }`}
                      >
                        {yao.yao_type}
                      </span>
                      {yao.is_shi && (
                        <span className="rounded bg-green-500/20 px-2 py-1 text-xs text-green-400">
                          {t('世', 'Host')}
                        </span>
                      )}
                      {yao.is_ying && (
                        <span className="rounded bg-orange-500/20 px-2 py-1 text-xs text-orange-400">
                          {t('应', 'Guest')}
                        </span>
                      )}
                      {yao.is_kong_wang && (
                        <span className="rounded bg-red-500/20 px-2 py-1 text-xs text-red-400">
                          {t('空', 'Void')}
                        </span>
                      )}
                    </div>
                    <div className="flex items-center space-x-4 text-sm">
                      <span>{t('纳干', 'Stem')}: {yao.stem}</span>
                      <span>{t('纳支', 'Branch')}: {yao.branch}</span>
                      <span className="font-medium text-[#dcb86f]">
                        {yao.liu_qin}
                      </span>
                      <span className="text-[#8f7b66]">{yao.liu_shen}</span>
                      <span className="text-xs text-[#6f6257]">
                        {yao.wang_shuai}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            {Array.isArray(zhuangGua.kong_wang) && zhuangGua.kong_wang.length > 0 && (
              <div className="mt-4 rounded bg-red-500/10 p-2 text-sm text-red-400">
                {t('空亡：', 'Void: ')}
                {zhuangGua.kong_wang.join(listSeparator)}
              </div>
            )}
          </CardContent>
        )}
      </Card>

      {yongShen.primary && (
        <Card className="panel">
          <CardHeader>
            <CardTitle className="cursor-pointer" onClick={() => toggleSection('yongShen')}>
              {t('用神分析', 'Useful Spirit Analysis')}
            </CardTitle>
          </CardHeader>
          {expandedSections.yongShen && (
            <CardContent>
              <div className="space-y-3">
                <div>
                  <span className="text-[#8f7b66]">
                    {t('首选用神：', 'Primary reference: ')}
                  </span>
                    <span className="font-bold text-[#dcb86f]">
                      {yongShen.primary}
                    </span>
                  {yongShen.yao_position && (
                    <span className="ml-2 text-sm">
                      ({isEn ? `Line ${yongShen.yao_position}` : `第${yongShen.yao_position}爻`}
                      {isEn ? ', ' : '，'}
                      {yongShen.branch || '-'})
                    </span>
                  )}
                </div>
                {Array.isArray(yongShen.auxiliary) && yongShen.auxiliary.length > 0 && (
                  <div>
                    <span className="text-[#8f7b66]">
                      {t('辅助参考：', 'Auxiliary references: ')}
                    </span>
                    <span>{yongShen.auxiliary.join(listSeparator)}</span>
                  </div>
                )}
                {yongShen.judgment_points && (
                  <div className="rounded bg-[#6a4a1e]/12 p-3">
                    <div className="mb-1 text-sm font-medium">
                      {t('判断要点：', 'Key points: ')}
                    </div>
                    <div className="text-sm">{yongShen.judgment_points}</div>
                  </div>
                )}
              </div>
            </CardContent>
          )}
        </Card>
      )}

      {wangShuai.overall_status && (
        <Card className="panel">
          <CardHeader>
            <CardTitle className="cursor-pointer" onClick={() => toggleSection('wangShuai')}>
              {t('旺衰分析', 'Strength Analysis')}
            </CardTitle>
          </CardHeader>
          {expandedSections.wangShuai && (
            <CardContent>
              <div className="space-y-3">
                {wangShuai.yue_jian_status && (
                  <div>
                    <span className="text-[#8f7b66]">{t('月建：', 'Month: ')}</span>
                    <span>{wangShuai.yue_jian_status}</span>
                  </div>
                )}
                {wangShuai.ri_chen_status && (
                  <div>
                    <span className="text-[#8f7b66]">{t('日辰：', 'Day: ')}</span>
                    <span>{wangShuai.ri_chen_status}</span>
                  </div>
                )}
                {Array.isArray(wangShuai.dong_yao_effects) &&
                  wangShuai.dong_yao_effects.length > 0 && (
                    <div>
                      <span className="text-[#8f7b66]">
                        {t('动爻影响：', 'Moving line effects: ')}
                      </span>
                      <ul className="mt-1 list-inside list-disc space-y-1">
                        {wangShuai.dong_yao_effects.map((effect, idx) => (
                          <li key={idx} className="text-sm">
                            {effect}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                <div className="rounded bg-[#7a3218]/12 p-3">
                  <div className="mb-1 text-sm font-medium">
                    {t('综合旺衰：', 'Overall status: ')}
                  </div>
                  <div className="text-sm font-bold">
                    {wangShuai.overall_status}
                  </div>
                </div>
              </div>
            </CardContent>
          )}
        </Card>
      )}

      {dongBian.type && (
        <Card className="panel">
          <CardHeader>
            <CardTitle className="cursor-pointer" onClick={() => toggleSection('dongBian')}>
              {t('动变分析', 'Change Analysis')}
            </CardTitle>
          </CardHeader>
          {expandedSections.dongBian && (
            <CardContent>
              <div className="space-y-3">
                <div>
                  <span className="text-[#8f7b66]">{t('类型：', 'Type: ')}</span>
                  <span className="font-bold">{dongBian.type}</span>
                </div>
                {dongBian.priority && (
                  <div>
                    <span className="text-[#8f7b66]">
                      {t('断卦优先级：', 'Priority: ')}
                    </span>
                    <span>{dongBian.priority}</span>
                  </div>
                )}
                {dongBian.interpretation && (
                  <div className="rounded bg-yellow-500/10 p-3">
                    <div className="text-sm">{dongBian.interpretation}</div>
                  </div>
                )}
                {dongBian['吉凶倾向'] && (
                  <div className="rounded bg-green-500/10 p-3">
                    <div className="text-sm font-medium">
                      {t('吉凶倾向：', 'Trend: ')}
                    </div>
                    <div className="text-sm">{dongBian['吉凶倾向']}</div>
                  </div>
                )}
              </div>
            </CardContent>
          )}
        </Card>
      )}

      {judgment.summary && (
        <Card className="panel gilded-border" glow>
          <CardHeader>
            <CardTitle>{t('综合判断', 'Overall Judgment')}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="whitespace-pre-wrap text-sm leading-relaxed text-gray-200">
              {judgment.summary}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
