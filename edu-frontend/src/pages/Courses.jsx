import { useState, useEffect } from 'react'
import { Plus, Search, BookOpen, X, Trash2 } from 'lucide-react'
import { courseApi } from '../api'
import useAuthStore from '../store/useAuthStore'

export default function Courses() {
  const user = useAuthStore(s => s.user)
  const isTeacher = user?.role === 'teacher'
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [search, setSearch] = useState('')
  const [form, setForm] = useState({
    courseName: '', courseCode: '', description: '', category: 'theory',
    credit: 3, classHours: 48, status: 'active'
  })

  const load = () => {
    setLoading(true)
    const req = isTeacher ? courseApi.list() : courseApi.list()
    req.then(res => setCourses(res.data || []))
      .catch(() => {}).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    try {
      await courseApi.create({ ...form, status: 'active' })
      setShowModal(false)
      setForm({ courseName: '', courseCode: '', description: '', category: 'theory', credit: 3, classHours: 48, status: 'active' })
      load()
    } catch (err) {
      alert(err.message)
    }
  }

  const handleDelete = async (id) => {
    if (!confirm('确定删除该课程?')) return
    try { await courseApi.delete(id); load() } catch (err) { alert(err.message) }
  }

  const filtered = courses.filter(c =>
    c.courseName?.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">{isTeacher ? '课程管理' : '我的课程'}</h2>
          <p className="text-gray-500 mt-1">{isTeacher ? '创建和管理所有课程' : '浏览可用课程'}</p>
        </div>
        {isTeacher && (
          <button onClick={() => setShowModal(true)} className="btn-primary flex items-center">
            <Plus className="w-4 h-4 mr-2" /> 新建课程
          </button>
        )}
      </div>

      <div className="relative max-w-md">
        <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
        <input className="input pl-10" placeholder="搜索课程..." value={search}
          onChange={e => setSearch(e.target.value)} />
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">加载中...</div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-12">
          <BookOpen className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-500">暂无课程</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filtered.map(c => (
            <div key={c.id} className="card hover:shadow-md transition-shadow">
              <div className="flex items-start justify-between mb-3">
                <div className="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                  <BookOpen className="w-5 h-5 text-primary-600" />
                </div>
                <span className={`px-2 py-1 text-xs rounded-full ${c.status === 'active' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'}`}>
                  {c.status === 'active' ? '进行中' : '已归档'}
                </span>
              </div>
              <h3 className="font-semibold text-gray-900 text-lg">{c.courseName}</h3>
              {c.courseCode && <p className="text-sm text-gray-400 mt-0.5">{c.courseCode}</p>}
              <p className="text-sm text-gray-500 mt-2 line-clamp-2">{c.description || '暂无描述'}</p>
              <div className="mt-4 pt-3 border-t border-gray-100 flex items-center justify-between text-xs text-gray-400">
                <div className="flex items-center space-x-4">
                  {c.category && <span className="bg-gray-100 px-2 py-0.5 rounded">{c.category}</span>}
                  {c.credit && <span>{c.credit} 学分</span>}
                  {c.classHours && <span>{c.classHours} 学时</span>}
                </div>
                {isTeacher && (
                  <button onClick={() => handleDelete(c.id)} className="text-gray-400 hover:text-red-500" title="删除">
                    <Trash2 className="w-4 h-4" />
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-lg p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-bold">新建课程</h3>
              <button onClick={() => setShowModal(false)}><X className="w-5 h-5 text-gray-400" /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="label">课程名称 *</label>
                <input className="input" required value={form.courseName}
                  onChange={e => setForm(p => ({ ...p, courseName: e.target.value }))} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">课程代码</label>
                  <input className="input" value={form.courseCode}
                    onChange={e => setForm(p => ({ ...p, courseCode: e.target.value }))} />
                </div>
                <div>
                  <label className="label">分类</label>
                  <select className="input" value={form.category}
                    onChange={e => setForm(p => ({ ...p, category: e.target.value }))}>
                    <option value="theory">理论课</option>
                    <option value="practice">实践课</option>
                    <option value="seminar">研讨课</option>
                  </select>
                </div>
              </div>
              <div>
                <label className="label">课程描述</label>
                <textarea className="input" rows={3} value={form.description}
                  onChange={e => setForm(p => ({ ...p, description: e.target.value }))} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">学分</label>
                  <input className="input" type="number" min="0" step="0.5" value={form.credit}
                    onChange={e => setForm(p => ({ ...p, credit: parseFloat(e.target.value) }))} />
                </div>
                <div>
                  <label className="label">学时</label>
                  <input className="input" type="number" min="0" value={form.classHours}
                    onChange={e => setForm(p => ({ ...p, classHours: parseInt(e.target.value) }))} />
                </div>
              </div>
              <div className="flex justify-end space-x-3 pt-2">
                <button type="button" onClick={() => setShowModal(false)} className="btn-secondary">取消</button>
                <button type="submit" className="btn-primary">创建</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
