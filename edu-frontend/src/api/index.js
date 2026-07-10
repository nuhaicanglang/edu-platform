import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code === 200) return data
    return Promise.reject(new Error(data.msg || '请求失败'))
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ===== Auth =====
export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  getUserInfo: () => api.get('/auth/info'),
}

// ===== Course (teacher: CRUD, student: view) =====
export const courseApi = {
  list: () => api.get('/system/course/list'),
  page: (params) => api.get('/system/course/page', { params }),
  getById: (id) => api.get(`/system/course/${id}`),
  myList: () => api.get('/system/course/my'),
  create: (data) => api.post('/system/course', data),
  update: (data) => api.put('/system/course', data),
  delete: (id) => api.delete(`/system/course/${id}`),
}

// ===== ClassGroup =====
export const classApi = {
  page: (params) => api.get('/system/class/page', { params }),
  getById: (id) => api.get(`/system/class/${id}`),
  create: (data) => api.post('/system/class', data),
  update: (data) => api.put('/system/class', data),
  delete: (id) => api.delete(`/system/class/${id}`),
  addStudent: (classId, studentId) => api.post(`/system/class/${classId}/student/${studentId}`),
  removeStudent: (classId, studentId) => api.delete(`/system/class/${classId}/student/${studentId}`),
  getStudents: (classId) => api.get(`/system/class/${classId}/students`),
  myClasses: () => api.get('/system/class/student/my'),
  searchAvailableStudents: (classId, keyword) =>
    api.get(`/system/class/${classId}/available-students`, { params: { keyword } }),
}

// ===== Assignment =====
export const assignmentApi = {
  page: (params) => api.get('/system/assignment/page', { params }),
  getById: (id) => api.get(`/system/assignment/${id}`),
  create: (data) => api.post('/system/assignment', data),
  createWithFile: (formData) => api.post('/system/assignment/create-with-file', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000,
  }),
  update: (data) => api.put('/system/assignment', data),
  delete: (id) => api.delete(`/system/assignment/${id}`),
  publish: (id) => api.post(`/system/assignment/${id}/publish`),
  submit: (assignmentId, content, file) => {
    const formData = new FormData()
    formData.append('assignmentId', assignmentId)
    if (content) formData.append('content', content)
    if (file) formData.append('file', file)
    return api.post('/system/assignment/submit', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  pageSubmissions: (assignmentId, params) => api.get(`/system/assignment/${assignmentId}/submissions`, { params }),
  listAllSubmissions: (assignmentId) => api.get(`/system/assignment/${assignmentId}/all-submissions`),
  getSubmission: (submissionId) => api.get(`/system/assignment/submission/${submissionId}`),
  mySubmission: (assignmentId) => api.get(`/system/assignment/${assignmentId}/my-submission`),
  aiGrade: (assignmentId, submissionId) => api.post(
    `/system/assignment/${assignmentId}/ai-grade/${submissionId}`, {}, { timeout: 10000 }
  ),
  getGradingStatus: (submissionId) => api.get(
    `/system/assignment/submission/${submissionId}/grading-status`
  ),
  quickGrade: (formData) => api.post('/system/assignment/quick-grade', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  }),
  fileUrl: (relativePath) => {
    if (!relativePath) return ''
    const encoded = relativePath.split('/').map(encodeURIComponent).join('/')
    return `/api/system/files/${encoded}?download=true`
  },
}

// ===== Knowledge Base =====
export const knowledgeApi = {
  documents: (params) => api.get('/knowledge/documents', { params }),
  getDocument: (id) => api.get(`/knowledge/documents/${id}`),
  deleteDocument: (id) => api.delete(`/knowledge/documents/${id}`),
  getChunks: (id) => api.get(`/knowledge/documents/${id}/chunks`),
  search: (keyword, courseId) => api.get('/knowledge/search', { params: { keyword, courseId } }),
  getCourseContext: (courseId) => api.get(`/knowledge/course/${courseId}/context`),
  upload: (formData, courseId) => api.post(`/knowledge/upload?courseId=${courseId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
}

// ===== Knowledge Points =====
export const knowledgePointApi = {
  listByCourse: (courseId) => api.get(`/system/knowledge-point/course/${courseId}`),
  create: (data) => api.post('/system/knowledge-point', data),
  update: (data) => api.put('/system/knowledge-point', data),
  delete: (id) => api.delete(`/system/knowledge-point/${id}`),
}

// ===== Analytics (real DB data) =====
export const analyticsApi = {
  studentData: (studentName, courseName) =>
    api.get('/system/analytics/student-data', { params: { studentName, courseName } }),
  classData: (className, courseName) =>
    api.get('/system/analytics/class-data', { params: { className, courseName } }),
}

// ===== AI Agent =====
export const agentApi = {
  askSimple: (question) => api.post('/agent/qa/ask-simple', { question }, { timeout: 120000 }),
  ask: (question, courseId, courseContext) => api.post(
    '/agent/qa/ask', { question, courseId, courseContext }, { timeout: 120000 }
  ),
  explain: (knowledgePoint, courseName) => api.post('/agent/qa/explain', { knowledgePoint, courseName }, { timeout: 120000 }),
  gradeText: (data) => api.post('/agent/grading/text', data, { timeout: 120000 }),
  gradeCode: (data) => api.post('/agent/grading/code', data, { timeout: 120000 }),
  studentReport: (data) => api.post('/agent/analytics/student-report', data, { timeout: 120000 }),
  classOverview: (data) => api.post('/agent/analytics/class-overview', data, { timeout: 120000 }),
  knowledgeGraph: (data) => api.post('/agent/analytics/knowledge-graph', data, { timeout: 120000 }),
  riskAnalysis: (data) => api.post('/agent/analytics/risk-analysis', data, { timeout: 120000 }),
  generatePractice: (data) => api.post('/agent/practice/generate', data, { timeout: 150000 }),
  personalizedPractice: (data) => api.post('/agent/practice/personalized', data, { timeout: 150000 }),
  examPaper: (data) => api.post('/agent/practice/exam-paper', data, { timeout: 180000 }),
  chatRecordsMy: (params) => api.get('/agent/chat-record/my', { params }),
  chatRecordsAll: (params) => api.get('/agent/chat-record/all', { params }),
  chatRecordStats: () => api.get('/agent/chat-record/stats'),
  smartAsk: (data) => api.post('/agent/smart/ask', data, { timeout: 180000 }),
}

// ===== Learning Records =====
export const learningApi = {
  myRecords: (params) => api.get('/system/analytics/learning-records', { params }),
  allRecords: (params) => api.get('/system/analytics/all-learning-records', { params }),
}

export default api
