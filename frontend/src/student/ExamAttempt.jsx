// ============================================================
// ExamAttempt.jsx  -  REWRITTEN so the exam survives a refresh.
//
// WHAT CHANGED, AND WHY
//
// Before, this screen kept everything in the browser: it counted the timer
// down from durationMinutes and kept the answers in memory. So pressing F5
// wiped both.
//
// Now the attempt lives on the SERVER:
//   * When the screen opens we call POST /exams/{id}/start. The server either
//     creates a new attempt or gives us back the one already running, along
//     with the SECONDS LEFT and the ANSWERS ALREADY SAVED. So a refresh just
//     asks the server "where was I?" and continues from there.
//   * Every time the student clicks an option we send it to the server with
//     PUT /attempts/{attemptId}/answer. Nothing is only in memory.
//   * The timer is derived from the server's remaining seconds, so refreshing
//     can never reset it or give extra time.
//   * If the student comes back after the deadline, the server has already
//     graded the attempt and tells us so - we go straight to the result page.
// ============================================================
import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import client from '../api/client'

export default function ExamAttempt() {
  const { id } = useParams()          // which exam, from the URL
  const navigate = useNavigate()

  const [attemptId, setAttemptId] = useState(null)   // the server's attempt row
  const [exam, setExam] = useState(null)             // title + questions
  const [current, setCurrent] = useState(0)          // which question is shown
  const [answers, setAnswers] = useState({})         // { questionId: "B" }
  const [secondsLeft, setSecondsLeft] = useState(null)
  const [saving, setSaving] = useState(false)        // small "Saving..." hint
  const [error, setError] = useState('')

  const submitted = useRef(false)     // guards against submitting twice

  // ------------------------------------------------------------
  // 1. OPEN OR RESUME THE ATTEMPT
  // ------------------------------------------------------------
  useEffect(() => {
    async function startOrResume() {
      try {
        // ONE call handles all three cases: first open, refresh, or too late.
        const res = await client.post(`/exams/${id}/start`)
        const data = res.data

        // CASE: the student came back after the time was over. The server has
        // already graded it using the saved answers, so just show the result.
        if (data.expired) {
          navigate(`/student/result/${data.resultId}`, { replace: true })
          return
        }

        setAttemptId(data.attemptId)
        setExam({ title: data.title, questions: data.questions })

        // Put the saved answers back into state, so the selected options and
        // the green navigator buttons look exactly as the student left them.
        setAnswers(data.savedAnswers || {})

        // The clock comes from the SERVER, not from the exam duration.
        setSecondsLeft(data.remainingSeconds)
      } catch (err) {
        // For example: the exam has not started yet, or is already closed.
        setError(err.response?.data?.error || 'Could not open this exam.')
      }
    }
    startOrResume()
  }, [id, navigate])

  // ------------------------------------------------------------
  // 2. THE COUNTDOWN
  // ------------------------------------------------------------
  useEffect(() => {
    if (secondsLeft === null) return                    // not loaded yet
    if (secondsLeft <= 0) { handleSubmit(true); return } // TIME OVER

    const t = setInterval(() => setSecondsLeft((s) => s - 1), 1000)
    return () => clearInterval(t)      // cleanup - without this the clock races
  }, [secondsLeft])

  // ------------------------------------------------------------
  // 3. CHOOSING AN OPTION  ->  save it on the server straight away
  // ------------------------------------------------------------
  async function choose(questionId, option) {
    // Update the screen first so it feels instant.
    setAnswers((prev) => ({ ...prev, [questionId]: option }))

    try {
      setSaving(true)
      await client.put(`/attempts/${attemptId}/answer`, {
        questionId: questionId,
        selectedOption: option,
      })
    } catch (err) {
      // A failed save is worth telling the student about, because it means
      // this answer might not survive a refresh.
      setError('Could not save that answer. Check your connection.')
    } finally {
      setSaving(false)
    }
  }

  // ------------------------------------------------------------
  // 4. SUBMITTING  (button, or the timer reaching zero)
  // ------------------------------------------------------------
  async function handleSubmit(auto = false) {
    if (submitted.current) return       // only ever once
    submitted.current = true

    try {
      // The answers are already on the server. We still send them as a safety
      // net, in case the very last click did not finish saving.
      const payload = {
        answers: Object.entries(answers).map(([questionId, selectedOption]) => ({
          questionId: Number(questionId),
          selectedOption,
        })),
      }

      // autoSubmitted tells the backend HOW the attempt ended, so the admin
      // can tell a timed-out attempt from a normal one.
      const res = await client.post(
        `/exams/${id}/submit?autoSubmitted=${auto}`, payload)

      navigate('/student/result', { state: res.data, replace: true })
    } catch (err) {
      submitted.current = false        // allow a retry if it failed
      setError(err.response?.data?.error || 'Submit failed. Please try again.')
    }
  }

  // ------------------------------------------------------------
  // 5. WHAT IS SHOWN
  // ------------------------------------------------------------
  if (error && !exam) {
    return (
      <div className="container">
        <div className="alert alert-danger">{error}</div>
        <button className="btn btn-secondary" onClick={() => navigate('/student')}>
          Back to Dashboard
        </button>
      </div>
    )
  }

  if (!exam || secondsLeft === null) {
    return <div className="container">Loading exam...</div>
  }

  const q = exam.questions[current]
  const mins = String(Math.floor(secondsLeft / 60)).padStart(2, '0')
  const secs = String(secondsLeft % 60).padStart(2, '0')
  const answeredCount = Object.keys(answers).length

  // Turn the timer red in the last minute, as a warning.
  const timerClass = secondsLeft <= 60 ? 'timer-box text-danger' : 'timer-box'

  return (
    <div className="container">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h4 className="m-0">{exam.title}</h4>
        <div className="d-flex align-items-center gap-3">
          {saving && <span className="text-muted small">Saving...</span>}
          <div className={timerClass}>{mins}:{secs}</div>
        </div>
      </div>

      {error && <div className="alert alert-warning py-2">{error}</div>}

      <div className="row">
        {/* -------- left: the current question -------- */}
        <div className="col-md-8">
          <div className="card p-3">
            <div className="text-muted small mb-1">
              Question {current + 1} of {exam.questions.length} - Topic: {q.topicName}
            </div>
            <p className="fs-5">{q.questionText}</p>

            {['A', 'B', 'C', 'D'].map((letter) => (
              <div key={letter}
                   className={'option-row border rounded p-2 mb-2 ' +
                     (answers[q.questionId] === letter ? 'bg-primary-subtle border-primary' : '')}
                   onClick={() => choose(q.questionId, letter)}>
                <strong>{letter}.</strong> {q['option' + letter]}
              </div>
            ))}

            <div className="d-flex justify-content-between mt-2">
              <button className="btn btn-outline-secondary" disabled={current === 0}
                      onClick={() => setCurrent(current - 1)}>Previous</button>
              <button className="btn btn-outline-secondary"
                      disabled={current === exam.questions.length - 1}
                      onClick={() => setCurrent(current + 1)}>Next</button>
            </div>
          </div>
        </div>

        {/* -------- right: navigator and submit -------- */}
        <div className="col-md-4">
          <div className="card p-3">
            <h6>Question Navigator</h6>
            <div className="d-flex flex-wrap gap-2">
              {exam.questions.map((qq, i) => (
                <button key={qq.questionId}
                        className={'btn btn-outline-dark qnav-btn ' +
                          (answers[qq.questionId] ? 'qnav-answered ' : '') +
                          (i === current ? 'qnav-current' : '')}
                        onClick={() => setCurrent(i)}>
                  {i + 1}
                </button>
              ))}
            </div>
            <hr />
            <div className="small text-muted mb-2">
              {answeredCount} answered - {exam.questions.length - answeredCount} remaining
            </div>
            <button className="btn btn-success w-100" onClick={() => handleSubmit(false)}>
              SUBMIT EXAM
            </button>
            <div className="small text-muted mt-1 text-center">
              Auto-submits at 00:00. Your answers are saved as you go.
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
