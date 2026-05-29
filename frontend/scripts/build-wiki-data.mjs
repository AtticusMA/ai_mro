import { readdirSync, readFileSync, writeFileSync, statSync } from 'node:fs'
import { join, relative, basename, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const SPECS_DIR = join(__dirname, '..', '..', 'specs')
const MOCK_DIR = join(__dirname, '..', 'src', 'mock', 'api')
const OUT_DIR = join(__dirname, '..', 'src', 'mock', 'data')

const FM_RE = /^---\r?\n([\s\S]*?)\r?\n---\r?\n?/

function parseScalar(raw) {
  const s = raw.trim()
  if (s === '' || s === 'null' || s === '~') return null
  if (s === 'true') return true
  if (s === 'false') return false
  if (/^-?\d+$/.test(s)) return Number(s)
  if (/^-?\d+\.\d+$/.test(s)) return Number(s)
  if ((s.startsWith('"') && s.endsWith('"')) || (s.startsWith("'") && s.endsWith("'")))
    return s.slice(1, -1)
  if (s.startsWith('[') && s.endsWith(']')) {
    const inner = s.slice(1, -1).trim()
    if (inner === '') return []
    return inner.split(',').map(v => parseScalar(v))
  }
  return s
}

function parseFrontMatter(content) {
  const m = content.match(FM_RE)
  if (!m) return { fm: null, body: content }
  const block = m[1]
  const obj = {}
  for (const line of block.split(/\r?\n/)) {
    const clean = line.replace(/\s+#\s.*$/, '').trim()
    if (!clean) continue
    const idx = clean.indexOf(':')
    if (idx < 0) continue
    obj[clean.slice(0, idx).trim()] = parseScalar(clean.slice(idx + 1))
  }
  return { fm: obj, body: content.slice(m[0].length) }
}

function walk(dir) {
  const results = []
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const full = join(dir, entry.name)
    if (entry.isDirectory()) {
      if (entry.name === '_templates' || entry.name === '_scripts') continue
      results.push(...walk(full))
    } else if (entry.name.endsWith('.md')) {
      results.push(full)
    }
  }
  return results
}

function inferType(fileName) {
  if (fileName.endsWith('.spec.md')) return 'spec'
  if (fileName.endsWith('.plan.md')) return 'plan'
  if (fileName.endsWith('.tasks.md')) return 'tasks'
  return 'other'
}

function inferDomain(relPath) {
  const parts = relPath.split(/[\\/]/)
  if (parts.length > 1) return parts[0]
  return 'root'
}

// ── Build wiki-specs.json ──
const SKIP = new Set(['README.md', 'USAGE.md', 'traceability.md', 'question.md'])
const allFiles = walk(SPECS_DIR)
const specs = []

for (const file of allFiles) {
  const rel = relative(SPECS_DIR, file).replace(/\\/g, '/')
  const name = basename(file)
  if (SKIP.has(name)) continue

  const content = readFileSync(file, 'utf8')
  const { fm, body } = parseFrontMatter(content)
  const relDir = dirname(rel)
  const isAdr = relDir === 'adr'
  const domain = isAdr ? 'adr' : inferDomain(rel)
  let type = isAdr ? 'adr' : inferType(name)
  if (name === 'CHARTER.md') type = 'charter'

  specs.push({
    id: fm?.id || name.replace(/\.md$/, ''),
    title: fm?.title || name,
    domain,
    status: fm?.status || 'unknown',
    version: fm?.version || null,
    owner: fm?.owner || null,
    created: fm?.created || fm?.date || null,
    updated: fm?.updated || fm?.date || null,
    dependsOn: fm?.['depends-on'] || [],
    supersedes: fm?.supersedes || [],
    type,
    fileName: rel,
    body,
  })
}

writeFileSync(join(OUT_DIR, 'wiki-specs.json'), JSON.stringify(specs, null, 2), 'utf8')
console.log(`[wiki] Generated wiki-specs.json (${specs.length} docs)`)

// ── Build wiki-api-docs.json ──
const ENDPOINT_RE = /url:\s*['"]([^'"]+)['"]/g
const METHOD_RE = /method:\s*['"]([^'"]+)['"]/g

const apiDocs = []
for (const entry of readdirSync(MOCK_DIR)) {
  if (!entry.endsWith('.js') || entry === 'wiki.js') continue
  const content = readFileSync(join(MOCK_DIR, entry), 'utf8')
  const urls = [...content.matchAll(ENDPOINT_RE)].map(m => m[1])
  const methods = [...content.matchAll(METHOD_RE)].map(m => m[1].toUpperCase())
  const endpoints = urls.map((url, i) => ({ url, method: methods[i] || 'GET' }))
  if (endpoints.length) {
    apiDocs.push({ domain: entry.replace('.js', ''), endpoints })
  }
}

writeFileSync(join(OUT_DIR, 'wiki-api-docs.json'), JSON.stringify(apiDocs, null, 2), 'utf8')
console.log(`[wiki] Generated wiki-api-docs.json (${apiDocs.length} domains)`)
