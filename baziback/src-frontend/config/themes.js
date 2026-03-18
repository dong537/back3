// 主题配置文件

export const themes = {
  yijing: {
    primary: '#4f46e5', // Indigo
    secondary: '#a5b4fc',
    gradient: { from: '#4f46e5', to: '#312e81' },
    name: 'yijing',
  },
  tarot: {
    primary: '#8b5cf6', // Violet
    secondary: '#c4b5fd',
    gradient: { from: '#8b5cf6', to: '#4c1d95' },
    name: 'tarot',
  },
  zodiac: {
    primary: '#3b82f6', // Sky Blue
    secondary: '#93c5fd',
    gradient: { from: '#3b82f6', to: '#1e3a8a' },
    name: 'zodiac',
  },
  bazi: {
    primary: '#f59e0b', // Amber
    secondary: '#fcd34d',
    gradient: { from: '#f59e0b', to: '#b45309' },
    name: 'bazi',
  },
  ai: {
    primary: '#06b6d4', // Cyan
    secondary: '#67e8f9',
    gradient: { from: '#06b6d4', to: '#0891b2' },
    name: 'ai',
  },
  default: {
    primary: '#a855f7', // Default Purple
    secondary: '#d8b4fe',
    gradient: { from: '#a855f7', to: '#581c87' },
    name: 'default',
  },
};

export const getTheme = (themeName) => {
  return themes[themeName] || themes.default;
};

