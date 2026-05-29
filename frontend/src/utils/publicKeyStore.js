/**
 * SM2 公钥内存缓存。
 * 首次调用时从后端拉取，后续直接返回缓存值。
 * 仅在页面生命周期内有效（刷新后重新拉取）。
 */

const BASE_URL = import.meta.env.VITE_USE_MOCK === 'true'
  ? ''
  : (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080')

let cachedPublicKey = null
let fetchPromise    = null

async function fetchPublicKey() {
  const res = await fetch(`${BASE_URL}/api/security/public-key`)
  if (!res.ok) throw new Error(`获取 SM2 公钥失败: ${res.status}`)
  const json = await res.json()
  return json.data?.publicKey
}

export const publicKeyStore = {
  /** 获取公钥（懒加载，自动缓存） */
  async get() {
    if (cachedPublicKey) return cachedPublicKey

    // 防止并发多次请求
    if (!fetchPromise) {
      fetchPromise = fetchPublicKey()
        .then((key) => {
          cachedPublicKey = key
          fetchPromise = null
          return key
        })
        .catch((err) => {
          fetchPromise = null
          throw err
        })
    }
    return fetchPromise
  },

  /** 强制刷新（密钥轮换后调用） */
  invalidate() {
    cachedPublicKey = null
    fetchPromise    = null
  },
}
