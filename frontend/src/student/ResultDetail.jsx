// ============================================================
// ResultDetail.jsx — the full answer sheet of one past attempt.
//
// Ideas used: useParams (read resultId from the URL), useEffect
// (fetch when the page opens), .map (draw the lists). Nothing new —
// which is a good sign that you are getting comfortable now.
//
// ONE THING WORTH NOTICING: this response DOES include
// correctOption. That is correct and intentional — the exam is
// over, so revealing the answers helps the student learn. During
// the attempt the backend sent a version without it. Same system,
// two different responses, decided by your backend. Good viva point.
// ============================================================
import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import client from '../api/client'

export default function ResultDetail() {
  const { resultId } = useParams()
  const [detail, setDetail] = useState(null)

  useEffect(() => {
    client.get(`/results/detail/${resultId}`).then((res) => setDetail(res.data))
  }, [resultId])

  if (!detail) return <div className="container">Loading...</div>

  return (
    <div className="container">
      <h4>
        {detail.examTitle} — {detail.obtainedMarks}/{detail.totalMarks} ({detail.percentage}%) · {detail.status}
      </h4>

      {/* ---- topic-wise summary (from your backend's breakdown) ---- */}
      <h6 className="mt-4">Topic Breakdown</h6>
      <table className="table table-bordered bg-white" style={{ maxWidth: 600 }}>
        <thead className="table-light">
          <tr><th>Topic</th><th>Correct</th><th>Total</th><th>Accuracy</th></tr>
        </thead>
        <tbody>
          {detail.topicBreakdown.map((t) => (
            <tr key={t.topicName}>
              <td>{t.topicName}</td><td>{t.correct}</td><td>{t.total}</td><td>{t.accuracy}%</td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* ---- question by question review ---- */}
      <h6 className="mt-4">Answer Review</h6>
      {detail.answers.map((a, i) => (
        // The border colour depends on correctness — conditional
        // class names again, the same trick as the exam options.
        <div key={a.questionId}
             className={'card p-3 mb-2 border-2 ' + (a.correct ? 'border-success' : 'border-danger')}>
          <div className="small text-muted">Q{i + 1} · {a.topicName}</div>
          <div>{a.questionText}</div>
          <div className="small mt-1">
            {/* ?? means "if selectedOption is null/undefined, show
                this instead" — a skipped question stored as NULL. */}
            Your answer: <strong>{a.selectedOption ?? 'Skipped'}</strong> ·
            Correct answer: <strong>{a.correctOption}</strong> ·
            {a.correct ? ' Correct' : ' Wrong'}
          </div>
        </div>
      ))}
    </div>
  )
}
