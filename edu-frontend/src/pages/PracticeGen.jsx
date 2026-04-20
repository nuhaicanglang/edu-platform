import { useState } from 'react'
import { Dumbbell, BookOpen, UserCheck, FileSpreadsheet, Loader2, ChevronDown, ChevronUp, CheckCircle, Tag } from 'lucide-react'
import ReactMarkdown from 'react-markdown'
import { agentApi } from '../api'

/* ── Parse JSON from raw string (handles ```json ``` wrappers) ── */
function parseQuestions(raw) {
  if (!raw) return null
  try {
    const s = raw.trim()
    // strip ```json ... ``` or ``` ... ```
    const stripped = s.replace(/^```(?:json)?\s*/i, '').replace(/\s*```\s*$/, '')
    const start = stripped.indexOf('[')
    const end = stripped.lastIndexOf(']')
    if (start >= 0 && end > start) {
      return JSON.parse(stripped.substring(start, end + 1))
    }
    return JSON.parse(stripped)
  } catch (_) {
    return null
  }
}

const TYPE_LABEL = { choice: '选择题', fill: '填空题', short_answer: '简答题', code: '编程题', essay: '论述题' }
const DIFF_COLOR = { easy: 'bg-green-100 text-green-700', medium: 'bg-yellow-100 text-yellow-700', hard: 'bg-red-100 text-red-700' }
const DIFF_LABEL = { easy: '简单', medium: '中等', hard: '困难' }

/* ── Single question card ── */
function QuestionCard({ q, index }) {
  const [showAnswer, setShowAnswer] = useState(false)

  return (
    <div className="border border-gray-200 rounded-xl overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-2.5 bg-gray-50 border-b border-gray-200">
        <div className="flex items-center space-x-2">
          <span className="text-sm font-bold text-gray-700">第 {index + 1} 题</span>
          <span className="px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-700">
            {TYPE_LABEL[q.type] || q.type}
          </span>
          {q.difficulty && (
            <span className={`px-2 py-0.5 rounded text-xs font-medium ${DIFF_COLOR[q.difficulty] || 'bg-gray-100 text-gray-600'}`}>
              {DIFF_LABEL[q.difficulty] || q.difficulty}
            </span>
          )}
        </div>
        <div className="flex items-center space-x-2">
          {q.score != null && (
            <span className="text-xs text-gray-500 font-medium">{q.score} 分</span>
          )}
          {q.knowledgePoints?.length > 0 && (
            <div className="hidden sm:flex items-center space-x-1">
              {q.knowledgePoints.slice(0, 2).map((kp, i) => (
                <span key={i} className="flex items-center px-1.5 py-0.5 bg-purple-50 text-purple-600 rounded text-xs">
                  <Tag className="w-2.5 h-2.5 mr-0.5" />{kp}
                </span>
              ))}
              {q.knowledgePoints.length > 2 && (
                <span className="text-xs text-gray-400">+{q.knowledgePoints.length - 2}</span>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Question body */}
      <div className="px-4 py-3 text-sm text-gray-800">
        <div className="prose prose-sm max-w-none">
          <ReactMarkdown>{q.question}</ReactMarkdown>
        </div>
      </div>

      {/* Options (choice) */}
      {q.options?.length > 0 && (
        <div className="px-4 pb-3 space-y-1.5">
          {q.options.map((opt, i) => (
            <div key={i} className="flex items-start space-x-2 text-sm text-gray-700">
              <span className="mt-0.5 w-5 h-5 flex-shrink-0 rounded-full bg-gray-100 flex items-center justify-center text-xs font-medium text-gray-600">
                {String.fromCharCode(65 + i)}
              </span>
              <span>{opt.replace(/^[A-D]\.\s*/, '')}</span>
            </div>
          ))}
        </div>
      )}

      {/* Toggle answer */}
      <div className="border-t border-gray-100">
        <button
          onClick={() => setShowAnswer(v => !v)}
          className="w-full flex items-center justify-between px-4 py-2 text-xs text-primary-600 hover:bg-primary-50 transition-colors font-medium">
          <span className="flex items-center"><CheckCircle className="w-3.5 h-3.5 mr-1.5" />查看答案与解析</span>
          {showAnswer ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
        </button>
        {showAnswer && (
          <div className="px-4 pb-4 space-y-2 text-sm">
            <div className="p-2.5 bg-green-50 rounded-lg border border-green-100">
              <p className="text-xs font-semibold text-green-700 mb-1">正确答案</p>
              <div className="text-green-800 prose prose-sm max-w-none">
                <ReactMarkdown>{String(q.answer)}</ReactMarkdown>
              </div>
            </div>
            {q.explanation && (
              <div className="p-2.5 bg-blue-50 rounded-lg border border-blue-100">
                <p className="text-xs font-semibold text-blue-700 mb-1">解析</p>
                <div className="text-blue-800 prose prose-sm max-w-none text-xs">
                  <ReactMarkdown>{q.explanation}</ReactMarkdown>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

const modes = [
  { key: 'byKnowledge', icon: BookOpen, label: '按知识点生成' },
  { key: 'personalized', icon: UserCheck, label: '个性化练习' },
  { key: 'examPaper', icon: FileSpreadsheet, label: '模拟试卷' },
]

export default function PracticeGen() {
  const [mode, setMode] = useState('byKnowledge')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState('')

  const [kpForm, setKpForm] = useState({ courseName: '', knowledgePoints: '', difficulty: 'medium', count: 5 })
  const [pForm, setPForm] = useState({ courseName: '', studentName: '', weakPoints: '', masteredPoints: '', count: 5 })
  const [examForm, setExamForm] = useState({ courseName: '', knowledgePoints: '', totalScore: 100, examType: '期末考试' })

  const handleGenerate = async () => {
    setLoading(true)
    setResult('')
    try {
      let res
      switch (mode) {
        case 'byKnowledge':
          res = await agentApi.generatePractice({
            courseName: kpForm.courseName,
            knowledgePoints: kpForm.knowledgePoints.split(/[,，、\n]/).filter(Boolean).map(s => s.trim()),
            difficulty: kpForm.difficulty,
            count: kpForm.count
          })
          break
        case 'personalized':
          res = await agentApi.personalizedPractice({
            courseName: pForm.courseName,
            studentName: pForm.studentName,
            weakPoints: pForm.weakPoints.split(/[,，、\n]/).filter(Boolean).map(s => s.trim()),
            masteredPoints: pForm.masteredPoints.split(/[,，、\n]/).filter(Boolean).map(s => s.trim()),
            count: pForm.count
          })
          break
        case 'examPaper':
          res = await agentApi.examPaper({
            courseName: examForm.courseName,
            knowledgePoints: examForm.knowledgePoints.split(/[,，、\n]/).filter(Boolean).map(s => s.trim()),
            totalScore: examForm.totalScore,
            examType: examForm.examType
          })
          break
      }
      setResult(res.data)
    } catch (err) {
      setResult('生成失败: ' + (err.message || '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">增量练习生成</h2>
        <p className="text-gray-500 mt-1">AI驱动的个性化练习题目生成引擎</p>
      </div>

      {/* Mode Toggle */}
      <div className="flex space-x-2">
        {modes.map(m => (
          <button key={m.key} onClick={() => { setMode(m.key); setResult('') }}
            className={`flex items-center px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              mode === m.key ? 'bg-primary-600 text-white' : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
            }`}>
            <m.icon className="w-4 h-4 mr-2" /> {m.label}
          </button>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Input */}
        <div className="card space-y-4">
          {mode === 'byKnowledge' && (
            <>
              <h3 className="font-semibold">按知识点生成练习</h3>
              <div>
                <label className="label">课程名称</label>
                <input className="input" value={kpForm.courseName}
                  onChange={e => setKpForm(p => ({ ...p, courseName: e.target.value }))} placeholder="Java程序设计" />
              </div>
              <div>
                <label className="label">目标知识点（逗号分隔）</label>
                <textarea className="input" rows={2} value={kpForm.knowledgePoints}
                  onChange={e => setKpForm(p => ({ ...p, knowledgePoints: e.target.value }))}
                  placeholder="面向对象, 继承与多态, 抽象类与接口" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">难度</label>
                  <select className="input" value={kpForm.difficulty}
                    onChange={e => setKpForm(p => ({ ...p, difficulty: e.target.value }))}>
                    <option value="easy">简单</option>
                    <option value="medium">中等</option>
                    <option value="hard">困难</option>
                  </select>
                </div>
                <div>
                  <label className="label">题目数量</label>
                  <input className="input" type="number" min={1} max={20} value={kpForm.count}
                    onChange={e => setKpForm(p => ({ ...p, count: parseInt(e.target.value) || 5 }))} />
                </div>
              </div>
            </>
          )}

          {mode === 'personalized' && (
            <>
              <h3 className="font-semibold">个性化练习</h3>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">课程名称</label>
                  <input className="input" value={pForm.courseName}
                    onChange={e => setPForm(p => ({ ...p, courseName: e.target.value }))} placeholder="Java程序设计" />
                </div>
                <div>
                  <label className="label">学生姓名</label>
                  <input className="input" value={pForm.studentName}
                    onChange={e => setPForm(p => ({ ...p, studentName: e.target.value }))} placeholder="张三" />
                </div>
              </div>
              <div>
                <label className="label">薄弱知识点（逗号分隔）</label>
                <textarea className="input" rows={2} value={pForm.weakPoints}
                  onChange={e => setPForm(p => ({ ...p, weakPoints: e.target.value }))}
                  placeholder="多线程, 异常处理" />
              </div>
              <div>
                <label className="label">已掌握知识点（逗号分隔）</label>
                <textarea className="input" rows={2} value={pForm.masteredPoints}
                  onChange={e => setPForm(p => ({ ...p, masteredPoints: e.target.value }))}
                  placeholder="变量, 控制流程, 数组" />
              </div>
              <div>
                <label className="label">题目数量</label>
                <input className="input" type="number" min={1} max={20} value={pForm.count}
                  onChange={e => setPForm(p => ({ ...p, count: parseInt(e.target.value) || 5 }))} />
              </div>
            </>
          )}

          {mode === 'examPaper' && (
            <>
              <h3 className="font-semibold">模拟试卷生成</h3>
              <div>
                <label className="label">课程名称</label>
                <input className="input" value={examForm.courseName}
                  onChange={e => setExamForm(p => ({ ...p, courseName: e.target.value }))} placeholder="Java程序设计" />
              </div>
              <div>
                <label className="label">知识点覆盖（逗号分隔）</label>
                <textarea className="input" rows={3} value={examForm.knowledgePoints}
                  onChange={e => setExamForm(p => ({ ...p, knowledgePoints: e.target.value }))}
                  placeholder="变量与数据类型, 控制流程, 面向对象, 异常处理, 集合框架, IO流, 多线程" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">总分</label>
                  <input className="input" type="number" value={examForm.totalScore}
                    onChange={e => setExamForm(p => ({ ...p, totalScore: parseInt(e.target.value) || 100 }))} />
                </div>
                <div>
                  <label className="label">考试类型</label>
                  <select className="input" value={examForm.examType}
                    onChange={e => setExamForm(p => ({ ...p, examType: e.target.value }))}>
                    <option value="期中考试">期中考试</option>
                    <option value="期末考试">期末考试</option>
                    <option value="随堂测验">随堂测验</option>
                  </select>
                </div>
              </div>
            </>
          )}

          <button onClick={handleGenerate} disabled={loading} className="btn-primary w-full flex items-center justify-center">
            {loading ? <><Loader2 className="w-4 h-4 mr-2 animate-spin" /> 生成中...</>
              : <><Dumbbell className="w-4 h-4 mr-2" /> 生成练习</>}
          </button>
        </div>

        {/* Result */}
        <div className="card">
          {(() => {
            const questions = parseQuestions(result)
            if (!result) return (
              <div className="text-center py-16 text-gray-400">
                <Dumbbell className="w-12 h-12 mx-auto mb-3 opacity-30" />
                <p>设置参数后生成个性化练习题</p>
                <p className="text-xs mt-1">支持按知识点、个性化推荐、模拟试卷</p>
              </div>
            )
            if (questions && Array.isArray(questions) && questions.length > 0) return (
              <div className="space-y-4 overflow-auto max-h-[calc(100vh-14rem)]">
                <div className="flex items-center justify-between mb-2">
                  <h3 className="font-semibold text-gray-900">共 {questions.length} 题</h3>
                  <span className="text-xs text-gray-400">总分：{questions.reduce((s, q) => s + (q.score || 0), 0)} 分</span>
                </div>
                {questions.map((q, i) => <QuestionCard key={q.id ?? i} q={q} index={i} />)}
              </div>
            )
            // fallback: not valid JSON, show as markdown
            return (
              <div className="markdown-body text-sm overflow-auto max-h-[calc(100vh-14rem)]">
                <ReactMarkdown>{result}</ReactMarkdown>
              </div>
            )
          })()}
        </div>
      </div>
    </div>
  )
}
