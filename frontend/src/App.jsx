// ============================================================
// App.jsx — the MAP of the application (Person A owns this file).
//
// react-router connects URLs to components:
//   URL /login  -> show the <Login/> component
//   URL /admin  -> show the admin dashboard (wrapped in the guard)
//
// This is Person A's file the way SecurityConfig was on the
// backend: TEAMMATES DO NOT EDIT IT DIRECTLY — they tell Person A
// which route to add, and A adds one import + one <Route> line at
// the marked spots below.
//
// RIGHT NOW /student and /admin show small placeholder pages
// (defined at the bottom of this file), so Person A can fully test
// login, logout and the guards before B, C and D exist — exactly
// like the backend auth slice ran standalone first.
// ============================================================
import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import Login from './auth/Login'
import Register from './auth/Register'
import ProtectedRoute from './auth/ProtectedRoute'
import { getSession } from './auth/authService'

// >>> PERSON B: add your imports here (Topics, Questions, ManageExams)
// >>> PERSON C: add your imports here (StudentDashboard, ExamAttempt, Result, ResultDetail, History)
// >>> PERSON D: add your imports here (Performance, AdminDashboard, Reminders)

export default function App() {
  return (
    <>
      <Navbar />
      <Routes>
        {/* Opening the site root sends you to the login page */}
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* ---- Student area (login required) ---- */}
        <Route path="/student" element={<ProtectedRoute><StudentHome /></ProtectedRoute>} />
        {/* >>> PERSON C: replace StudentHome above with your StudentDashboard,
            and add your routes here:
            /student/exam/:id  /student/result  /student/result/:resultId  /student/history */}
        {/* >>> PERSON D: add /student/performance here */}

        {/* ---- Admin area (login + ADMIN role required) ---- */}
        <Route path="/admin" element={<ProtectedRoute adminOnly><AdminHome /></ProtectedRoute>} />
        {/* >>> PERSON B: add /admin/topics  /admin/questions  /admin/exams here */}
        {/* >>> PERSON D: replace AdminHome with your AdminDashboard, add /admin/reminders */}
      </Routes>
    </>
  )
}

// ---------- Temporary placeholder pages (Person A's test targets) ----------
// These prove that login lands in the right place for each role.
// They are replaced when C and D deliver their real dashboards.
function StudentHome() {
  const { name } = getSession()
  return (
    <div className="container">
      <div className="card p-4">
        <h4>Welcome, {name}! (Student)</h4>
        <p className="text-muted mb-0">
          Login works. Person C's dashboard and exam screens will appear here.
        </p>
      </div>
    </div>
  )
}

function AdminHome() {
  const { name } = getSession()
  return (
    <div className="container">
      <div className="card p-4">
        <h4>Welcome, {name}! (Admin)</h4>
        <p className="text-muted mb-0">
          Admin login works. Person B's and D's admin screens will appear here.
        </p>
      </div>
    </div>
  )
}
