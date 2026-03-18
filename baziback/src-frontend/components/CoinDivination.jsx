import { useState, useEffect } from 'react'
import { Coins, RotateCcw } from 'lucide-react'
import Button from './Button'

/**
 * 三枚铜钱摇卦组件
 */
export default function CoinDivination({ onComplete, question }) {
  const [currentRound, setCurrentRound] = useState(0) // 当前第几爻（0-5，对应1-6爻）
  const [rounds, setRounds] = useState([]) // 存储6次摇卦结果
  const [isShaking, setIsShaking] = useState(false)
  const [coins, setCoins] = useState([null, null, null]) // 三枚硬币状态：null=未摇，0=字(阴)，1=背(阳)

  // 摇卦
  const shakeCoins = () => {
    if (isShaking || currentRound >= 6) return
    
    setIsShaking(true)
    
    // 模拟摇卦动画
    const shakeInterval = setInterval(() => {
      setCoins([
        Math.floor(Math.random() * 2),
        Math.floor(Math.random() * 2),
        Math.floor(Math.random() * 2)
      ])
    }, 100)
    
    // 1秒后停止，确定结果
    setTimeout(() => {
      clearInterval(shakeInterval)
      
      // 随机生成最终结果（但确保有老阳或老阴的概率）
      const finalCoins = [
        Math.floor(Math.random() * 2),
        Math.floor(Math.random() * 2),
        Math.floor(Math.random() * 2)
      ]
      
      setCoins(finalCoins)
      setIsShaking(false)
      
      // 计算爻象
      const sum = finalCoins.reduce((a, b) => a + b, 0)
      let yaoType = ''
      let isDongYao = false
      
      if (sum === 0) {
        // 三字 = 老阴 = 动爻
        yaoType = '老阴'
        isDongYao = true
      } else if (sum === 3) {
        // 三背 = 老阳 = 动爻
        yaoType = '老阳'
        isDongYao = true
      } else if (sum === 1) {
        // 一字二背 = 少阴
        yaoType = '少阴'
      } else {
        // 二背一字 = 少阳
        yaoType = '少阳'
      }
      
      // 保存本轮结果
      const roundResult = {
        round: currentRound + 1,
        coins: [...finalCoins],
        yaoType: yaoType,
        isDongYao: isDongYao,
        binary: sum === 0 || sum === 1 ? 0 : 1 // 老阴和少阴为0，老阳和少阳为1
      }
      
      const newRounds = [...rounds, roundResult]
      setRounds(newRounds)
      
      // 如果完成6次摇卦，调用完成回调
      if (currentRound === 5) {
        setTimeout(() => {
          const changingLines = newRounds
            .map((r, idx) => r.isDongYao ? idx + 1 : null)
            .filter(x => x !== null)
          onComplete(newRounds, changingLines)
        }, 500)
      } else {
        setCurrentRound(currentRound + 1)
        // 重置硬币状态
        setTimeout(() => setCoins([null, null, null]), 300)
      }
    }, 1500)
  }

  // 重新开始
  const reset = () => {
    setCurrentRound(0)
    setRounds([])
    setCoins([null, null, null])
    setIsShaking(false)
  }

  return (
    <div className="space-y-6">
      {/* 进度提示 */}
      <div className="text-center">
        <div className="text-lg font-medium mb-2">
          {currentRound < 6 ? `第 ${currentRound + 1} 爻（从下往上）` : '摇卦完成'}
        </div>
        <div className="flex justify-center space-x-2">
          {[1, 2, 3, 4, 5, 6].map((num) => (
            <div
              key={num}
              className={`w-8 h-8 rounded-full flex items-center justify-center text-sm ${
                num <= currentRound
                  ? 'bg-purple-500 text-white'
                  : num === currentRound + 1
                  ? 'bg-purple-300 text-purple-900'
                  : 'bg-gray-200 text-gray-500'
              }`}
            >
              {num}
            </div>
          ))}
        </div>
      </div>

      {/* 三枚硬币 */}
      <div className="flex justify-center items-center space-x-4 py-8">
        {coins.map((coin, index) => (
          <div
            key={index}
            className={`relative w-20 h-20 rounded-full border-4 flex items-center justify-center text-2xl font-bold transition-all duration-300 ${
              coin === null
                ? 'bg-gray-300 border-gray-400'
                : coin === 0
                ? 'bg-blue-400 border-blue-600 text-white'
                : 'bg-yellow-400 border-yellow-600 text-yellow-900'
            } ${isShaking ? 'animate-bounce' : ''}`}
          >
            {coin === null ? (
              <Coins className="w-10 h-10 text-gray-500" />
            ) : coin === 0 ? (
              '字'
            ) : (
              '背'
            )}
          </div>
        ))}
      </div>

      {/* 当前爻象显示 */}
      {rounds.length > 0 && (
        <div className="text-center">
          <div className="inline-block px-4 py-2 rounded-lg bg-purple-500/20 border border-purple-500/50">
            <div className="text-sm text-gray-400 mb-1">第 {rounds[rounds.length - 1].round} 爻</div>
            <div className="text-lg font-bold text-purple-400">
              {rounds[rounds.length - 1].yaoType}
              {rounds[rounds.length - 1].isDongYao && (
                <span className="ml-2 text-red-400">
                  {rounds[rounds.length - 1].yaoType === '老阳' ? '○' : '×'}
                </span>
              )}
            </div>
            {rounds[rounds.length - 1].isDongYao && (
              <div className="text-xs text-red-400 mt-1">动爻 - 将触发阴阳互变</div>
            )}
          </div>
        </div>
      )}

      {/* 已完成的爻象列表 */}
      {rounds.length > 0 && (
        <div className="space-y-2">
          <div className="text-sm font-medium text-gray-400">已完成的爻：</div>
          <div className="grid grid-cols-3 gap-2">
            {rounds.map((round) => (
              <div
                key={round.round}
                className={`p-2 rounded text-xs text-center ${
                  round.isDongYao
                    ? 'bg-red-500/20 border border-red-500/50'
                    : 'bg-gray-500/20 border border-gray-500/30'
                }`}
              >
                <div className="font-bold">{round.yaoType}</div>
                <div className="text-gray-400">
                  {round.coins.map(c => (c === 0 ? '字' : '背')).join('')}
                </div>
                {round.isDongYao && (
                  <div className="text-red-400 font-bold mt-1">
                    {round.yaoType === '老阳' ? '○' : '×'}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 操作按钮 */}
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
            <span>{isShaking ? '摇卦中...' : '摇卦'}</span>
          </Button>
        ) : (
          <div className="text-center space-y-2">
            <div className="text-green-400 font-bold">✓ 摇卦完成</div>
            <Button onClick={reset} variant="secondary" size="sm">
              <RotateCcw size={16} />
              <span>重新摇卦</span>
            </Button>
          </div>
        )}
      </div>
    </div>
  )
}
