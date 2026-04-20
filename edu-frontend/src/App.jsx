import { Routes, Route, Navigate } from 'react-router-dom'
import useAuthStore from './store/useAuthStore'
import MainLayout from './layouts/MainLayout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Courses from './pages/Courses'
import Classes from './pages/Classes'
import Assignments from './pages/Assignments'
import AssignmentDetail from './pages/AssignmentDetail'
import KnowledgeBase from './pages/KnowledgeBase'
import AIChat from './pages/AIChat'
import AIGrading from './pages/AIGrading'
import Analytics from './pages/Analytics'
import PracticeGen from './pages/PracticeGen'
import MockData from './pages/MockData'

function PrivateRoute({ children }) {
  const token = useAuthStore(s => s.token)
  return token ? children : <Navigate to="/login" replace />
}

function TeacherRoute({ children }) {
  const user = useAuthStore(s => s.user)
  return user?.role === 'teacher' ? children : <Navigate to="/" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<PrivateRoute><MainLayout /></PrivateRoute>}>
        <Route index element={<Dashboard />} />
        <Route path="courses" element={<Courses />} />
        <Route path="classes" element={<Classes />} />
        <Route path="assignments" element={<Assignments />} />
        <Route path="assignments/:id" element={<AssignmentDetail />} />
        <Route path="knowledge" element={<KnowledgeBase />} />
        <Route path="chat" element={<AIChat />} />
        <Route path="grading" element={<TeacherRoute><AIGrading /></TeacherRoute>} />
        <Route path="analytics" element={<Analytics />} />
        <Route path="practice" element={<PracticeGen />} />
        <Route path="mock-data" element={<TeacherRoute><MockData /></TeacherRoute>} />
      </Route>
    </Routes>
  )
}
