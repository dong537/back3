import { useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
  ArrowLeft,
  Camera,
  Coins,
  ShieldAlert,
  Sparkles,
  Trash2,
  UploadCloud,
} from 'lucide-react'
import { geminiApi, unwrapApiData } from '../api'
import { POINTS_COST } from '../utils/pointsConfig'
import { toast } from '../components/Toast'
import { useAuth } from '../context/AuthContext'
import { logger } from '../utils/logger'
import { resolvePageLocale, safeArray, safeText } from '../utils/displayText'

const MAX_FILE_SIZE = 5 * 1024 * 1024
const SUPPORTED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const INTERNAL_PROMPT =
  'Analyze the visible face count, angle, expression, image clarity, and any facial features or overall aura that can be described in a traditional culture framing.'

const GEMINI_FACE_COPY = {
  'zh-CN': {
    headerTitle: 'Gemini 面相文化报告',
    headerSubtitle: '传统文化视角，仅供娱乐参考',
    heroTitle: '上传照片，生成面相文化解读',
    heroDesc:
      '系统会先提取照片中可见的五官特征与整体气质，再整理成一份偏传统文化语境的娱乐性报告。',
    costLabel: '单次消耗',
    creditUnit: '积分',
    uploadTitle: '上传照片',
    clear: '清空',
    choosePhoto: '点击选择照片',
    choosePhotoDesc: '支持 JPG / PNG / WEBP，大小不超过 5MB',
    privacyTitle: '隐私提醒',
    privacyDesc:
      '仅上传你有权处理的照片。图片会发送到 Gemini 接口做分析，结果仅用于文化娱乐说明，不构成现实判断。',
    analyzing: 'Gemini 正在生成报告...',
    generate: '生成报告',
    previewTitle: '照片预览',
    previewAlt: '待分析照片',
    previewEmpty: '选择照片后，这里会显示预览',
    resultTitle: '报告结果',
    resultEmpty: '上传照片并发起分析后，这里会显示 Gemini 返回的面相文化报告。',
    resultLoading: '正在提取五官特征并整理传统文化解读，请稍候。',
    validationPick: '请选择图片',
    validationType: '仅支持 JPG、PNG、WEBP 图片',
    validationSize: '图片不能超过 5MB',
    notEnoughCredits: (cost, current) =>
      `积分不足，面相分析需要 ${cost} 积分，当前余额：${current}`,
    spendReason: 'Gemini 面相分析',
    spendSuccess: (cost) => `分析完成，已消耗 ${cost} 积分`,
    spendFailed: '积分扣除失败',
    analyzeFailed: '分析失败，请稍后重试',
    faceDetected: '是否检测到人脸',
    yes: '是',
    no: '否',
    unknown: '未明确',
    faceCount: '人脸数量',
    visibleSummary: '可见特征概述',
    noResult: '暂无结果',
    featureTitle: '五官观察',
    featureFallback: (index) => `特征 ${index + 1}`,
    noFeatureDesc: '暂无描述',
    clarity: '清晰度：',
    noFeatureList: '暂无五官观察列表',
    traditionTitle: '传统面相文化解读',
    traditionSections: [
      { key: 'forehead', label: '额头' },
      { key: 'eyesAndBrows', label: '眉眼' },
      { key: 'nose', label: '鼻部' },
      { key: 'mouthAndChin', label: '口唇与下颌' },
      { key: 'overallImpression', label: '整体印象' },
    ],
    imageQuality: '图像质量',
    summaryTitle: '报告总结',
    noSummary: '暂无总结',
    suggestionsTitle: '拍摄建议',
    noSuggestions: '暂无建议',
    disclaimer:
      '本报告仅为传统文化娱乐性说明，不包含身份识别，也不构成对人格、命运或能力的事实判断。',
  },
  'en-US': {
    headerTitle: 'Gemini Face Culture Report',
    headerSubtitle: 'Traditional culture view, for entertainment only',
    heroTitle: 'Upload a photo for a face culture reading',
    heroDesc:
      'The system extracts visible facial traits first, then turns them into an entertainment-oriented report in a traditional culture framing.',
    costLabel: 'Cost per run',
    creditUnit: 'credits',
    uploadTitle: 'Upload photo',
    clear: 'Clear',
    choosePhoto: 'Choose a photo',
    choosePhotoDesc: 'Supports JPG / PNG / WEBP up to 5MB',
    privacyTitle: 'Privacy notice',
    privacyDesc:
      'Upload only photos you are allowed to process. The image is sent to Gemini for analysis, and the report is for cultural entertainment only.',
    analyzing: 'Gemini is generating the report...',
    generate: 'Generate report',
    previewTitle: 'Photo preview',
    previewAlt: 'Selected photo',
    previewEmpty: 'Preview appears here after selecting a photo',
    resultTitle: 'Report result',
    resultEmpty:
      'The Gemini face culture report will appear here after analysis.',
    resultLoading:
      'Extracting visible features and preparing the cultural reading, please wait.',
    validationPick: 'Please choose an image',
    validationType: 'Only JPG, PNG, and WEBP are supported',
    validationSize: 'The image cannot exceed 5MB',
    notEnoughCredits: (cost, current) =>
      `Not enough credits. Face analysis needs ${cost}, current balance: ${current}`,
    spendReason: 'Gemini face analysis',
    spendSuccess: (cost) => `Analysis complete. ${cost} credits used`,
    spendFailed: 'Failed to deduct credits',
    analyzeFailed: 'Analysis failed, please try again later',
    faceDetected: 'Face detected',
    yes: 'Yes',
    no: 'No',
    unknown: 'Unknown',
    faceCount: 'Face count',
    visibleSummary: 'Visible feature summary',
    noResult: 'No result yet',
    featureTitle: 'Feature observations',
    featureFallback: (index) => `Feature ${index + 1}`,
    noFeatureDesc: 'No description yet',
    clarity: 'Clarity: ',
    noFeatureList: 'No feature list yet',
    traditionTitle: 'Traditional face-culture reading',
    traditionSections: [
      { key: 'forehead', label: 'Forehead' },
      { key: 'eyesAndBrows', label: 'Eyes and brows' },
      { key: 'nose', label: 'Nose' },
      { key: 'mouthAndChin', label: 'Mouth and chin' },
      { key: 'overallImpression', label: 'Overall impression' },
    ],
    imageQuality: 'Image quality',
    summaryTitle: 'Summary',
    noSummary: 'No summary yet',
    suggestionsTitle: 'Suggestions',
    noSuggestions: 'No suggestions yet',
    disclaimer:
      'This report is for traditional-culture entertainment only. It does not identify a person or make factual judgments about personality, fate, or ability.',
  },
}

function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : ''
      const base64 = result.includes(',') ? result.split(',')[1] : result
      resolve(base64)
    }
    reader.onerror = () => reject(new Error('Failed to read image'))
    reader.readAsDataURL(file)
  })
}

export default function GeminiFacePage() {
  const navigate = useNavigate()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = GEMINI_FACE_COPY[locale] || GEMINI_FACE_COPY['zh-CN']
  const fileInputRef = useRef(null)
  const { credits, canSpendCredits, spendCredits } = useAuth()
  const [imageFile, setImageFile] = useState(null)
  const [previewUrl, setPreviewUrl] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)

  const physiognomyReport = result?.physiognomyReport || {}
  const observedFeatures = safeArray(result?.observedFeatures)

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl)
      }
    }
  }, [previewUrl])

  const traditionSections = useMemo(
    () =>
      copy.traditionSections.filter((item) =>
        safeText(physiognomyReport[item.key])
      ),
    [copy.traditionSections, physiognomyReport]
  )

  const validateFile = (file) => {
    if (!file) return { valid: false, message: copy.validationPick }
    if (!SUPPORTED_TYPES.includes(file.type)) {
      return { valid: false, message: copy.validationType }
    }
    if (file.size > MAX_FILE_SIZE) {
      return { valid: false, message: copy.validationSize }
    }
    return { valid: true }
  }

  const handleFileChange = (event) => {
    const file = event.target.files?.[0]
    const validation = validateFile(file)
    if (!validation.valid) {
      toast.error(validation.message)
      event.target.value = ''
      return
    }

    if (previewUrl) URL.revokeObjectURL(previewUrl)
    setImageFile(file)
    setPreviewUrl(URL.createObjectURL(file))
    setResult(null)
  }

  const handleClear = () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl)
    setImageFile(null)
    setPreviewUrl('')
    setResult(null)
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  const handleAnalyze = async () => {
    const validation = validateFile(imageFile)
    if (!validation.valid) {
      toast.error(validation.message)
      return
    }

    if (!canSpendCredits(POINTS_COST.AI_FACE_ANALYZE)) {
      toast.error(copy.notEnoughCredits(POINTS_COST.AI_FACE_ANALYZE, credits))
      return
    }

    setLoading(true)
    setResult(null)

    try {
      const imageBase64 = await fileToBase64(imageFile)
      const response = await geminiApi.analyzeFace(
        imageBase64,
        imageFile.type,
        INTERNAL_PROMPT
      )
      const data = unwrapApiData(response)

      const spendResult = await spendCredits(
        POINTS_COST.AI_FACE_ANALYZE,
        copy.spendReason
      )
      if (spendResult.success) {
        toast.success(copy.spendSuccess(POINTS_COST.AI_FACE_ANALYZE))
      } else {
        toast.error(spendResult.message || copy.spendFailed)
      }

      setResult(data)
    } catch (error) {
      logger.error('Gemini face analyze error:', error)
      toast.error(error?.message || copy.analyzeFailed)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-shell" data-theme="ai">
      <div className="safe-area-top sticky top-0 z-40 mb-6 border-b border-white/10 bg-[#0f0a09]/82 backdrop-blur-xl">
        <div className="app-sticky-inner flex items-center justify-between py-3">
          <button
            onClick={() => navigate(-1)}
            className="rounded-xl p-2 text-[#f4ece1] transition-colors hover:bg-white/[0.06]"
          >
            <ArrowLeft size={20} />
          </button>
          <div className="text-center">
            <div className="text-lg font-bold text-[#f4ece1]">
              {copy.headerTitle}
            </div>
            <div className="text-xs text-[#8f7b66]">{copy.headerSubtitle}</div>
          </div>
          <div className="flex items-center gap-1 rounded-full border border-[#d0a85b]/20 bg-[#6a4a1e]/16 px-3 py-1.5">
            <Coins size={14} className="text-[#dcb86f]" />
            <span className="text-sm font-bold text-[#f0d9a5]">
              {credits ?? 0}
            </span>
          </div>
        </div>
      </div>

      <div className="app-page-shell space-y-6 pb-8">
        <section className="page-hero overflow-hidden">
          <div className="page-hero-inner !text-left">
            <div className="mb-3 inline-flex items-center gap-2 rounded-full border border-[#d0a85b]/18 bg-[#6a4a1e]/18 px-4 py-2 text-xs uppercase tracking-[0.28em] text-[#dcb86f]">
              <Sparkles size={14} />
              <span>Gemini Vision</span>
            </div>
            <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
              <div className="max-w-3xl">
                <h1 className="page-title !mt-0">{copy.heroTitle}</h1>
                <p className="page-subtitle !mx-0 !max-w-2xl !text-left">
                  {copy.heroDesc}
                </p>
              </div>
              <div className="panel-soft min-w-[180px] px-5 py-4 text-right">
                <div className="text-xs uppercase tracking-[0.2em] text-[#8f7b66]">
                  {copy.costLabel}
                </div>
                <div className="mt-2 text-2xl font-bold text-[#f0d9a5]">
                  {POINTS_COST.AI_FACE_ANALYZE} {copy.creditUnit}
                </div>
              </div>
            </div>
          </div>
        </section>

        <div className="grid gap-6 lg:grid-cols-[1.05fr_0.95fr]">
          <div className="panel p-5 md:p-6">
            <div className="mb-4 flex items-center justify-between gap-4">
              <h2 className="text-lg font-bold text-[#f4ece1]">
                {copy.uploadTitle}
              </h2>
              <button
                onClick={handleClear}
                className="inline-flex items-center gap-2 rounded-2xl border border-white/10 bg-white/[0.04] px-3 py-2 text-sm text-[#bdaa94] transition hover:bg-white/[0.08] hover:text-[#f4ece1]"
              >
                <Trash2 size={16} />
                <span>{copy.clear}</span>
              </button>
            </div>

            <button
              onClick={() => fileInputRef.current?.click()}
              className="group w-full rounded-[28px] border border-dashed border-[#d0a85b]/24 bg-[linear-gradient(180deg,rgba(106,74,30,0.1),rgba(22,17,16,0.72))] p-6 text-left transition-all hover:border-[#d0a85b]/40 hover:bg-[linear-gradient(180deg,rgba(122,50,24,0.14),rgba(22,17,16,0.82))]"
            >
              <div className="flex items-center gap-4">
                <div className="mystic-icon-badge h-14 w-14 rounded-[22px]">
                  <UploadCloud size={28} />
                </div>
                <div>
                  <div className="text-base font-semibold text-[#f4ece1]">
                    {copy.choosePhoto}
                  </div>
                  <div className="mt-1 text-sm text-[#bdaa94]">
                    {copy.choosePhotoDesc}
                  </div>
                </div>
              </div>
            </button>

            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              className="hidden"
              onChange={handleFileChange}
            />

            <div className="mt-5 rounded-[24px] border border-[#a34224]/20 bg-[#7a3218]/12 px-4 py-4 text-sm text-[#e4d6c8]">
              <div className="flex items-start gap-3">
                <ShieldAlert size={18} className="mt-0.5 text-[#e19a84]" />
                <div>
                  <div className="font-medium text-[#f4ece1]">
                    {copy.privacyTitle}
                  </div>
                  <div className="mt-1 text-[#bdaa94]">{copy.privacyDesc}</div>
                </div>
              </div>
            </div>

            <button
              onClick={handleAnalyze}
              disabled={loading || !imageFile}
              className="btn-primary-theme mt-5 inline-flex w-full items-center justify-center gap-2 px-5 py-4 font-semibold disabled:cursor-not-allowed disabled:opacity-50"
            >
              <Camera size={18} className={loading ? 'animate-pulse' : ''} />
              <span>{loading ? copy.analyzing : copy.generate}</span>
            </button>
          </div>

          <div className="space-y-6">
            <div className="panel p-5 md:p-6">
              <h2 className="mb-4 text-lg font-bold text-[#f4ece1]">
                {copy.previewTitle}
              </h2>
              {previewUrl ? (
                <img
                  src={previewUrl}
                  alt={copy.previewAlt}
                  className="h-[360px] w-full rounded-[24px] object-cover"
                />
              ) : (
                <div className="flex h-[360px] items-center justify-center rounded-[24px] border border-dashed border-white/10 bg-white/[0.03] text-[#8f7b66]">
                  {copy.previewEmpty}
                </div>
              )}
            </div>

            <div className="panel p-5 md:p-6">
              <h2 className="mb-4 text-lg font-bold text-[#f4ece1]">
                {copy.resultTitle}
              </h2>
              {!result && !loading && (
                <div className="rounded-[24px] border border-dashed border-white/10 bg-white/[0.03] p-5 text-sm text-[#8f7b66]">
                  {copy.resultEmpty}
                </div>
              )}

              {loading && (
                <div className="rounded-[24px] border border-[#d0a85b]/20 bg-[#6a4a1e]/12 p-5 text-sm text-[#dcb86f]">
                  {copy.resultLoading}
                </div>
              )}

              {result && (
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-3">
                    <InfoBlock
                      label={copy.faceDetected}
                      value={
                        result.hasFace === true
                          ? copy.yes
                          : result.hasFace === false
                            ? copy.no
                            : copy.unknown
                      }
                    />
                    <InfoBlock
                      label={copy.faceCount}
                      value={result.faceCount ?? copy.unknown}
                    />
                  </div>

                  <SectionBlock label={copy.visibleSummary}>
                    {result.visualSummary ||
                      result.reportSummary ||
                      result.rawText ||
                      copy.noResult}
                  </SectionBlock>

                  <div className="rounded-[24px] border border-white/10 bg-white/[0.03] p-4">
                    <div className="text-xs uppercase tracking-[0.18em] text-[#8f7b66]">
                      {copy.featureTitle}
                    </div>
                    <div className="mt-3 space-y-2">
                      {observedFeatures.length > 0 ? (
                        observedFeatures.map((item, index) => (
                          <div
                            key={index}
                            className="rounded-[18px] border border-white/10 bg-[#140f0f]/72 px-3 py-3 text-sm text-[#f4ece1]"
                          >
                            <div className="font-medium text-[#dcb86f]">
                              {item.region || copy.featureFallback(index)}
                            </div>
                            <div className="mt-1">
                              {item.observation || copy.noFeatureDesc}
                            </div>
                            {item.clarity && (
                              <div className="mt-1 text-xs text-[#8f7b66]">
                                {copy.clarity}
                                {item.clarity}
                              </div>
                            )}
                          </div>
                        ))
                      ) : (
                        <span className="text-sm text-[#8f7b66]">
                          {copy.noFeatureList}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="rounded-[24px] border border-white/10 bg-white/[0.03] p-4">
                    <div className="text-xs uppercase tracking-[0.18em] text-[#8f7b66]">
                      {copy.traditionTitle}
                    </div>
                    <div className="mt-3 space-y-3">
                      {traditionSections.map((item) => (
                        <div
                          key={item.key}
                          className="rounded-[18px] border border-[#d0a85b]/18 bg-[#6a4a1e]/10 px-3 py-3 text-sm text-[#f4ece1]"
                        >
                          <div className="font-medium text-[#f0d9a5]">
                            {item.label}
                          </div>
                          <div className="mt-1 whitespace-pre-wrap leading-relaxed">
                            {physiognomyReport[item.key]}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  <SectionBlock label={copy.imageQuality}>
                    {result.imageQuality || copy.unknown}
                  </SectionBlock>

                  <SectionBlock label={copy.summaryTitle}>
                    {result.reportSummary ||
                      result.visualSummary ||
                      copy.noSummary}
                  </SectionBlock>

                  <div className="rounded-[24px] border border-white/10 bg-white/[0.03] p-4">
                    <div className="text-xs uppercase tracking-[0.18em] text-[#8f7b66]">
                      {copy.suggestionsTitle}
                    </div>
                    <div className="mt-3 space-y-2">
                      {safeArray(result.suggestions).length > 0 ? (
                        safeArray(result.suggestions).map((item, index) => (
                          <div
                            key={index}
                            className="rounded-[18px] border border-white/10 bg-[#140f0f]/72 px-3 py-2 text-sm text-[#f4ece1]"
                          >
                            {item}
                          </div>
                        ))
                      ) : (
                        <div className="text-sm text-[#8f7b66]">
                          {copy.noSuggestions}
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="rounded-[24px] border border-[#a34224]/20 bg-[#7a3218]/12 p-4 text-sm leading-7 text-[#e4d6c8]">
                    {result.disclaimer || copy.disclaimer}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

function InfoBlock({ label, value }) {
  return (
    <div className="rounded-[24px] border border-white/10 bg-white/[0.03] p-4">
      <div className="text-xs uppercase tracking-[0.18em] text-[#8f7b66]">
        {label}
      </div>
      <div className="mt-2 text-lg font-bold text-[#f4ece1]">{value}</div>
    </div>
  )
}

function SectionBlock({ label, children }) {
  return (
    <div className="rounded-[24px] border border-white/10 bg-white/[0.03] p-4">
      <div className="text-xs uppercase tracking-[0.18em] text-[#8f7b66]">
        {label}
      </div>
      <div className="mt-2 whitespace-pre-wrap text-sm leading-7 text-[#f4ece1]">
        {children}
      </div>
    </div>
  )
}
