// Walk specs/ and validate front-matter of all .md files (except templates and README).
// Exit code: 0 = ok, 1 = errors found.
import { readdirSync, statSync } from 'node:fs'
import { join, relative, basename, dirname, sep } from 'node:path'
import { fileURLToPath } from 'node:url'
import { readFM } from './_frontmatter.mjs'

const SPECS_ROOT = join(dirname(fileURLToPath(import.meta.url)), '..')

const SPEC_STATUS = ['draft', 'review', 'approved', 'deprecated']
const ADR_STATUS = ['proposed', 'accepted', 'superseded', 'rejected']

const ID_RE = /^[A-Z]{2,8}-\d{3}$/
const ADR_ID_RE = /^ADR-\d{3}$/
const VERSION_RE = /^\d+\.\d+\.\d+$/
const DATE_RE = /^\d{4}-\d{2}-\d{2}$/

function classify(file) {
  const name = basename(file)
  const rel = relative(SPECS_ROOT, file).split(sep).join('/')
  if (rel.startsWith('_templates/')) return 'template'
  if (name === 'README.md') return 'readme'
  if (name === 'CHARTER.md') return 'charter'
  if (name === 'traceability.md') return 'generated'
  if (rel.startsWith('adr/')) return 'adr'
  if (name.endsWith('.spec.md')) return 'spec'
  if (name.endsWith('.plan.md')) return 'plan'
  if (name.endsWith('.tasks.md')) return 'tasks'
  return 'other'
}

function* walk(dir) {
  for (const entry of readdirSync(dir)) {
    const p = join(dir, entry)
    const s = statSync(p)
    if (s.isDirectory()) yield* walk(p)
    else if (s.isFile() && p.endsWith('.md')) yield p
  }
}

function checkRequired(fm, required, errs, file) {
  for (const k of required) {
    if (fm[k] === undefined || fm[k] === '' || fm[k] === null) {
      errs.push(`${file}: missing front-matter field '${k}'`)
    }
  }
}

function check(file) {
  const errs = []
  const kind = classify(file)
  if (['template', 'readme', 'generated', 'other'].includes(kind)) return errs
  const { fm } = readFM(file)
  if (!fm) {
    errs.push(`${file}: missing YAML front-matter`)
    return errs
  }

  if (kind === 'charter') {
    checkRequired(fm, ['id', 'title', 'status', 'version', 'created', 'updated'], errs, file)
    if (fm.version && !VERSION_RE.test(String(fm.version)))
      errs.push(`${file}: version must be semver x.y.z`)
    return errs
  }

  if (kind === 'spec') {
    checkRequired(
      fm,
      ['id', 'title', 'domain', 'status', 'owner', 'version', 'created', 'updated'],
      errs,
      file
    )
    if (fm.id && !ID_RE.test(fm.id)) errs.push(`${file}: id '${fm.id}' must match ${ID_RE}`)
    if (fm.status && !SPEC_STATUS.includes(fm.status))
      errs.push(`${file}: status '${fm.status}' not in ${SPEC_STATUS.join('|')}`)
    if (fm.version && !VERSION_RE.test(String(fm.version)))
      errs.push(`${file}: version must be semver x.y.z`)
    for (const k of ['created', 'updated']) {
      if (fm[k] && !DATE_RE.test(String(fm[k])))
        errs.push(`${file}: ${k} must be YYYY-MM-DD`)
    }
    return errs
  }

  if (kind === 'plan') {
    checkRequired(fm, ['id', 'spec', 'status', 'owner'], errs, file)
    if (fm.id && !ID_RE.test(fm.id)) errs.push(`${file}: id '${fm.id}' must match ${ID_RE}`)
    return errs
  }

  if (kind === 'tasks') {
    checkRequired(fm, ['id', 'plan'], errs, file)
    if (fm.id && !ID_RE.test(fm.id)) errs.push(`${file}: id '${fm.id}' must match ${ID_RE}`)
    return errs
  }

  if (kind === 'adr') {
    checkRequired(fm, ['id', 'title', 'status', 'date'], errs, file)
    if (fm.id && !ADR_ID_RE.test(fm.id))
      errs.push(`${file}: id '${fm.id}' must match ${ADR_ID_RE}`)
    if (fm.status && !ADR_STATUS.includes(fm.status))
      errs.push(`${file}: status '${fm.status}' not in ${ADR_STATUS.join('|')}`)
    if (fm.date && !DATE_RE.test(String(fm.date)))
      errs.push(`${file}: date must be YYYY-MM-DD`)
    return errs
  }

  return errs
}

const allErrs = []
let total = 0
for (const f of walk(SPECS_ROOT)) {
  if (classify(f) === 'template' || classify(f) === 'readme' || classify(f) === 'generated')
    continue
  total++
  allErrs.push(...check(f))
}

if (allErrs.length) {
  console.error(`[specs:validate] ${allErrs.length} error(s) in ${total} file(s):`)
  for (const e of allErrs) console.error('  - ' + e)
  process.exit(1)
}
console.log(`[specs:validate] ok — ${total} file(s) checked.`)
