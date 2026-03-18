import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, Camera, Coins, ShieldAlert, Sparkles, Trash2, UploadCloud } from 'lucide-react'
import { geminiApi, unwrapApiData } from '../api'
import { POINTS_COST } from '../utils/pointsConfig'
import { toast } from '../components/Toast'
import { useAuth } from '../context/AuthContext'
import { logger } from '../utils/logger'

const MAX_FILE_SIZE = 5 * 1024 * 1024
const SUPPORTED_TYPES = ['image/jpeg', 'image/png', 'image/webp']

function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : ''
      const base64 = result.includes(',') ? result.split(',')[1] : result
      resolve(base64)
    }
    reader.onerror = () => reject(new Error('图片读取失败'))
    reader.readAsDataURL(file)
  })
}

export default function GeminiFacePage() {
  const navigate = useNavigate()
  const fileInputRef = useRef(null)
  const { credits, canSpendCredits, spendCredits } = useAuth()
  const [imageFile, setImageFile] = useState(null)
  const [previewUrl, setPreviewUrl] = useState('')
  const [prompt, setPrompt] = useState('请帮我分析照片中的人脸数量、角度、表情状态和画面清晰度。')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)

  const physiognomyReport = result?.physiognomyReport || {}
  const observedFeatures = Array.isArray(result?.observedFeatures) ? result.observedFeatures : []

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl)
      }
    }
  }, [previewUrl])

  const validateFile = (file) => {
    if (!file) {
      return { valid: false, message: '请选择图片' }
    }
    if (!SUPPORTED_TYPES.includes(file.type)) {
      return { valid: false, message: '仅支持 JPG、PNG、WEBP 图片' }
    }
    if (file.size > MAX_FILE_SIZE) {
      return { valid: false, message: '图片不能超过 5MB' }
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

    if (previewUrl) {
      URL.revokeObjectURL(previewUrl)
    }

    setImageFile(file)
    setPreviewUrl(URL.createObjectURL(file))
    setResult(null)
  }

  const handleClear = () => {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl)
    }
    setImageFile(null)
    setPreviewUrl('')
    setResult(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  const handleAnalyze = async () => {
    const validation = validateFile(imageFile)
    if (!validation.valid) {
      toast.error(validation.message)
      return
    }

    if (!canSpendCredits(POINTS_COST.AI_FACE_ANALYZE)) {
      toast.error(`积分不足，人脸分析需要 ${POINTS_COST.AI_FACE_ANALYZE} 积分，当前余额：${credits}`)
      return
    }

    setLoading(true)
    setResult(null)

    try {
      const imageBase64 = await fileToBase64(imageFile)
      const response = await geminiApi.analyzeFace(imageBase64, imageFile.type, prompt)
      const data = unwrapApiData(response)

      const spendResult = await spendCredits(POINTS_COST.AI_FACE_ANALYZE, 'Gemini人脸分析')
      if (spendResult.success) {
        toast.success(`分析完成，消耗 ${POINTS_COST.AI_FACE_ANALYZE} 积分`)
      } else {
        toast.error(spendResult.message || '积分扣除失败')
      }

      setResult(data)
    } catch (error) {
      logger.error('Gemini face analyze error:', error)
      toast.error(error?.message || '分析失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-sky-950 to-slate-900 text-white">
      <div className="sticky top-0 z-40 border-b border-white/10 bg-slate-950/80 backdrop-blur-xl">
        <div className="px-4 py-3 flex items-center justify-between">
          <button
            onClick={() => navigate(-1)}
            className="p-2 rounded-xl hover:bg-white/10 transition-colors"
          >
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div className="text-center">
            <div className="text-lg font-bold">Gemini 面相文化报告</div>
            <div className="text-xs text-sky-200/70">传统文化视角，仅供娱乐参考</div>
          </div>
          <div className="flex items-center space-x-1 rounded-full border border-amber-400/20 bg-amber-500/10 px-3 py-1.5">
            <Coins size={14} className="text-amber-300" />
            <span className="text-sm font-bold text-amber-200">{credits ?? 0}</span>
          </div>
        </div>
      </div>

      <div className="mx-auto max-w-5xl px-4 py-6 space-y-6">
        <div className="rounded-3xl border border-sky-400/20 bg-gradient-to-br from-sky-500/15 via-slate-900/60 to-cyan-500/10 p-6 shadow-2xl shadow-sky-950/40">
          <div className="flex items-start justify-between gap-4">
            <div>
              <div className="mb-2 inline-flex items-center space-x-2 rounded-full border border-sky-400/20 bg-sky-500/10 px-3 py-1 text-xs text-sky-200">
                <Sparkles size={14} />
                <span>Gemini Vision</span>
              </div>
              <h1 className="text-2xl font-bold">上传照片，生成面相文化解读报告</h1>
              <p className="mt-2 max-w-2xl text-sm leading-relaxed text-slate-300">
                该功能会先提取可见五官特征，再用传统面相学的文化语境组织成一份娱乐性报告。结果不用于判断真实人格、命运、健康或身份。
              </p>
            </div>
            <div className="rounded-2xl border border-amber-400/20 bg-amber-500/10 px-4 py-3 text-right">
              <div className="text-xs text-amber-200/70">单次消耗</div>
              <div className="mt-1 text-xl font-bold text-amber-200">{POINTS_COST.AI_FACE_ANALYZE} 积分</div>
            </div>
          </div>
        </div>

        <div className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
          <div className="rounded-3xl border border-white/10 bg-white/5 p-5 backdrop-blur-xl">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-lg font-bold">上传照片</h2>
              <button
                onClick={handleClear}
                className="inline-flex items-center space-x-2 rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-sm text-slate-200 hover:bg-white/10"
              >
                <Trash2 size={16} />
                <span>清空</span>
              </button>
            </div>

            <button
              onClick={() => fileInputRef.current?.click()}
              className="group w-full rounded-3xl border border-dashed border-sky-400/30 bg-gradient-to-br from-sky-500/10 to-cyan-500/10 p-6 text-left hover:border-sky-300/60 hover:bg-sky-500/15 transition-all"
            >
              <div className="flex items-center space-x-4">
                <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-sky-500/20 text-sky-200">
                  <UploadCloud size={28} />
                </div>
                <div>
                  <div className="text-base font-semibold text-white">点击选择照片</div>
                  <div className="mt-1 text-sm text-slate-300">支持 JPG / PNG / WEBP，大小不超过 5MB</div>
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

            <div className="mt-5">
              <label className="mb-2 block text-sm font-medium text-slate-200">补充要求</label>
              <textarea
                value={prompt}
                onChange={(event) => setPrompt(event.target.value)}
                rows={4}
                className="w-full rounded-2xl border border-white/10 bg-slate-950/70 px-4 py-3 text-sm text-white outline-none transition-colors placeholder:text-slate-500 focus:border-sky-400/40"
                placeholder="例如：希望多讲额头、眉眼和整体气质的传统文化解读。"
              />
            </div>

            <div className="mt-5 flex items-center justify-between rounded-2xl border border-rose-400/20 bg-rose-500/10 px-4 py-3 text-sm text-rose-100">
              <div className="flex items-start space-x-3">
                <ShieldAlert size={18} className="mt-0.5 text-rose-300" />
                <div>
                  <div className="font-medium">隐私提醒</div>
                  <div className="mt-1 text-rose-100/80">仅上传你有权处理的照片。图片会发送到 Gemini 接口做分析，报告只用于文化娱乐，不构成现实判断。</div>
                </div>
              </div>
            </div>

            <button
              onClick={handleAnalyze}
              disabled={loading || !imageFile}
              className="mt-5 inline-flex w-full items-center justify-center space-x-2 rounded-2xl bg-gradient-to-r from-sky-500 to-cyan-500 px-5 py-4 font-semibold text-slate-950 shadow-lg shadow-sky-900/30 transition-all hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <Camera size={18} className={loading ? 'animate-pulse' : ''} />
              <span>{loading ? 'Gemini 生成报告中...' : '生成报告'}</span>
            </button>
          </div>

          <div className="space-y-6">
            <div className="rounded-3xl border border-white/10 bg-white/5 p-5 backdrop-blur-xl">
              <h2 className="mb-4 text-lg font-bold">照片预览</h2>
              {previewUrl ? (
                <img
                  src={previewUrl}
                  alt="待分析照片"
                  className="h-[360px] w-full rounded-2xl object-cover"
                />
              ) : (
                <div className="flex h-[360px] items-center justify-center rounded-2xl border border-dashed border-white/10 bg-slate-950/60 text-slate-400">
                  选择一张照片后在这里预览
                </div>
              )}
            </div>

            <div className="rounded-3xl border border-white/10 bg-white/5 p-5 backdrop-blur-xl">
              <h2 className="mb-4 text-lg font-bold">报告结果</h2>
              {!result && !loading && (
                <div className="rounded-2xl border border-dashed border-white/10 bg-slate-950/60 p-5 text-sm text-slate-400">
                  上传照片后，这里会显示 Gemini 返回的面相文化报告。
                </div>
              )}

              {loading && (
                <div className="rounded-2xl border border-sky-400/20 bg-sky-500/10 p-5 text-sm text-sky-100">
                  正在提取可见五官特征并整理传统文化解读，请稍候。
                </div>
              )}

              {result && (
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-3">
                    <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                      <div className="text-xs text-slate-400">是否检测到人脸</div>
                      <div className="mt-2 text-lg font-bold text-white">
                        {result.hasFace === true ? '是' : result.hasFace === false ? '否' : '未明确'}
                      </div>
                    </div>
                    <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                      <div className="text-xs text-slate-400">人脸数量</div>
                      <div className="mt-2 text-lg font-bold text-white">{result.faceCount ?? '未明确'}</div>
                    </div>
                  </div>

                  <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                    <div className="text-xs text-slate-400">可见特征概述</div>
                    <div className="mt-2 whitespace-pre-wrap text-sm leading-relaxed text-slate-100">
                      {result.visualSummary || result.reportSummary || result.rawText || '暂无结果'}
                    </div>
                  </div>

                  <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                    <div className="text-xs text-slate-400">五官观察</div>
                    <div className="mt-3 space-y-2">
                      {observedFeatures.length > 0 ? (
                        observedFeatures.map((item, index) => (
                          <div key={index} className="rounded-xl bg-white/5 px-3 py-3 text-sm text-slate-100">
                            <div className="font-medium text-sky-200">{item.region || `特征 ${index + 1}`}</div>
                            <div className="mt-1">{item.observation || '暂无描述'}</div>
                            {item.clarity && (
                              <div className="mt-1 text-xs text-slate-400">清晰度：{item.clarity}</div>
                            )}
                          </div>
                        ))
                      ) : (
                        <span className="text-sm text-slate-400">暂无五官观察列表</span>
                      )}
                    </div>
                  </div>

                  <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                    <div className="text-xs text-slate-400">传统面相学文化解读</div>
                    <div className="mt-3 space-y-3">
                      {[
                        { label: '额头', value: physiognomyReport.forehead },
                        { label: '眉眼', value: physiognomyReport.eyesAndBrows },
                        { label: '鼻部', value: physiognomyReport.nose },
                        { label: '口唇与下巴', value: physiognomyReport.mouthAndChin },
                        { label: '整体印象', value: physiognomyReport.overallImpression },
                      ].map((item) => (
                        item.value ? (
                          <div key={item.label} className="rounded-xl bg-cyan-500/10 px-3 py-3 text-sm text-slate-100">
                            <div className="font-medium text-cyan-200">{item.label}</div>
                            <div className="mt-1 whitespace-pre-wrap leading-relaxed">{item.value}</div>
                          </div>
                        ) : null
                      ))}
                    </div>
                  </div>

                  <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                    <div className="text-xs text-slate-400">图像质量</div>
                    <div className="mt-2 text-sm leading-relaxed text-slate-100">{result.imageQuality || '暂无'}</div>
                  </div>

                  <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                    <div className="text-xs text-slate-400">报告总结</div>
                    <div className="mt-2 whitespace-pre-wrap text-sm leading-relaxed text-slate-100">
                      {result.reportSummary || result.visualSummary || '暂无总结'}
                    </div>
                  </div>

                  <div className="rounded-2xl border border-white/10 bg-slate-950/60 p-4">
                    <div className="text-xs text-slate-400">拍摄建议</div>
                    <div className="mt-3 space-y-2">
                      {Array.isArray(result.suggestions) && result.suggestions.length > 0 ? (
                        result.suggestions.map((item, index) => (
                          <div key={index} className="rounded-xl bg-white/5 px-3 py-2 text-sm text-slate-100">
                            {item}
                          </div>
                        ))
                      ) : (
                        <div className="text-sm text-slate-400">暂无建议</div>
                      )}
                    </div>
                  </div>

                  <div className="rounded-2xl border border-amber-400/20 bg-amber-500/10 p-4 text-sm text-amber-100">
                    {result.disclaimer || '本报告仅为传统文化娱乐性说明，不包含身份识别，也不构成对人格、命运或能力的事实判断。'}
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
