// ============================================================
// Reminders.jsx — send exam reminder emails and view the log.
//
// NEW IDEA 2 (small): a "status message" state.
// Before now, our extra state was only for ERRORS. Here one state
// holds any short message — "Sending...", the success count, or a
// failure — and the same blue box shows whichever is current. It
// is the simplest form of user feedback for an action that takes
// a moment.
//
// WHY THE LOG TABLE MATTERS: your backend writes one row per email
// attempt, SENT or FAILED. Showing FAILED rows is not a bug in the
// demo — it proves that one bad address never stops the broadcast.
// ============================================================
import { useEffect, useState } from 'react'
import client from '../api/client'

export default function Reminders() {
  const [exams, setExams] = useState([])
  const [logs, setLogs] = useState([])
  const [message, setMessage] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    // Two lists at once: the exams to choose from, and the audit log.
    const [e, l] = await Promise.all([
      client.get('/exams/all'),        // Person B's admin list
      client.get('/reminders/logs'),   // your own log endpoint
    ])
    setExams(e.data)
    setLogs(l.data)
  }

  async function send(examId) {
    setMessage('Sending...')          // instant feedback while we wait
    try {
      const res = await client.post(`/reminders/send/${examId}`)
      // Your backend replies { message, sent } — show how many went out.
      setMessage(`Reminders processed — sent: ${res.data.sent}`)
      load()                          // refresh the log table
    } catch (err) {
      setMessage(err.response?.data?.error || 'Send failed')
    }
  }

  return (
    <div className="container">
      <h4>Email Reminders</h4>
      {message && <div className="alert alert-info py-2">{message}</div>}

      <h6>Send a reminder now</h6>
      <p className="text-muted small">
        The system also sends reminders automatically 24 hours before each exam
        (your scheduled job). This button is the manual trigger.
      </p>
      <table className="table table-bordered bg-white" style={{ maxWidth: 700 }}>
        <thead className="table-light">
          <tr><th>Exam</th><th>Scheduled</th><th></th></tr>
        </thead>
        <tbody>
          {exams.map((ex) => (
            <tr key={ex.examId}>
              <td>{ex.title}</td>
              <td>{ex.scheduledAt?.replace('T', ' ')}</td>
              <td>
                <button className="btn btn-sm btn-primary" onClick={() => send(ex.examId)}>
                  Send Reminders
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <h6 className="mt-4">Email Log</h6>
      <table className="table table-bordered bg-white">
        <thead className="table-light">
          <tr><th>ID</th><th>Student</th><th>Exam</th><th>Status</th><th>Sent At</th></tr>
        </thead>
        <tbody>
          {logs.map((l) => (
            <tr key={l.logId}>
              <td>{l.logId}</td>
              <td>{l.studentEmail}</td>
              <td>{l.examTitle}</td>
              <td>
                {/* Colour by status: green SENT, red FAILED, grey PENDING */}
                <span className={'badge ' +
                  (l.status === 'SENT' ? 'bg-success'
                   : l.status === 'FAILED' ? 'bg-danger' : 'bg-secondary')}>
                  {l.status}
                </span>
              </td>
              {/* sentAt is NULL for failed attempts — show a dash */}
              <td>{l.sentAt ? l.sentAt.replace('T', ' ').slice(0, 16) : '—'}</td>
            </tr>
          ))}
          {logs.length === 0 && (
            <tr><td colSpan="5" className="text-muted">No emails sent yet.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
