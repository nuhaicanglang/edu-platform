import { useState } from 'react'
import { Sparkles, UserPlus, Users, BookOpen, FileText, Loader2, CheckCircle2 } from 'lucide-react'
import { authApi, courseApi, classApi, assignmentApi } from '../api'

const STUDENT_NAMES = [
  '张伟', '王芳', '李娜', '刘洋', '陈静', '杨帆', '赵磊', '黄丽', '周杰', '吴敏',
  '徐浩', '孙悦', '马超', '朱婷', '胡明', '郭靖', '林峰', '何雨', '高远', '罗晨',
  '梁博', '宋雪', '唐亮', '韩冰', '冯涛', '董琳', '萧蓉', '曹鹏', '袁媛', '邓辉',
]

const COURSE_TEMPLATES = [
  { courseName: 'Java程序设计', courseCode: 'CS201', category: '理论+实践', credit: 4, classHours: 64, description: '面向对象编程语言Java的核心语法、面向对象设计、集合框架、多线程、IO流、网络编程等' },
  { courseName: 'Python数据分析', courseCode: 'CS202', category: '实践', credit: 3, classHours: 48, description: 'Python基础语法、NumPy、Pandas、Matplotlib数据可视化、数据清洗与分析' },
  { courseName: '数据结构与算法', courseCode: 'CS101', category: '理论', credit: 4, classHours: 64, description: '线性表、栈、队列、树、图、排序算法、查找算法、动态规划' },
  { courseName: 'Web前端开发', courseCode: 'CS301', category: '实践', credit: 3, classHours: 48, description: 'HTML5、CSS3、JavaScript、React框架、响应式布局、前后端交互' },
  { courseName: '数据库原理与应用', courseCode: 'CS203', category: '理论+实践', credit: 3.5, classHours: 56, description: '关系数据库理论、SQL语言、数据库设计、事务处理、索引优化' },
]

const ASSIGNMENT_TEMPLATES = [
  { title: '面向对象编程实验', assignmentType: 'experiment', totalScore: 100, description: '设计一个图书管理系统，要求使用继承、多态、接口等面向对象特性' },
  { title: '数组与链表练习', assignmentType: 'homework', totalScore: 50, description: '完成数组排序和链表反转的编程练习' },
  { title: '期中编程测试', assignmentType: 'quiz', totalScore: 100, description: '限时90分钟，包含选择题、填空题和编程题' },
  { title: '课程大作业', assignmentType: 'project', totalScore: 200, description: '团队协作完成一个完整的应用系统开发' },
  { title: 'SQL查询练习', assignmentType: 'homework', totalScore: 60, description: '编写复杂SQL查询语句，包含多表连接、子查询和聚合函数' },
]

export default function MockData() {
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(false)
  const [studentCount, setStudentCount] = useState(20)
  const [courseCount, setCourseCount] = useState(3)

  const log = (msg, type = 'info') => setLogs(prev => [...prev, { msg, type, time: new Date().toLocaleTimeString() }])

  const generateAll = async () => {
    setLoading(true)
    setLogs([])
    log('开始生成模拟数据...')

    try {
      // 1. Register students
      log(`正在注册 ${studentCount} 名学生...`)
      const studentIds = []
      for (let i = 0; i < studentCount; i++) {
        const name = STUDENT_NAMES[i % STUDENT_NAMES.length]
        const suffix = i >= STUDENT_NAMES.length ? `_${Math.floor(i / STUDENT_NAMES.length)}` : ''
        const username = `student_${String(i + 1).padStart(3, '0')}`
        try {
          await authApi.register({
            username: username + suffix,
            password: '123456',
            realName: name,
            role: 'student',
            userCode: `S${String(2024001 + i)}`
          })
          // Login to get the student's userId
          const loginRes = await authApi.login({ username: username + suffix, password: '123456' })
          studentIds.push({ id: loginRes.data.userId, name, username: username + suffix })
          if ((i + 1) % 5 === 0) log(`  已注册 ${i + 1}/${studentCount} 名学生`)
        } catch (err) {
          // Student might already exist, try to login
          try {
            const loginRes = await authApi.login({ username: username + suffix, password: '123456' })
            studentIds.push({ id: loginRes.data.userId, name, username: username + suffix })
          } catch { /* skip */ }
        }
      }
      log(`✓ 注册完成，共 ${studentIds.length} 名学生`, 'success')

      // 2. Create courses
      log(`正在创建 ${courseCount} 门课程...`)
      const courseIds = []
      for (let i = 0; i < courseCount && i < COURSE_TEMPLATES.length; i++) {
        try {
          await courseApi.create({ ...COURSE_TEMPLATES[i], status: 'active' })
          log(`  创建课程: ${COURSE_TEMPLATES[i].courseName}`)
        } catch (err) {
          log(`  课程可能已存在: ${COURSE_TEMPLATES[i].courseName}`, 'warn')
        }
      }
      // Fetch all courses to get IDs
      const coursesRes = await courseApi.list()
      const courses = coursesRes.data || []
      courses.forEach(c => courseIds.push(c.id))
      log(`✓ 课程就绪，共 ${courses.length} 门`, 'success')

      // 3. Create classes and add students
      log('正在创建班级并分配学生...')
      for (const course of courses.slice(0, courseCount)) {
        const className = `${course.courseName}-${new Date().getFullYear()}秋季班`
        try {
          await classApi.create({
            className,
            courseId: course.id,
            semester: '2025-2026-1',
            status: 0
          })
          log(`  创建班级: ${className}`)
        } catch (err) {
          log(`  班级可能已存在: ${className}`, 'warn')
        }
      }
      // Fetch classes and add students
      const classRes = await classApi.page({ pageNum: 1, pageSize: 50 })
      const classes = classRes.data?.records || []
      for (const cls of classes) {
        const studentsToAdd = studentIds.slice(0, Math.min(studentIds.length, 30))
        let added = 0
        for (const stu of studentsToAdd) {
          try {
            await classApi.addStudent(cls.id, stu.id)
            added++
          } catch { /* might already be in class */ }
        }
        log(`  班级 "${cls.className}" 添加了 ${added} 名学生`)
      }
      log(`✓ 班级分配完成`, 'success')

      // 4. Create assignments
      log('正在创建作业...')
      for (const course of courses.slice(0, courseCount)) {
        const templates = ASSIGNMENT_TEMPLATES.slice(0, 3)
        for (const tpl of templates) {
          try {
            await assignmentApi.create({
              ...tpl,
              courseId: course.id,
              status: 'published'
            })
            log(`  创建作业: ${tpl.title} (${course.courseName})`)
          } catch (err) {
            log(`  作业创建失败: ${err.message}`, 'warn')
          }
        }
      }
      log(`✓ 作业创建完成`, 'success')

      log('🎉 所有模拟数据生成完成！', 'success')
    } catch (err) {
      log(`✗ 生成失败: ${err.message}`, 'error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-gray-900">模拟数据生成</h2>
        <p className="text-gray-500 mt-1">一键生成学生、班级、课程、作业等模拟数据，用于平台功能演示</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Config */}
        <div className="card space-y-4">
          <h3 className="font-semibold text-gray-900">生成配置</h3>
          
          <div className="space-y-3">
            <div className="flex items-center space-x-3 p-3 bg-blue-50 rounded-lg">
              <UserPlus className="w-5 h-5 text-blue-600" />
              <div className="flex-1">
                <p className="text-sm font-medium text-gray-900">学生数量</p>
                <p className="text-xs text-gray-500">自动注册学生账号(密码均为123456)</p>
              </div>
              <input type="number" min={5} max={30} value={studentCount}
                onChange={e => setStudentCount(parseInt(e.target.value) || 20)}
                className="input w-20 text-center" />
            </div>

            <div className="flex items-center space-x-3 p-3 bg-green-50 rounded-lg">
              <BookOpen className="w-5 h-5 text-green-600" />
              <div className="flex-1">
                <p className="text-sm font-medium text-gray-900">课程数量</p>
                <p className="text-xs text-gray-500">从预设模板创建课程</p>
              </div>
              <input type="number" min={1} max={5} value={courseCount}
                onChange={e => setCourseCount(parseInt(e.target.value) || 3)}
                className="input w-20 text-center" />
            </div>

            <div className="flex items-center space-x-3 p-3 bg-purple-50 rounded-lg">
              <Users className="w-5 h-5 text-purple-600" />
              <div className="flex-1">
                <p className="text-sm font-medium text-gray-900">班级</p>
                <p className="text-xs text-gray-500">每门课程创建1个班级</p>
              </div>
              <span className="text-sm font-medium text-gray-700">{courseCount} 个</span>
            </div>

            <div className="flex items-center space-x-3 p-3 bg-orange-50 rounded-lg">
              <FileText className="w-5 h-5 text-orange-600" />
              <div className="flex-1">
                <p className="text-sm font-medium text-gray-900">作业</p>
                <p className="text-xs text-gray-500">每门课程3个作业</p>
              </div>
              <span className="text-sm font-medium text-gray-700">{courseCount * 3} 个</span>
            </div>
          </div>

          <button onClick={generateAll} disabled={loading}
            className="btn-primary w-full flex items-center justify-center py-3">
            {loading
              ? <><Loader2 className="w-5 h-5 mr-2 animate-spin" /> 生成中...</>
              : <><Sparkles className="w-5 h-5 mr-2" /> 一键生成全部数据</>
            }
          </button>

          <div className="text-xs text-gray-400 p-2 bg-gray-50 rounded">
            <p>提示：生成的学生账号格式为 student_001 ~ student_{String(studentCount).padStart(3,'0')}</p>
            <p>密码统一为 123456</p>
          </div>
        </div>

        {/* Logs */}
        <div className="lg:col-span-2 card">
          <h3 className="font-semibold text-gray-900 mb-4">执行日志</h3>
          <div className="bg-gray-900 rounded-lg p-4 h-[500px] overflow-y-auto font-mono text-xs space-y-1">
            {logs.length === 0 ? (
              <p className="text-gray-500">点击"一键生成"开始...</p>
            ) : (
              logs.map((l, i) => (
                <div key={i} className={`flex ${
                  l.type === 'success' ? 'text-green-400' :
                  l.type === 'error' ? 'text-red-400' :
                  l.type === 'warn' ? 'text-yellow-400' : 'text-gray-300'
                }`}>
                  <span className="text-gray-600 mr-2 flex-shrink-0">[{l.time}]</span>
                  <span>{l.msg}</span>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
