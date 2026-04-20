import { useState, useRef, useEffect } from 'react'
import { Send, Bot, User, Loader2, Trash2, History, MessageSquare } from 'lucide-react'
import ReactMarkdown from 'react-markdown'
import { agentApi } from '../api'

export default function AIChat() {
  const [tab, setTab] = useState('chat') // 'chat' | 'history'
  const [messages, setMessages] = useState([
    { role: 'assistant', content: '你好！我是AI教学助手，可以为你解答课程相关问题。请问有什么可以帮助你的？' }
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [agentMode, setAgentMode] = useState(false)
  const bottomRef = useRef(null)

  // History state
  const [historyRecords, setHistoryRecords] = useState([])
  const [historyTotal, setHistoryTotal] = useState(0)
  const [historyPage, setHistoryPage] = useState(1)
  const [historyLoading, setHistoryLoading] = useState(false)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  useEffect(() => {
    if (tab === 'history') loadHistory()
  }, [tab, historyPage])

  const loadHistory = async () => {
    setHistoryLoading(true)
    try {
      const res = await agentApi.chatRecordsMy({ pageNum: historyPage, pageSize: 20 })
      setHistoryRecords(res.data.records || [])
      setHistoryTotal(res.data.total || 0)
    } catch { /* ignore */ }
    finally { setHistoryLoading(false) }
  }

  const handleSend = async () => {
    const q = input.trim()
    if (!q || loading) return
    setInput('')
    setMessages(prev => [...prev, { role: 'user', content: q }])
    setLoading(true)
    try {
      const res = agentMode
        ? await agentApi.smartAsk({ question: q })
        : await agentApi.askSimple(q)
      setMessages(prev => [...prev, { role: 'assistant', content: res.data }])
    } catch (err) {
      setMessages(prev => [...prev, { role: 'assistant', content: '抱歉，请求出错：' + (err.message || '未知错误') }])
    } finally {
      setLoading(false)
    }
  }

  const actionLabel = { user: '提问', assistant: 'AI回复', system: '系统' }
  const totalPages = Math.ceil(historyTotal / 20)

  return (
    <div className="h-[calc(100vh-7rem)] flex flex-col">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">智能问答</h2>
          <p className="text-gray-500 text-sm">基于课程知识的AI教学助手</p>
        </div>
        <div className="flex items-center space-x-2">
          <button onClick={() => setTab('chat')}
            className={`flex items-center px-3 py-1.5 rounded-lg text-sm font-medium transition ${
              tab === 'chat' ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
            <MessageSquare className="w-4 h-4 mr-1" /> 对话
          </button>
          <button onClick={() => setTab('history')}
            className={`flex items-center px-3 py-1.5 rounded-lg text-sm font-medium transition ${
              tab === 'history' ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
            <History className="w-4 h-4 mr-1" /> 对话历史
          </button>
          {tab === 'chat' && (
            <>
              <button
                onClick={() => { setAgentMode(v => !v); setMessages([{ role: 'assistant', content: agentMode ? '已切换为普通问答模式。' : '已切换为 Agent 模式！我会自主查询数据库和知识库后再回答你，支持提问如「课程1的学情怎么样？」' }]) }}
                className={`flex items-center px-3 py-1.5 rounded-lg text-sm font-medium border transition ${
                  agentMode ? 'bg-purple-600 text-white border-purple-600' : 'bg-white text-purple-600 border-purple-300 hover:bg-purple-50'}`}>
                🤖 {agentMode ? 'Agent模式' : '普通模式'}
              </button>
              <button onClick={() => setMessages([{ role: 'assistant', content: '对话已清空，请问有什么可以帮助你的？' }])}
                className="btn-secondary flex items-center text-sm">
                <Trash2 className="w-4 h-4 mr-1" /> 清空
              </button>
            </>
          )}
        </div>
      </div>

      {tab === 'chat' ? (
        <>
          {/* Messages */}
          <div className="flex-1 overflow-y-auto card p-4 space-y-4 mb-4">
            {messages.map((msg, i) => (
              <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div className={`flex max-w-[80%] ${msg.role === 'user' ? 'flex-row-reverse' : 'flex-row'}`}>
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
                    msg.role === 'user'
                      ? 'bg-primary-100 text-primary-600 ml-3'
                      : 'bg-green-100 text-green-600 mr-3'
                  }`}>
                    {msg.role === 'user' ? <User className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
                  </div>
                  <div className={`px-4 py-3 rounded-2xl ${
                    msg.role === 'user'
                      ? 'bg-primary-600 text-white'
                      : 'bg-gray-100 text-gray-800'
                  }`}>
                    {msg.role === 'user' ? (
                      <p className="text-sm whitespace-pre-wrap">{msg.content}</p>
                    ) : (
                      <div className="markdown-body text-sm">
                        <ReactMarkdown>{msg.content}</ReactMarkdown>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
            {loading && (
              <div className="flex justify-start">
                <div className="flex items-center space-x-3">
                  <div className="w-8 h-8 rounded-full bg-green-100 flex items-center justify-center">
                    <Bot className="w-4 h-4 text-green-600" />
                  </div>
                  <div className="bg-gray-100 px-4 py-3 rounded-2xl">
                    <Loader2 className="w-5 h-5 animate-spin text-gray-400" />
                  </div>
                </div>
              </div>
            )}
            <div ref={bottomRef} />
          </div>

          {/* Input */}
          <div className="flex space-x-3">
            <input className="input flex-1"
              placeholder="输入你的问题..."
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && !e.shiftKey && handleSend()}
              disabled={loading} />
            <button onClick={handleSend} disabled={loading || !input.trim()} className="btn-primary px-6">
              <Send className="w-5 h-5" />
            </button>
          </div>
        </>
      ) : (
        /* History Tab */
        <div className="flex-1 overflow-y-auto">
          <div className="card p-4">
            <div className="flex items-center justify-between mb-3">
              <p className="text-sm text-gray-500">共 {historyTotal} 条对话记录</p>
              <button onClick={loadHistory} className="text-sm text-primary-600 hover:underline">刷新</button>
            </div>

            {historyLoading ? (
              <div className="flex justify-center py-12"><Loader2 className="w-6 h-6 animate-spin text-gray-400" /></div>
            ) : historyRecords.length === 0 ? (
              <div className="text-center py-12 text-gray-400">暂无对话记录，去对话页面提问吧</div>
            ) : (
              <div className="space-y-3">
                {historyRecords.map((r) => (
                  <div key={r.id} className={`flex ${r.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                    <div className={`max-w-[85%] px-4 py-3 rounded-2xl ${
                      r.role === 'user'
                        ? 'bg-primary-50 border border-primary-200'
                        : 'bg-gray-50 border border-gray-200'
                    }`}>
                      <div className="flex items-center justify-between mb-1">
                        <span className={`text-xs font-medium ${r.role === 'user' ? 'text-primary-600' : 'text-green-600'}`}>
                          {r.role === 'user' ? '🧑 我的提问' : '🤖 AI回复'}
                        </span>
                        <span className="text-xs text-gray-400 ml-4">{r.createTime?.replace('T', ' ')?.slice(0, 19)}</span>
                      </div>
                      <div className="text-sm text-gray-700 whitespace-pre-wrap break-words">
                        {r.content?.length > 300 ? r.content.slice(0, 300) + '...' : r.content}
                      </div>
                      {r.model && <span className="text-xs text-gray-400 mt-1 inline-block">模型: {r.model}</span>}
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-center space-x-2 mt-4 pt-3 border-t">
                <button disabled={historyPage <= 1}
                  onClick={() => setHistoryPage(p => p - 1)}
                  className="px-3 py-1 text-sm rounded border disabled:opacity-40 hover:bg-gray-50">上一页</button>
                <span className="text-sm text-gray-500">{historyPage} / {totalPages}</span>
                <button disabled={historyPage >= totalPages}
                  onClick={() => setHistoryPage(p => p + 1)}
                  className="px-3 py-1 text-sm rounded border disabled:opacity-40 hover:bg-gray-50">下一页</button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
