export function getDailySeed(key = 'default') {
  const now = new Date()
  const dateKey = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  return `${dateKey}:${key}`
}

// Simple deterministic hash to [0, 1)
export function hashToUnitInterval(str) {
  let hash = 2166136261
  for (let i = 0; i < str.length; i++) {
    hash ^= str.charCodeAt(i)
    hash = Math.imul(hash, 16777619)
  }
  // Convert to unsigned 32-bit
  const u = hash >>> 0
  return u / 4294967296
}

// Deterministic daily random int in [min, max]
export function getDailyRandomInt(key, min, max) {
  const seed = getDailySeed(key)
  const unit = hashToUnitInterval(seed)
  const n = Math.floor(unit * (max - min + 1)) + min
  return Math.max(min, Math.min(max, n))
}

export function getDailyOverallScore(min = 70, max = 99) {
  return getDailyRandomInt('overallScore', min, max)
}

export function getDailyCategoryScores(overall, categories) {
  // categories: array of {name, min, max}
  return categories.map((c) => {
    // Center around overall with some variation
    const spread = 15
    const baseMin = Math.max(c.min ?? 0, overall - spread)
    const baseMax = Math.min(c.max ?? 100, overall + spread)
    return {
      name: c.name,
      score: getDailyRandomInt(`cat:${c.name}`, baseMin, baseMax),
    }
  })
}
