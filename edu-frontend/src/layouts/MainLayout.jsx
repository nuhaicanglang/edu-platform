import { useState, useEffect } from 'react'
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import {
  LayoutDashboard, BookOpen, Users, FileText, Database,
  MessageSquare, CheckSquare, BarChart3, Dumbbell, LogOut,
  Menu, X, Brain, ChevronRight, Sparkles
} from 'lucide-react'
import useAuthStore from '../store/useAuthStore'
import { authApi } from '../api'

const teacherNav = [
  { path: '/', icon: LayoutDashboard, label: '工作台', end: true },
  { path: '/courses', icon: BookOpen, label: '课程管理' },
  { path: '/classes', icon: Users, label: '班级管理' },
  { path: '/assignments', icon: FileText, label: '作业管理' },
  { path: '/knowledge', icon: Database, label: '知识库' },
  { type: 'divider', label: 'AI 智能引擎' },
  { path: '/chat', icon: MessageSquare, label: '智能问答' },
  { path: '/grading', icon: CheckSquare, label: '作业批改' },
  { path: '/analytics', icon: BarChart3, label: '学情分析' },
  { path: '/practice', icon: Dumbbell, label: '练习生成' },
  { type: 'divider', label: '系统工具' },
  { path: '/mock-data', icon: Sparkles, label: '模拟数据生成' },
]

const studentNav = [
  { path: '/', icon: LayoutDashboard, label: '学习中心', end: true },
  { path: '/courses', icon: BookOpen, label: '我的课程' },
  { path: '/classes', icon: Users, label: '我的班级' },
  { path: '/assignments', icon: FileText, label: '我的作业' },
  { path: '/knowledge', icon: Database, label: '知识检索' },
  { type: 'divider', label: 'AI 学习助手' },
  { path: '/chat', icon: MessageSquare, label: '智能问答' },
  { path: '/analytics', icon: BarChart3, label: '我的学情' },
  { path: '/practice', icon: Dumbbell, label: '练习训练' },
]

export default function MainLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const { user, logout, login, token } = useAuthStore()
  const navigate = useNavigate()

  useEffect(() => {
    if (user && !user.email) {
      authApi.getUserInfo().then(res => {
        const u = res.data || {}
        login(token, { ...user, email: u.email || '', phone: u.phone || '', userCode: u.userCode || '', avatar: u.avatar || '' })
      }).catch(() => {})
    }
  }, [])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <aside className={`${sidebarOpen ? 'w-64' : 'w-20'} bg-white border-r border-gray-200 flex flex-col transition-all duration-300 flex-shrink-0`}>
        {/* Logo */}
        <div className="h-16 flex items-center px-4 border-b border-gray-100">
          <Brain className="w-8 h-8 text-primary-600 flex-shrink-0" />
          {sidebarOpen && (
            <div className="ml-3 overflow-hidden">
              <h1 className="text-sm font-bold text-gray-900 whitespace-nowrap">AI 智慧教育平台</h1>
              <p className="text-xs text-gray-500 whitespace-nowrap">跨课程Agent架构</p>
            </div>
          )}
        </div>

        {/* Nav */}
        <nav className="flex-1 py-4 overflow-y-auto">
          {(user?.role === 'teacher' ? teacherNav : studentNav).map((item, i) => {
            if (item.type === 'divider') {
              return sidebarOpen ? (
                <div key={i} className="px-4 pt-5 pb-2">
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider">{item.label}</p>
                </div>
              ) : <div key={i} className="my-2 mx-3 border-t border-gray-200" />
            }
            const Icon = item.icon
            return (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.end}
                className={({ isActive }) =>
                  `flex items-center mx-3 my-0.5 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-primary-50 text-primary-700'
                      : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                  }`
                }
              >
                <Icon className="w-5 h-5 flex-shrink-0" />
                {sidebarOpen && <span className="ml-3 whitespace-nowrap">{item.label}</span>}
              </NavLink>
            )
          })}
        </nav>

        {/* User */}
        <div className="border-t border-gray-200 p-4">
          <div className="flex items-start">
            <div className="w-9 h-9 rounded-full bg-primary-100 flex items-center justify-center text-primary-700 font-bold text-sm flex-shrink-0 mt-0.5">
              {user?.realName?.[0] || user?.username?.[0] || 'U'}
            </div>
            {sidebarOpen && (
              <div className="ml-3 flex-1 min-w-0">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium text-gray-900 truncate">{user?.realName || user?.username}</p>
                  <button onClick={handleLogout} className="text-gray-400 hover:text-red-500 transition-colors ml-1 flex-shrink-0" title="退出登录">
                    <LogOut className="w-4 h-4" />
                  </button>
                </div>
                <span className={`inline-block px-1.5 py-0.5 text-xs rounded font-medium ${user?.role === 'teacher' ? 'bg-blue-100 text-blue-700' : 'bg-green-100 text-green-700'}`}>
                  {user?.role === 'teacher' ? '教师' : '学生'}
                </span>
                <div className="mt-1.5 space-y-0.5">
                  {user?.userCode && (
                    <p className="text-xs text-gray-500 truncate">
                      <span className="text-gray-400">{user?.role === 'teacher' ? '工号' : '学号'}：</span>
                      {user.userCode}
                    </p>
                  )}
                  {user?.email && (
                    <p className="text-xs text-gray-500 truncate" title={user.email}>
                      <span className="text-gray-400">邮箱：</span>{user.email}
                    </p>
                  )}
                  {user?.phone && (
                    <p className="text-xs text-gray-500 truncate">
                      <span className="text-gray-400">手机：</span>{user.phone}
                    </p>
                  )}
                </div>
              </div>
            )}
            {!sidebarOpen && (
              <button onClick={handleLogout} className="text-gray-400 hover:text-red-500 transition-colors ml-1" title="退出登录">
                <LogOut className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>
      </aside>

      {/* Main */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <header className="h-16 bg-white border-b border-gray-200 flex items-center px-6 flex-shrink-0">
          <button onClick={() => setSidebarOpen(!sidebarOpen)} className="text-gray-500 hover:text-gray-700 mr-4">
            {sidebarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
          <div className="flex items-center text-sm text-gray-500">
            <span>可嵌入式跨课程AI Agent通用架构平台</span>
            <ChevronRight className="w-4 h-4 mx-1" />
            <span className="text-gray-900 font-medium">{user?.role === 'teacher' ? '教师工作台' : '学生学习中心'}</span>
          </div>
          <div className="ml-auto">
            <span className={`px-2.5 py-1 text-xs font-medium rounded-full ${
              user?.role === 'teacher' ? 'bg-blue-100 text-blue-700' : 'bg-green-100 text-green-700'
            }`}>
              {user?.role === 'teacher' ? '教师' : '学生'}
            </span>
          </div>
        </header>

        {/* Content */}
        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
