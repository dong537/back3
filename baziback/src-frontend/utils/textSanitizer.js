// 统一清洗 AI/服务端返回的文本，去掉 Markdown/列表/分隔线/代码块等符号展示
export function stripSymbols(input) {
  if (input == null) return ''
  let text = String(input)

  // 统一换行
  text = text.replace(/\r\n/g, '\n')

  // 移除代码块标记（保留内部内容）
  text = text.replace(/```[\s\S]*?```/g, (m) => m.replace(/```/g, ''))

  // 移除常见 markdown 标题/引用/分隔线/加粗等标记
  text = text
    .replace(/^\s{0,3}#{1,6}\s+/gm, '')
    .replace(/^\s*>\s?/gm, '')
    .replace(/^\s*(-{3,}|_{3,}|\*{3,})\s*$/gm, '')
    .replace(/\*\*(.*?)\*\*/g, '$1')
    .replace(/__(.*?)__/g, '$1')
    .replace(/`([^`]*)`/g, '$1')

  // 去掉列表前缀符号（- * + 1. 1) 等）
  text = text.replace(/^\s*(?:[-*+]|\d+[.)])\s+/gm, '')

  // 去掉多余的连续空行
  text = text.replace(/\n{3,}/g, '\n\n')

  return text.trim()
}

