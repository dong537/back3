import { useEffect, useMemo, useState } from 'react'
import { ShoppingBag, Package, History, AlertTriangle } from 'lucide-react'
import Card, { CardHeader, CardTitle, CardContent } from '../components/Card'
import Button from '../components/Button'
import { creditApi } from '../api'
import { toast } from '../components/Toast'
import { useAuth } from '../context/AuthContext'
import { logger } from '../utils/logger'

function ConfirmModal({ open, title, description, confirmText = '确认', cancelText = '取消', onConfirm, onCancel }) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60" onClick={onCancel} />
      <div className="relative w-[92vw] max-w-md rounded-2xl border border-white/10 bg-slate-900/90 p-6 shadow-2xl">
        <div className="flex items-start space-x-3">
          <div className="w-10 h-10 rounded-xl bg-yellow-500/15 flex items-center justify-center">
            <AlertTriangle className="text-yellow-400" size={22} />
          </div>
          <div className="flex-1">
            <div className="text-lg font-semibold">{title}</div>
            {description ? <div className="text-sm text-gray-400 mt-1">{description}</div> : null}
          </div>
        </div>
        <div className="mt-6 flex justify-end space-x-3">
          <Button variant="secondary" onClick={onCancel}>{cancelText}</Button>
          <Button onClick={onConfirm}>{confirmText}</Button>
        </div>
      </div>
    </div>
  )
}

export default function CreditShopPage() {
  const auth = useAuth()

  const [loading, setLoading] = useState(true)
  const [products, setProducts] = useState([])
  const [records, setRecords] = useState([])
  const [tab, setTab] = useState('products') // products | records

  const [confirmOpen, setConfirmOpen] = useState(false)
  const [pendingProduct, setPendingProduct] = useState(null)
  const [exchangeBusy, setExchangeBusy] = useState(false)

  const canExchange = useMemo(() => {
    if (!pendingProduct) return false
    const cost = pendingProduct.pointsCost ?? 0
    return (auth.credits ?? 0) >= cost
  }, [auth.credits, pendingProduct])

  const loadAll = async () => {
    try {
      setLoading(true)
      const [pRes, rRes] = await Promise.all([
        creditApi.getProducts(),
        creditApi.getExchangeRecords(),
      ])

      if (pRes.data?.code === 200) setProducts(pRes.data.data || [])
      if (rRes.data?.code === 200) setRecords(rRes.data.data || [])
    } catch (e) {
      logger.error('加载积分商城数据失败', e)
      toast.error('加载失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadAll()
  }, [])

  const requestExchange = (product) => {
    setPendingProduct(product)
    setConfirmOpen(true)
  }

  const doExchange = async () => {
    if (!pendingProduct || exchangeBusy) return

    if (!canExchange) {
      toast.error('积分不足')
      return
    }

    try {
      setExchangeBusy(true)
      const res = await creditApi.exchange(pendingProduct.productCode)
      if (res.data?.code === 200) {
        const payload = res.data.data
        if (payload?.success === false) {
          toast.error(payload?.message || '兑换失败')
          return
        }
        toast.success(payload?.message || '兑换成功')
        await auth.refreshCredits?.()
        await loadAll()
        setTab('records')
      }
    } catch (e) {
      logger.error('兑换失败', e)
      toast.error(e?.message || '兑换失败')
    } finally {
      setExchangeBusy(false)
      setConfirmOpen(false)
      setPendingProduct(null)
    }
  }

  if (loading) {
    return (
      <div className="page-shell">
        <div className="max-w-5xl mx-auto px-4 py-8">
          <div className="text-gray-400">加载中...</div>
        </div>
      </div>
    )
  }

  return (
    <div className="page-shell">
      <div className="max-w-5xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-3xl font-bold flex items-center space-x-2">
              <ShoppingBag className="text-purple-400" />
              <span>积分商城</span>
            </h1>
            <div className="text-sm text-gray-400 mt-1">当前积分：<span className="text-skin-primary font-semibold">{auth.credits ?? 0}</span></div>
          </div>
          <div className="flex items-center space-x-2">
            <Button variant={tab === 'products' ? 'primary' : 'secondary'} onClick={() => setTab('products')}>
              <Package size={18} />
              <span>商品</span>
            </Button>
            <Button variant={tab === 'records' ? 'primary' : 'secondary'} onClick={() => setTab('records')}>
              <History size={18} />
              <span>记录</span>
            </Button>
          </div>
        </div>

        {tab === 'products' ? (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
            {products.map((p) => {
              const cost = p.pointsCost ?? 0
              const affordable = (auth.credits ?? 0) >= cost
              return (
                <Card key={p.id} className="bg-white/5 border-white/10">
                  <CardHeader>
                    <CardTitle className="flex items-center justify-between">
                      <span>{p.productName}</span>
                      <span className="text-sm px-2 py-1 rounded bg-purple-500/15 text-purple-200">💎 {cost}</span>
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div className="text-sm text-gray-400">{p.productDescription || '—'}</div>
                    <div className="text-xs text-gray-500">类型：{p.productType} / 价值：{p.productValue}</div>
                    <Button className="w-full" disabled={!affordable} onClick={() => requestExchange(p)}>
                      {affordable ? '兑换' : '积分不足'}
                    </Button>
                  </CardContent>
                </Card>
              )
            })}
          </div>
        ) : (
          <Card className="bg-white/5 border-white/10">
            <CardHeader>
              <CardTitle>兑换记录</CardTitle>
            </CardHeader>
            <CardContent>
              {records.length === 0 ? (
                <div className="text-gray-400">暂无记录</div>
              ) : (
                <div className="space-y-2">
                  {records.map((r) => (
                    <div key={r.id} className="flex items-center justify-between p-3 rounded-lg bg-white/5">
                      <div>
                        <div className="font-medium">{r.productName}</div>
                        <div className="text-xs text-gray-500">{r.createTime ? new Date(r.createTime).toLocaleString('zh-CN') : ''}</div>
                      </div>
                      <div className="text-sm text-purple-200">- 💎 {r.pointsCost}</div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        )}
      </div>

      <ConfirmModal
        open={confirmOpen}
        title={pendingProduct ? `确认兑换「${pendingProduct.productName}」？` : '确认兑换？'}
        description={pendingProduct ? `将消耗 ${pendingProduct.pointsCost} 积分。` : ''}
        confirmText={exchangeBusy ? '处理中...' : (canExchange ? '确认兑换' : '积分不足')}
        onCancel={() => {
          if (exchangeBusy) return
          setConfirmOpen(false)
          setPendingProduct(null)
        }}
        onConfirm={doExchange}
      />
    </div>
  )
}
