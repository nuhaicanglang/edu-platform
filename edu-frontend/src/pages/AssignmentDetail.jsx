import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  ArrowLeft, Paperclip, Download, Brain, CheckCircle,
  Clock, AlertCircle, User, FileText, Star, ChevronDown, ChevronUp
} from 'lucide-react'
import { assignmentApi } from '../api'
import useAuthStore from '../store/useAuthStore'

const statusBadge = {
  pending:   { label: '待批改', color: 'bg-gray-100 text-gray-600', icon: Clock },
  grading:   { label: '批改中', color: 'bg-blue-100 text-blue-600', icon: Brain },
  completed: { label: '已批改', color: 'bg-green-100 text-green-700', icon: CheckCircle },
  failed:    { label: '批改失败', color: 'bg-red-100 text-red-600', icon: AlertCircle },
  submitted: { label: '已提交', color: 'bg-yellow-100 text-yellow-700', icon: Clock },
}

function GradingResultPanel({ result }) {
  const [open, setOpen] = useState(false)
  if (!result) return null
  let parsed = null
  try {
    const clean = result.trim()
    const s = clean.indexOf('{'), e = clean.lastIndexOf('}')
    parsed = JSON.parse(s >= 0 ? clean.substring(s, e + 1) : clean)
  } catch (_) {}

  return (
    <div className="mt-3 border border-blue-100 rounded-lg overflow-hidden">
      <button
        onClick={() => setOpen(o => !o)}
        className="w-full flex items-center justify-between px-4 py-2 bg-blue-50 text-sm font-medium text-blue-700 hover:bg-blue-100">
        <span>查看批改详情</span>
        {open ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
      </button>
      {open && (
        <div className="p-4 text-sm space-y-3">
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
                          ? 'border-red-400 bg-red-50'
                          : 'border-green-400 bg-green-50'
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

function getBaseName(url) {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1] || ''
}

export default function AssignmentDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const user = useAuthStore(s => s.user)
  const isTeacher = user?.role === 'teacher'

  const [assignment, setAssignment] = useState(null)
  const [submissions, setSubmissions] = useState([])
  const [loading, setLoading] = useState(true)
  const [gradingId, setGradingId] = useState(null)  // 正在轮询的 submissionId
  const pollRef = useRef(null)  // 轮询定时器引用

  const load = async () => {
    setLoading(true)
    try {
      const [aRes, sRes] = await Promise.all([
        assignmentApi.getById(id),
        assignmentApi.listAllSubmissions(id),
      ])
      setAssignment(aRes.data)
      setSubmissions(sRes.data || [])
    } catch (e) {
      alert('加载失败: ' + e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    return () => { if (pollRef.current) clearInterval(pollRef.current) }
  }, [id])

  const stopPolling = () => {
    if (pollRef.current) { clearInterval(pollRef.current); pollRef.current = null }
  }

  const handleAiGrade = async (submissionId) => {
    if (gradingId) return
    if (!window.confirm('确定对该学生作业进行AI智能批改？后台自动完成，无需等待。')) return
    setGradingId(submissionId)
    try {
      // 触发异步批改，后端立即返回 status=grading
      const res = await assignmentApi.aiGrade(id, submissionId)
      setSubmissions(prev => prev.map(s => s.id === submissionId ? res.data : s))

      // 每 3 秒轮询一次批改状态，直到 completed 或 failed
      stopPolling()
      pollRef.current = setInterval(async () => {
        try {
          const statusRes = await assignmentApi.getGradingStatus(submissionId)
          const updated = statusRes.data
          setSubmissions(prev => prev.map(s => s.id === submissionId ? updated : s))
          if (updated.gradingStatus === 'completed' || updated.gradingStatus === 'failed') {
            stopPolling()
            setGradingId(null)
            if (updated.gradingStatus === 'failed') alert('AI批改失败，请重试')
          }
        } catch (_) { stopPolling(); setGradingId(null) }
      }, 3000)
    } catch (e) {
      alert('触发批改失败: ' + e.message)
      setGradingId(null)
    }
  }

  if (loading) return <div className="flex items-center justify-center h-64 text-gray-400">加载中...</div>
  if (!assignment) return <div className="text-center py-12 text-gray-400">作业不存在</div>

  const typeMap = { homework: '作业', experiment: '实验', quiz: '测验', project: '项目' }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-3">
        <button onClick={() => navigate('/assignments')}
          className="p-2 hover:bg-gray-100 rounded-lg transition-colors">
          <ArrowLeft className="w-5 h-5 text-gray-500" />
        </button>
        <div className="flex-1">
          <h2 className="text-2xl font-bold text-gray-900">{assignment.title}</h2>
          <p className="text-gray-500 text-sm mt-0.5">
            {typeMap[assignment.assignmentType] || assignment.assignmentType} ·
            总分 {assignment.totalScore} 分 ·
            {assignment.status === 'published' ? ' 已发布' : ' 草稿'}
          </p>
        </div>
      </div>

      {/* Assignment Info Card */}
      <div className="card p-5">
        <h3 className="font-semibold text-gray-800 mb-3">作业信息</h3>
        <div className="grid grid-cols-1 gap-4">
          {assignment.description && (
            <div className="p-3 bg-blue-50 rounded-lg">
              <p className="text-sm font-medium text-blue-800 mb-1">作业要求</p>
              <p className="text-sm text-blue-700 whitespace-pre-wrap">{assignment.description}</p>
            </div>
          )}
          {assignment.attachmentUrl && (
            <div className="flex items-center p-3 bg-gray-50 rounded-lg">
              <Paperclip className="w-4 h-4 text-gray-400 mr-2 flex-shrink-0" />
              <span className="text-sm text-gray-600 mr-3">题目附件：</span>
              <a
                href={assignmentApi.fileUrl(assignment.attachmentUrl)}
                target="_blank"
                rel="noreferrer"
                className="text-primary-600 hover:underline text-sm flex items-center">
                <Download className="w-3 h-3 mr-1" />
                {assignment.attachmentName || '下载题目文件'}
              </a>
            </div>
          )}
        </div>
      </div>

      {/* Submissions */}
      <div className="card p-0 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 className="font-semibold text-gray-800">
            学生提交 <span className="text-gray-400 font-normal ml-1">({submissions.length} 份)</span>
          </h3>
          {isTeacher && submissions.length > 0 && (
            <span className="text-xs text-gray-400">
              已批改 {submissions.filter(s => s.gradingStatus === 'completed').length} / {submissions.length}
            </span>
          )}
        </div>

        {submissions.length === 0 ? (
          <div className="py-16 text-center">
            <FileText className="w-10 h-10 text-gray-200 mx-auto mb-3" />
            <p className="text-gray-400">暂无学生提交</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {submissions.map(sub => {
              const badge = statusBadge[sub.gradingStatus] || statusBadge['submitted']
              const BadgeIcon = badge.icon
              const isGrading = gradingId === sub.id

              return (
                <div key={sub.id} className="px-6 py-4">
                  <div className="flex items-start justify-between">
                    {/* Left: student info + file */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center space-x-2 mb-1">
                        <div className="w-7 h-7 rounded-full bg-primary-100 flex items-center justify-center flex-shrink-0">
                          <User className="w-4 h-4 text-primary-600" />
                        </div>
                        <span className="font-medium text-gray-800 text-sm">
                          {sub.studentName || '学生 ' + sub.studentId}
                        </span>
                        <span className="text-xs text-gray-400">{sub.submitTime?.slice(0, 16)}</span>
                        <span className={`flex items-center text-xs px-2 py-0.5 rounded-full ${badge.color}`}>
                          <BadgeIcon className="w-3 h-3 mr-1" />
                          {badge.label}
                        </span>
                      </div>

                      {/* Submitted file */}
                      {sub.fileUrl && (
                        <div className="flex items-center mt-2 ml-9 text-xs text-gray-500">
                          <Paperclip className="w-3 h-3 mr-1" />
                          <span className="mr-2">提交文件：</span>
                          <a href={assignmentApi.fileUrl(sub.fileUrl)}
                            target="_blank" rel="noreferrer"
                            className="text-primary-600 hover:underline">
                            {sub.fileName || getBaseName(sub.fileUrl) || '查看文件'}
                          </a>
                        </div>
                      )}

                      {/* Text content */}
                      {!sub.fileUrl && sub.content && (
                        <p className="ml-9 mt-1 text-xs text-gray-500 line-clamp-2">{sub.content}</p>
                      )}

                      {/* Score + AI comment */}
                      {sub.gradingStatus === 'completed' && (
                        <div className="ml-9 mt-2 flex flex-wrap items-center gap-3">
                          {sub.score !== null && sub.score !== undefined && (
                            <div className="flex items-center text-sm font-bold">
                              <Star className="w-4 h-4 text-yellow-400 mr-1" />
                              <span className={
                                sub.score >= (assignment.totalScore * 0.9) ? 'text-green-600'
                                  : sub.score >= (assignment.totalScore * 0.6) ? 'text-yellow-600'
                                  : 'text-red-600'
                              }>
                                {sub.score} / {assignment.totalScore}
                              </span>
                            </div>
                          )}
                          {sub.aiComment && (
                            <p className="text-xs text-gray-500 italic flex-1 line-clamp-1">{sub.aiComment}</p>
                          )}
                        </div>
                      )}

                      {/* Annotated file download */}
                      {sub.annotatedFileUrl && (
                        <div className="ml-9 mt-2">
                          <a href={assignmentApi.fileUrl(sub.annotatedFileUrl)}
                            target="_blank" rel="noreferrer"
                            className="inline-flex items-center px-3 py-1.5 rounded-lg bg-purple-600 text-white text-xs font-medium hover:bg-purple-700 transition-colors shadow-sm">
                            <Brain className="w-3 h-3 mr-1.5" />
                            下载AI批改文档（含批注）
                          </a>
                        </div>
                      )}

                      {/* Grading result detail */}
                      {sub.gradingResult && (
                        <div className="ml-9">
                          <GradingResultPanel result={sub.gradingResult} />
                        </div>
                      )}
                    </div>

                    {/* Right: AI grade button (teacher only) */}
                    {isTeacher && (
                      <div className="ml-4 flex-shrink-0">
                        {sub.gradingStatus === 'completed' ? (
                          <button
                            onClick={() => handleAiGrade(sub.id)}
                            disabled={!!gradingId}
                            className="text-xs px-3 py-1.5 rounded-lg border border-purple-200 text-purple-600 hover:bg-purple-50 disabled:opacity-40">
                            重新批改
                          </button>
                        ) : (
                          <button
                            onClick={() => handleAiGrade(sub.id)}
                            disabled={!!gradingId}
                            className="flex items-center text-xs px-3 py-1.5 rounded-lg bg-gradient-to-r from-purple-600 to-blue-600 text-white hover:opacity-90 disabled:opacity-50 transition-all shadow-sm">
                            {isGrading ? (
                              <>
                                <span className="w-3 h-3 border-2 border-white/40 border-t-white rounded-full animate-spin mr-1.5" />
                                批改中...
                              </>
                            ) : (
                              <>
                                <Brain className="w-3 h-3 mr-1.5" />
                                AI 智能批改
                              </>
                            )}
                          </button>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
