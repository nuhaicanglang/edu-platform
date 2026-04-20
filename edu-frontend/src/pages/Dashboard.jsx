import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { BookOpen, Users, FileText, MessageSquare, CheckSquare, BarChart3, Dumbbell, Database, ArrowRight } from 'lucide-react'
import { courseApi, classApi, assignmentApi, knowledgeApi } from '../api'
import useAuthStore from '../store/useAuthStore'

const teacherActions = [
  { icon: MessageSquare, label: '智能问答', desc: '基于课程知识的AI答疑', path: '/chat', color: 'bg-blue-500' },
  { icon: CheckSquare, label: '作业批改', desc: '精细化AI智能批改', path: '/grading', color: 'bg-green-500' },
  { icon: BarChart3, label: '学情分析', desc: '多维度学习数据分析', path: '/analytics', color: 'bg-purple-500' },
  { icon: Dumbbell, label: '练习生成', desc: '个性化增量练习', path: '/practice', color: 'bg-orange-500' },
]

const studentActions = [
  { icon: MessageSquare, label: '智能问答', desc: '有问题随时问AI助手', path: '/chat', color: 'bg-blue-500' },
  { icon: FileText, label: '我的作业', desc: '查看和提交课程作业', path: '/assignments', color: 'bg-green-500' },
  { icon: BarChart3, label: '我的学情', desc: '查看个人学习报告', path: '/analytics', color: 'bg-purple-500' },
  { icon: Dumbbell, label: '练习训练', desc: 'AI生成个性化练习', path: '/practice', color: 'bg-orange-500' },
]

export default function Dashboard() {
  const user = useAuthStore(s => s.user)
  const isTeacher = user?.role === 'teacher'
  const navigate = useNavigate()
  const [courses, setCourses] = useState([])
  const [stats, setStats] = useState({ courses: 0, classes: 0, assignments: 0, docs: 0 })

  useEffect(() => {
    courseApi.list().then(res => {
      const list = res.data || []
      setCourses(list.slice(0, 6))
      setStats(prev => ({ ...prev, courses: list.length }))
    }).catch(() => {})

    classApi.page({ pageNum: 1, pageSize: 1 }).then(res => {
      const total = res.data?.total ?? res.data?.records?.length ?? 0
      setStats(prev => ({ ...prev, classes: total }))
    }).catch(() => {})

    assignmentApi.page({ pageNum: 1, pageSize: 1 }).then(res => {
      const total = res.data?.total ?? res.data?.records?.length ?? 0
      setStats(prev => ({ ...prev, assignments: total }))
    }).catch(() => {})

    knowledgeApi.documents({ pageNum: 1, pageSize: 1 }).then(res => {
      const total = res.data?.total ?? res.data?.records?.length ?? 0
      setStats(prev => ({ ...prev, docs: total }))
    }).catch(() => {})
  }, [])

  const statCards = [
    { icon: BookOpen, label: '课程数', value: stats.courses, color: 'text-blue-600', bg: 'bg-blue-50' },
    { icon: Users, label: '班级数', value: stats.classes, color: 'text-green-600', bg: 'bg-green-50' },
    { icon: FileText, label: '作业数', value: stats.assignments, color: 'text-orange-600', bg: 'bg-orange-50' },
    { icon: Database, label: '知识文档', value: stats.docs, color: 'text-purple-600', bg: 'bg-purple-50' },
  ]

  return (
    <div className="space-y-6">
      {/* Welcome */}
      <div className="bg-gradient-to-r from-primary-600 to-indigo-600 rounded-2xl p-6 text-white">
        <h2 className="text-2xl font-bold">
          欢迎回来，{user?.realName || user?.username}
        </h2>
        <p className="mt-1 text-primary-100">
          {user?.role === 'teacher' ? '教师工作台 - 管理课程、批改作业、分析学情' : '学生工作台 - 学习课程、完成作业、智能答疑'}
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {statCards.map(s => (
          <div key={s.label} className="card flex items-center space-x-4">
            <div className={`w-12 h-12 ${s.bg} rounded-xl flex items-center justify-center`}>
              <s.icon className={`w-6 h-6 ${s.color}`} />
            </div>
            <div>
              <p className="text-2xl font-bold text-gray-900">{s.value}</p>
              <p className="text-sm text-gray-500">{s.label}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Quick Actions */}
      <div>
        <h3 className="text-lg font-semibold text-gray-900 mb-4">AI 智能引擎</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {(isTeacher ? teacherActions : studentActions).map(a => (
            <button key={a.path} onClick={() => navigate(a.path)}
              className="card hover:shadow-md transition-shadow text-left group">
              <div className={`w-10 h-10 ${a.color} rounded-lg flex items-center justify-center mb-3`}>
                <a.icon className="w-5 h-5 text-white" />
              </div>
              <h4 className="font-semibold text-gray-900 group-hover:text-primary-600 flex items-center">
                {a.label}
                <ArrowRight className="w-4 h-4 ml-1 opacity-0 group-hover:opacity-100 transition-opacity" />
              </h4>
              <p className="text-sm text-gray-500 mt-1">{a.desc}</p>
            </button>
          ))}
        </div>
      </div>

      {/* Recent Courses */}
      {courses.length > 0 && (
        <div>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">最近课程</h3>
            <button onClick={() => navigate('/courses')} className="text-sm text-primary-600 hover:text-primary-700 font-medium">
              查看全部 &rarr;
            </button>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {courses.map(c => (
              <div key={c.id} className="card hover:shadow-md transition-shadow">
                <div className="flex items-start justify-between">
                  <div className="flex-1 min-w-0">
                    <h4 className="font-semibold text-gray-900 truncate">{c.courseName}</h4>
                    <p className="text-sm text-gray-500 mt-1 line-clamp-2">{c.description || '暂无描述'}</p>
                  </div>
                  <span className={`ml-2 px-2 py-1 text-xs rounded-full ${c.status === 'active' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'}`}>
                    {c.status === 'active' ? '进行中' : '已归档'}
                  </span>
                </div>
                <div className="mt-3 flex items-center text-xs text-gray-400 space-x-3">
                  {c.category && <span>{c.category}</span>}
                  {c.credit && <span>{c.credit} 学分</span>}
                  {c.classHours && <span>{c.classHours} 学时</span>}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
