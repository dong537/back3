import React from 'react'
import { FixedSizeList as List } from 'react-window'
import { Eye, Trash2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import {
  formatLocaleDate,
  resolvePageLocale,
  safeText,
} from '../utils/displayText'

const RECORD_LIST_COPY = {
  'zh-CN': {
    empty: '暂无记录',
    untitled: '无标题',
    type: '类型',
    view: '查看详情',
    delete: '删除',
  },
  'en-US': {
    empty: 'No records yet',
    untitled: 'Untitled',
    type: 'Type',
    view: 'View details',
    delete: 'Delete',
  },
}

const VirtualRecordList = React.memo(
  ({ records, onDelete, onView, loading = false }) => {
    const { i18n } = useTranslation()
    const locale = resolvePageLocale(i18n.language)
    const copy = RECORD_LIST_COPY[locale]

    if (loading) {
      return (
        <div className="flex h-96 items-center justify-center">
          <div className="h-12 w-12 animate-spin rounded-full border-2 border-white/10 border-t-[#dcb86f]"></div>
        </div>
      )
    }

    if (!records || records.length === 0) {
      return (
        <div className="flex h-96 items-center justify-center text-[#8f7b66]">
          <p>{copy.empty}</p>
        </div>
      )
    }

    const Row = ({ index, style }) => {
      const record = records[index]
      const title =
        safeText(record.question) ||
        safeText(record.title) ||
        safeText(record.recordTitle) ||
        copy.untitled
      const date = formatLocaleDate(record.createTime, locale)

      return (
        <div style={style} className="px-4 py-2">
          <div className="flex items-center justify-between rounded-[22px] border border-white/10 bg-[#140f0f]/72 p-3 transition-all hover:border-[#d0a85b]/18 hover:bg-[#171110]/84">
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-semibold text-[#f4ece1]">
                {title}
              </p>
              <p className="mt-1 text-xs text-[#8f7b66]">
                {record.recordType && `${copy.type}: ${record.recordType}`}
                {record.recordType && date ? ' · ' : ''}
                {date}
              </p>
            </div>

            <div className="ml-4 flex items-center space-x-2">
              {onView && (
                <button
                  onClick={() => onView(record)}
                  className="rounded-xl p-2 text-[#bdaa94] transition-colors hover:bg-[#6a4a1e]/16 hover:text-[#f0d9a5]"
                  title={copy.view}
                >
                  <Eye size={18} />
                </button>
              )}

              {onDelete && (
                <button
                  onClick={() => onDelete(record.id)}
                  className="rounded-xl p-2 text-[#bdaa94] transition-colors hover:bg-[#7a3218]/16 hover:text-[#e19a84]"
                  title={copy.delete}
                >
                  <Trash2 size={18} />
                </button>
              )}
            </div>
          </div>
        </div>
      )
    }

    return (
      <List height={600} itemCount={records.length} itemSize={100} width="100%">
        {Row}
      </List>
    )
  }
)

VirtualRecordList.displayName = 'VirtualRecordList'

export default VirtualRecordList
