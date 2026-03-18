import React from 'react'
import { FixedSizeList as List } from 'react-window'
import { Trash2, Eye } from 'lucide-react'

/**
 * 虚拟列表组件 - 用于渲染大量记录
 * 只渲染可见的项目，大幅提升性能
 */
const VirtualRecordList = React.memo(({ records, onDelete, onView, loading = false }) => {
  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    )
  }

  if (!records || records.length === 0) {
    return (
      <div className="flex items-center justify-center h-96 text-gray-500">
        <p>暂无记录</p>
      </div>
    )
  }

  const Row = ({ index, style }) => {
    const record = records[index]
    
    return (
      <div style={style} className="px-4 py-2">
        <div className="flex items-center justify-between p-3 rounded-lg bg-white border border-gray-200 hover:shadow-md transition-shadow">
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-gray-800 truncate">
              {record.question || record.title || record.recordTitle || '无标题'}
            </p>
            <p className="text-xs text-gray-500 mt-1">
              {record.recordType && `类型: ${record.recordType}`}
              {record.createTime && ` · ${new Date(record.createTime).toLocaleDateString('zh-CN')}`}
            </p>
          </div>
          
          <div className="flex items-center space-x-2 ml-4">
            {onView && (
              <button
                onClick={() => onView(record)}
                className="p-2 hover:bg-blue-100 rounded-lg transition-colors"
                title="查看详情"
              >
                <Eye size={18} className="text-blue-600" />
              </button>
            )}
            
            {onDelete && (
              <button
                onClick={() => onDelete(record.id)}
                className="p-2 hover:bg-red-100 rounded-lg transition-colors"
                title="删除"
              >
                <Trash2 size={18} className="text-red-600" />
              </button>
            )}
          </div>
        </div>
      </div>
    )
  }

  return (
    <List
      height={600}
      itemCount={records.length}
      itemSize={100}
      width="100%"
    >
      {Row}
    </List>
  )
})

VirtualRecordList.displayName = 'VirtualRecordList'

export default VirtualRecordList
