// ============================================================
// History.jsx — "My Results": every past attempt of this student.
//
// Nothing new here at all: getSession for the user id, useEffect to
// load, .map to draw rows, Link to open the answer sheet.
//
// WHAT THE DATA PROVES: if you take the same exam twice, TWO rows
// appear. That is your backend design — each attempt creates a new
// Result row instead of overwriting the old one, so history is
// never lost. Worth demonstrating in the viva.
// ============================================================
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import client from '../api/client'
import { getSession } from '../auth/authService'

export default function History() {
  const [rows, setRows] = useState([])
  const { userId } = getSession()

  useEffect(() => {
    client.get(`/results/${userId}`).then((res) => setRows(res.data))
  }, [userId])

  return (
    <div className="container">
      <h4>My Results</h4>
      <table className="table table-bordered bg-white">
        <thead className="table-light">
          <tr><th>Exam</th><th>Date</th><th>Score</th><th>%</th><th>Status</th><th></th></tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.resultId}>
              <td>{r.examTitle}</td>
              {/* slice(0,16) cuts "2026-07-25T10:00:00" down to date + time */}
              <td>{r.examDate?.replace('T', ' ').slice(0, 16)}</td>
              <td>{r.obtainedMarks}/{r.totalMarks}</td>
              <td>{r.percentage}%</td>
              <td>
                <span className={'badge ' + (r.status === 'PASS' ? 'bg-success' : 'bg-danger')}>
                  {r.status}
                </span>
              </td>
              <td>
                <Link className="btn btn-sm btn-outline-primary" to={`/student/result/${r.resultId}`}>
                  Review
                </Link>
              </td>
            </tr>
          ))}
          {rows.length === 0 && (
            <tr><td colSpan="6" className="text-muted">No attempts yet.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
