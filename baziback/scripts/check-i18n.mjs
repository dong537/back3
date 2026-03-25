import fs from 'node:fs'
import path from 'node:path'

const projectRoot = process.cwd()
const frontendRoot = path.join(projectRoot, 'src-frontend')

function readJson(relativePath) {
  return JSON.parse(
    fs.readFileSync(path.join(frontendRoot, relativePath), 'utf8')
  )
}

function flattenKeys(value, prefix = '', result = []) {
  if (Array.isArray(value)) {
    value.forEach((item, index) =>
      flattenKeys(item, `${prefix}[${index}]`, result)
    )
    return result
  }

  if (value && typeof value === 'object') {
    Object.entries(value).forEach(([key, item]) => {
      const nextPrefix = prefix ? `${prefix}.${key}` : key
      flattenKeys(item, nextPrefix, result)
    })
    return result
  }

  result.push(prefix)
  return result
}

function flattenLeafStrings(value, result = []) {
  if (Array.isArray(value)) {
    value.forEach((item) => flattenLeafStrings(item, result))
    return result
  }

  if (value && typeof value === 'object') {
    Object.values(value).forEach((item) => flattenLeafStrings(item, result))
    return result
  }

  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (trimmed) {
      result.push(trimmed)
    }
  }

  return result
}

function walkFiles(dirPath, matcher, result = []) {
  for (const entry of fs.readdirSync(dirPath, { withFileTypes: true })) {
    const fullPath = path.join(dirPath, entry.name)
    if (entry.isDirectory()) {
      walkFiles(fullPath, matcher, result)
      continue
    }
    if (matcher(fullPath)) {
      result.push(fullPath)
    }
  }
  return result
}

const MANUAL_TRANSLATION_STRINGS = [
  '加载中...',
  '正在加载...',
  '请先登录',
  '请先登录后再签到',
  '请先登录后再进行签到',
  '签到失败',
  '签到失败，请稍后重试',
  '登录解锁 AI 解读',
  '今天',
  '日',
  '周',
  '月',
  '年',
  '查看星盘',
  '综合分数',
  '今天运气还不错',
  '抽张卡获得启发，提升能量吧',
  '免费抽卡',
  '爱情提醒',
  '来自生肖',
  '全文',
  '建议',
  '避免',
  '宝石蓝',
  '白水晶',
  '正东',
  '周日',
  '周一',
  '周二',
  '周三',
  '周四',
  '周五',
  '周六',
  '最后更新：',
  '隐私政策',
  '用户协议',
  '用户服务协议',
]

function buildKnownChineseStrings(zh, en) {
  const known = new Set(flattenLeafStrings(zh))
  flattenLeafStrings(en).forEach((value) => known.add(value))
  MANUAL_TRANSLATION_STRINGS.forEach((value) => known.add(value))
  return known
}

function extractChineseSegments(line) {
  return (
    line.match(
      /[\u4e00-\u9fff][\u4e00-\u9fffA-Za-z0-9：:，,。、“”‘’！？!（）()《》…·\-\/\s]*/gu
    ) || []
  )
    .map((item) => item.trim())
    .filter(Boolean)
}

function countChar(line, char) {
  return (line.match(new RegExp(`\\${char}`, 'g')) || []).length
}

function collectLocaleCopyLines(lines) {
  const ignored = new Set()

  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index]
    if (!/const\s+[A-Za-z0-9_]+\s*=\s*{/.test(line)) continue

    const lookahead = lines.slice(index, index + 200).join('\n')
    if (!lookahead.includes("'zh-CN'") || !lookahead.includes("'en-US'")) {
      continue
    }

    let depth = 0
    let started = false

    for (let cursor = index; cursor < lines.length; cursor += 1) {
      const currentLine = lines[cursor]
      const opens = countChar(currentLine, '{')
      const closes = countChar(currentLine, '}')

      depth += opens
      if (opens > 0) {
        started = true
      }

      ignored.add(cursor)
      depth -= closes

      if (started && depth <= 0) {
        index = cursor
        break
      }
    }
  }

  return ignored
}

function scanForSuspiciousText(filePath, knownChineseStrings) {
  const lines = fs.readFileSync(filePath, 'utf8').split(/\r?\n/)
  const findings = []
  const suspiciousPattern = /[\u4e00-\u9fff]/u
  const ignoredLocaleCopyLines = collectLocaleCopyLines(lines)

  lines.forEach((line, index) => {
    if (ignoredLocaleCopyLines.has(index)) return
    if (!suspiciousPattern.test(line)) return
    const trimmed = line.trim()
    if (!trimmed) return
    if (trimmed.startsWith('//') || trimmed.startsWith('*')) return
    if (trimmed.startsWith('{/*') || trimmed.endsWith('*/}')) return
    if (trimmed.includes('logger.') || trimmed.includes('console.')) return
    if (trimmed.includes("locale === 'en-US'") || trimmed.includes('isEn ?'))
      return

    const unresolvedSegments = extractChineseSegments(trimmed).filter(
      (segment) => {
        if (knownChineseStrings.has(segment)) return false
        if (segment.length === 1) return false
        return true
      }
    )

    if (unresolvedSegments.length === 0) return

    findings.push({
      line: index + 1,
      text: trimmed.slice(0, 120),
      unresolvedSegments,
    })
  })

  return findings
}

const zh = readJson(path.join('i18n', 'zh-CN.json'))
const en = readJson(path.join('i18n', 'en-US.json'))
const zhKeys = flattenKeys(zh)
const enKeys = flattenKeys(en)
const knownChineseStrings = buildKnownChineseStrings(zh, en)

const missingInEn = zhKeys.filter((key) => !enKeys.includes(key))
const missingInZh = enKeys.filter((key) => !zhKeys.includes(key))

console.log(`Locale key count: zh-CN=${zhKeys.length}, en-US=${enKeys.length}`)

if (missingInEn.length) {
  console.error('\nMissing in en-US:')
  missingInEn.forEach((key) => console.error(`- ${key}`))
}

if (missingInZh.length) {
  console.error('\nMissing in zh-CN:')
  missingInZh.forEach((key) => console.error(`- ${key}`))
}

const sourceFiles = [
  path.join(frontendRoot, 'App.jsx'),
  path.join(frontendRoot, 'main.jsx'),
  ...walkFiles(path.join(frontendRoot, 'pages'), (file) =>
    /\.(jsx|js)$/.test(file)
  ),
  ...walkFiles(path.join(frontendRoot, 'components'), (file) =>
    /\.(jsx|js)$/.test(file)
  ),
]

const suspiciousFiles = sourceFiles
  .map((filePath) => ({
    filePath,
    findings: scanForSuspiciousText(filePath, knownChineseStrings),
  }))
  .filter((item) => item.findings.length > 0)

if (suspiciousFiles.length) {
  console.warn('\nSuspicious hard-coded or non-ASCII UI text found:')
  suspiciousFiles.forEach(({ filePath, findings }) => {
    console.warn(`\n${path.relative(projectRoot, filePath)}`)
    findings.slice(0, 5).forEach(({ line, text, unresolvedSegments }) => {
      console.warn(`  L${line}: ${text}`)
      console.warn(`      unresolved: ${unresolvedSegments.join(' | ')}`)
    })
    if (findings.length > 5) {
      console.warn(`  ... ${findings.length - 5} more`)
    }
  })
}

if (missingInEn.length || missingInZh.length) {
  process.exitCode = 1
}
