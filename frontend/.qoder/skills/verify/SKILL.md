---
name: verify
description: Verify the frontend still builds cleanly by running `npm run build`. Use after non-trivial edits, before handing work back to the user, or whenever the AI wants confidence that no syntax/import errors were introduced.
---

# /verify

Run a production build to confirm the project still compiles after recent changes.

## Steps

1. From the project root (`D:\ai_code\ui\frontend`), run:

   ```bash
   npm run build
   ```

2. If the build succeeds, report success in one line (e.g., "Build OK").

3. If the build fails:
   - Show the relevant error excerpt (don't dump the whole log).
   - Identify the offending file(s) and line(s).
   - Propose a focused fix, then re-run `npm run build` to confirm.

## Notes

- There is no test runner or lint script in this repo, so `vite build` is the only automated signal.
- The build runs against the current env file selected by Vite mode (defaults to `.env.production` for `vite build`). Mock plugin is off in production mode — import-time errors in mock files won't surface here.
- Do not run `npm install` unless the build fails specifically because of missing dependencies.
