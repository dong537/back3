export const AUTH_EVENTS = {
  LOGOUT: 'auth:logout',
}

export function emitLogout(reason = 'unauthorized') {
  window.dispatchEvent(new CustomEvent(AUTH_EVENTS.LOGOUT, { detail: { reason } }))
}

export function onLogout(handler) {
  const listener = (e) => handler?.(e?.detail)
  window.addEventListener(AUTH_EVENTS.LOGOUT, listener)
  return () => window.removeEventListener(AUTH_EVENTS.LOGOUT, listener)
}
