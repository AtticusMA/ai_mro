import request from '@/utils/request'

export const getSpecs = (params) => request.get('/api/wiki/specs', { params })

export const getSpecDetail = (id) => request.get('/api/wiki/specs/' + id, { params: { id } })

export const getCodeMapping = () => request.get('/api/wiki/code-mapping')

export const askAi = (question) => request.post('/api/wiki/ai/chat', { question })

export const getApiDocs = () => request.get('/api/wiki/api-docs')
