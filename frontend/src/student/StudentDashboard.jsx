// ============================================================
// StudentDashboard.jsx  -  UPDATED for the exam status toggle.
//
// WHAT CHANGED
// Each exam now carries a status from the backend:
//   NOT_STARTED - opens later, so the Start button is disabled and we show
//                 a yellow badge saying when it opens
//   ACTIVE      - open right now, green badge and a working Start button
//   EXPIRED     - the backend already hides these from students
//
// The badge and the button both read the same status field, so they can
// never disagree.
// ============================================================
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client from '../api/client'
import { getSession } from '../auth/authService'

export default function StudentDashboard() {
  const [exams, setExams] = useState([])
  const [stats, setStats] = useState(null)
  const { name, userId } = getSession()

  useEffect(() => {
    load()
    // Refresh every 60 seconds, so an exam that opens while the student is
    // sitting on this page turns ACTIVE by itself without a manual refresh.
    const t = setInterval(load, 60000)
    return () => clearInterval(t)
  }, [userId])

  function load() {
    client.get('/exams').then((res) => setExams(res.data))
    client.get(`/dashboard/student/${userId}`)
      .then((res) => setStats(res.data))
      .catch(() => {})
  }

  // Turns 2026-08-10T17:00:00 into a readable 2026-08-10 17:00
  function pretty(dt) {
    return dt ? dt.replace('T', ' ').slice(0, 16) : ''
  }

  return (
    <div className="container">
      <h4>Welcome back, {name}</h4>

      {stats && (
        <div className="row g-3 my-1">
          <Stat label="Available Exams" value={stats.availableExams} />
          <Stat label="Exams Taken" value={stats.examsTaken} />
          <Stat label="Average %" value={stats.averagePercentage} />
          <Stat label="Weak Topics" value={stats.weakTopicsCount} />
        </div>
      )}

      <h5 className="mt-4">Available Exams</h5>
      <table className="table table-bordered bg-white align-middle">
        <thead className="table-light">
          <tr>
            <th>Exam</th><th>Opens At</th><th>Open Until</th>
            <th>Duration</th><th>Questions</th><th>Status</th><th></th>
          </tr>
        </thead>
        <tbody>
          {exams.map((ex) => {
            const active = ex.status === 'ACTIVE'
            return (
              <tr key={ex.examId}>
                <td>{ex.title}</td>
                <td>{pretty(ex.scheduledAt)}</td>
                <td>{pretty(ex.endsAt)}</td>
                <td>{ex.durationMinutes} min</td>
                <td>{ex.totalQuestions}</td>
                <td>
                  {/* THE TOGGLE: green when open, yellow when still upcoming */}
                  {active ? (
                    <span className="badge bg-success">Active</span>
                  ) : (
                    <span className="badge bg-warning text-dark">About to start</span>
                  )}
                </td>
                <td>
                  {active ? (
                    <Link className="btn btn-sm btn-primary"
                          to={`/student/exam/${ex.examId}`}>Start</Link>
                  ) : (
                    // Disabled button, so the student can see the exam exists
                    // but cannot open it before its time.
                    <button className="btn btn-sm btn-secondary" disabled>
                      Not open yet
                    </button>
                  )}
                </td>
              </tr>
            )
          })}
          {exams.length === 0 && (
            <tr><td colSpan="7" className="text-muted">No exams available yet.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  )
}

function Stat({ label, value }) {
  return (
    <div className="col-6 col-md-3">
      <div className="card p-3 text-center">
        <div className="fs-3 fw-bold">{value}</div>
        <div className="text-muted small text-uppercase">{label}</div>
      </div>
    </div>
  )
}
