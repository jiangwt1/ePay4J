const KEY = 'ep-theme'

export function getTheme() {
  return localStorage.getItem(KEY) || 'light'
}

export function applyTheme(theme) {
  document.documentElement.classList.toggle('dark', theme === 'dark')
  document.documentElement.classList.toggle('light', theme !== 'dark')
  localStorage.setItem(KEY, theme)
}

export function toggleTheme() {
  applyTheme(getTheme() === 'dark' ? 'light' : 'dark')
}
