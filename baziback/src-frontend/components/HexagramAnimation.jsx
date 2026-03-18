import { useEffect, useState } from 'react'
import Card, { CardContent } from './Card'

/**
 * 卦象生成动画组件
 */
export default function HexagramAnimation({ isGenerating, onComplete, children }) {
  const [stage, setStage] = useState('idle') // idle, generating, revealing, complete
  const [hexagramLines, setHexagramLines] = useState([])

  useEffect(() => {
    if (isGenerating) {
      setStage('generating')
      setHexagramLines([])
      
      // 模拟生成六爻的过程
      const lines = []
      const timeouts = []
      
      for (let i = 0; i < 6; i++) {
        const timeout = setTimeout(() => {
          lines.push({
            position: 6 - i,
            type: Math.random() > 0.5 ? 'yang' : 'yin',
            changing: Math.random() > 0.3
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
    } else if (children) {
      // 如果有结果且不在生成中，直接显示
      setStage('complete')
    } else {
      setStage('idle')
      setHexagramLines([])
    }
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
                    key={index}
                    className={`hexagram-line ${line.type} ${line.changing ? 'changing' : ''}`}
                    style={{
                      animationDelay: `${index * 0.1}s`
                    }}
                  >
                    {line.type === 'yang' ? (
                      <div className="yang-line">━━━</div>
                    ) : (
                      <div className="yin-line">━ ━</div>
                    )}
                    {line.changing && (
                      <span className="changing-indicator">○</span>
                    )}
                  </div>
                ))}
                {hexagramLines.length < 6 && (
                  <div className="hexagram-loading-dots">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                )}
              </div>
              <p className="text-center text-gray-400 mt-4 text-sm">
                正在生成卦象...
              </p>
            </div>
          </CardContent>
        </Card>
      )}

      {stage === 'revealing' && (
        <div className="hexagram-revealing">
          <div className="hexagram-glow"></div>
        </div>
      )}

      {stage === 'complete' && children && (
        <div className="hexagram-complete">
          {children}
        </div>
      )}
    </div>
  )
}
