import { useState } from 'react'
import { Download, Share2, Star } from 'lucide-react'
import Card, { CardHeader, CardTitle, CardContent } from './Card'
import Button from './Button'
import { toast } from './Toast'

/**
 * 六爻占卜报告组件
 */
export default function LiuYaoReport({ reportData, onFavorite, onShare }) {
  const [expandedSections, setExpandedSections] = useState({
    zhuangGua: true,
    yongShen: true,
    wangShuai: true,
    dongBian: true
  })

  if (!reportData) return null

  const toggleSection = (section) => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }))
  }

  const zhuangGua = reportData.zhuang_gua || {}
  const yaos = zhuangGua.yaos || []
  const yongShen = reportData.yong_shen || {}
  const wangShuai = reportData.wang_shuai || {}
  const dongBian = reportData.dong_bian || {}
  const judgment = reportData.overall_judgment || {}

  return (
    <div className="space-y-6">
      {/* 报告头部 */}
      <Card className="panel gilded-border" glow>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-2xl">
              {reportData.original?.chinese || '未知'}卦
              {reportData.changed && ` → ${reportData.changed.chinese || '未知'}卦`}
            </CardTitle>
            <div className="flex space-x-2">
              <Button onClick={onFavorite} variant="secondary" size="sm">
                <Star size={16} />
              </Button>
              <Button onClick={onShare} variant="secondary" size="sm">
                <Share2 size={16} />
              </Button>
              <Button onClick={() => toast.info('报告下载功能开发中')} variant="secondary" size="sm">
                <Download size={16} />
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-2 gap-4 text-sm">
            <div>
              <span className="text-gray-400">占问：</span>
              <span>{reportData.question}</span>
            </div>
            <div>
              <span className="text-gray-400">类别：</span>
              <span>{reportData.category}</span>
            </div>
            <div>
              <span className="text-gray-400">占卜日期：</span>
              <span>{reportData.divination_date}</span>
            </div>
            <div>
              <span className="text-gray-400">综合判断：</span>
              <span className={`font-bold ${
                judgment.overall === '大吉' ? 'text-green-400' :
                judgment.overall === '吉' ? 'text-blue-400' :
                judgment.overall === '凶' ? 'text-red-400' : 'text-yellow-400'
              }`}>
                {judgment.overall || '中平'}
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 装卦结果 */}
      <Card className="panel">
        <CardHeader>
          <CardTitle 
            className="cursor-pointer flex items-center justify-between"
            onClick={() => toggleSection('zhuangGua')}
          >
            <span>装卦结果</span>
            <span className="text-sm text-gray-400">
              {zhuangGua.palace_nature} | 月建：{zhuangGua.yue_jian} | 日辰：{zhuangGua.ri_chen}
            </span>
          </CardTitle>
        </CardHeader>
        {expandedSections.zhuangGua && (
          <CardContent>
            <div className="space-y-2">
              {yaos
                .sort((a, b) => b.yao_position - a.yao_position) // 从上往下显示
                .map((yao) => (
                <div
                  key={yao.yao_position}
                  className={`p-3 rounded-lg border ${
                    yao.is_kong_wang 
                      ? 'bg-red-500/10 border-red-500/30' 
                      : 'bg-white/5 border-white/10'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <span className="font-bold text-purple-400">
                        {yao.yao_position === 6 ? '上' : yao.yao_position === 5 ? '五' : 
                         yao.yao_position === 4 ? '四' : yao.yao_position === 3 ? '三' : 
                         yao.yao_position === 2 ? '二' : '初'}
                        {yao.yao_type === '阳' ? '九' : '六'}
                      </span>
                      <span className={`px-2 py-1 rounded text-xs ${
                        yao.yao_type === '阳' ? 'bg-yellow-500/20 text-yellow-400' : 'bg-blue-500/20 text-blue-400'
                      }`}>
                        {yao.yao_type}
                      </span>
                      {yao.is_shi && <span className="px-2 py-1 rounded bg-green-500/20 text-green-400 text-xs">世</span>}
                      {yao.is_ying && <span className="px-2 py-1 rounded bg-orange-500/20 text-orange-400 text-xs">应</span>}
                      {yao.is_kong_wang && <span className="px-2 py-1 rounded bg-red-500/20 text-red-400 text-xs">空</span>}
                    </div>
                    <div className="flex items-center space-x-4 text-sm">
                      <span>纳干：{yao.stem}</span>
                      <span>纳支：{yao.branch}</span>
                      <span className="text-purple-400 font-medium">{yao.liu_qin}</span>
                      <span className="text-gray-400">{yao.liu_shen}</span>
                      <span className="text-xs text-gray-500">{yao.wang_shuai}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            {zhuangGua.kong_wang && zhuangGua.kong_wang.length > 0 && (
              <div className="mt-4 p-2 bg-red-500/10 rounded text-sm text-red-400">
                空亡：{zhuangGua.kong_wang.join('、')}
              </div>
            )}
          </CardContent>
        )}
      </Card>

      {/* 用神分析 */}
      {yongShen.primary && (
        <Card className="panel">
          <CardHeader>
            <CardTitle 
              className="cursor-pointer"
              onClick={() => toggleSection('yongShen')}
            >
              用神分析
            </CardTitle>
          </CardHeader>
          {expandedSections.yongShen && (
            <CardContent>
              <div className="space-y-3">
                <div>
                  <span className="text-gray-400">首选用神：</span>
                  <span className="font-bold text-purple-400">{yongShen.primary}</span>
                  {yongShen.yao_position && (
                    <span className="ml-2 text-sm">（第{yongShen.yao_position}爻，{yongShen.branch}）</span>
                  )}
                </div>
                {yongShen.auxiliary && yongShen.auxiliary.length > 0 && (
                  <div>
                    <span className="text-gray-400">辅助参考：</span>
                    <span>{yongShen.auxiliary.join('、')}</span>
                  </div>
                )}
                {yongShen.judgment_points && (
                  <div className="p-3 bg-purple-500/10 rounded">
                    <div className="text-sm font-medium mb-1">判断要点：</div>
                    <div className="text-sm">{yongShen.judgment_points}</div>
                  </div>
                )}
              </div>
            </CardContent>
          )}
        </Card>
      )}

      {/* 旺衰分析 */}
      {wangShuai.overall_status && (
        <Card className="panel">
          <CardHeader>
            <CardTitle 
              className="cursor-pointer"
              onClick={() => toggleSection('wangShuai')}
            >
              旺衰分析
            </CardTitle>
          </CardHeader>
          {expandedSections.wangShuai && (
            <CardContent>
              <div className="space-y-3">
                {wangShuai.yue_jian_status && (
                  <div>
                    <span className="text-gray-400">月建：</span>
                    <span>{wangShuai.yue_jian_status}</span>
                  </div>
                )}
                {wangShuai.ri_chen_status && (
                  <div>
                    <span className="text-gray-400">日辰：</span>
                    <span>{wangShuai.ri_chen_status}</span>
                  </div>
                )}
                {wangShuai.dong_yao_effects && wangShuai.dong_yao_effects.length > 0 && (
                  <div>
                    <span className="text-gray-400">动爻影响：</span>
                    <ul className="list-disc list-inside mt-1 space-y-1">
                      {wangShuai.dong_yao_effects.map((effect, idx) => (
                        <li key={idx} className="text-sm">{effect}</li>
                      ))}
                    </ul>
                  </div>
                )}
                {wangShuai.overall_status && (
                  <div className="p-3 bg-blue-500/10 rounded">
                    <div className="text-sm font-medium mb-1">综合旺衰：</div>
                    <div className="text-sm font-bold">{wangShuai.overall_status}</div>
                  </div>
                )}
              </div>
            </CardContent>
          )}
        </Card>
      )}

      {/* 动变分析 */}
      {dongBian.type && (
        <Card className="panel">
          <CardHeader>
            <CardTitle 
              className="cursor-pointer"
              onClick={() => toggleSection('dongBian')}
            >
              动变分析
            </CardTitle>
          </CardHeader>
          {expandedSections.dongBian && (
            <CardContent>
              <div className="space-y-3">
                <div>
                  <span className="text-gray-400">类型：</span>
                  <span className="font-bold">{dongBian.type}</span>
                </div>
                {dongBian.priority && (
                  <div>
                    <span className="text-gray-400">断卦优先级：</span>
                    <span>{dongBian.priority}</span>
                  </div>
                )}
                {dongBian.interpretation && (
                  <div className="p-3 bg-yellow-500/10 rounded">
                    <div className="text-sm">{dongBian.interpretation}</div>
                  </div>
                )}
                {dongBian.吉凶倾向 && (
                  <div className="p-3 bg-green-500/10 rounded">
                    <div className="text-sm font-medium">吉凶倾向：</div>
                    <div className="text-sm">{dongBian.吉凶倾向}</div>
                  </div>
                )}
              </div>
            </CardContent>
          )}
        </Card>
      )}

      {/* 综合判断 */}
      {judgment.summary && (
        <Card className="panel gilded-border" glow>
          <CardHeader>
            <CardTitle>综合判断</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="p-4 bg-gradient-to-r from-purple-500/20 to-blue-500/20 rounded-lg">
                <div className="text-lg font-bold mb-2">{judgment.summary}</div>
                {judgment.suggestion && (
                  <div className="text-sm text-gray-300">{judgment.suggestion}</div>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
