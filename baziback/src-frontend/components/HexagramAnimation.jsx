import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import Card, { CardContent } from './Card'
import { resolvePageLocale } from '../utils/displayText'

const HEXAGRAM_ANIMATION_COPY = {
  'zh-CN': {
    generating: '正在生成卦象...',
  },
  'en-US': {
    generating: 'Generating your hexagram...',
  },
}

export default function HexagramAnimation({ isGenerating, onComplete, children }) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = HEXAGRAM_ANIMATION_COPY[locale] || HEXAGRAM_ANIMATION_COPY['zh-CN']
  const [stage, setStage] = useState('idle')
  const [hexagramLines, setHexagramLines] = useState([])

  useEffect(() => {
    if (isGenerating) {
      setStage('generating')
      setHexagramLines([])

      const lines = []
      const timeouts = []

      for (let i = 0; i < 6; i += 1) {
        const timeout = setTimeout(() => {
          lines.push({
            position: 6 - i,
            type: Math.random() > 0.5 ? 'yang' : 'yin',
            changing: Math.random() > 0.3,
          })
          setHexagramLines([...lines])

          if (i === 5) {
            setTimeout(() => {
              setStage('revealing')
              setTimeout(() => {
                setStage('complete')
                if (onComplete) onComplete()
              }, 800)
            }, 300)
          }
        }, i * 200)
        timeouts.push(timeout)
      }

      return () => {
        timeouts.forEach(clearTimeout)
      }
    }

    if (children) {
      setStage('complete')
      return undefined
    }

    setStage('idle')
    setHexagramLines([])
    return undefined
  }, [isGenerating, onComplete, children])

  if (stage === 'idle' && !children) {
    return null
  }

  return (
    <div className="hexagram-animation-container">
      {stage === 'generating' && (
        <Card className="panel mb-6">
          <CardContent className="py-12">
            <div className="hexagram-generating">
              <div className="hexagram-lines-preview">
                {hexagramLines.map((line, index) => (
                  <div
                    key={`${line.position}-${index}`}
                    className={`hexagram-line ${line.type} ${line.changing ? 'changing' : ''}`}
                    style={{ animationDelay: `${index * 0.1}s` }}
                  >
                    {line.type === 'yang' ? (
                      <div className="yang-line">━━━</div>
                    ) : (
                      <div className="yin-line">━ ━</div>
                    )}
                    {line.changing && <span className="changing-indicator">○</span>}
                  </div>
                ))}
                {hexagramLines.length < 6 && (
                  <div className="hexagram-loading-dots">
                    <span />
                    <span />
                    <span />
                  </div>
                )}
              </div>
              <p className="mt-4 text-center text-sm text-gray-400">{copy.generating}</p>
            </div>
          </CardContent>
        </Card>
      )}

      {stage === 'revealing' && (
        <div className="hexagram-revealing">
          <div className="hexagram-glow" />
        </div>
      )}

      {stage === 'complete' && children && <div className="hexagram-complete">{children}</div>}
    </div>
  )
}
