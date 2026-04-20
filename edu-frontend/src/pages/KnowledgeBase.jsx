import { useState, useEffect, useRef } from 'react'
import { Database, Search, Upload, FileText, Trash2 } from 'lucide-react'
import { knowledgeApi, courseApi } from '../api'
import useAuthStore from '../store/useAuthStore'

export default function KnowledgeBase() {
  const user = useAuthStore(s => s.user)
  const isTeacher = user?.role === 'teacher'
  const [docs, setDocs] = useState([])
  const [courses, setCourses] = useState([])
  const [searchResults, setSearchResults] = useState([])
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(true)
  const [searching, setSearching] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [uploadCourseId, setUploadCourseId] = useState('')
  const fileRef = useRef(null)

  const load = () => {
    setLoading(true)
    Promise.all([
      knowledgeApi.documents({ pageNum: 1, pageSize: 20 }),
      courseApi.list()
    ]).then(([dRes, cRes]) => {
      setDocs(dRes.data?.records || dRes.data || [])
      setCourses(cRes.data || [])
    }).catch(() => {}).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleSearch = async () => {
    if (!keyword.trim()) return
    setSearching(true)
    try {
      const res = await knowledgeApi.search(keyword)
      setSearchResults(res.data || [])
    } catch { setSearchResults([]) }
    finally { setSearching(false) }
  }

  const handleUpload = async (e) => {
    const file = e.target.files?.[0]
    if (!file || !uploadCourseId) return alert('请先选择课程')
    setUploading(true)
    try {
      const formData = new FormData()
      formData.append('file', file)
      await knowledgeApi.upload(formData, uploadCourseId)
      load()
    } catch (err) { alert('上传失败: ' + err.message) }
    finally { setUploading(false); if (fileRef.current) fileRef.current.value = '' }
  }

  const handleDelete = async (id) => {
    if (!confirm('确定删除该文档?')) return
    try { await knowledgeApi.deleteDocument(id); load() } catch (err) { alert(err.message) }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">{isTeacher ? '知识库管理' : '知识检索'}</h2>
          <p className="text-gray-500 mt-1">{isTeacher ? '上传文档构建课程知识库' : '搜索课程知识内容'}</p>
        </div>
      </div>

      {/* Teacher: Upload */}
      {isTeacher && (
        <div className="card">
          <h3 className="font-semibold text-gray-900 mb-3">上传文档</h3>
          <div className="flex items-center space-x-3">
            <select className="input w-48" value={uploadCourseId} onChange={e => setUploadCourseId(e.target.value)}>
              <option value="">选择课程</option>
              {courses.map(c => <option key={c.id} value={c.id}>{c.courseName}</option>)}
            </select>
            <label className={`btn-primary flex items-center cursor-pointer ${uploading ? 'opacity-50' : ''}`}>
              <Upload className="w-4 h-4 mr-2" />
              {uploading ? '上传中...' : '选择文件'}
              <input ref={fileRef} type="file" className="hidden" onChange={handleUpload} disabled={uploading} />
            </label>
          </div>
          <p className="text-xs text-gray-400 mt-2">支持 PDF、Word、TXT 等文档格式</p>
        </div>
      )}

      {/* Search */}
      <div className="card">
        <h3 className="font-semibold text-gray-900 mb-3">知识检索</h3>
        <div className="flex space-x-3">
          <div className="flex-1 relative">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input className="input pl-10" placeholder="输入关键词搜索知识库..."
              value={keyword} onChange={e => setKeyword(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSearch()} />
          </div>
          <button onClick={handleSearch} disabled={searching} className="btn-primary">
            {searching ? '搜索中...' : '搜索'}
          </button>
        </div>

        {searchResults.length > 0 && (
          <div className="mt-4 space-y-3">
            <p className="text-sm text-gray-500">找到 {searchResults.length} 个相关片段</p>
            {searchResults.map((r, i) => (
              <div key={i} className="p-4 bg-gray-50 rounded-lg border-l-4 border-primary-400">
                <p className="text-sm text-gray-700 whitespace-pre-wrap">{r.content}</p>
                <div className="mt-2 flex items-center text-xs text-gray-400 space-x-3">
                  <span>文档ID: {r.documentId}</span>
                  <span>片段: #{r.chunkIndex}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Documents */}
      <div>
        <h3 className="font-semibold text-gray-900 mb-4">知识文档</h3>
        {loading ? (
          <div className="text-center py-12 text-gray-500">加载中...</div>
        ) : docs.length === 0 ? (
          <div className="text-center py-12 card">
            <Database className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p className="text-gray-500 mb-1">暂无知识文档</p>
            <p className="text-sm text-gray-400">上传课程相关文档以构建知识库</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {docs.map(d => (
              <div key={d.id} className="card hover:shadow-md transition-shadow">
                <div className="flex items-start space-x-3">
                  <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center flex-shrink-0">
                    <FileText className="w-5 h-5 text-purple-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h4 className="font-medium text-gray-900 truncate">{d.title || d.fileName}</h4>
                    <p className="text-xs text-gray-400 mt-1">
                      {d.fileType?.toUpperCase()} {d.fileSize ? `· ${(d.fileSize / 1024).toFixed(1)}KB` : ''}
                    </p>
                  </div>
                  <span className={`px-2 py-1 text-xs rounded-full flex-shrink-0 ${
                    d.status === 'completed' ? 'bg-green-100 text-green-700' :
                    d.status === 'parsing' ? 'bg-yellow-100 text-yellow-700' : 'bg-gray-100 text-gray-600'
                  }`}>
                    {d.status === 'completed' ? '已完成' : d.status === 'parsing' ? '解析中' : d.status}
                  </span>
                </div>
                <div className="mt-3 flex items-center justify-between">
                  {d.chunkCount && (
                    <p className="text-xs text-gray-400">{d.chunkCount} 个知识片段</p>
                  )}
                  {isTeacher && (
                    <button onClick={() => handleDelete(d.id)} className="text-xs text-red-400 hover:text-red-600">
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
