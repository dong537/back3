export function getBaseRewardForStreak(streakDays) {
  if (streakDays >= 7) return 30
  if (streakDays >= 3) return 20
  return 10
}

export function getBonusRewardForStreak(streakDays) {
  if (streakDays === 3) return 20
  if (streakDays === 7) return 50
  return 0
}

export function getTotalRewardForStreak(streakDays) {
  return getBaseRewardForStreak(streakDays) + getBonusRewardForStreak(streakDays)
}

export function getNextRewardMilestone(currentStreak) {
  if (currentStreak < 3) return 3
  if (currentStreak < 7) return 7
  return (Math.floor(currentStreak / 7) + 1) * 7
}
