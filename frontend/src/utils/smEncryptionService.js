/**
 * 国密传输加密服务
 *
 * 职责：
 *  1. 前端生成 SM4 key（16字节）+ IV（16字节）
 *  2. 用后端 SM2 公钥加密 key / IV（SM2 C1C3C2 模式）
 *  3. 用 SM4/CBC/PKCS#7 加密 DTO 中标记为敏感的字段（由调用方明确传入字段名列表）
 *  4. 用 HMAC-SM3 对 body + timestamp + requestId 签名
 *  5. 返回加密后的请求头 + 处理后的 body
 */

import { sm2, sm4, sm3 } from 'sm-crypto'
import { publicKeyStore } from './publicKeyStore'

/**
 * 生成 16 字节随机 Hex 字符串（SM4 key / IV 长度）
 */
function randomHex16() {
  const arr = new Uint8Array(16)
  crypto.getRandomValues(arr)
  return Array.from(arr, (b) => b.toString(16).padStart(2, '0')).join('')
}

/**
 * 将 Hex 字符串转为 WordArray 供 sm4 使用
 */
function hexToBytes(hex) {
  const bytes = []
  for (let i = 0; i < hex.length; i += 2) {
    bytes.push(parseInt(hex.slice(i, i + 2), 16))
  }
  return bytes
}

/**
 * Base64 编码 Hex 字符串
 */
function hexToBase64(hex) {
  return btoa(
    hex
      .match(/.{1,2}/g)
      .map((byte) => String.fromCharCode(parseInt(byte, 16)))
      .join('')
  )
}

/**
 * 加密单个字符串字段（SM4 CBC）
 * @param {string} plaintext
 * @param {string} keyHex   16 字节 hex
 * @param {string} ivHex    16 字节 hex
 * @returns {string} Base64 密文
 */
export function sm4Encrypt(plaintext, keyHex, ivHex) {
  const encrypted = sm4.encrypt(plaintext, keyHex, { mode: 'cbc', iv: ivHex, padding: 'pkcs#7', output: 'array' })
  return hexToBase64(encrypted.map((b) => b.toString(16).padStart(2, '0')).join(''))
}

/**
 * 解密单个 Base64 密文字段（SM4 CBC）
 */
export function sm4Decrypt(cipherBase64, keyHex, ivHex) {
  const hex = atob(cipherBase64)
    .split('')
    .map((c) => c.charCodeAt(0).toString(16).padStart(2, '0'))
    .join('')
  return sm4.decrypt(hex, keyHex, { mode: 'cbc', iv: ivHex, padding: 'pkcs#7', output: 'string' })
}

/**
 * 对请求做国密加密处理，返回加密后的 headers 和 body
 *
 * @param {object} bodyObj          原始请求体对象
 * @param {string[]} sensitiveFields  需要加密的字段名列表（支持点路径，如 'user.password'）
 * @returns {Promise<{ headers: object, body: object }>}
 */
export async function encryptRequest(bodyObj, sensitiveFields = []) {
  const publicKey = await publicKeyStore.get()

  // 1. 生成本次请求的 SM4 key + IV
  const sm4KeyHex = randomHex16()
  const sm4IvHex  = randomHex16()

  // 2. 加密敏感字段
  const encryptedBody = JSON.parse(JSON.stringify(bodyObj))
  for (const fieldPath of sensitiveFields) {
    const parts = fieldPath.split('.')
    let obj = encryptedBody
    for (let i = 0; i < parts.length - 1; i++) {
      obj = obj?.[parts[i]]
    }
    const lastKey = parts[parts.length - 1]
    if (obj && typeof obj[lastKey] === 'string' && obj[lastKey]) {
      obj[lastKey] = sm4Encrypt(obj[lastKey], sm4KeyHex, sm4IvHex)
    }
  }

  // 3. 防重放元数据
  const timestamp = Math.floor(Date.now() / 1000).toString()
  const requestId = crypto.randomUUID()

  // 4. HMAC-SM3 签名：body（序列化）+ timestamp + requestId
  const bodyStr   = JSON.stringify(encryptedBody)
  const payload   = bodyStr + timestamp + requestId
  const hmacHex   = sm3(payload, { mode: 'hmac', key: sm4KeyHex })
  const signature = hexToBase64(hmacHex)

  // 5. SM2 加密 key / IV（C1C3C2 = mode 1）
  const encryptedKey = sm2.doEncrypt(sm4KeyHex, publicKey, 1)
  const encryptedIv  = sm2.doEncrypt(sm4IvHex,  publicKey, 1)

  return {
    headers: {
      'X-Encrypted-Key': hexToBase64(encryptedKey),
      'X-Encrypted-Iv':  hexToBase64(encryptedIv),
      'X-Signature':     signature,
      'X-Timestamp':     timestamp,
      'X-Request-Id':    requestId,
    },
    body: encryptedBody,
  }
}
