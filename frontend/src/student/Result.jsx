// ============================================================
// Result.jsx — the instant verdict screen.
//
// NEW IDEA: useLocation() — reading data that was PASSED with the
// navigation. In ExamAttempt we wrote:
//     navigate('/student/result', { state: res.data })
// Here we read it back with useLocation().state. No extra backend
// call is needed — the graded result travelled with the page change.
//
// SIDE EFFECT TO KNOW (and to mention in the viva): this data lives
// only in memory, so refreshing this page loses it. That is why we
// show a friendly message instead of crashing, and why the PERMANENT
// view is the answer sheet page (/student/result/{resultId}), which
// re-fetches from the backend.
// ============================================================
import { Link, useLocation } from 'react-router-dom'

export default function Result() {
  const { state } = useLocation()

  if (!state) {
    return (
      <div className="container">
        No result to show. <Link to="/student">Back to Dashboard</Link>
      </div>
    )
  }

  const pass = state.status === 'PASS'

  return (
    <div className="container" style={{ maxWidth: 700 }}>
      <h4>Result</h4>
      <div className="row g-3 my-1">
        {/* These four values are EXACTLY your backend's ResultDto */}
        <Card label="Total Marks" value={state.totalMarks} />
        <Card label="Obtained" value={state.obtainedMarks} />
        <Card label="Percentage" value={state.percentage + '%'} />
        <div className="col-6 col-md-3">
          <div className={'card p-3 text-center text-white ' + (pass ? 'bg-success' : 'bg-danger')}>
            <div className="fs-3 fw-bold">{state.status}</div>
            <div className="small text-uppercase">Status</div>
          </div>
        </div>
      </div>

      <div className="mt-3">
        <Link className="btn btn-primary me-2" to={`/student/result/${state.resultId}`}>
          View Answer Sheet
        </Link>
        <Link className="btn btn-outline-secondary" to="/student">Back to Dashboard</Link>
      </div>
    </div>
  )
}

function Card({ label, value }) {
  return (
    <div className="col-6 col-md-3">
      <div className="card p-3 text-center">
        <div className="fs-3 fw-bold">{value}</div>
        <div className="text-muted small text-uppercase">{label}</div>
      </div>
    </div>
  )
}
