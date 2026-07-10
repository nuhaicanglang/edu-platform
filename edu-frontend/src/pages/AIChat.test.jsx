import '@testing-library/jest-dom/vitest'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AIChat from './AIChat'
import { agentApi, courseApi } from '../api'

vi.mock('../api', () => ({
  agentApi: {
    ask: vi.fn(),
    smartAsk: vi.fn(),
    chatRecordsMy: vi.fn()
  },
  courseApi: { myList: vi.fn() }
}))

describe('AIChat RAG flow', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    Element.prototype.scrollIntoView = vi.fn()
    courseApi.myList.mockResolvedValue({
      data: [
        { id: 12, courseName: '数据结构' },
        { id: 13, courseName: '操作系统' }
      ]
    })
    agentApi.ask.mockResolvedValue({
      data: {
        answer: '前序遍历先访问根节点。[1]',
        retrievalMode: 'hybrid',
        sources: [{
          documentId: 7,
          documentTitle: '数据结构基础',
          chunkId: '31',
          chunkIndex: 4,
          score: 0.03
        }]
      }
    })
  })

  it('requires a course and renders server-provided sources', async () => {
    render(<AIChat />)

    const courseSelect = await screen.findByLabelText('检索课程')
    fireEvent.change(courseSelect, { target: { value: '12' } })
    fireEvent.change(screen.getByPlaceholderText('输入你的问题...'), {
      target: { value: '什么是前序遍历？' }
    })
    fireEvent.click(screen.getByLabelText('发送问题'))

    await waitFor(() => {
      expect(agentApi.ask).toHaveBeenCalledWith('什么是前序遍历？', 12)
    })
    expect(await screen.findByText(/数据结构基础/)).toBeInTheDocument()
    expect(screen.getByText('向量 + 关键词混合检索')).toBeInTheDocument()
  })
})
