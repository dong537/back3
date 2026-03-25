const PLACEHOLDER_SLOT_IDS = new Set([
  '',
  'YOUR_SLOT_ID_HERE',
  'YOUR_PANGLE_SLOT_ID',
  'YOUR_CAROUSEL_SLOT_ID_HERE',
  'YOUR_SPLASH_SLOT_ID_HERE',
])

function normalizeSlotId(value) {
  if (typeof value !== 'string') return ''
  const trimmed = value.trim()
  return PLACEHOLDER_SLOT_IDS.has(trimmed) ? '' : trimmed
}

export function getPangleSlotId(slotName) {
  const generalSlotId = normalizeSlotId(import.meta.env.VITE_PANGLE_SLOT_ID)
  const carouselSlotId = normalizeSlotId(
    import.meta.env.VITE_PANGLE_CAROUSEL_SLOT_ID
  )
  const splashSlotId = normalizeSlotId(import.meta.env.VITE_PANGLE_SPLASH_SLOT_ID)

  switch (slotName) {
    case 'carousel':
      return carouselSlotId || generalSlotId
    case 'splash':
      return splashSlotId || generalSlotId
    case 'reward':
    case 'banner':
    default:
      return generalSlotId || carouselSlotId
  }
}

export function hasPangleSlotId(slotName) {
  return Boolean(getPangleSlotId(slotName))
}

export const pangleConfig = {
  bannerSlotId: getPangleSlotId('banner'),
  rewardSlotId: getPangleSlotId('reward'),
  carouselSlotId: getPangleSlotId('carousel'),
  splashSlotId: getPangleSlotId('splash'),
}
