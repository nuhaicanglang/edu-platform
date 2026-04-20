import { useState, useEffect } from 'react'
import { BarChart3, User, Users, GitBranch, AlertTriangle, Loader2, Database, Activity } from 'lucide-react'
import ReactMarkdown from 'react-markdown'
import { agentApi, analyticsApi, learningApi, courseApi, classApi } from '../api'
import useAuthStore from '../store/useAuthStore'

const teacherTabs = [
  { key: 'student', icon: User, label: '学生报告' },
  { key: 'class', icon: Users, label: '班级概览' },
  { key: 'knowledge', icon: GitBranch, label: '知识图谱' },
  { key: 'risk', icon: AlertTriangle, label: '学习预警' },
  { key: 'track', icon: Activity, label: '学习轨迹' },
]

const studentTabs = [
  { key: 'student', icon: User, label: '我的学情报告' },
  { key: 'knowledge', icon: GitBranch, label: '知识掌握' },
  { key: 'track', icon: Activity, label: '学习轨迹' },
]

const actionTypeMap = {
  view: { label: '浏览课程', color: 'bg-blue-100 text-blue-700' },
  submit: { label: '提交作业', color: 'bg-green-100 text-green-700' },
  qa: { label: 'AI问答', color: 'bg-purple-100 text-purple-700' },
  practice: { label: '练习生成', color: 'bg-orange-100 text-orange-700' },
}

export default function Analytics() {
  const user = useAuthStore(s => s.user)
  const isTeacher = user?.role === 'teacher'
  const tabs = isTeacher ? teacherTabs : studentTabs
  const [activeTab, setActiveTab] = useState('student')
  const [loading, setLoading] = useState(false)
  const [loadingMsg, setLoadingMsg] = useState('')
  const [result, setResult] = useState('')
  const [realData, setRealData] = useState(null) // fetched DB data preview

  const [studentForm, setStudentForm] = useState({ studentName: user?.realName || '', courseName: '' })
  const [classForm, setClassForm] = useState({ className: '', courseName: '' })
  const [kgForm, setKgForm] = useState({ courseName: '', knowledgePoints: '' })
  const [riskForm, setRiskForm] = useState({ studentName: '', courseName: '' })

  // Courses & classes for dropdowns
  const [courses, setCourses] = useState([])
  const [classes, setClasses] = useState([])

  useEffect(() => {
    const loadOptions = async () => {
      try {
        const cRes = isTeacher ? await courseApi.list() : await courseApi.myList()
        const list = cRes.data || []
        setCourses(list)
        if (list.length > 0) {
          const first = list[0].courseName
          setStudentForm(p => ({ ...p, courseName: p.courseName || first }))
          setClassForm(p => ({ ...p, courseName: p.courseName || first }))
          setKgForm(p => ({ ...p, courseName: p.courseName || first }))
          setRiskForm(p => ({ ...p, courseName: p.courseName || first }))
        }
      } catch { /* ignore */ }
      if (isTeacher) {
        try {
          const clRes = await classApi.page({ pageNum: 1, pageSize: 100 })
          setClasses(clRes.data?.records || [])
          if ((clRes.data?.records || []).length > 0) {
            setClassForm(p => ({ ...p, className: p.className || clRes.data.records[0].className }))
          }
        } catch { /* ignore */ }
      }
    }
    loadOptions()
  }, [])

  // Learning track state
  const [trackRecords, setTrackRecords] = useState([])
  const [trackTotal, setTrackTotal] = useState(0)
  const [trackPage, setTrackPage] = useState(1)
  const [trackLoading, setTrackLoading] = useState(false)

  useEffect(() => {
    if (activeTab === 'track') loadTrack()
  }, [activeTab, trackPage])

  const loadTrack = async () => {
    setTrackLoading(true)
    try {
      const fn = isTeacher ? learningApi.allRecords : learningApi.myRecords
      const res = await fn({ pageNum: trackPage, pageSize: 15 })
      setTrackRecords(res.data.records || [])
      setTrackTotal(res.data.total || 0)
    } catch { /* ignore */ }
    finally { setTrackLoading(false) }
  }

  const handleSubmit = async () => {
    setLoading(true)
    setResult('')
    setRealData(null)
    try {
      let res

      switch (activeTab) {
        case 'student': {
          // 1. 从数据库获取真实学习数据
          setLoadingMsg('正在读取数据库中的真实作业数据...')
          const dbRes = await analyticsApi.studentData(studentForm.studentName, studentForm.courseName)
          const db = dbRes.data
          setRealData(db)
          // 2. 将真实数据传给 AI 分析
          setLoadingMsg('AI 正在分析真实学习数据...')
          res = await agentApi.studentReport({
            studentName: db.studentName,
            courseName: db.courseName,
            learningData: {
              '作业总数': String(db.totalAssignments),
              '已提交数': String(db.submittedCount),
              '已批改数': String(db.gradedCount),
              '提交完成率': db.completionRate,
              '作业平均分': db.averageScore,
              '最高分': String(db.maxScore),
              '最低分': String(db.minScore),
              '各次得分': db.scores.join(', ') || '暂无',
              '成绩趋势': db.trendDescription,
              '近期作业详情': db.scoreTrend,
            }
          })
          break
        }
        case 'class': {
          setLoadingMsg('正在读取数据库中的班级数据...')
          const dbRes = await analyticsApi.classData(classForm.className, classForm.courseName)
          const db = dbRes.data
          setRealData(db)
          setLoadingMsg('AI 正在分析班级数据...')
          res = await agentApi.classOverview({
            className: db.className,
            courseName: db.courseName,
            classData: {
              '班级学生数': String(db.studentCount),
              '已批改提交数': String(db.gradedSubmissions),
              '平均分': db.averageScore,
              '最高分': String(db.maxScore),
              '最低分': String(db.minScore),
              '及格率': db.passRate,
              '分数段分布': JSON.stringify(db.scoreDistribution),
            }
          })
          break
        }
        case 'knowledge': {
          const kps = kgForm.knowledgePoints.split(/[,，、\n]/).filter(Boolean).map(s => s.trim())
          const scores = {}
          kps.forEach(kp => { scores[kp] = parseFloat((0.3 + Math.random() * 0.6).toFixed(2)) })
          setLoadingMsg('AI 正在生成知识图谱分析...')
          res = await agentApi.knowledgeGraph({ courseName: kgForm.courseName, knowledgePoints: kps, masteryScores: scores })
          break
        }
        case 'risk': {
          // 获取真实数据用于预警
          setLoadingMsg('正在读取数据库中的真实作业数据...')
          const dbRes = await analyticsApi.studentData(riskForm.studentName, riskForm.courseName)
          const db = dbRes.data
          setRealData(db)
          setLoadingMsg('AI 正在进行风险预警分析...')
          res = await agentApi.riskAnalysis({
            studentName: db.studentName,
            riskData: {
              '课程': db.courseName,
              '近期作业完成率': db.completionRate,
              '作业平均分': db.averageScore,
              '成绩趋势': db.trendDescription,
              '各次得分明细': db.scoreTrend || '暂无',
              '已提交/总作业数': `${db.submittedCount}/${db.totalAssignments}`,
              '最低分': String(db.minScore),
              '最高分': String(db.maxScore),
            }
          })
          break
        }
      }
      setResult(res.data)
    } catch (err) {
      setResult('分析失败: ' + (err.response?.data?.msg || err.message || '未知错误'))
    } finally {
      setLoading(false)
      setLoadingMsg('')
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">{isTeacher ? '学情分析' : '我的学情'}</h2>
        <p className="text-gray-500 mt-1">{isTeacher ? '多维度学习数据分析与学习画像构建' : '查看个人学习报告与知识掌握度'}</p>
      </div>

      {/* Tabs */}
      <div className="flex space-x-2">
        {tabs.map(t => (
          <button key={t.key} onClick={() => { setActiveTab(t.key); setResult('') }}
            className={`flex items-center px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              activeTab === t.key ? 'bg-primary-600 text-white' : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
            }`}>
            <t.icon className="w-4 h-4 mr-2" /> {t.label}
          </button>
        ))}
      </div>

      {activeTab === 'track' ? (
        /* Learning Track Tab */
        <div className="card p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-semibold text-gray-900">
              {isTeacher ? '全部学生学习轨迹' : '我的学习轨迹'}
            </h3>
            <div className="flex items-center space-x-3">
              <span className="text-sm text-gray-500">共 {trackTotal} 条记录</span>
              <button onClick={loadTrack} className="text-sm text-primary-600 hover:underline">刷新</button>
            </div>
          </div>

          {trackLoading ? (
            <div className="flex justify-center py-12"><Loader2 className="w-6 h-6 animate-spin text-gray-400" /></div>
          ) : trackRecords.length === 0 ? (
            <div className="text-center py-12 text-gray-400">
              <Activity className="w-12 h-12 mx-auto mb-3 opacity-30" />
              <p>暂无学习记录</p>
              <p className="text-xs mt-1">浏览课程、提交作业、AI问答等操作会自动记录</p>
            </div>
          ) : (
            <div className="relative">
              <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-gray-200" />
              <div className="space-y-4">
                {trackRecords.map((r, i) => {
                  const at = actionTypeMap[r.action_type] || { label: r.action_type, color: 'bg-gray-100 text-gray-700' }
                  return (
                    <div key={r.id || i} className="relative flex items-start pl-10">
                      <div className="absolute left-2.5 w-3 h-3 rounded-full bg-white border-2 border-primary-400 mt-1.5" />
                      <div className="flex-1 bg-gray-50 rounded-lg p-3 border border-gray-100">
                        <div className="flex items-center justify-between mb-1">
                          <div className="flex items-center space-x-2">
                            <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${at.color}`}>{at.label}</span>
                            {r.course_name && <span className="text-xs text-gray-500">{r.course_name}</span>}
                            {isTeacher && r.student_name && <span className="text-xs bg-gray-200 px-1.5 py-0.5 rounded">{r.student_name}</span>}
                          </div>
                          <span className="text-xs text-gray-400">{r.create_time?.replace('T', ' ')?.slice(0, 19)}</span>
                        </div>
                        <p className="text-sm text-gray-700">{r.action_detail}</p>
                        {r.score != null && <span className="text-xs text-green-600 mt-1 inline-block">得分: {r.score}</span>}
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>
          )}

          {Math.ceil(trackTotal / 15) > 1 && (
            <div className="flex items-center justify-center space-x-2 mt-4 pt-3 border-t">
              <button disabled={trackPage <= 1}
                onClick={() => setTrackPage(p => p - 1)}
                className="px-3 py-1 text-sm rounded border disabled:opacity-40 hover:bg-gray-50">上一页</button>
              <span className="text-sm text-gray-500">{trackPage} / {Math.ceil(trackTotal / 15)}</span>
              <button disabled={trackPage >= Math.ceil(trackTotal / 15)}
                onClick={() => setTrackPage(p => p + 1)}
                className="px-3 py-1 text-sm rounded border disabled:opacity-40 hover:bg-gray-50">下一页</button>
            </div>
          )}
        </div>
      ) : (
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Input */}
        <div className="card space-y-4">
          {activeTab === 'student' && (
            <>
              <h3 className="font-semibold">学生学情报告</h3>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">学生姓名</label>
                  <input className="input" value={studentForm.studentName}
                    onChange={e => setStudentForm(p => ({ ...p, studentName: e.target.value }))} placeholder="张三" />
                </div>
                <div>
                  <label className="label">课程名称</label>
                  <select className="input" value={studentForm.courseName}
                    onChange={e => setStudentForm(p => ({ ...p, courseName: e.target.value }))}>
                    {courses.length === 0 && <option value="">暂无课程</option>}
                    {courses.map(c => <option key={c.id} value={c.courseName}>{c.courseName}</option>)}
                  </select>
                </div>
              </div>
              <p className="text-xs text-gray-400">基于真实作业数据进行AI分析</p>
            </>
          )}
          {activeTab === 'class' && (
            <>
              <h3 className="font-semibold">班级学情概览</h3>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="label">班级名称</label>
                  <select className="input" value={classForm.className}
                    onChange={e => setClassForm(p => ({ ...p, className: e.target.value }))}>
                    {classes.length === 0 && <option value="">暂无班级</option>}
                    {classes.map(c => <option key={c.id} value={c.className}>{c.className}</option>)}
                  </select>
                </div>
                <div>
                  <label className="label">课程名称</label>
                  <select className="input" value={classForm.courseName}
                    onChange={e => setClassForm(p => ({ ...p, courseName: e.target.value }))}>
                    {courses.length === 0 && <option value="">暂无课程</option>}
                    {courses.map(c => <option key={c.id} value={c.courseName}>{c.courseName}</option>)}
                  </select>
                </div>
              </div>
            </>
          )}
          {activeTab === 'knowledge' && (
            <>
              <h3 className="font-semibold">知识图谱分析</h3>
              <div>
                <label className="label">课程名称</label>
                <select className="input" value={kgForm.courseName}
                  onChange={e => setKgForm(p => ({ ...p, courseName: e.target.value }))}>
                  {courses.length === 0 && <option value="">暂无课程</option>}
                  {courses.map(c => <option key={c.id} value={c.courseName}>{c.courseName}</option>)}
                </select>
              </div>
              <div>
                <label className="label">知识点列表（逗号分隔）</label>
                <textarea className="input" rows={3} value={kgForm.knowledgePoints}
                  onChange={e => setKgForm(p => ({ ...p, knowledgePoints: e.target.value }))}
                  placeholder="变量与数据类型, 控制流程, 面向对象, 异常处理, 集合框架, 多线程" />
              </div>
            </>
          )}
          {activeTab === 'risk' && (
            <>
              <h3 className="font-semibold">学习预警分析</h3>
              <div>
                <label className="label">学生姓名</label>
                <input className="input" value={riskForm.studentName}
                  onChange={e => setRiskForm(p => ({ ...p, studentName: e.target.value }))} placeholder="李四" />
              </div>
              <p className="text-xs text-gray-400">将使用模拟风险指标数据进行预警分析</p>
            </>
          )}

          <button onClick={handleSubmit} disabled={loading} className="btn-primary w-full flex items-center justify-center">
            {loading ? <><Loader2 className="w-4 h-4 mr-2 animate-spin" /> 分析中...</>
              : <><BarChart3 className="w-4 h-4 mr-2" /> 开始分析</>}
          </button>
        </div>

        {/* Result */}
        <div className="card">
          <h3 className="font-semibold text-gray-900 mb-4">分析结果</h3>
          {result ? (
            <div className="markdown-body text-sm overflow-auto max-h-[calc(100vh-16rem)]">
              <ReactMarkdown>{result}</ReactMarkdown>
            </div>
          ) : (
            <div className="text-center py-16 text-gray-400">
              <BarChart3 className="w-12 h-12 mx-auto mb-3 opacity-30" />
              <p>填写参数并点击分析后将在此显示结果</p>
            </div>
          )}
        </div>
      </div>
      )}
    </div>
  )
}
