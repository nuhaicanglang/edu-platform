import { useState, useEffect, useRef } from 'react'
import { Plus, FileText, X, Send, ChevronRight, Paperclip, Upload } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { assignmentApi, courseApi } from '../api'
import useAuthStore from '../store/useAuthStore'

export default function Assignments() {
  const user = useAuthStore(s => s.user)
  const isTeacher = user?.role === 'teacher'
  const navigate = useNavigate()
  const attachRef = useRef(null)
  const submitFileRef = useRef(null)

  const [assignments, setAssignments] = useState([])
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [showSubmit, setShowSubmit] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [creating, setCreating] = useState(false)
  const [submitFile, setSubmitFile] = useState(null)
  const [submitContent, setSubmitContent] = useState('')
  const [attachFile, setAttachFile] = useState(null)
  const [form, setForm] = useState({
    title: '', courseId: '', assignmentType: 'homework', description: '',
    totalScore: 100, status: 'published'
  })

  const load = () => {
    setLoading(true)
    Promise.all([
      assignmentApi.page({ pageNum: 1, pageSize: 50 }),
      courseApi.list()
    ]).then(([aRes, cRes]) => {
      setAssignments(aRes.data?.records || aRes.data || [])
      setCourses(cRes.data || [])
    }).catch(() => {}).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    setCreating(true)
    try {
      const fd = new FormData()
      fd.append('title', form.title)
      fd.append('courseId', form.courseId)
      fd.append('assignmentType', form.assignmentType)
      if (form.description) fd.append('description', form.description)
      fd.append('totalScore', form.totalScore)
      fd.append('status', form.status)
      fd.append('aiGradingEnabled', 'true')
      if (attachFile) fd.append('file', attachFile)

      await assignmentApi.createWithFile(fd)
      setShowModal(false)
      setForm({ title: '', courseId: '', assignmentType: 'homework', description: '', totalScore: 100, status: 'published' })
      setAttachFile(null)
      load()
    } catch (err) { alert('创建失败: ' + err.message) }
    finally { setCreating(false) }
  }

  const handleSubmitAssignment = async () => {
    if (!submitFile && !submitContent.trim()) return alert('请上传作业文件或输入作业内容')
    setSubmitting(true)
    try {
      await assignmentApi.submit(showSubmit.id, submitContent || null, submitFile || null)
      alert('提交成功！老师将对您的作业进行AI批改')
      setShowSubmit(null)
      setSubmitFile(null)
      setSubmitContent('')
    } catch (err) { alert('提交失败: ' + err.message) }
    finally { setSubmitting(false) }
  }

  const statusMap = { draft: '草稿', published: '已发布', closed: '已关闭' }
  const statusColor = {
    draft: 'bg-yellow-100 text-yellow-700',
    published: 'bg-green-100 text-green-700',
    closed: 'bg-gray-100 text-gray-600'
  }
  const typeMap = { homework: '作业', experiment: '实验', quiz: '测验', project: '项目' }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">{isTeacher ? '作业管理' : '我的作业'}</h2>
          <p className="text-gray-500 mt-1">
            {isTeacher ? '创建作业并上传题目文件，支持AI智能批改' : '查看和提交课程作业'}
          </p>
        </div>
        {isTeacher && (
          <button onClick={() => setShowModal(true)} className="btn-primary flex items-center">
            <Plus className="w-4 h-4 mr-2" /> 新建作业
          </button>
        )}
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">加载中...</div>
      ) : assignments.length === 0 ? (
        <div className="text-center py-12">
          <FileText className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-500">暂无作业</p>
        </div>
      ) : (
        <div className="card p-0 overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr className="text-left text-sm text-gray-500">
                <th className="px-6 py-3 font-medium">标题</th>
                <th className="px-6 py-3 font-medium">类型</th>
                <th className="px-6 py-3 font-medium">附件</th>
                <th className="px-6 py-3 font-medium">总分</th>
                <th className="px-6 py-3 font-medium">状态</th>
                <th className="px-6 py-3 font-medium">创建时间</th>
                <th className="px-6 py-3 font-medium">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {assignments.map(a => (
                <tr key={a.id}
                  className="hover:bg-blue-50 cursor-pointer transition-colors"
                  onClick={() => navigate(`/assignments/${a.id}`)}>
                  <td className="px-6 py-4">
                    <div className="flex items-center">
                      <p className="font-medium text-gray-900 group-hover:text-primary-600">{a.title}</p>
                      <ChevronRight className="w-4 h-4 text-gray-300 ml-1" />
                    </div>
                    {a.description && <p className="text-xs text-gray-400 mt-1 line-clamp-1">{a.description}</p>}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">{typeMap[a.assignmentType] || a.assignmentType || '-'}</td>
                  <td className="px-6 py-4">
                    {a.attachmentName ? (
                      <span className="flex items-center text-xs text-blue-600">
                        <Paperclip className="w-3 h-3 mr-1" />
                        {a.attachmentName}
                      </span>
                    ) : <span className="text-xs text-gray-300">无</span>}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">{a.totalScore || '-'}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 text-xs rounded-full ${statusColor[a.status] || 'bg-gray-100 text-gray-600'}`}>
                      {statusMap[a.status] || a.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-400">{a.createTime?.slice(0, 16)}</td>
                  <td className="px-6 py-4" onClick={e => e.stopPropagation()}>
                    {!isTeacher && a.status === 'published' && (
                      <button onClick={() => setShowSubmit(a)}
                        className="text-green-600 hover:text-green-700 text-sm font-medium flex items-center">
                        <Send className="w-4 h-4 mr-1" /> 提交作业
                      </button>
                    )}
                    {isTeacher && (
                      <button onClick={() => navigate(`/assignments/${a.id}`)}
                        className="text-primary-600 hover:text-primary-700 text-sm font-medium flex items-center">
                        <ChevronRight className="w-4 h-4 mr-1" /> 查看详情
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Teacher: Create Modal */}
      {showModal && isTeacher && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-bold">新建作业</h3>
              <button onClick={() => setShowModal(false)}><X className="w-5 h-5 text-gray-400" /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="label">作业标题 *</label>
                <input className="input" required value={form.title}
                  onChange={e => setForm(p => ({ ...p, title: e.target.value }))} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">所属课程 *</label>
                  <select className="input" required value={form.courseId}
                    onChange={e => setForm(p => ({ ...p, courseId: e.target.value }))}>
                    <option value="">选择课程</option>
                    {courses.map(c => <option key={c.id} value={c.id}>{c.courseName}</option>)}
                  </select>
                </div>
                <div>
                  <label className="label">类型</label>
                  <select className="input" value={form.assignmentType}
                    onChange={e => setForm(p => ({ ...p, assignmentType: e.target.value }))}>
                    <option value="homework">作业</option>
                    <option value="experiment">实验</option>
                    <option value="quiz">测验</option>
                    <option value="project">项目</option>
                  </select>
                </div>
              </div>
              <div>
                <label className="label">作业描述/要求</label>
                <textarea className="input" rows={3} value={form.description}
                  onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
                  placeholder="描述作业要求，AI批改时将参考此内容..." />
              </div>
              <div>
                <label className="label">总分</label>
                <input className="input" type="number" value={form.totalScore}
                  onChange={e => setForm(p => ({ ...p, totalScore: e.target.value }))} />
              </div>
              <div>
                <label className="label">作业题目文件（可选）</label>
                <div
                  onClick={() => attachRef.current?.click()}
                  className="border-2 border-dashed border-gray-200 rounded-lg p-4 text-center cursor-pointer hover:border-primary-400 hover:bg-primary-50 transition-colors">
                  {attachFile ? (
                    <div className="flex items-center justify-center text-sm text-primary-600">
                      <Paperclip className="w-4 h-4 mr-2" />
                      {attachFile.name}
                      <button type="button" onClick={e => { e.stopPropagation(); setAttachFile(null) }}
                        className="ml-2 text-gray-400 hover:text-red-500">
                        <X className="w-3 h-3" />
                      </button>
                    </div>
                  ) : (
                    <div className="text-sm text-gray-400">
                      <Upload className="w-5 h-5 mx-auto mb-1" />
                      点击上传题目文件（Excel / PDF / Word）
                    </div>
                  )}
                  <input ref={attachRef} type="file" className="hidden"
                    accept=".xlsx,.xls,.pdf,.doc,.docx"
                    onChange={e => setAttachFile(e.target.files[0] || null)} />
                </div>
              </div>
              <div className="flex justify-end space-x-3 pt-2">
                <button type="button" onClick={() => setShowModal(false)} className="btn-secondary">取消</button>
                <button type="submit" disabled={creating} className="btn-primary">
                  {creating ? '创建中...' : '创建作业'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Student: Submit Modal */}
      {showSubmit && !isTeacher && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-lg p-6">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-lg font-bold">提交作业</h3>
                <p className="text-sm text-gray-500">{showSubmit.title}</p>
              </div>
              <button onClick={() => setShowSubmit(null)}><X className="w-5 h-5 text-gray-400" /></button>
            </div>
            {showSubmit.description && (
              <div className="mb-4 p-3 bg-blue-50 rounded-lg text-sm text-blue-800">
                <p className="font-medium mb-1">作业要求：</p>
                <p>{showSubmit.description}</p>
              </div>
            )}
            {showSubmit.attachmentUrl && (
              <div className="mb-4 p-3 bg-gray-50 rounded-lg flex items-center text-sm">
                <Paperclip className="w-4 h-4 text-gray-400 mr-2 flex-shrink-0" />
                <span className="text-gray-600 mr-2">题目文件：</span>
                <a href={assignmentApi.fileUrl(showSubmit.attachmentUrl)}
                  target="_blank" rel="noreferrer"
                  className="text-primary-600 hover:underline truncate"
                  onClick={e => e.stopPropagation()}>
                  {showSubmit.attachmentName || '下载题目'}
                </a>
              </div>
            )}
            <div className="space-y-4">
              <div>
                <label className="label">上传作业文件（Word .docx 推荐）</label>
                <div
                  onClick={() => submitFileRef.current?.click()}
                  className="border-2 border-dashed border-gray-200 rounded-lg p-4 text-center cursor-pointer hover:border-green-400 hover:bg-green-50 transition-colors">
                  {submitFile ? (
                    <div className="flex items-center justify-center text-sm text-green-600">
                      <Paperclip className="w-4 h-4 mr-2" />
                      {submitFile.name}
                      <button type="button" onClick={e => { e.stopPropagation(); setSubmitFile(null) }}
                        className="ml-2 text-gray-400 hover:text-red-500">
                        <X className="w-3 h-3" />
                      </button>
                    </div>
                  ) : (
                    <div className="text-sm text-gray-400">
                      <Upload className="w-5 h-5 mx-auto mb-1" />
                      点击上传作业文件（.docx / .doc / .pdf / .txt）
                    </div>
                  )}
                  <input ref={submitFileRef} type="file" className="hidden"
                    accept=".docx,.doc,.pdf,.txt"
                    onChange={e => setSubmitFile(e.target.files[0] || null)} />
                </div>
              </div>
              <div>
                <label className="label">或输入文本内容</label>
                <textarea className="input text-sm" rows={5} value={submitContent}
                  onChange={e => setSubmitContent(e.target.value)}
                  placeholder="也可以直接在此输入作业文本内容..." />
              </div>
              <div className="flex justify-end space-x-3">
                <button onClick={() => { setShowSubmit(null); setSubmitFile(null); setSubmitContent('') }}
                  className="btn-secondary">取消</button>
                <button onClick={handleSubmitAssignment} disabled={submitting}
                  className="btn-primary flex items-center">
                  <Send className="w-4 h-4 mr-2" />
                  {submitting ? '提交中...' : '提交作业'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
