// ============================================================
// AdminDashboard.jsx — the admin's home page.
//
// Two things on one screen:
//   1. four summary cards  (from /dashboard/admin)
//   2. the Difficult Topics report (from /analytics/difficult-topics)
//
// Both come from YOUR backend. Notice the two separate client.get
// calls inside one useEffect — they are independent, so if one is
// slow the other still renders when it arrives.
//
// The small <Stat/> helper at the bottom is the same idea Person C
// used: four cards that differ only in label and number, so write
// the markup once and pass props.
// ============================================================
import { useEffect, useState } from 'react'
import client from '../api/client'

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)      // null until loaded
  const [difficult, setDifficult] = useState([]) // empty list is a safe start

  useEffect(() => {
    client.get('/dashboard/admin').then((res) => setStats(res.data))
    client.get('/analytics/difficult-topics').then((res) => setDifficult(res.data))
  }, [])

  return (
    <div className="container">
      <h4>Admin Dashboard</h4>

      {stats && (
        <div className="row g-3 my-1">
          <Stat label="Students" value={stats.totalStudents} />
          <Stat label="Exams" value={stats.totalExams} />
          <Stat label="Questions" value={stats.totalQuestions} />
          <Stat label="Batch Avg %" value={stats.batchAveragePercentage} />
        </div>
      )}

      {/* Your batch-level report: which topics does the WHOLE class
          get wrong most often. Your backend already sorts it with
          the hardest topic first, so we just draw the list in order. */}
      <h6 className="mt-4">Difficult Topics Report (highest failure rate first)</h6>
      <table className="table table-bordered bg-white" style={{ maxWidth: 640 }}>
        <thead className="table-light">
          <tr><th>Topic</th><th>Answers</th><th>Failure Rate</th></tr>
        </thead>
        <tbody>
          {difficult.map((d) => (
            <tr key={d.topicName}>
              <td>{d.topicName}</td>
              <td>{d.totalAnswers}</td>
              <td>
                {d.failureRate}%
                <div className="progress mt-1" style={{ height: 8 }}>
                  <div className="progress-bar bg-danger" style={{ width: d.failureRate + '%' }} />
                </div>
              </td>
            </tr>
          ))}
          {difficult.length === 0 && (
            <tr><td colSpan="3" className="text-muted">No attempts yet.</td></tr>
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
