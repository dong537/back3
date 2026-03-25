import { useState } from 'react'
import { Coins, RotateCcw } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Button from './Button'
import { resolvePageLocale } from '../utils/displayText'

const COIN_DIVINATION_COPY = {
  'zh-CN': {
    roundLabel: (round) => `第 ${round} 爻（从下往上）`,
    completed: '摇卦完成',
    currentLine: (round) => `第 ${round} 爻`,
    movingLine: '动爻，将触发阴阳变化',
    finishedLines: '已完成的爻：',
    shaking: '摇卦中...',
    shake: '摇卦',
    done: '摇卦完成',
    restart: '重新摇卦',
    yaoTypes: {
      oldYin: '老阴',
      oldYang: '老阳',
      youngYin: '少阴',
      youngYang: '少阳',
    },
    coinFront: '阴',
    coinBack: '阳',
  },
  'en-US': {
    roundLabel: (round) => `Line ${round} (bottom to top)`,
    completed: 'Casting complete',
    currentLine: (round) => `Line ${round}`,
    movingLine: 'Moving line, polarity will change',
    finishedLines: 'Completed lines:',
    shaking: 'Casting...',
    shake: 'Cast Coins',
    done: 'Casting complete',
    restart: 'Cast Again',
    yaoTypes: {
      oldYin: 'Old Yin',
      oldYang: 'Old Yang',
      youngYin: 'Young Yin',
      youngYang: 'Young Yang',
    },
    coinFront: 'Yin',
    coinBack: 'Yang',
  },
}

function resolveYao(sum, copy) {
  if (sum === 0) {
    return { yaoType: copy.yaoTypes.oldYin, isDongYao: true }
  }
  if (sum === 3) {
    return { yaoType: copy.yaoTypes.oldYang, isDongYao: true }
  }
  if (sum === 1) {
    return { yaoType: copy.yaoTypes.youngYin, isDongYao: false }
  }
  return { yaoType: copy.yaoTypes.youngYang, isDongYao: false }
}

function getMovingMarker(yaoType, copy) {
  return yaoType === copy.yaoTypes.oldYang ? '○' : '×'
}

export default function CoinDivination({ onComplete }) {
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = COIN_DIVINATION_COPY[locale] || COIN_DIVINATION_COPY['zh-CN']
  const [currentRound, setCurrentRound] = useState(0)
  const [rounds, setRounds] = useState([])
  const [isShaking, setIsShaking] = useState(false)
  const [coins, setCoins] = useState([null, null, null])

  const shakeCoins = () => {
    if (isShaking || currentRound >= 6) return

    setIsShaking(true)

    const shakeInterval = setInterval(() => {
      setCoins([
        Math.floor(Math.random() * 2),
        Math.floor(Math.random() * 2),
        Math.floor(Math.random() * 2),
      ])
    }, 100)

    setTimeout(() => {
      clearInterval(shakeInterval)

      const finalCoins = [
        Math.floor(Math.random() * 2),
        Math.floor(Math.random() * 2),
        Math.floor(Math.random() * 2),
      ]

      setCoins(finalCoins)
      setIsShaking(false)

      const sum = finalCoins.reduce((acc, value) => acc + value, 0)
      const { yaoType, isDongYao } = resolveYao(sum, copy)

      const roundResult = {
        round: currentRound + 1,
        coins: [...finalCoins],
        yaoType,
        isDongYao,
        binary: sum === 0 || sum === 1 ? 0 : 1,
      }

      const newRounds = [...rounds, roundResult]
      setRounds(newRounds)

      if (currentRound === 5) {
        setTimeout(() => {
          const changingLines = newRounds
            .map((item, index) => (item.isDongYao ? index + 1 : null))
            .filter((value) => value !== null)
          onComplete(newRounds, changingLines)
        }, 500)
        return
      }

      setCurrentRound(currentRound + 1)
      setTimeout(() => setCoins([null, null, null]), 300)
    }, 1500)
  }

  const reset = () => {
    setCurrentRound(0)
    setRounds([])
    setCoins([null, null, null])
    setIsShaking(false)
  }

  const latestRound = rounds[rounds.length - 1]

  return (
    <div className="space-y-6">
      <div className="text-center">
        <div className="mb-2 text-lg font-medium text-[#f4ece1]">
          {currentRound < 6 ? copy.roundLabel(currentRound + 1) : copy.completed}
        </div>
        <div className="flex justify-center space-x-2">
          {[1, 2, 3, 4, 5, 6].map((num) => (
            <div
              key={num}
              className={`flex h-8 w-8 items-center justify-center rounded-full border text-sm ${
                num <= currentRound
                  ? 'border-[#d0a85b]/40 bg-[#6a4a1e]/22 text-[#f0d9a5]'
                  : num === currentRound + 1
                    ? 'border-[#a34224]/40 bg-[#7a3218]/18 text-[#e19a84]'
                    : 'border-white/10 bg-white/[0.03] text-[#8f7b66]'
              }`}
            >
              {num}
            </div>
          ))}
        </div>
      </div>

      <div className="flex items-center justify-center space-x-4 py-8">
        {coins.map((coin, index) => (
          <div
            key={`coin-${index}`}
            className={`relative flex h-20 w-20 items-center justify-center rounded-full border-4 text-2xl font-bold transition-all duration-300 ${
              coin === null
                ? 'border-white/10 bg-white/[0.04] text-[#8f7b66]'
                : coin === 0
                  ? 'border-[#a34224]/40 bg-[#7a3218]/22 text-[#f4ece1]'
                  : 'border-[#d0a85b]/40 bg-[#6a4a1e]/22 text-[#f0d9a5]'
            } ${isShaking ? 'animate-bounce' : ''}`}
          >
            {coin === null ? (
              <Coins className="h-10 w-10 text-[#8f7b66]" />
            ) : coin === 0 ? (
              copy.coinFront
            ) : (
              copy.coinBack
            )}
          </div>
        ))}
      </div>

      {latestRound && (
        <div className="text-center">
          <div className="inline-block rounded-[22px] border border-[#d0a85b]/20 bg-[#6a4a1e]/12 px-4 py-3">
            <div className="mb-1 text-sm text-[#8f7b66]">
              {copy.currentLine(latestRound.round)}
            </div>
            <div className="text-lg font-bold text-[#f0d9a5]">
              {latestRound.yaoType}
              {latestRound.isDongYao && (
                <span className="ml-2 text-[#e19a84]">
                  {getMovingMarker(latestRound.yaoType, copy)}
                </span>
              )}
            </div>
            {latestRound.isDongYao && (
              <div className="mt-1 text-xs text-[#e19a84]">
                {copy.movingLine}
              </div>
            )}
          </div>
        </div>
      )}

      {rounds.length > 0 && (
        <div className="space-y-2">
          <div className="text-sm font-medium text-[#8f7b66]">
            {copy.finishedLines}
          </div>
          <div className="grid grid-cols-3 gap-2">
            {rounds.map((round) => (
              <div
                key={round.round}
                className={`rounded-[18px] border p-2 text-center text-xs ${
                  round.isDongYao
                    ? 'border-[#a34224]/26 bg-[#7a3218]/12'
                    : 'border-white/10 bg-white/[0.03]'
                }`}
              >
                <div className="font-bold text-[#f4ece1]">{round.yaoType}</div>
                <div className="text-[#8f7b66]">
                  {round.coins
                    .map((coin) => (coin === 0 ? copy.coinFront : copy.coinBack))
                    .join('')}
                </div>
                {round.isDongYao && (
                  <div className="mt-1 font-bold text-[#e19a84]">
                    {getMovingMarker(round.yaoType, copy)}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="flex justify-center space-x-4">
        {currentRound < 6 ? (
          <Button
            onClick={shakeCoins}
            disabled={isShaking}
            loading={isShaking}
            className="btn-primary-theme"
            size="lg"
          >
            <Coins size={20} />
            <span>{isShaking ? copy.shaking : copy.shake}</span>
          </Button>
        ) : (
          <div className="space-y-2 text-center">
            <div className="font-bold text-[#dcb86f]">✦ {copy.done}</div>
            <Button onClick={reset} variant="secondary" size="sm">
              <RotateCcw size={16} />
              <span>{copy.restart}</span>
            </Button>
          </div>
        )}
      </div>
    </div>
  )
}
