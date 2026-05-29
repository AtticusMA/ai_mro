// Minimal YAML front-matter parser (no external deps).
// Supports: scalars, single-line lists `[a, b]`, quoted strings, comments stripped.
import { readFileSync } from 'node:fs'

const FM_RE = /^---\r?\n([\s\S]*?)\r?\n---\r?\n?/

function stripComment(s) {
  // Strip ` # comment` only when # is preceded by whitespace and outside quotes.
  let out = ''
  let inSingle = false
  let inDouble = false
  for (let i = 0; i < s.length; i++) {
    const c = s[i]
    const prev = i > 0 ? s[i - 1] : ''
    if (c === "'" && !inDouble) inSingle = !inSingle
    else if (c === '"' && !inSingle) inDouble = !inDouble
    if (c === '#' && !inSingle && !inDouble && /\s/.test(prev)) break
    out += c
  }
  return out.trimEnd()
}

function parseScalar(raw) {
  const s = raw.trim()
  if (s === '') return ''
  if (s === 'null' || s === '~') return null
  if (s === 'true') return true
  if (s === 'false') return false
  if (/^-?\d+$/.test(s)) return Number(s)
  if (/^-?\d+\.\d+$/.test(s)) return Number(s)
  if ((s.startsWith('"') && s.endsWith('"')) || (s.startsWith("'") && s.endsWith("'"))) {
    return s.slice(1, -1)
  }
  if (s.startsWith('[') && s.endsWith(']')) {
    const inner = s.slice(1, -1).trim()
    if (inner === '') return []
    return inner.split(',').map((v) => parseScalar(v))
  }
  return s
}

export function parseFrontMatter(content) {
  const m = content.match(FM_RE)
  if (!m) return { fm: null, body: content }
  const block = m[1]
  const obj = {}
  for (const rawLine of block.split(/\r?\n/)) {
    const line = stripComment(rawLine)
    if (!line.trim()) continue
    const idx = line.indexOf(':')
    if (idx < 0) continue
    const key = line.slice(0, idx).trim()
    const val = line.slice(idx + 1)
    obj[key] = parseScalar(val)
  }
  return { fm: obj, body: content.slice(m[0].length) }
}

export function readFM(path) {
  const text = readFileSync(path, 'utf8')
  return parseFrontMatter(text)
}
