// ============================================================
// ManageExams.jsx  -  UPDATED for the exam window.
//
// WHAT CHANGED
//   * The create form has a new "Active for (hours)" box, so the admin
//     decides how long the exam stays open after it starts.
//   * The table shows two new columns: when the exam closes, and a live
//     status badge (About to start / Active / Expired).
//   * The status comes from the backend, which calculates it from the
//     schedule - the frontend never works it out itself.
// ============================================================
import { useEffect, useState } from 'react'
import client from '../api/client'

export default function ManageExams() {
  const [exams, setExams] = useState([])
  const [questions, setQuestions] = useState([])
  // NEW FIELD in the form: activeHours, defaulted to 24
  const [form, setForm] = useState({
    title: '', durationMinutes: 30, passingMarks: 1, scheduledAt: '', activeHours: 24,
  })
  const [selectedExam, setSelectedExam] = useState(null)
  const [checked, setChecked] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    load()
    // Refresh every minute so the status badges stay correct while the admin
    // has the page open.
    const t = setInterval(load, 60000)
    return () => clearInterval(t)
  }, [])

  async function load() {
    const [e, q] = await Promise.all([
      client.get('/exams/all'),
      client.get('/questions'),
    ])
    setExams(e.data)
    setQuestions(q.data)
  }

  function update(e) { setForm({ ...form, [e.target.name]: e.target.value }) }

  async function createExam(e) {
    e.preventDefault(); setError('')
    try {
      await client.post('/exams', {
        ...form,
        durationMinutes: Number(form.durationMinutes),
        passingMarks: Number(form.passingMarks),
        activeHours: Number(form.activeHours),      // NEW
      })
      setForm({ title: '', durationMinutes: 30, passingMarks: 1, scheduledAt: '', activeHours: 24 })
      load()
    } catch (err) {
      const d = err.response?.data
      setError(d?.error || Object.values(d || {})[0] || 'Create failed')
    }
  }

  async function removeExam(id) {
    setError('')
    try { await client.delete(`/exams/${id}`); load() }
    catch (err) { setError(err.response?.data?.error || 'Delete failed') }
  }

  function toggle(qid) {
    setChecked(checked.includes(qid)
      ? checked.filter((x) => x !== qid)
      : [...checked, qid])
  }

  async function mapQuestions() {
    setError('')
    try {
      await client.post(`/exams/${selectedExam.examId}/questions`, { questionIds: checked })
      setSelectedExam(null); setChecked([]); load()
    } catch (err) {
      setError(err.response?.data?.error || 'Mapping failed')
    }
  }

  function pretty(dt) { return dt ? dt.replace('T', ' ').slice(0, 16) : '' }

  // Small helper that turns the status text into a coloured badge.
  function statusBadge(status) {
    if (status === 'ACTIVE')      return <span className="badge bg-success">Active</span>
    if (status === 'NOT_STARTED') return <span className="badge bg-warning text-dark">About to start</span>
    return <span className="badge bg-secondary">Expired</span>
  }

  return (
    <div className="container">
      <h4>Manage Exams</h4>
      {error && <div className="alert alert-danger py-2">{error}</div>}

      {/* ---------- create form ---------- */}
      <form className="card p-3 my-3" onSubmit={createExam}>
        <div className="row g-2">
          <div className="col-md-3">
            <label className="form-label small text-muted">Title</label>
            <input className="form-control" name="title" placeholder="Exam title"
                   value={form.title} onChange={update} required />
          </div>
          <div className="col-md-2">
            <label className="form-label small text-muted">Duration (min)</label>
            <input className="form-control" type="number" min="1" name="durationMinutes"
                   value={form.durationMinutes} onChange={update} required />
          </div>
          <div className="col-md-2">
            <label className="form-label small text-muted">Passing marks</label>
            <input className="form-control" type="number" min="0" name="passingMarks"
                   value={form.passingMarks} onChange={update} required />
          </div>
          <div className="col-md-3">
            <label className="form-label small text-muted">Opens at</label>
            <input className="form-control" type="datetime-local" name="scheduledAt"
                   value={form.scheduledAt} onChange={update} required />
          </div>
          {/* NEW INPUT */}
          <div className="col-md-2">
            <label className="form-label small text-muted">Active for (hours)</label>
            <input className="form-control" type="number" min="1" name="activeHours"
                   value={form.activeHours} onChange={update} required />
          </div>
        </div>
        <div className="mt-2">
          <button className="btn btn-primary">Create Exam</button>
          <span className="text-muted small ms-3">
            The exam opens at the chosen time and stays open for the hours given.
          </span>
        </div>
      </form>

      {/* ---------- exam list ---------- */}
      <table className="table table-bordered bg-white align-middle">
        <thead className="table-light">
          <tr>
            <th>Title</th><th>Opens At</th><th>Open Until</th><th>Duration</th>
            <th>Passing</th><th>Questions</th><th>Status</th>
            <th style={{ width: 210 }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {exams.map((ex) => (
            <tr key={ex.examId}>
              <td>{ex.title}</td>
              <td>{pretty(ex.scheduledAt)}</td>
              <td>{pretty(ex.endsAt)}</td>
              <td>{ex.durationMinutes} min</td>
              <td>{ex.passingMarks}</td>
              <td>{ex.totalQuestions}</td>
              <td>{statusBadge(ex.status)}</td>
              <td>
                <button className="btn btn-sm btn-outline-primary me-2"
                        onClick={() => { setSelectedExam(ex); setChecked([]) }}>
                  Add Questions
                </button>
                <button className="btn btn-sm btn-outline-danger"
                        onClick={() => removeExam(ex.examId)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* ---------- mapping panel ---------- */}
      {selectedExam && (
        <div className="card p-3">
          <h6>Add questions to: {selectedExam.title}</h6>
          <div style={{ maxHeight: 260, overflowY: 'auto' }}>
            {questions.map((q) => (
              <div className="form-check" key={q.questionId}>
                <input className="form-check-input" type="checkbox"
                       checked={checked.includes(q.questionId)}
                       onChange={() => toggle(q.questionId)} />
                <label className="form-check-label">[{q.topicName}] {q.questionText}</label>
              </div>
            ))}
          </div>
          <div className="mt-2">
            <button className="btn btn-primary me-2" onClick={mapQuestions}
                    disabled={checked.length === 0}>
              Map {checked.length} question(s)
            </button>
            <button className="btn btn-secondary" onClick={() => setSelectedExam(null)}>Close</button>
          </div>
        </div>
      )}
    </div>
  )
}
