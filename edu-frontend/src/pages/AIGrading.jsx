import { useState, useRef } from 'react'
import {
  CheckSquare, FileCode, Loader2, Upload, X, Brain,
  Star, ChevronDown, ChevronUp, Download, FileText, Paperclip
} from 'lucide-react'
import { agentApi, assignmentApi } from '../api'

/* ── Grading result panel (structured JSON) ── */
function GradingResultPanel({ result }) {
  const [open, setOpen] = useState(true)
  if (!result) return null
  let parsed = null
  try {
    const clean = result.trim()
    const s = clean.indexOf('{'), e = clean.lastIndexOf('}')
    parsed = JSON.parse(s >= 0 ? clean.substring(s, e + 1) : clean)
  } catch (_) {}

  return (
    <div className="border border-blue-100 rounded-lg overflow-hidden">
      <button
        onClick={() => setOpen(o => !o)}
        className="w-full flex items-center justify-between px-4 py-2 bg-blue-50 text-sm font-medium text-blue-700 hover:bg-blue-100">
        <span>查看批改详情</span>
        {open ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
      </button>
      {open && (
        <div className="p-4 text-sm space-y-3 max-h-[60vh] overflow-y-auto">
          {parsed ? (
            <>
              {parsed.overallComment && (
                <div className="p-3 bg-blue-50 rounded-lg">
                  <p className="font-medium text-blue-800 mb-1">总体评价</p>
                  <p className="text-blue-700">{parsed.overallComment}</p>
                </div>
              )}
              {parsed.annotations?.length > 0 && (
                <div>
                  <p className="font-medium text-gray-700 mb-2">逐项批注</p>
                  <div className="space-y-2">
                    {parsed.annotations.map((ann, i) => (
                      <div key={i} className={`p-3 rounded-lg border-l-4 ${
                        ann.errorType?.includes('错误') || ann.errorType?.includes('Error')
                          ? 'border-red-400 bg-red-50' : 'border-green-400 bg-green-50'
                      }`}>
                        <div className="flex items-center justify-between mb-1">
                          <span className="font-medium text-sm">{ann.position}</span>
                          {ann.score !== undefined && (
                            <span className="text-xs font-medium text-gray-600">
                              {ann.score}/{ann.maxScore}分
                            </span>
                          )}
                        </div>
                        {ann.errorType && (
                          <span className={`inline-block text-xs px-1.5 py-0.5 rounded mb-1 ${
                            ann.errorType === '正确' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                          }`}>{ann.errorType}</span>
                        )}
                        <p className="text-gray-600 text-xs">{ann.comment}</p>
                        {ann.suggestion && <p className="text-gray-500 text-xs mt-1">建议：{ann.suggestion}</p>}
                        {ann.knowledgePoint && (
                          <p className="text-xs text-purple-600 mt-1">知识点：{ann.knowledgePoint}</p>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}
              {parsed.knowledgeSummary && (
                <div className="p-3 bg-gray-50 rounded-lg">
                  <p className="font-medium text-gray-700 mb-2">知识点掌握情况</p>
                  {parsed.knowledgeSummary.mastered?.length > 0 && (
                    <p className="text-xs text-green-700 mb-1">
                      ✅ 已掌握：{parsed.knowledgeSummary.mastered.join('、')}
                    </p>
                  )}
                  {parsed.knowledgeSummary.needImprovement?.length > 0 && (
                    <p className="text-xs text-yellow-700 mb-1">
                      ⚠️ 需加强：{parsed.knowledgeSummary.needImprovement.join('、')}
                    </p>
                  )}
                  {parsed.knowledgeSummary.notGrasped?.length > 0 && (
                    <p className="text-xs text-red-700">
                      ❌ 未掌握：{parsed.knowledgeSummary.notGrasped.join('、')}
                    </p>
                  )}
                </div>
              )}
              {parsed.improvementPlan && (
                <div className="p-3 bg-green-50 rounded-lg">
                  <p className="font-medium text-green-800 mb-1">改进建议</p>
                  <p className="text-green-700 text-xs">{parsed.improvementPlan}</p>
                </div>
              )}
            </>
          ) : (
            <pre className="text-xs text-gray-600 whitespace-pre-wrap overflow-x-auto">{result}</pre>
          )}
        </div>
      )}
    </div>
  )
}

/* ── Main Component ── */
export default function AIGrading() {
  const [inputMode, setInputMode] = useState('file') // 'file' | 'text' | 'code'
  const [loading, setLoading] = useState(false)
  const [gradeResult, setGradeResult] = useState(null) // {score, overallComment, gradingJson, annotatedFileUrl}
  const [error, setError] = useState('')

  /* file mode */
  const [selectedFile, setSelectedFile] = useState(null)
  const fileRef = useRef(null)

  /* shared form */
  const [form, setForm] = useState({
    assignmentTitle: '',
    assignmentRequirement: '',
    referenceAnswer: '',
    studentAnswer: '',   // text mode only
    studentCode: '',     // code mode only
    testCases: '',       // code mode only
  })

  const setF = (key, val) => setForm(p => ({ ...p, [key]: val }))

  const handleFileSelect = (e) => {
    const f = e.target.files?.[0]
    if (f) setSelectedFile(f)
  }

  const handleDrop = (e) => {
    e.preventDefault()
    const f = e.dataTransfer.files?.[0]
    if (f) setSelectedFile(f)
  }

  const handleGrade = async () => {
    if (inputMode === 'file' && !selectedFile) {
      setError('请上传学生作业文件'); return
    }
    if (inputMode === 'text' && !form.studentAnswer.trim()) {
      setError('请输入学生提交内容'); return
    }
    if (inputMode === 'code' && !form.studentCode.trim()) {
      setError('请输入学生代码'); return
    }

    setLoading(true)
    setError('')
    setGradeResult(null)

    try {
      if (inputMode === 'code') {
        // Use existing code grading agent API
        const res = await agentApi.gradeCode({
          assignmentTitle: form.assignmentTitle,
          requirement: form.assignmentRequirement,
          studentCode: form.studentCode,
          testCases: form.testCases,
        })
        setGradeResult({ gradingJson: res.data, score: null, overallComment: null, annotatedFileUrl: null })
      } else {
        // File or text → quickGrade endpoint
        const fd = new FormData()
        fd.append('assignmentTitle', form.assignmentTitle)
        fd.append('assignmentRequirement', form.assignmentRequirement)
        if (form.referenceAnswer) fd.append('referenceAnswer', form.referenceAnswer)
        if (inputMode === 'file' && selectedFile) {
          fd.append('file', selectedFile)
        } else {
          fd.append('content', form.studentAnswer)
        }
        const res = await assignmentApi.quickGrade(fd)
        setGradeResult(res.data)
      }
    } catch (err) {
      setError('批改失败: ' + (err.response?.data?.msg || err.message || '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  const tabs = [
    { key: 'file', label: '上传文件', icon: Upload },
    { key: 'text', label: '文字内容', icon: FileText },
    { key: 'code', label: '代码作业', icon: FileCode },
  ]

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">AI 智能批改</h2>
        <p className="text-gray-500 mt-1">上传Word/PDF文件或直接输入内容，AI自动批注并生成批改文档</p>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 items-start">
        {/* ── Left: Input ── */}
        <div className="card space-y-4">
          {/* Mode tabs */}
          <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg">
            {tabs.map(({ key, label, icon: Icon }) => (
              <button key={key} onClick={() => { setInputMode(key); setGradeResult(null); setError('') }}
                className={`flex-1 flex items-center justify-center px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                  inputMode === key ? 'bg-white text-primary-700 shadow-sm' : 'text-gray-500 hover:text-gray-700'
                }`}>
                <Icon className="w-3.5 h-3.5 mr-1.5" />{label}
              </button>
            ))}
          </div>

          {/* Common fields */}
          <div>
            <label className="label">作业标题</label>
            <input className="input" value={form.assignmentTitle}
              onChange={e => setF('assignmentTitle', e.target.value)}
              placeholder="例：Java面向对象编程" />
          </div>
          <div>
            <label className="label">作业要求</label>
            <textarea className="input" rows={2} value={form.assignmentRequirement}
              onChange={e => setF('assignmentRequirement', e.target.value)}
              placeholder="描述作业的具体要求（可留空）" />
          </div>

          {/* Mode-specific input */}
          {inputMode === 'file' && (
            <>
              <div>
                <label className="label">学生作业文件 * <span className="text-gray-400 font-normal">(.docx .pdf .txt)</span></label>
                <div
                  onDrop={handleDrop}
                  onDragOver={e => e.preventDefault()}
                  onClick={() => fileRef.current?.click()}
                  className={`border-2 border-dashed rounded-lg p-6 text-center cursor-pointer transition-colors ${
                    selectedFile ? 'border-primary-400 bg-primary-50' : 'border-gray-300 hover:border-primary-400 hover:bg-gray-50'
                  }`}>
                  <input ref={fileRef} type="file"
                    accept=".docx,.doc,.pdf,.txt"
                    onChange={handleFileSelect} className="hidden" />
                  {selectedFile ? (
                    <div className="flex items-center justify-center space-x-2">
                      <Paperclip className="w-5 h-5 text-primary-600" />
                      <span className="text-sm font-medium text-primary-700">{selectedFile.name}</span>
                      <button onClick={e => { e.stopPropagation(); setSelectedFile(null) }}
                        className="text-gray-400 hover:text-red-500">
                        <X className="w-4 h-4" />
                      </button>
                    </div>
                  ) : (
                    <>
                      <Upload className="w-8 h-8 text-gray-300 mx-auto mb-2" />
                      <p className="text-sm text-gray-500">点击选择或拖拽文件到此处</p>
                      <p className="text-xs text-gray-400 mt-1">支持 Word (.docx)、PDF、文本文件</p>
                    </>
                  )}
                </div>
              </div>
              <div>
                <label className="label">参考答案（可选）</label>
                <textarea className="input" rows={2} value={form.referenceAnswer}
                  onChange={e => setF('referenceAnswer', e.target.value)}
                  placeholder="提供参考答案可提升批改准确度" />
              </div>
            </>
          )}

          {inputMode === 'text' && (
            <>
              <div>
                <label className="label">学生提交内容 *</label>
                <textarea className="input" rows={7} value={form.studentAnswer}
                  onChange={e => setF('studentAnswer', e.target.value)}
                  placeholder="粘贴学生的作业内容" />
              </div>
              <div>
                <label className="label">参考答案（可选）</label>
                <textarea className="input" rows={2} value={form.referenceAnswer}
                  onChange={e => setF('referenceAnswer', e.target.value)}
                  placeholder="提供参考答案可提升批改准确度" />
              </div>
            </>
          )}

          {inputMode === 'code' && (
            <>
              <div>
                <label className="label">学生代码 *</label>
                <textarea className="input font-mono text-sm" rows={8} value={form.studentCode}
                  onChange={e => setF('studentCode', e.target.value)}
                  placeholder="粘贴学生提交的代码" />
              </div>
              <div>
                <label className="label">测试用例（可选）</label>
                <textarea className="input font-mono text-sm" rows={2} value={form.testCases}
                  onChange={e => setF('testCases', e.target.value)}
                  placeholder="输入 -> 预期输出" />
              </div>
            </>
          )}

          {error && <p className="text-red-500 text-sm">{error}</p>}

          <button onClick={handleGrade} disabled={loading}
            className="btn-primary w-full flex items-center justify-center py-2.5">
            {loading
              ? <><Loader2 className="w-4 h-4 mr-2 animate-spin" />AI 批改中（30-60秒）...</>
              : <><Brain className="w-4 h-4 mr-2" />开始 AI 批改</>}
          </button>
        </div>

        {/* ── Right: Result ── */}
        <div className="card space-y-4">
          <h3 className="font-semibold text-gray-900">批改结果</h3>

          {!gradeResult && !loading && (
            <div className="text-center py-16 text-gray-400">
              <Brain className="w-12 h-12 mx-auto mb-3 opacity-20" />
              <p>提交作业后将在此显示批改结果</p>
              <p className="text-xs mt-1">包含得分、逐项批注、知识点分析、改进建议</p>
              {inputMode === 'file' && (
                <p className="text-xs mt-1 text-primary-500">上传文件后还会生成可下载的带批注Word文档</p>
              )}
            </div>
          )}

          {loading && (
            <div className="text-center py-16 text-gray-400">
              <Loader2 className="w-10 h-10 mx-auto mb-3 animate-spin text-primary-400" />
              <p className="text-sm">AI 正在批改中，请稍候...</p>
            </div>
          )}

          {gradeResult && !loading && (
            <div className="space-y-4">
              {/* Score + comment header */}
              {gradeResult.score != null && (
                <div className="flex items-center justify-between p-4 bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl border border-purple-100">
                  <div className="flex items-center space-x-3">
                    <div className="w-10 h-10 rounded-full bg-white shadow flex items-center justify-center">
                      <Star className="w-5 h-5 text-yellow-400" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">AI 评分</p>
                      <p className={`text-2xl font-bold ${
                        gradeResult.score >= 90 ? 'text-green-600'
                          : gradeResult.score >= 60 ? 'text-yellow-600' : 'text-red-600'
                      }`}>{gradeResult.score} 分</p>
                    </div>
                  </div>
                  {gradeResult.overallComment && (
                    <p className="text-sm text-gray-600 flex-1 ml-4 italic line-clamp-2">
                      {gradeResult.overallComment}
                    </p>
                  )}
                </div>
              )}

              {/* Download annotated file */}
              {gradeResult.annotatedFileUrl && (
                <a href={assignmentApi.fileUrl(gradeResult.annotatedFileUrl)}
                  target="_blank" rel="noreferrer"
                  className="flex items-center justify-center px-4 py-3 rounded-xl bg-purple-600 text-white font-medium hover:bg-purple-700 transition-colors shadow-sm">
                  <Download className="w-4 h-4 mr-2" />
                  下载AI批改文档（含批注）
                </a>
              )}

              {/* Structured result panel */}
              <GradingResultPanel result={gradeResult.gradingJson} />
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
