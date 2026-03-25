const TOKEN_KEY = 'token'
const USER_KEY = 'user'

export function getStoredToken() {
  return sessionStorage.getItem(TOKEN_KEY) || localStorage.getItem(TOKEN_KEY) || null
}

export function getStoredUser() {
  return sessionStorage.getItem(USER_KEY) || localStorage.getItem(USER_KEY) || null
}

export function storeSessionAuth(token, user) {
  if (token) {
    sessionStorage.setItem(TOKEN_KEY, token)
  }
  if (user) {
    sessionStorage.setItem(USER_KEY, typeof user === 'string' ? user : JSON.stringify(user))
  }
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function clearStoredAuth() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function migrateLegacyAuthToSession() {
  const sessionToken = sessionStorage.getItem(TOKEN_KEY)
  const sessionUser = sessionStorage.getItem(USER_KEY)
  const legacyToken = localStorage.getItem(TOKEN_KEY)
  const legacyUser = localStorage.getItem(USER_KEY)

  if (!sessionToken && legacyToken) {
    sessionStorage.setItem(TOKEN_KEY, legacyToken)
  }
  if (!sessionUser && legacyUser) {
    sessionStorage.setItem(USER_KEY, legacyUser)
  }
  if (legacyToken || legacyUser) {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }
}
