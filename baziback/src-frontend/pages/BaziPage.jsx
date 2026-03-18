import { useState, useMemo, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Calendar, Star, ArrowLeft, Sparkles, Coins, ChevronRight, Users, Flame, History } from 'lucide-react'
import ThinkingChain from '../components/ThinkingChain'
import { historyStorage, favoritesStorage } from '../utils/storage'
import { points } from '../utils/referral'
import { POINTS_COST } from '../utils/pointsConfig'
import { baziApi, deepseekApi, calculationRecordApi } from '../api'
import { logger } from '../utils/logger'
import { toast } from '../components/Toast'
import { useAuth } from '../context/AuthContext'

const getWuxingFromTianGan = (tianGan) => {
  const wuxingMap = { '甲': 'wood', '乙': 'wood', '丙': 'fire', '丁': 'fire', '戊': 'earth', '己': 'earth', '庚': 'metal', '辛': 'metal', '壬': 'water', '癸': 'water' }
  return wuxingMap[tianGan] || null
}

const getWuxingFromDiZhi = (diZhi) => {
  const wuxingMap = { '子': 'water', '亥': 'water', '寅': 'wood', '卯': 'wood', '巳': 'fire', '午': 'fire', '申': 'metal', '酉': 'metal', '辰': 'earth', '戌': 'earth', '丑': 'earth', '未': 'earth' }
  return wuxingMap[diZhi] || null
}

// 十神图标和颜色配置
const shiShenConfig = {
  '偏财': { icon: '💰', color: 'from-amber-400 to-orange-500', bgColor: 'bg-amber-50', textColor: 'text-amber-700', borderColor: 'border-amber-200' },
  '正财': { icon: '💎', color: 'from-yellow-400 to-amber-500', bgColor: 'bg-yellow-50', textColor: 'text-yellow-700', borderColor: 'border-yellow-200' },
  '正官': { icon: '👔', color: 'from-blue-400 to-indigo-500', bgColor: 'bg-blue-50', textColor: 'text-blue-700', borderColor: 'border-blue-200' },
  '七杀': { icon: '⚔️', color: 'from-red-400 to-rose-500', bgColor: 'bg-red-50', textColor: 'text-red-700', borderColor: 'border-red-200' },
  '正印': { icon: '📚', color: 'from-emerald-400 to-teal-500', bgColor: 'bg-emerald-50', textColor: 'text-emerald-700', borderColor: 'border-emerald-200' },
  '偏印': { icon: '🔮', color: 'from-purple-400 to-violet-500', bgColor: 'bg-purple-50', textColor: 'text-purple-700', borderColor: 'border-purple-200' },
  '比肩': { icon: '🤝', color: 'from-cyan-400 to-blue-500', bgColor: 'bg-cyan-50', textColor: 'text-cyan-700', borderColor: 'border-cyan-200' },
  '劫财': { icon: '💫', color: 'from-orange-400 to-red-500', bgColor: 'bg-orange-50', textColor: 'text-orange-700', borderColor: 'border-orange-200' },
  '食神': { icon: '🍀', color: 'from-green-400 to-emerald-500', bgColor: 'bg-green-50', textColor: 'text-green-700', borderColor: 'border-green-200' },
  '伤官': { icon: '✨', color: 'from-pink-400 to-rose-500', bgColor: 'bg-pink-50', textColor: 'text-pink-700', borderColor: 'border-pink-200' },
}

// 详细数据展示组件 - 根据实际数据动态展示
function BaziDetailTabs({ result, wuxingData }) {
  const [detailTab, setDetailTab] = useState(null)
  
  // 兼容多种数据结构
  const pillars = result?.详细各柱信息 || result?.八字各柱信息 || result?.四柱详情 || {}
  const nayin = result?.纳音 || {}
  const kongwang = result?.空亡 || {}
  const xingyun = result?.星运 || {}
  const taimingshen = result?.胎命身 || {}
  const shensha = result?.神煞 || {}
  const dayunData = result?.大运数据 || result?.大运 || {}
  const qiyunInfo = result?.起运信息 || {}
  const riZhuInfo = result?.日柱等级信息 || {}
  const xiYongShen = result?.喜用神分析 || {}
  const geJuInfo = result?.参考格局信息 || {}
  
  const wuxingNames = { wood: '木', fire: '火', earth: '土', metal: '金', water: '水' }
  const wuxingColors = { 
    wood: 'from-green-400 to-emerald-500', 
    fire: 'from-red-400 to-orange-500', 
    earth: 'from-yellow-400 to-amber-500', 
    metal: 'from-gray-300 to-slate-400', 
    water: 'from-blue-400 to-cyan-500' 
  }

  // 动态计算可用的标签页
  const availableTabs = useMemo(() => {
    const tabs = []
    // 基础信息：胎命身、纳音、空亡、星运等
    const hasBasicInfo = Object.keys(taimingshen).length > 0 || 
                         Object.keys(nayin).length > 0 || 
                         Object.keys(kongwang).length > 0 ||
                         Object.keys(xingyun).length > 0 ||
                         Object.keys(pillars).length > 0
    if (hasBasicInfo) tabs.push({ key: '基础信息', icon: '📋' })
    
    // 五行分析
    if (wuxingData && Object.values(wuxingData).some(v => v > 0)) tabs.push({ key: '五行分析', icon: '🔥' })
    
    // 十神格局：日柱等级、格局、喜用神等
    const hasShiShen = Object.keys(riZhuInfo).length > 0 || 
                       (geJuInfo && (typeof geJuInfo === 'string' || Object.keys(geJuInfo).length > 0)) || 
                       Object.keys(xiYongShen).length > 0
    if (hasShiShen) tabs.push({ key: '十神格局', icon: '⭐' })
    
    // 大运流年
    const dayunList = dayunData?.大运列表 || dayunData?.list || (Array.isArray(dayunData) ? dayunData : [])
    if (dayunList.length > 0 || Object.keys(qiyunInfo).length > 0) tabs.push({ key: '大运流年', icon: '🌊' })
    
    // 神煞
    const hasShenSha = Object.keys(shensha).length > 0
    if (hasShenSha) tabs.push({ key: '神煞', icon: '✨' })
    
    return tabs
  }, [result, wuxingData, pillars, taimingshen, nayin, kongwang, xingyun, riZhuInfo, geJuInfo, xiYongShen, dayunData, qiyunInfo, shensha])

  // 设置默认标签
  const currentTab = detailTab || availableTabs[0]?.key
  if (availableTabs.length === 0) return null

  const getWuxingStrength = (value) => {
    if (value >= 50) return { text: '旺', color: 'text-green-400 bg-green-500/20' }
    if (value >= 25) return { text: '平', color: 'text-yellow-400 bg-yellow-500/20' }
    return { text: '弱', color: 'text-red-400 bg-red-500/20' }
  }

  const DataItem = ({ label, value, highlight = false }) => {
    if (value === undefined || value === null || value === '' || value === '—') return null
    // 处理值可能是对象或数组的情况
    let displayVal = value
    if (typeof value === 'object') {
      if (Array.isArray(value)) {
        displayVal = value.join('、')
      } else {
        displayVal = Object.values(value).join(' ')
      }
    }
    return (
      <div className="flex justify-between items-center text-sm bg-[#1e1e3f]/60 rounded-lg p-3 border border-purple-500/10">
        <span className="text-purple-300/80">{label}</span>
        <span className={highlight ? 'text-amber-400 font-medium' : 'text-white'}>{displayVal}</span>
      </div>
    )
  }

  const PillarDataGrid = ({ label, dataSource }) => {
    if (!dataSource || typeof dataSource !== 'object') return null
    const pillarKeys = ['年', '月', '日', '时']
    // 检查是否有数据
    const hasData = pillarKeys.some(k => {
      const val = dataSource[k]
      return val !== undefined && val !== null && val !== ''
    })
    if (!hasData) return null
    
    return (
      <div className="mb-4">
        {label && <div className="text-xs text-purple-400/70 mb-2 font-medium">{label}</div>}
        <div className="grid grid-cols-4 gap-2">
          {pillarKeys.map(key => {
            let val = dataSource[key]
            // 处理值可能是对象或数组的情况
            if (val && typeof val === 'object') {
              if (Array.isArray(val)) {
                val = val.join(' ')
              } else {
                val = Object.values(val).join(' ')
              }
            }
            return (
              <div key={key} className="text-center">
                <div className="text-[10px] text-purple-500/60 mb-1">{key}</div>
                <div className="bg-[#1e1e3f]/80 rounded-lg py-2 px-1 text-sm text-white border border-purple-500/20">
                  {val || '—'}
                </div>
              </div>
            )
          })}
        </div>
      </div>
    )
  }
  
  // 从详细各柱信息中提取特定字段
  const PillarFieldGrid = ({ dataKey, label }) => {
    const pillarKeys = ['年', '月', '日', '时']
    const hasData = pillarKeys.some(k => pillars[k]?.[dataKey])
    if (!hasData) return null
    
    return (
      <div className="mb-4">
        {label && <div className="text-xs text-purple-400/70 mb-2 font-medium">{label}</div>}
        <div className="grid grid-cols-4 gap-2">
          {pillarKeys.map(key => {
            let val = pillars[key]?.[dataKey]
            // 处理值可能是数组或对象的情况
            if (val && typeof val === 'object') {
              if (Array.isArray(val)) {
                val = val.join(' ')
              } else {
                val = Object.values(val).join(' ')
              }
            }
            return (
              <div key={key} className="text-center">
                <div className="text-[10px] text-purple-500/60 mb-1">{key}</div>
                <div className="bg-[#1e1e3f]/80 rounded-lg py-2 px-1 text-sm text-white border border-purple-500/20">
                  {val || '—'}
                </div>
              </div>
            )
          })}
        </div>
      </div>
    )
  }

  return (
    <div className="mt-4 bg-gradient-to-br from-[#0d0d1a] via-[#1a1a2e] to-[#0f0f23] rounded-2xl overflow-hidden border border-purple-500/20 shadow-xl shadow-purple-900/20">
      {/* 标签页导航 */}
      <div className="flex border-b border-purple-500/20 bg-[#0a0a15]/50">
        {availableTabs.map(tab => (
          <button
            key={tab.key}
            onClick={() => setDetailTab(tab.key)}
            className={`flex-1 px-3 py-3.5 text-sm font-medium whitespace-nowrap transition-all relative ${
              currentTab === tab.key ? 'text-amber-400' : 'text-purple-400/70 hover:text-purple-300'
            }`}
          >
            <span className="mr-1.5">{tab.icon}</span>
            {tab.key}
            {currentTab === tab.key && (
              <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-12 h-0.5 bg-gradient-to-r from-amber-400 to-orange-400 rounded-full" />
            )}
          </button>
        ))}
      </div>

      <div className="p-5">
        {/* 基础信息 */}
        {currentTab === '基础信息' && (
          <div className="space-y-5">
            {/* 胎命身信息 */}
            {Object.keys(taimingshen).length > 0 && (
              <div>
                <h4 className="text-amber-400 font-medium mb-3 flex items-center text-sm">
                  <span className="w-1 h-4 bg-amber-400 rounded-full mr-2" />胎命身
                </h4>
                <div className="grid grid-cols-3 gap-2">
                  <DataItem label="胎元" value={taimingshen.胎元} />
                  <DataItem label="命宫" value={taimingshen.命宫} />
                  <DataItem label="身宫" value={taimingshen.身宫} />
                </div>
              </div>
            )}
            
            {/* 纳音 */}
            {Object.keys(nayin).length > 0 && (
              <PillarDataGrid label="纳音" dataSource={nayin} />
            )}
            
            {/* 空亡 */}
            {Object.keys(kongwang).length > 0 && (
              <PillarDataGrid label="空亡" dataSource={kongwang} />
            )}
            
            {/* 星运 */}
            {Object.keys(xingyun).length > 0 && (
              <PillarDataGrid label="星运" dataSource={xingyun} />
            )}
            
            {/* 详细各柱信息 */}
            {Object.keys(pillars).length > 0 && (
              <>
                <PillarFieldGrid dataKey="十神" label="十神" />
                <PillarFieldGrid dataKey="藏干" label="藏干" />
                <PillarFieldGrid dataKey="十二长生" label="十二长生" />
              </>
            )}
            
            {/* 自坐 */}
            {result?.自坐 && Object.keys(result.自坐).length > 0 && (
              <PillarDataGrid label="自坐" dataSource={result.自坐} />
            )}
          </div>
        )}

        {/* 五行分析 */}
        {currentTab === '五行分析' && wuxingData && (
          <div className="space-y-5">
            <h4 className="text-amber-400 font-medium mb-4 flex items-center text-sm">
              <span className="w-1 h-4 bg-amber-400 rounded-full mr-2" />五行力量分布
            </h4>
            <div className="space-y-3">
              {Object.entries(wuxingData).map(([key, value]) => {
                const strength = getWuxingStrength(value)
                return (
                  <div key={key} className="flex items-center space-x-3">
                    <div className={`w-8 h-8 rounded-lg bg-gradient-to-br ${wuxingColors[key]} flex items-center justify-center text-white text-sm font-bold shadow-lg`}>
                      {wuxingNames[key]}
                    </div>
                    <div className="flex-1 h-3 bg-[#1e1e3f] rounded-full overflow-hidden">
                      <div className={`h-full bg-gradient-to-r ${wuxingColors[key]} rounded-full transition-all duration-700`} style={{ width: `${Math.max(value, 5)}%` }} />
                    </div>
                    <span className="text-white w-10 text-right text-sm font-medium">{value}%</span>
                    <span className={`text-xs px-2 py-1 rounded-md font-medium ${strength.color}`}>{strength.text}</span>
                  </div>
                )
              })}
            </div>
            {(result?.四柱详情?.日?.天干 || result?.日主五行) && (
              <div className="p-4 bg-gradient-to-r from-purple-900/30 to-indigo-900/30 rounded-xl border border-purple-500/20">
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center space-x-4">
                    {result?.四柱详情?.日?.天干 && <div><span className="text-purple-400/70">日主：</span><span className="text-white font-bold text-lg ml-1">{result.四柱详情.日.天干}</span></div>}
                    {result?.日主五行 && <div><span className="text-purple-400/70">五行：</span><span className="text-amber-400 font-medium ml-1">{result.日主五行}</span></div>}
                  </div>
                  {result?.身强弱 && <span className={`px-3 py-1 rounded-full text-xs font-medium ${result.身强弱 === '身强' ? 'bg-green-500/20 text-green-400' : 'bg-orange-500/20 text-orange-400'}`}>{result.身强弱}</span>}
                </div>
              </div>
            )}
          </div>
        )}

        {/* 十神格局 */}
        {currentTab === '十神格局' && (
          <div className="space-y-5">
            {/* 日柱等级信息 */}
            {Object.keys(riZhuInfo).length > 0 && (
              <div>
                <h4 className="text-amber-400 font-medium mb-3 flex items-center text-sm">
                  <span className="w-1 h-4 bg-amber-400 rounded-full mr-2" />日柱等级
                </h4>
                <div className="grid grid-cols-2 gap-2">
                  <DataItem label="日柱" value={riZhuInfo.日柱} />
                  <DataItem label="等级" value={riZhuInfo.等级} highlight />
                  <DataItem label="日柱特点" value={riZhuInfo.日柱特点} />
                  <DataItem label="纳音" value={riZhuInfo.纳音} />
                </div>
                {riZhuInfo.解读 && (
                  <div className="mt-3 p-3 bg-[#1e1e3f]/40 rounded-lg text-sm text-purple-200/80 leading-relaxed">
                    {riZhuInfo.解读}
                  </div>
                )}
              </div>
            )}
            
            {/* 参考格局信息 */}
            {geJuInfo && (typeof geJuInfo === 'string' ? geJuInfo : Object.keys(geJuInfo).length > 0) && (
              <div>
                <h4 className="text-amber-400 font-medium mb-3 flex items-center text-sm">
                  <span className="w-1 h-4 bg-amber-400 rounded-full mr-2" />参考格局
                </h4>
                <div className="p-4 bg-gradient-to-r from-amber-900/20 to-orange-900/20 rounded-xl border border-amber-500/20">
                  <span className="text-amber-400 font-bold text-lg">
                    {typeof geJuInfo === 'string' ? geJuInfo : (geJuInfo.格局名称 || geJuInfo.格局 || JSON.stringify(geJuInfo))}
                  </span>
                  {typeof geJuInfo === 'object' && geJuInfo.说明 && <p className="text-purple-200/70 text-sm mt-2">{geJuInfo.说明}</p>}
                </div>
              </div>
            )}
            
            {/* 喜用神分析 */}
            {Object.keys(xiYongShen).length > 0 && (
              <div>
                <h4 className="text-amber-400 font-medium mb-3 flex items-center text-sm">
                  <span className="w-1 h-4 bg-amber-400 rounded-full mr-2" />喜用神分析
                </h4>
                <div className="grid grid-cols-2 gap-2">
                  <DataItem label="喜神" value={xiYongShen.喜神} highlight />
                  <DataItem label="用神" value={xiYongShen.用神} highlight />
                  <DataItem label="忌神" value={xiYongShen.忌神} />
                  <DataItem label="仇神" value={xiYongShen.仇神} />
                  <DataItem label="闲神" value={xiYongShen.闲神} />
                  <DataItem label="日主强弱" value={xiYongShen.日主强弱} />
                </div>
              </div>
            )}
          </div>
        )}

        {/* 大运流年 */}
        {currentTab === '大运流年' && (
          <div className="space-y-5">
            {/* 起运信息 */}
            {Object.keys(qiyunInfo).length > 0 && (
              <div>
                <h4 className="text-amber-400 font-medium mb-3 flex items-center text-sm">
                  <span className="w-1 h-4 bg-amber-400 rounded-full mr-2" />起运信息
                </h4>
                <div className="grid grid-cols-2 gap-2">
                  <DataItem label="起运年龄" value={qiyunInfo.起运年龄} />
                  <DataItem label="起运年份" value={qiyunInfo.起运年份} />
                  <DataItem label="交运时间" value={qiyunInfo.交运时间} />
                  <DataItem label="起运描述" value={qiyunInfo.起运描述} />
                </div>
              </div>
            )}
            
            {/* 大运列表 */}
            {(() => {
              const dayunList = dayunData?.大运列表 || dayunData?.list || (Array.isArray(dayunData) ? dayunData : [])
              if (dayunList.length === 0) return null
              return (
                <div>
                  <h4 className="text-amber-400 font-medium mb-4 flex items-center text-sm">
                    <span className="w-1 h-4 bg-amber-400 rounded-full mr-2" />大运
                  </h4>
                  <div className="overflow-x-auto pb-2 -mx-2 px-2">
                    <div className="flex space-x-3">
                      {dayunList.slice(0, 10).map((dy, idx) => (
                        <div key={idx} className="flex-shrink-0 w-[72px] text-center group">
                          <div className="text-xs text-purple-400/70 mb-1.5 font-medium">
                            {dy.起运年龄 || dy.年龄 || dy.岁数 || `${(qiyunInfo.起运年龄 || 4) + idx * 10}岁`}
                          </div>
                          <div className="bg-gradient-to-b from-[#1e1e3f] to-[#151530] rounded-xl p-3 border border-purple-500/20 group-hover:border-amber-500/40 transition-colors">
                            <div className="text-xl font-bold text-white mb-0.5">{dy.天干}</div>
                            <div className="text-xl font-bold text-amber-400">{dy.地支}</div>
                            {dy.纳音 && <div className="text-[10px] text-purple-400/60 mt-1.5">{dy.纳音}</div>}
                          </div>
                          {(dy.年份 || dy.起始年 || dy.开始年份) && (
                            <div className="text-[10px] text-purple-400/50 mt-1.5">{dy.年份 || dy.起始年 || dy.开始年份}</div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )
            })()}
            
            {/* 流年数据 */}
            {result?.流年数据 && result.流年数据.length > 0 && (
              <div>
                <h4 className="text-amber-400 font-medium mb-3 flex items-center text-sm">
                  <span className="w-1 h-4 bg-amber-400 rounded-full mr-2" />近期流年
                </h4>
                <div className="grid grid-cols-5 gap-2">
                  {result.流年数据.slice(0, 10).map((ln, idx) => (
                    <div key={idx} className="text-center bg-[#1e1e3f]/60 rounded-lg p-2 border border-purple-500/10">
                      <div className="text-[10px] text-purple-400/60">{ln.年份}</div>
                      <div className="text-sm font-bold text-white">{ln.天干}{ln.地支}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {/* 神煞 */}
        {currentTab === '神煞' && (
          <div className="space-y-5">
            {/* 按柱展示神煞 */}
            {Object.entries(shensha).map(([pillar, shenshaList]) => {
              if (!shenshaList || (Array.isArray(shenshaList) && shenshaList.length === 0)) return null
              const list = Array.isArray(shenshaList) ? shenshaList : [shenshaList]
              return (
                <div key={pillar}>
                  <h4 className="text-amber-400 font-medium mb-3 flex items-center text-sm">
                    <span className="w-1 h-4 bg-amber-400 rounded-full mr-2" />{pillar}柱神煞
                  </h4>
                  <div className="flex flex-wrap gap-2">
                    {list.map((shen, idx) => {
                      // 处理shen可能是对象的情况
                      const shenStr = typeof shen === 'object' ? (shen.名称 || shen.name || JSON.stringify(shen)) : String(shen)
                      // 判断吉凶
                      const isJi = ['天乙贵人', '文昌贵人', '太极贵人', '天德贵人', '月德贵人', '福星贵人', '天厨贵人', '金舆', '驿马', '华盖', '将星', '天医', '禄神', '羊刃'].some(j => shenStr.includes(j))
                      const isXiong = ['劫煞', '亡神', '桃花', '咸池', '孤辰', '寡宿', '丧门', '吊客', '白虎', '天狗', '血刃', '飞刃'].some(x => shenStr.includes(x))
                      const colorClass = isJi ? 'bg-green-500/15 text-green-400 border-green-500/20' : 
                                        isXiong ? 'bg-red-500/15 text-red-400 border-red-500/20' : 
                                        'bg-purple-500/15 text-purple-300 border-purple-500/20'
                      return (
                        <span key={idx} className={`px-3 py-1.5 rounded-lg text-sm border ${colorClass}`}>{shenStr}</span>
                      )
                    })}
                  </div>
                </div>
              )
            })}
            
            {/* 如果没有按柱的神煞数据，显示旧格式 */}
            {Object.keys(shensha).length === 0 && (
              <>
                {result?.吉神?.length > 0 && (
                  <div>
                    <h4 className="text-green-400 font-medium mb-3 flex items-center text-sm">
                      <span className="w-1 h-4 bg-green-400 rounded-full mr-2" />吉神
                    </h4>
                    <div className="flex flex-wrap gap-2">
                      {result.吉神.map((shen, idx) => (
                        <span key={idx} className="px-3 py-1.5 bg-green-500/15 text-green-400 rounded-lg text-sm border border-green-500/20">{shen}</span>
                      ))}
                    </div>
                  </div>
                )}
                {result?.凶神?.length > 0 && (
                  <div>
                    <h4 className="text-red-400 font-medium mb-3 flex items-center text-sm">
                      <span className="w-1 h-4 bg-red-400 rounded-full mr-2" />凶神
                    </h4>
                    <div className="flex flex-wrap gap-2">
                      {result.凶神.map((shen, idx) => (
                        <span key={idx} className="px-3 py-1.5 bg-red-500/15 text-red-400 rounded-lg text-sm border border-red-500/20">{shen}</span>
                      ))}
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

// 状态存储key
const BAZI_STATE_KEY = 'bazi_page_state'

export default function BaziPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { credits, isLoggedIn, refreshCredits, spendCredits, canSpendCredits } = useAuth()
  const [birthDate, setBirthDate] = useState('')
  const [birthTime, setBirthTime] = useState('')
  const [gender, setGender] = useState('male')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [interpretations, setInterpretations] = useState([])
  const [aiLoading, setAiLoading] = useState(false)
  const [aiResult, setAiResult] = useState('')
  const [activeTab, setActiveTab] = useState('shengchen')
  const [userPoints, setUserPoints] = useState(points.get())

  // 页面加载时同步一次后端积分余额
  useEffect(() => {
    if (isLoggedIn) {
      refreshCredits()
    }
  }, [isLoggedIn, refreshCredits])

  // 从详情页返回时恢复状态
  useEffect(() => {
    const savedState = sessionStorage.getItem(BAZI_STATE_KEY)
    if (savedState) {
      try {
        const state = JSON.parse(savedState)
        if (state.result) {
          setResult(state.result)
          setBirthDate(state.birthDate || '')
          setBirthTime(state.birthTime || '')
          setGender(state.gender || 'male')
          setInterpretations(state.interpretations || [])
          setAiResult(state.aiResult || '')
          // 清除保存的状态
          sessionStorage.removeItem(BAZI_STATE_KEY)
        }
      } catch (e) {
        console.error('恢复八字页面状态失败:', e)
        sessionStorage.removeItem(BAZI_STATE_KEY)
      }
    }
  }, [location])

  // 从收藏页跳转回来时，恢复当次排盘结果（尽量恢复）
  useEffect(() => {
    const state = location?.state
    if (!state || !state.fromFavorite) return

    if (state.result) {
      setResult(state.result)
      setLoading(false)
    }
    // 兼容收藏里可能保存的解析列表/AI内容
    if (Array.isArray(state.interpretations)) {
      setInterpretations(state.interpretations)
    }
    if (typeof state.aiResult === 'string') {
      setAiResult(state.aiResult)
    }
  }, [location?.state])

  // 点击解读卡片时保存状态并跳转
  const handleInterpretationClick = (interp) => {
    // 保存当前状态
    const stateToSave = {
      result,
      birthDate,
      birthTime,
      gender,
      interpretations,
      aiResult
    }
    sessionStorage.setItem(BAZI_STATE_KEY, JSON.stringify(stateToSave))
    
    // 跳转到详情页
    navigate(`/bazi/interpretation/${interp.id}`, { 
      state: { interpretation: interp, baziData: result } 
    })
  }

  const handleAnalyze = async () => {
    if (!birthDate || !birthTime) {
      toast.error('请填写完整的出生信息')
      return
    }
    setLoading(true)
    setResult(null)
    setInterpretations([])
    setAiResult('')
    // 清除之前保存的状态
    sessionStorage.removeItem(BAZI_STATE_KEY)
    try {
      const normalizedBirthDate = String(birthDate).replace(/\//g, '-')
      const birthDateTime = `${normalizedBirthDate} ${birthTime}:00`
      const response = await baziApi.generate(birthDateTime, gender === 'male', 4)
      const resultData = response.data
      console.log('八字排盘结果:', resultData) // 调试日志
      setResult(resultData)
      
      // 获取十神解读
      if (resultData) {
        try {
          const interpRes = await fetch('/api/bazi/interpretation/from-bazi-data', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(resultData)
          })
          const interpData = await interpRes.json()
          if (interpData.code === 200 && interpData.data) {
            setInterpretations(interpData.data)
          }
        } catch (e) {
          console.error('获取十神解读失败:', e)
        }
      }
      
      if (resultData?.八字) {
        historyStorage.add({
          type: 'bazi',
          question: `八字排盘 - ${birthDate} ${birthTime}`,
          dataId: (resultData.八字 || '').replace(/\s/g, ''),
          summary: `八字：${resultData.八字} - ${resultData.性别 || (gender === 'male' ? '男' : '女')}`,
          data: resultData
        })
      }
    } catch (error) {
      logger.error('Analysis error:', error)
      toast.error(error?.response?.data?.error || error?.message || '分析失败')
    } finally {
      setLoading(false)
    }
  }

  const handleAIReport = async () => {
    if (!result) return
    const cost = POINTS_COST.AI_INTERPRET
    
    // 已登录用户使用后端积分系统
    if (isLoggedIn) {
      if (!canSpendCredits(cost)) {
        toast.error(`积分不足，AI解读需要 ${cost} 积分，当前余额：${credits}`)
        return
      }
    } else {
      // 未登录用户使用本地积分系统
      if (!points.canSpend(cost)) {
        toast.error(`积分不足，AI解读需要 ${cost} 积分`)
        return
      }
    }
    
    setAiLoading(true)
    setAiResult('')
    try {
      const baZi = result?.八字
      
      // 构建提示词 - 要求纯文本输出
      const prompt = `你是一位精通八字命理的资深命理师，请为用户进行详细的八字分析。

【重要格式要求】
仅输出纯文本，不使用任何 Markdown 或富文本格式。
禁止出现以下符号：井号、星号、反引号、下划线、波浪号、大于号、小于号、方括号、圆括号内的链接格式、竖线，以及以连字符作为项目符号的列表。
使用自然段落和换行来组织内容，用数字序号（如1、2、3）代替符号列表。

【命主信息】
八字：${baZi}
性别：${gender === 'male' ? '男' : '女'}

【分析要求】
请从以下几个方面进行详细分析：

1、八字格局分析
分析日主强弱、五行分布、格局类型

2、性格特点
根据八字分析命主的性格特征、优势和需要注意的地方

3、事业财运
分析适合的职业方向、财运特点、事业发展建议

4、感情婚姻
分析感情特点、婚姻状况、配偶特征

5、健康提示
根据五行分析需要注意的健康问题

6、流年运势
分析近期的运势走向和需要把握的机会

7、综合建议
给出整体的人生建议和注意事项

请用专业、温和的语气，让用户感受到传统命理学的智慧。`

      const response = await deepseekApi.generateReport(prompt)
      
      // 解析AI响应 - 后端返回 Result { code, message, data }
      let aiContent = ''
      if (response.data?.code === 200 && response.data?.data) {
        aiContent = response.data.data
      } else if (typeof response.data === 'string') {
        aiContent = response.data
      } else if (response.data?.content) {
        aiContent = response.data.content
      } else {
        aiContent = '生成报告失败'
      }
      
      // 扣除积分
      if (isLoggedIn) {
        const spendResult = await spendCredits(cost, 'AI八字解读')
        if (spendResult.success) {
          toast.success(`消耗 ${cost} 积分`)
        } else {
          toast.error(spendResult.message || '积分扣除失败')
        }
      } else {
        const spendResult = points.spend(cost, 'AI八字解读')
        if (spendResult.success) {
          setUserPoints(spendResult.newTotal)
          toast.success(`消耗 ${cost} 积分`)
        }
      }
      
      setAiResult(aiContent)
    } catch (error) {
      logger.error('AI report error:', error)
      toast.error('AI报告生成失败，请稍后重试')
      setAiResult('AI报告生成失败，请稍后重试')
    } finally {
      setAiLoading(false)
    }
  }

  const handleFavorite = async () => {
    if (!result) return
    const item = {
      type: 'bazi',
      question: `八字排盘 - ${birthDate}`,
      dataId: result.八字?.replace(/\s/g, '') || '',
      title: `八字：${result.八字}`,
      summary: `${result.性别 || (gender === 'male' ? '男' : '女')} - ${result.生肖}`,
      data: result
    }
    try {
      const favorited = await favoritesStorage.toggle(item)
      toast.success(favorited ? '已收藏' : '已取消收藏')
    } catch (error) {
      logger.error('收藏操作失败:', error)
      toast.error('收藏操作失败')
    }
  }

  const handleSaveRecord = async () => {
    if (!result) return
    try {
      const record = {
        recordType: 'bazi',
        recordTitle: `八字排盘 - ${birthDate}`,
        question: `性别：${gender === 'male' ? '男' : '女'}，起运年龄：${qiYunAge}`,
        summary: `${result.八字} - ${result.生肖}`,
        data: JSON.stringify(result)
      }
      await calculationRecordApi.save(record)
      toast.success('记录已保存')
    } catch (error) {
      toast.error('保存记录失败')
    }
  }

  const wuxingData = useMemo(() => {
    if (!result) return null
    const wuxing = { wood: 0, fire: 0, earth: 0, metal: 0, water: 0 }
    const pillars = result.详细各柱信息 || result.八字各柱信息 || {}
    const pillarKeys = ['年', '月', '日', '时']
    pillarKeys.forEach(key => {
      const pillar = pillars[key]
      if (pillar) {
        const tianGanWuxing = getWuxingFromTianGan(pillar.天干)
        if (tianGanWuxing) wuxing[tianGanWuxing] += 25
        const diZhiWuxing = getWuxingFromDiZhi(pillar.地支)
        if (diZhiWuxing) wuxing[diZhiWuxing] += 25
      }
    })
    return wuxing
  }, [result])

  const renderPillar = (pillar, label) => {
    if (!pillar) return null
    return (
      <div className="text-center">
        <div className="text-xs text-amber-600 font-medium mb-2">{label}</div>
        <div className="bg-gradient-to-b from-amber-50 to-orange-50 rounded-xl p-3 border border-amber-200">
          <div className="text-2xl font-bold text-amber-700 mb-1">{pillar.天干}</div>
          <div className="text-2xl font-bold text-orange-600">{pillar.地支}</div>
          <div className="text-xs text-amber-500 mt-2">{pillar.纳音}</div>
        </div>
      </div>
    )
  }

  const tabs = [
    { key: 'shengchen', label: '生辰' },
    { key: 'xingzuo', label: '星座' },
    { key: 'xingxiu', label: '星宿' },
    { key: 'ziwei', label: '紫微' },
  ]

  return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 via-orange-50 to-white">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-50 bg-white/80 backdrop-blur-xl border-b border-amber-100">
        <div className="px-4 py-3 flex items-center justify-between">
          <button onClick={() => navigate(-1)} className="p-2 hover:bg-amber-100 rounded-xl transition-all">
            <ArrowLeft size={20} className="text-gray-700" />
          </button>
          <div className="flex items-center space-x-2">
            <span className="text-lg font-bold text-gray-800">自己</span>
            <span className="text-gray-400">♈</span>
          </div>
          <div className="flex items-center space-x-2">
            <button className="p-2 hover:bg-amber-100 rounded-xl transition-all">
              <Users size={20} className="text-gray-500" />
            </button>
          </div>
        </div>

        {/* 标签页切换 */}
        <div className="px-4 pb-3 flex items-center justify-between">
          <div className="flex space-x-6">
            {tabs.map(tab => (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key)}
                className={`text-sm font-medium pb-1 border-b-2 transition-colors ${
                  activeTab === tab.key 
                    ? 'text-amber-600 border-amber-500' 
                    : 'text-gray-500 border-transparent hover:text-gray-700'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
          {result && (
            <button 
              onClick={() => navigate('/records')}
              className="text-sm text-amber-600 flex items-center hover:text-amber-700 transition-colors"
            >
              查看生辰历 <ChevronRight size={16} />
            </button>
          )}
        </div>
      </div>

      <div className="px-4 pb-20">
        {/* 输入表单 - 未测算时显示 */}
        {!result && (
          <>
            <div className="mt-6 bg-white rounded-3xl p-5 shadow-sm border border-amber-100">
              <h3 className="text-base font-bold text-gray-800 mb-4">输入出生信息</h3>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm text-gray-600 mb-2">出生日期</label>
                  <input
                    type="date"
                    value={birthDate}
                    onChange={(e) => setBirthDate(e.target.value)}
                    className="w-full bg-amber-50 border border-amber-200 text-gray-800 rounded-xl p-3 focus:border-amber-400 focus:ring-amber-200 focus:outline-none focus:ring-2"
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-600 mb-2">出生时间</label>
                  <input
                    type="time"
                    value={birthTime}
                    onChange={(e) => setBirthTime(e.target.value)}
                    className="w-full bg-amber-50 border border-amber-200 text-gray-800 rounded-xl p-3 focus:border-amber-400 focus:ring-amber-200 focus:outline-none focus:ring-2"
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-600 mb-2">性别</label>
                  <div className="flex gap-3">
                    <button
                      onClick={() => setGender('male')}
                      className={`flex-1 py-3 rounded-xl font-medium transition-all ${
                        gender === 'male' 
                          ? 'bg-gradient-to-r from-blue-100 to-cyan-100 text-blue-600 border border-blue-300' 
                          : 'bg-gray-50 text-gray-500 border border-gray-200 hover:bg-gray-100'
                      }`}
                    >
                      男
                    </button>
                    <button
                      onClick={() => setGender('female')}
                      className={`flex-1 py-3 rounded-xl font-medium transition-all ${
                        gender === 'female' 
                          ? 'bg-gradient-to-r from-pink-100 to-rose-100 text-pink-600 border border-pink-300' 
                          : 'bg-gray-50 text-gray-500 border border-gray-200 hover:bg-gray-100'
                      }`}
                    >
                      女
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <button
              onClick={handleAnalyze}
              disabled={loading}
              className="w-full mt-5 py-4 rounded-2xl font-bold text-white text-lg bg-gradient-to-r from-amber-500 via-orange-500 to-amber-500 hover:opacity-90 transition-all disabled:opacity-50 shadow-lg shadow-amber-200 flex items-center justify-center space-x-2"
            >
              <Calendar size={20} className={loading ? 'animate-spin' : ''} />
              <span>开始排盘</span>
            </button>
          </>
        )}

        {/* 分析结果 */}
        {result && (
          <>
            {/* 四柱八字展示 */}
            <div className="mt-4 bg-white rounded-3xl p-5 shadow-sm border border-amber-100">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-base font-bold text-gray-800">四柱八字</h3>
                <span className="text-sm text-gray-500">
                  {result?.出生时间} · {result?.性别} · {result?.生肖}
                </span>
              </div>
              
              <div className="grid grid-cols-4 gap-3 mb-4">
                {renderPillar(result?.四柱详情?.年, '年柱')}
                {renderPillar(result?.四柱详情?.月, '月柱')}
                {renderPillar(result?.四柱详情?.日, '日柱')}
                {renderPillar(result?.四柱详情?.时, '时柱')}
              </div>

              <div className="text-center bg-gradient-to-r from-amber-50 to-orange-50 rounded-xl p-3 border border-amber-200">
                <span className="text-gray-600 text-sm">八字：</span>
                <span className="text-lg font-bold text-amber-700 ml-2">{result?.八字}</span>
              </div>
            </div>

            {/* 详细数据展示区域 */}
            <BaziDetailTabs result={result} wuxingData={wuxingData} />

            {/* 十神解读列表 - 参考图片样式 */}
            {interpretations.length > 0 && (
              <div className="mt-4 space-y-3">
                {interpretations.map((interp, index) => {
                  const config = shiShenConfig[interp.shiShen] || shiShenConfig['偏财']
                  return (
                    <div 
                      key={interp.id || index}
                      onClick={() => handleInterpretationClick(interp)}
                      className={`bg-white rounded-2xl p-4 shadow-sm border ${config.borderColor} cursor-pointer hover:shadow-md transition-all active:scale-[0.99]`}
                    >
                      {/* 标题行 */}
                      <div className="flex items-center space-x-3 mb-3">
                        <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${config.color} flex items-center justify-center text-xl shadow-sm`}>
                          {config.icon}
                        </div>
                        <div>
                          <h4 className="font-bold text-gray-800">{interp.title}</h4>
                        </div>
                      </div>

                      {/* 基础定义 - 完整显示 */}
                      {interp.basicDef && (
                        <p className="text-sm text-gray-500 mb-3 leading-relaxed">
                          {interp.basicDef}
                        </p>
                      )}

                      {/* 主要内容 - 完整显示 */}
                      {interp.mainContent && (
                        <p className="text-[15px] text-gray-700 leading-relaxed mb-3">
                          {interp.mainContent?.replace(/【[^】]+】/g, '').trim()}
                        </p>
                      )}

                      {/* 底部信息 */}
                      <div className="flex items-center justify-between pt-3 border-t border-gray-100">
                        <div className="flex items-center space-x-2 text-sm text-gray-500">
                          <Flame size={14} className="text-orange-400" />
                          <span>同类交流地</span>
                          <span>·</span>
                          <span>{(Math.random() * 2 + 0.5).toFixed(1)}w测友正在讨论</span>
                        </div>
                        <ChevronRight size={18} className="text-gray-400" />
                      </div>
                    </div>
                  )
                })}
              </div>
            )}

            {/* 操作按钮 */}
            <div className="mt-5 flex gap-3">
              <button
                onClick={handleAIReport}
                disabled={aiLoading}
                className="flex-1 py-3 rounded-xl font-bold text-white bg-gradient-to-r from-amber-500 to-orange-500 hover:opacity-90 transition-all flex items-center justify-center space-x-2 shadow-lg shadow-amber-200"
              >
                <Sparkles size={18} className={aiLoading ? 'animate-spin' : ''} />
                <span>AI解读</span>
                <span className="px-2 py-0.5 bg-white/20 rounded-full text-xs">{POINTS_COST.AI_INTERPRET}积分</span>
              </button>
              <button onClick={handleFavorite} className="p-3 rounded-xl bg-amber-100 text-amber-600 hover:bg-amber-200 transition-all border border-amber-200" title="收藏">
                <Star size={18} />
              </button>
              <button onClick={handleSaveRecord} className="p-3 rounded-xl bg-blue-100 text-blue-600 hover:bg-blue-200 transition-all border border-blue-200" title="保存记录">
                <History size={18} />
              </button>
            </div>

            {/* 重新测算按钮 */}
            <button
              onClick={() => { setResult(null); setInterpretations([]); setAiResult(''); }}
              className="w-full mt-3 py-3 rounded-xl font-medium text-gray-600 bg-gray-100 hover:bg-gray-200 transition-all"
            >
              重新测算
            </button>
          </>
        )}

        {/* AI 解读结果 */}
        {aiResult && (
          <div className="mt-5 bg-white rounded-3xl p-5 shadow-sm border border-amber-100">
            <h3 className="text-base font-bold text-gray-800 mb-3 flex items-center">
              <Sparkles className="w-5 h-5 text-amber-500 mr-2" />
              AI命理解读
            </h3>
            <div className="bg-gradient-to-r from-amber-50 to-orange-50 rounded-xl p-4 border border-amber-200">
              <ThinkingChain isThinking={aiLoading} finalContent={aiResult} />
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
