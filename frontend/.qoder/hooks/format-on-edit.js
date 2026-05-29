#!/usr/bin/env node
// Format-on-edit hook: reads tool-event JSON from stdin and runs `npx prettier --write`
// on the edited file if it's a type Prettier should format.

const { spawnSync } = require('node:child_process')

let raw = ''
process.stdin.setEncoding('utf8')
process.stdin.on('data', (chunk) => { raw += chunk })
process.stdin.on('end', () => {
  let file
  try {
    const payload = JSON.parse(raw || '{}')
    file = payload?.tool_input?.file_path
  } catch {
    process.exit(0)
  }

  if (!file) process.exit(0)
  if (!/\.(js|mjs|cjs|vue|json|css|scss|html|md)$/i.test(file)) process.exit(0)

  const result = spawnSync(
    'npx',
    ['--no', '--', 'prettier', '--write', '--log-level', 'warn', file],
    { stdio: 'inherit', shell: true }
  )
  process.exit(result.status ?? 0)
})
