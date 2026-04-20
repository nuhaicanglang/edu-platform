import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Brain, Eye, EyeOff } from 'lucide-react'
import { authApi } from '../api'
import useAuthStore from '../store/useAuthStore'

export default function Login() {
  const [isRegister, setIsRegister] = useState(false)
  const [showPwd, setShowPwd] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    username: '', password: '', realName: '', role: 'teacher', userCode: ''
  })
  const navigate = useNavigate()
  const login = useAuthStore(s => s.login)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (isRegister) {
        await authApi.register(form)
      }
      const res = await authApi.login({ username: form.username, password: form.password })
      const { token, userId, username, role, realName } = res.data
      localStorage.setItem('token', token)
      let userObj = { userId, username, role, realName }
      try {
        const infoRes = await authApi.getUserInfo()
        const u = infoRes.data || {}
        userObj = { ...userObj, email: u.email || '', phone: u.phone || '', userCode: u.userCode || '', avatar: u.avatar || '' }
      } catch (_) {}
      login(token, userObj)
      navigate('/')
    } catch (err) {
      setError(err.message || '操作失败')
    } finally {
      setLoading(false)
    }
  }

  const set = (k, v) => setForm(prev => ({ ...prev, [k]: v }))

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-600 via-primary-700 to-indigo-800 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-white/10 backdrop-blur rounded-2xl mb-4">
            <Brain className="w-9 h-9 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-white">AI 智慧教育平台</h1>
          <p className="text-primary-200 mt-1">可嵌入式跨课程AI Agent通用架构</p>
        </div>

        {/* Form Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <h2 className="text-xl font-bold text-gray-900 mb-6">
            {isRegister ? '注册新账户' : '登录'}
          </h2>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label">用户名</label>
              <input className="input" placeholder="请输入用户名" value={form.username}
                onChange={e => set('username', e.target.value)} required />
            </div>

            <div>
              <label className="label">密码</label>
              <div className="relative">
                <input className="input pr-10" type={showPwd ? 'text' : 'password'}
                  placeholder="请输入密码" value={form.password}
                  onChange={e => set('password', e.target.value)} required />
                <button type="button" className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  onClick={() => setShowPwd(!showPwd)}>
                  {showPwd ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {isRegister && (
              <>
                <div>
                  <label className="label">真实姓名</label>
                  <input className="input" placeholder="请输入姓名" value={form.realName}
                    onChange={e => set('realName', e.target.value)} required />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="label">角色</label>
                    <select className="input" value={form.role} onChange={e => set('role', e.target.value)}>
                      <option value="teacher">教师</option>
                      <option value="student">学生</option>
                    </select>
                  </div>
                  <div>
                    <label className="label">工号/学号</label>
                    <input className="input" placeholder="编号" value={form.userCode}
                      onChange={e => set('userCode', e.target.value)} />
                  </div>
                </div>
              </>
            )}

            <button type="submit" disabled={loading} className="btn-primary w-full py-2.5">
              {loading ? '处理中...' : (isRegister ? '注册并登录' : '登 录')}
            </button>
          </form>

          <div className="mt-6 text-center">
            <button className="text-sm text-primary-600 hover:text-primary-700 font-medium"
              onClick={() => { setIsRegister(!isRegister); setError('') }}>
              {isRegister ? '已有账户？去登录' : '没有账户？注册一个'}
            </button>
          </div>

          <div className="mt-4 p-3 bg-gray-50 rounded-lg text-xs text-gray-500">
            <p className="font-medium text-gray-600 mb-1">测试账户：</p>
            <p>教师: teacher1 / 123456</p>
            <p>学生: student1 / 123456</p>
          </div>
        </div>
      </div>
    </div>
  )
}
