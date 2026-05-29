// Cross-check links between specs, plans, tasks; validate depends-on / supersedes; relative file links.
import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs'
import { join, relative, basename, dirname, sep } from 'node:path'
import { fileURLToPath } from 'node:url'
import { readFM } from './_frontmatter.mjs'

const SPECS_ROOT = join(dirname(fileURLToPath(import.meta.url)), '..')

function* walk(dir) {
  for (const entry of readdirSync(dir)) {
    const p = join(dir, entry)
    const s = statSync(p)
    if (s.isDirectory()) yield* walk(p)
    else if (s.isFile() && p.endsWith('.md')) yield p
  }
}

function rel(p) {
  return relative(SPECS_ROOT, p).split(sep).join('/')
}

const specs = new Map() // id -> { file, fm }
const plans = new Map() // id -> { file, fm }
const tasks = new Map() // id -> { file, fm }
const adrs = new Map() // id -> { file, fm }

for (const f of walk(SPECS_ROOT)) {
  const r = rel(f)
  if (r.startsWith('_templates/') || basename(f) === 'README.md') continue
  if (r === 'traceability.md') continue
  const { fm } = readFM(f)
  if (!fm || !fm.id) continue
  if (basename(f).endsWith('.spec.md')) specs.set(fm.id, { file: f, rel: r, fm })
  else if (basename(f).endsWith('.plan.md')) plans.set(fm.id, { file: f, rel: r, fm })
  else if (basename(f).endsWith('.tasks.md')) tasks.set(fm.id, { file: f, rel: r, fm })
  else if (r.startsWith('adr/')) adrs.set(fm.id, { file: f, rel: r, fm })
}

const errs = []

for (const [id, s] of specs) {
  const list = s.fm['depends-on']
  if (Array.isArray(list)) {
    for (const dep of list) {
      if (!specs.has(dep)) errs.push(`${s.rel}: depends-on '${dep}' not found among specs`)
    }
  }
  const sup = s.fm.supersedes
  if (Array.isArray(sup)) {
    for (const old of sup) {
      if (!specs.has(old)) errs.push(`${s.rel}: supersedes '${old}' not found among specs`)
    }
  }
}

for (const [id, p] of plans) {
  if (!specs.has(id)) errs.push(`${p.rel}: plan id '${id}' has no matching spec`)
  else {
    const expected = specs.get(id).rel
    if (p.fm.spec && !expected.endsWith(p.fm.spec)) {
      errs.push(`${p.rel}: spec '${p.fm.spec}' does not match expected '${expected}'`)
    }
  }
  if (specs.has(id) && specs.get(id).fm.status === 'draft' && p.fm.status !== 'draft') {
    errs.push(`${p.rel}: spec ${id} still draft, plan should not advance`)
  }
}

for (const [id, t] of tasks) {
  if (!plans.has(id)) errs.push(`${t.rel}: tasks id '${id}' has no matching plan`)
}

const allText = []
for (const f of walk(SPECS_ROOT)) {
  if (rel(f).startsWith('_templates/')) continue
  allText.push({ file: f, rel: rel(f), text: readFileSync(f, 'utf8') })
}
const linkRe = /\]\(([^)]+\.md)(#[^)]*)?\)/g
for (const { file, rel: r, text } of allText) {
  let m
  while ((m = linkRe.exec(text))) {
    const target = m[1]
    if (target.startsWith('http://') || target.startsWith('https://')) continue
    const resolved = join(dirname(file), target)
    if (!existsSync(resolved)) errs.push(`${r}: broken link '${target}'`)
  }
}

if (errs.length) {
  console.error(`[specs:check-links] ${errs.length} issue(s):`)
  for (const e of errs) console.error('  - ' + e)
  process.exit(1)
}
console.log(
  `[specs:check-links] ok — ${specs.size} spec, ${plans.size} plan, ${tasks.size} tasks, ${adrs.size} adr.`
)
