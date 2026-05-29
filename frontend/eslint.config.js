// ESLint flat config for Vue 3 + JavaScript (Composition API, <script setup>)
// Requires: eslint >=9, eslint-plugin-vue, vue-eslint-parser, eslint-config-prettier
// Install with:
//   npm install -D eslint@^9 eslint-plugin-vue vue-eslint-parser eslint-config-prettier globals

import js from '@eslint/js'
import vue from 'eslint-plugin-vue'
import vueParser from 'vue-eslint-parser'
import prettier from 'eslint-config-prettier'
import globals from 'globals'

export default [
  {
    ignores: ['dist/**', 'node_modules/**', 'public/**'],
  },
  js.configs.recommended,
  ...vue.configs['flat/recommended'],
  {
    files: ['**/*.{js,mjs,cjs,vue}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      parser: vueParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
      globals: {
        ...globals.browser,
        ...globals.node,
      },
    },
    rules: {
      // Match repo style (no semis, single quotes) — Prettier handles the formatting,
      // these only catch things Prettier won't.
      'no-unused-vars': ['warn', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      'vue/multi-word-component-names': 'off',
      'vue/no-v-html': 'off',
    },
  },
  // Mock files use Mock.js globals and looser rules.
  {
    files: ['src/mock/**/*.js'],
    rules: {
      'no-unused-vars': 'off',
    },
  },
  // Must come last to disable rules that conflict with Prettier formatting.
  prettier,
]
