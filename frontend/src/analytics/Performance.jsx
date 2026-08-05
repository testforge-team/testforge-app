// ============================================================
// Performance.jsx — the student's "Performance Analysis" screen
// (PERSON D). Start with this file.
//
// GOOD NEWS: your screens introduce almost no new React. You
// already have everything from A, B and C:
//   useState  (memory)      useEffect (load when page opens)
//   .map      (draw lists)  conditional rendering {x && ...}
// Your job is DISPLAY: turning the numbers your backend derived
// into something a student can understand at a glance.
//
// NEW IDEA 1 (small): a CSS bar instead of a chart library.
// A real chart library is extra complexity we do not need. A
// Bootstrap "progress" div whose width is set to the percentage
// gives a clean visual bar in two lines:
//     <div className="progress"><div className="progress-bar"
//          style={{ width: value + '%' }} /></div>
// style={{ ... }} is JSX's way of writing inline CSS: the outer
// braces mean "JavaScript here", the inner ones are the object.
// Note CSS property names become camelCase (backgroundColor).
// ============================================================
import { useEffect, useState } from 'react'
import client from '../api/client'
import { getSession } from '../auth/authService'

export default function Performance() {
  const [data, setData] = useState(null)   // null = still loading
  const { userId } = getSession()          // who is logged in (saved by Person A)

  useEffect(() => {
    // YOUR backend endpoint: returns { trends, strengths, weakTopics }
    client.get(`/analytics/student/${userId}`).then((res) => setData(res.data))
  }, [userId])

  // Early return while the data is on its way. Without this, the
  // next lines would try to read data.trends of null and crash.
  if (!data) return <div className="container">Loading...</div>

  return (
    <div className="container">
      <h4>Performance Analysis</h4>

      {/* ---------- 1) score over time ---------- */}
      <h6 className="mt-4">Performance Trend (latest first)</h6>
      <table className="table table-bordered bg-white" style={{ maxWidth: 640 }}>
        <thead className="table-light">
          <tr><th>Exam</th><th>Date</th><th>Percentage</th></tr>
        </thead>
        <tbody>
          {data.trends.map((t, i) => (
            // When a list has no unique id, the index i can be the key.
            <tr key={i}>
              <td>{t.examTitle}</td>
              <td>{t.examDate?.replace('T', ' ').slice(0, 16)}</td>
              <td>
                {t.percentage}%
                <div className="progress mt-1" style={{ height: 8 }}>
                  <div className="progress-bar" style={{ width: t.percentage + '%' }} />
                </div>
              </td>
            </tr>
          ))}
          {data.trends.length === 0 && (
            <tr><td colSpan="3" className="text-muted">Take an exam to see trends.</td></tr>
          )}
        </tbody>
      </table>

      {/* ---------- 2) strengths and weaknesses side by side ---------- */}
      <div className="row mt-4">
        <div className="col-md-6">
          <h6>Strengths (80% and above)</h6>
          {data.strengths.map((s) => (
            <span key={s.topicName} className="badge bg-success me-2 mb-2 p-2">
              {s.topicName} · {s.accuracy}%
            </span>
          ))}
          {data.strengths.length === 0 && <div className="text-muted">None yet.</div>}
        </div>

        <div className="col-md-6">
          <h6>Weak Topics — revise these (below 50%)</h6>
          {data.weakTopics.map((w) => (
            <span key={w.topicName} className="badge bg-danger me-2 mb-2 p-2">
              {w.topicName} · {w.accuracy}%
            </span>
          ))}
          {data.weakTopics.length === 0 && <div className="text-muted">None — great job!</div>}
        </div>
      </div>

      {/* The 50% and 80% thresholds shown here are exactly the ones
          your WeaknessService uses on the backend. */}
    </div>
  )
}
