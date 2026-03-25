import { useEffect, useMemo, useState } from 'react'
import { AlertTriangle, History, Package, ShoppingBag } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import Card, { CardContent, CardHeader, CardTitle } from '../components/Card'
import Button from '../components/Button'
import { creditApi } from '../api'
import { toast } from '../components/Toast'
import { useAuth } from '../context/AuthContext'
import { logger } from '../utils/logger'
import {
  formatLocaleDateTime,
  resolvePageLocale,
  safeText,
} from '../utils/displayText'

const SHOP_COPY = {
  'zh-CN': {
    confirm: '确认',
    cancel: '取消',
    loadFailed: '加载失败',
    insufficientCredits: '积分不足',
    exchangeFailed: '兑换失败',
    exchangeSuccess: '兑换成功',
    loading: '加载中...',
    title: '积分商城',
    currentCredits: '当前积分',
    products: '商品',
    records: '记录',
    type: '类型',
    value: '价值',
    emptyDescription: '暂无说明',
    exchange: '兑换',
    recordsTitle: '兑换记录',
    emptyRecords: '暂无记录',
    confirmExchange: (name) => `确认兑换「${name}」？`,
    confirmExchangeDefault: '确认兑换？',
    costDescription: (cost) => `将消耗 ${cost} 积分。`,
    processing: '处理中...',
    confirmExchangeButton: '确认兑换',
  },
  'en-US': {
    confirm: 'Confirm',
    cancel: 'Cancel',
    loadFailed: 'Failed to load',
    insufficientCredits: 'Not enough credits',
    exchangeFailed: 'Exchange failed',
    exchangeSuccess: 'Exchange successful',
    loading: 'Loading...',
    title: 'Credit Shop',
    currentCredits: 'Current credits',
    products: 'Products',
    records: 'Records',
    type: 'Type',
    value: 'Value',
    emptyDescription: 'No description available',
    exchange: 'Exchange',
    recordsTitle: 'Exchange Records',
    emptyRecords: 'No exchange records yet',
    confirmExchange: (name) => `Exchange "${name}"?`,
    confirmExchangeDefault: 'Confirm exchange?',
    costDescription: (cost) => `This will cost ${cost} credits.`,
    processing: 'Processing...',
    confirmExchangeButton: 'Confirm exchange',
  },
}

function ConfirmModal({
  open,
  title,
  description,
  confirmText,
  cancelText,
  onConfirm,
  onCancel,
}) {
  if (!open) return null

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60" onClick={onCancel} />
      <div className="relative w-[92vw] max-w-md rounded-[28px] border border-white/10 bg-[#120d0c]/95 p-6 shadow-[0_24px_80px_rgba(0,0,0,0.45)] backdrop-blur-xl">
        <div className="flex items-start space-x-3">
          <div className="mystic-icon-badge h-10 w-10">
            <AlertTriangle className="text-[#fff3df]" size={22} />
          </div>
          <div className="flex-1">
            <div className="text-lg font-semibold text-[#f8eee2]">{title}</div>
            {description ? (
              <div className="mt-1 text-sm text-[#bdaa94]">{description}</div>
            ) : null}
          </div>
        </div>
        <div className="mt-6 flex justify-end space-x-3">
          <Button variant="secondary" onClick={onCancel}>
            {cancelText}
          </Button>
          <Button onClick={onConfirm}>{confirmText}</Button>
        </div>
      </div>
    </div>
  )
}

export default function CreditShopPage() {
  const auth = useAuth()
  const { i18n } = useTranslation()
  const locale = resolvePageLocale(i18n.language)
  const copy = SHOP_COPY[locale]

  const [loading, setLoading] = useState(true)
  const [products, setProducts] = useState([])
  const [records, setRecords] = useState([])
  const [tab, setTab] = useState('products')
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
      const [productResponse, recordResponse] = await Promise.all([
        creditApi.getProducts(),
        creditApi.getExchangeRecords(),
      ])

      if (productResponse.data?.code === 200) {
        setProducts(productResponse.data.data || [])
      }
      if (recordResponse.data?.code === 200) {
        setRecords(recordResponse.data.data || [])
      }
    } catch (error) {
      logger.error('Load credit shop data failed', error)
      toast.error(copy.loadFailed)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadAll()
  }, [locale])

  const requestExchange = (product) => {
    setPendingProduct(product)
    setConfirmOpen(true)
  }

  const doExchange = async () => {
    if (!pendingProduct || exchangeBusy) return

    if (!canExchange) {
      toast.error(copy.insufficientCredits)
      return
    }

    try {
      setExchangeBusy(true)
      const response = await creditApi.exchange(pendingProduct.productCode)
      if (response.data?.code === 200) {
        const payload = response.data.data
        if (payload?.success === false) {
          toast.error(payload?.message || copy.exchangeFailed)
          return
        }
        toast.success(payload?.message || copy.exchangeSuccess)
        await auth.refreshCredits?.()
        await loadAll()
        setTab('records')
      }
    } catch (error) {
      logger.error('Exchange product failed', error)
      toast.error(error?.message || copy.exchangeFailed)
    } finally {
      setExchangeBusy(false)
      setConfirmOpen(false)
      setPendingProduct(null)
    }
  }

  if (loading) {
    return (
      <div className="page-shell" data-theme="default">
        <div className="mx-auto max-w-5xl px-4 py-8">
          <div className="text-[#bdaa94]">{copy.loading}</div>
        </div>
      </div>
    )
  }

  return (
    <div className="page-shell" data-theme="default">
      <div className="page-hero">
        <div className="page-hero-inner">
          <div className="page-badge">
            <ShoppingBag className="text-theme h-4 w-4" />
            <span>{copy.products}</span>
          </div>
          <h1 className="page-title font-serif-title text-white">{copy.title}</h1>
          <p className="page-subtitle">
            {copy.currentCredits}: {auth.credits ?? 0}
          </p>
        </div>
      </div>

      <div className="mx-auto max-w-5xl px-4 pb-8">
        <div className="mb-6 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div className="mystic-chip">
            {copy.currentCredits}: {auth.credits ?? 0}
          </div>

          <div className="flex items-center space-x-2">
            <Button
              variant={tab === 'products' ? 'primary' : 'secondary'}
              onClick={() => setTab('products')}
            >
              <Package size={18} />
              <span>{copy.products}</span>
            </Button>
            <Button
              variant={tab === 'records' ? 'primary' : 'secondary'}
              onClick={() => setTab('records')}
            >
              <History size={18} />
              <span>{copy.records}</span>
            </Button>
          </div>
        </div>

        {tab === 'products' ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {products.map((product) => {
              const cost = product.pointsCost ?? 0
              const affordable = (auth.credits ?? 0) >= cost

              return (
                <Card key={product.id} className="panel">
                  <CardHeader>
                    <CardTitle className="flex items-center justify-between">
                      <span>{product.productName}</span>
                      <span className="mystic-chip normal-case tracking-normal">
                        🪙 {cost}
                      </span>
                    </CardTitle>
                  </CardHeader>

                  <CardContent className="space-y-3">
                    <div className="text-sm text-[#bdaa94]">
                      {safeText(
                        product.productDescription,
                        copy.emptyDescription
                      )}
                    </div>
                    <div className="text-xs text-[#9b8770]">
                      {copy.type}: {product.productType} / {copy.value}:{' '}
                      {product.productValue}
                    </div>
                    <Button
                      className="w-full"
                      disabled={!affordable}
                      onClick={() => requestExchange(product)}
                    >
                      {affordable ? copy.exchange : copy.insufficientCredits}
                    </Button>
                  </CardContent>
                </Card>
              )
            })}
          </div>
        ) : (
          <Card className="panel">
            <CardHeader>
              <CardTitle>{copy.recordsTitle}</CardTitle>
            </CardHeader>
            <CardContent>
              {records.length === 0 ? (
                <div className="text-[#bdaa94]">{copy.emptyRecords}</div>
              ) : (
                <div className="space-y-2">
                  {records.map((record) => (
                    <div
                      key={record.id}
                      className="mystic-muted-box flex items-center justify-between"
                    >
                      <div>
                        <div className="font-medium">{record.productName}</div>
                        <div className="text-xs text-[#9b8770]">
                          {record.createTime
                            ? formatLocaleDateTime(record.createTime, locale)
                            : ''}
                        </div>
                      </div>
                      <div className="text-sm text-[#dcb86f]">
                        - 🪙 {record.pointsCost}
                      </div>
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
        title={
          pendingProduct
            ? copy.confirmExchange(pendingProduct.productName)
            : copy.confirmExchangeDefault
        }
        description={
          pendingProduct ? copy.costDescription(pendingProduct.pointsCost) : ''
        }
        confirmText={
          exchangeBusy
            ? copy.processing
            : canExchange
              ? copy.confirmExchangeButton
              : copy.insufficientCredits
        }
        cancelText={copy.cancel}
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
