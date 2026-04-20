import { useState, useEffect, useRef } from 'react'
import { Plus, Users, X, UserPlus, Eye, Trash2, Search, UserMinus, Loader2 } from 'lucide-react'
import { classApi, courseApi } from '../api'
import useAuthStore from '../store/useAuthStore'

export default function Classes() {
  const user = useAuthStore(s => s.user)
  const isTeacher = user?.role === 'teacher'
  const [classes, setClasses] = useState([])
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [showStudents, setShowStudents] = useState(null)
  const [students, setStudents] = useState([])
  const [searchKeyword, setSearchKeyword] = useState('')
  const [searchResults, setSearchResults] = useState([])
  const [searching, setSearching] = useState(false)
  const [addingId, setAddingId] = useState(null)
  const searchTimer = useRef(null)
  const [form, setForm] = useState({ className: '', courseId: '', semester: '2025-2026-1', status: 0 })

  const load = () => {
    setLoading(true)
    Promise.all([
      classApi.page({ pageNum: 1, pageSize: 50 }),
      courseApi.list()
    ]).then(([clsRes, crsRes]) => {
      setClasses(clsRes.data?.records || clsRes.data || [])
      setCourses(crsRes.data || [])
    }).catch(() => {}).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    try {
      await classApi.create({ ...form, courseId: parseInt(form.courseId) })
      setShowModal(false)
      setForm({ className: '', courseId: '', semester: '2025-2026-1', status: 0 })
      load()
    } catch (err) { alert(err.message) }
  }

  const handleViewStudents = async (cls) => {
    setShowStudents(cls)
    setSearchKeyword('')
    setSearchResults([])
    try {
      const res = await classApi.getStudents(cls.id)
      setStudents(res.data || [])
    } catch { setStudents([]) }
  }

  const handleRemoveStudent = async (classId, studentId) => {
    if (!confirm('确定移除该学生?')) return
    try {
      await classApi.removeStudent(classId, studentId)
      handleViewStudents(showStudents)
    } catch (err) { alert(err.message) }
  }

  const handleSearchChange = (keyword) => {
    setSearchKeyword(keyword)
    clearTimeout(searchTimer.current)
    if (!keyword.trim()) { setSearchResults([]); return }
    searchTimer.current = setTimeout(async () => {
      setSearching(true)
      try {
        const res = await classApi.searchAvailableStudents(showStudents.id, keyword)
        setSearchResults(res.data || [])
      } catch { setSearchResults([]) }
      finally { setSearching(false) }
    }, 400)
  }

  const handleAddStudent = async (studentId) => {
    setAddingId(studentId)
    try {
      await classApi.addStudent(showStudents.id, studentId)
      setSearchResults(prev => prev.filter(s => s.id !== studentId))
      const res = await classApi.getStudents(showStudents.id)
      setStudents(res.data || [])
    } catch (err) { alert(err.message) }
    finally { setAddingId(null) }
  }

  const handleDelete = async (id) => {
    if (!confirm('确定删除该班级?')) return
    try { await classApi.delete(id); load() } catch (err) { alert(err.message) }
  }

  const getCourseName = (id) => courses.find(c => c.id === id)?.courseName || '-'

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">{isTeacher ? '班级管理' : '我的班级'}</h2>
          <p className="text-gray-500 mt-1">{isTeacher ? '创建和管理教学班级' : '查看已加入的班级'}</p>
        </div>
        {isTeacher && (
          <button onClick={() => setShowModal(true)} className="btn-primary flex items-center">
            <Plus className="w-4 h-4 mr-2" /> 新建班级
          </button>
        )}
      </div>

      {loading ? (
        <div className="text-center py-12 text-gray-500">加载中...</div>
      ) : classes.length === 0 ? (
        <div className="text-center py-12">
          <Users className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-500">暂无班级</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {classes.map(cls => (
            <div key={cls.id} className="card">
              <div className="flex items-center justify-between mb-3">
                <h3 className="font-semibold text-gray-900">{cls.className}</h3>
                <span className={`px-2 py-1 text-xs rounded-full ${cls.status === 0 ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'}`}>
                  {cls.status === 0 ? '进行中' : '已结束'}
                </span>
              </div>
              <div className="space-y-1 text-sm text-gray-500">
                <p>课程: {getCourseName(cls.courseId)}</p>
                <p>学期: {cls.semester || '-'}</p>
                <p>学生人数: {cls.studentCount || 0}</p>
              </div>
              <div className="mt-3 pt-3 border-t border-gray-100 flex space-x-2">
                <button onClick={() => handleViewStudents(cls)}
                  className="text-sm text-primary-600 hover:text-primary-700 flex items-center">
                  <Eye className="w-3.5 h-3.5 mr-1" /> 查看学生
                </button>
                {isTeacher && (
                  <button onClick={() => handleDelete(cls.id)}
                    className="text-sm text-red-500 hover:text-red-600 flex items-center ml-auto">
                    <Trash2 className="w-3.5 h-3.5 mr-1" /> 删除
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Modal */}
      {showModal && isTeacher && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-lg p-6">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-bold">新建班级</h3>
              <button onClick={() => setShowModal(false)}><X className="w-5 h-5 text-gray-400" /></button>
            </div>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="label">班级名称 *</label>
                <input className="input" required value={form.className}
                  onChange={e => setForm(p => ({ ...p, className: e.target.value }))} />
              </div>
              <div>
                <label className="label">所属课程 *</label>
                <select className="input" required value={form.courseId}
                  onChange={e => setForm(p => ({ ...p, courseId: e.target.value }))}>
                  <option value="">选择课程</option>
                  {courses.map(c => <option key={c.id} value={c.id}>{c.courseName}</option>)}
                </select>
              </div>
              <div>
                <label className="label">学期</label>
                <input className="input" value={form.semester} placeholder="如: 2025-2026-1"
                  onChange={e => setForm(p => ({ ...p, semester: e.target.value }))} />
              </div>
              <div className="flex justify-end space-x-3 pt-2">
                <button type="button" onClick={() => setShowModal(false)} className="btn-secondary">取消</button>
                <button type="submit" className="btn-primary">创建</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Student List Modal */}
      {showStudents && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl w-full max-w-lg p-6 flex flex-col max-h-[90vh]">
            {/* Header */}
            <div className="flex items-center justify-between mb-4 flex-shrink-0">
              <div>
                <h3 className="text-lg font-bold">班级学生</h3>
                <p className="text-sm text-gray-500">{showStudents.className} · 共 {students.length} 名学生</p>
              </div>
              <button onClick={() => setShowStudents(null)}><X className="w-5 h-5 text-gray-400" /></button>
            </div>

            {/* Search bar (teacher only) */}
            {isTeacher && (
              <div className="mb-3 flex-shrink-0">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                  <input
                    className="input pl-9 text-sm"
                    placeholder="输入学号或姓名搜索并添加学生..."
                    value={searchKeyword}
                    onChange={e => handleSearchChange(e.target.value)}
                  />
                  {searching && <Loader2 className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 animate-spin" />}
                </div>
                {/* Search results */}
                {searchKeyword.trim() && !searching && (() => {
                  const kw = searchKeyword.trim().toLowerCase()
                  const alreadyIn = students.filter(s =>
                    (s.username || '').toLowerCase().includes(kw) ||
                    (s.realName || '').toLowerCase().includes(kw)
                  )
                  const hasAny = searchResults.length > 0 || alreadyIn.length > 0
                  return hasAny ? (
                    <div className="mt-1 border border-gray-200 rounded-lg divide-y divide-gray-100 bg-white shadow-sm max-h-48 overflow-y-auto">
                      {searchResults.map(s => (
                        <div key={s.id} className="flex items-center justify-between px-3 py-2">
                          <div>
                            <span className="text-sm font-medium text-gray-800">{s.real_name || '未知'}</span>
                            <span className="text-xs text-gray-400 ml-2">（{s.username}）</span>
                          </div>
                          <button
                            onClick={() => handleAddStudent(s.id)}
                            disabled={addingId === s.id}
                            className="text-xs px-2 py-1 bg-primary-600 text-white rounded hover:bg-primary-700 disabled:opacity-50 flex items-center">
                            {addingId === s.id
                              ? <Loader2 className="w-3 h-3 animate-spin" />
                              : <><UserPlus className="w-3 h-3 mr-1" />添加</>}
                          </button>
                        </div>
                      ))}
                      {alreadyIn.map(s => (
                        <div key={'in-' + s.studentId} className="flex items-center justify-between px-3 py-2 bg-gray-50">
                          <div>
                            <span className="text-sm font-medium text-gray-600">{s.realName || '未知'}</span>
                            <span className="text-xs text-gray-400 ml-2">（{s.username || s.studentId}）</span>
                          </div>
                          <span className="text-xs px-2 py-1 bg-green-100 text-green-600 rounded">已在班级</span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-xs text-gray-400 mt-1 px-1">未找到匹配的学生</p>
                  )
                })()}
              </div>
            )}

            {/* Student list */}
            <div className="overflow-y-auto flex-1 divide-y divide-gray-100">
              {students.length === 0 ? (
                <div className="text-center py-8 text-gray-400">
                  <Users className="w-10 h-10 mx-auto mb-2 opacity-50" />
                  <p>暂无学生</p>
                </div>
              ) : (
                students.map((s, i) => (
                  <div key={s.id || i} className="py-2.5 flex items-center justify-between">
                    <div className="flex items-center">
                      <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center text-primary-600 text-xs font-semibold mr-3 flex-shrink-0">
                        {String(i + 1).padStart(2, '0')}
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-900">
                          {s.realName || '未知'}
                          <span className="text-xs font-normal text-gray-400 ml-1.5">({s.username || s.studentId})</span>
                        </p>
                        <p className="text-xs text-gray-400">加入: {s.joinTime?.slice(0, 10) || s.createTime?.slice(0, 10) || '-'}</p>
                      </div>
                    </div>
                    {isTeacher && (
                      <button onClick={() => handleRemoveStudent(showStudents.id, s.studentId)}
                        className="text-xs text-red-400 hover:text-red-600 flex items-center ml-2 flex-shrink-0">
                        <UserMinus className="w-3.5 h-3.5 mr-0.5" />移除
                      </button>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
