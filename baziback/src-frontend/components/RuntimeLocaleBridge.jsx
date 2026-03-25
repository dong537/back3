import { useEffect, useRef } from 'react'
import { translateUiText } from '../utils/runtimeLocale'
import useAppLocale from '../hooks/useAppLocale'

const ATTRIBUTES = ['placeholder', 'title', 'aria-label', 'alt']
const SKIP_TAGS = new Set(['SCRIPT', 'STYLE', 'NOSCRIPT', 'INPUT', 'TEXTAREA'])

function translateElementAttributes(element, locale) {
  for (const attribute of ATTRIBUTES) {
    const current = element.getAttribute(attribute)
    if (current == null) continue

    const key = `data-runtime-orig-${attribute.replace(/[^a-z]/gi, '').toLowerCase()}`
    if (!element.getAttribute(key)) {
      element.setAttribute(key, current)
    }

    const translated = translateUiText(element.getAttribute(key), locale)
    if (translated !== current) {
      element.setAttribute(attribute, translated)
    }
  }
}

function translateTextNode(node, locale) {
  if (!node.parentElement || SKIP_TAGS.has(node.parentElement.tagName)) return

  if (node.__runtimeOrigText === undefined) {
    node.__runtimeOrigText = node.textContent || ''
  }

  const translated = translateUiText(node.__runtimeOrigText, locale)
  if (translated !== node.textContent) {
    node.textContent = translated
  }
}

function translateSubtree(root, locale) {
  if (!(root instanceof Element) && !(root instanceof Document)) return

  const base = root instanceof Document ? root.body : root
  if (!base) return

  if (base instanceof Element) {
    translateElementAttributes(base, locale)
  }

  const elementWalker = document.createTreeWalker(base, NodeFilter.SHOW_ELEMENT)
  let elementNode = elementWalker.nextNode()
  while (elementNode) {
    translateElementAttributes(elementNode, locale)
    elementNode = elementWalker.nextNode()
  }

  const textWalker = document.createTreeWalker(base, NodeFilter.SHOW_TEXT)
  let textNode = textWalker.nextNode()
  while (textNode) {
    translateTextNode(textNode, locale)
    textNode = textWalker.nextNode()
  }
}

export default function RuntimeLocaleBridge() {
  const { locale } = useAppLocale()
  const isMutatingRef = useRef(false)

  useEffect(() => {
    const runTranslate = (target = document) => {
      isMutatingRef.current = true
      try {
        translateSubtree(target, locale)
      } finally {
        isMutatingRef.current = false
      }
    }

    runTranslate(document)

    const observer = new MutationObserver((mutations) => {
      if (isMutatingRef.current) return
      for (const mutation of mutations) {
        if (mutation.type === 'characterData' && mutation.target?.parentElement) {
          runTranslate(mutation.target.parentElement)
          continue
        }
        if (mutation.type === 'attributes' && mutation.target instanceof Element) {
          runTranslate(mutation.target)
          continue
        }
        mutation.addedNodes.forEach((node) => {
          if (node instanceof Element) {
            runTranslate(node)
          }
        })
      }
    })

    observer.observe(document.body, {
      subtree: true,
      childList: true,
      characterData: true,
      attributes: true,
      attributeFilter: ATTRIBUTES,
    })

    return () => observer.disconnect()
  }, [locale])

  return null
}
