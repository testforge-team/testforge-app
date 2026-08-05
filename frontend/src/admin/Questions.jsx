// ============================================================
// Questions.jsx — the Question Bank screen.
// Same CRUD pattern as Topics, with two additions:
//
// NEW IDEA 3: Promise.all — load two things AT THE SAME TIME.
// This page needs the question list AND the topic list (for the
// dropdown). Promise.all fires both GETs together and waits for
// both, which is faster than one after the other.
//
// NEW IDEA 4: <select> dropdowns as controlled inputs.
// Dropdowns work like text inputs: value={...} + onChange. Using
// dropdowns for correctOption (A-D) and topic means the admin
// simply CANNOT type an invalid value — the UI mirrors your
// backend's @Pattern("[ABCD]") and topic foreign-key checks.
// ============================================================
import { useEffect, useState } from 'react'
import client from '../api/client'

// The empty shape of the form — used to reset it after save/cancel.
const empty = { questionText: '', optionA: '', optionB: '', optionC: '', optionD: '',
                correctOption: 'A', topicId: '' }

export default function Questions() {
  const [questions, setQuestions] = useState([])
  const [topics, setTopics] = useState([])
  const [form, setForm] = useState(empty)
  const [editId, setEditId] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => { load() }, [])

  async function load() {
    // Two requests in parallel; destructure the two results.
    const [q, t] = await Promise.all([client.get('/questions'), client.get('/topics')])
    setQuestions(q.data)
    setTopics(t.data)
  }

  // The generic one-handler-for-all-inputs trick from Register.jsx:
  // every input has a name attribute, and we overwrite that one key.
  function update(e) { setForm({ ...form, [e.target.name]: e.target.value }) }

  async function save(e) {
    e.preventDefault(); setError('')
    try {
      if (editId) await client.put(`/questions/${editId}`, form)
      else        await client.post('/questions', form)
      setForm(empty); setEditId(null); load()
    } catch (err) {
      const d = err.response?.data
      setError(d?.error || Object.values(d || {})[0] || 'Save failed')
    }
  }

  async function remove(id) {
    setError('')
    try { await client.delete(`/questions/${id}`); load() }
    catch (err) { setError(err.response?.data?.error || 'Delete failed') }
  }

  function startEdit(q) {
    setEditId(q.questionId)
    // Copy the row's values into the form (field names must match).
    setForm({ questionText: q.questionText, optionA: q.optionA, optionB: q.optionB,
              optionC: q.optionC, optionD: q.optionD, correctOption: q.correctOption,
              topicId: q.topicId })
  }

  return (
    <div className="container">
      <h4>Question Bank</h4>
      {error && <div className="alert alert-danger py-2">{error}</div>}

      {/* ---------- the add/edit form ---------- */}
      <form className="card p-3 my-3" onSubmit={save}>
        <textarea className="form-control mb-2" name="questionText" rows="2"
                  placeholder="Question text" value={form.questionText}
                  onChange={update} required />

        {/* Four option inputs, generated with .map to avoid repeating code.
            'option'+letter builds the names optionA..optionD, matching
            both the form keys and the backend DTO fields. */}
        <div className="row g-2 mb-2">
          {['A', 'B', 'C', 'D'].map((letter) => (
            <div className="col-6" key={letter}>
              <input className="form-control" name={'option' + letter}
                     placeholder={'Option ' + letter}
                     value={form['option' + letter]} onChange={update} required />
            </div>
          ))}
        </div>

        <div className="d-flex gap-2">
          <select className="form-select" style={{ maxWidth: 180 }} name="correctOption"
                  value={form.correctOption} onChange={update}>
            {['A', 'B', 'C', 'D'].map((l) => <option key={l} value={l}>Correct: {l}</option>)}
          </select>

          {/* The topic dropdown is filled from the topics state —
              the admin picks a real topic, never types an id. */}
          <select className="form-select" style={{ maxWidth: 240 }} name="topicId"
                  value={form.topicId} onChange={update} required>
            <option value="">Select topic...</option>
            {topics.map((t) => (
              <option key={t.topicId} value={t.topicId}>{t.topicName}</option>
            ))}
          </select>

          <button className="btn btn-primary">{editId ? 'Update Question' : 'Add Question'}</button>
          {editId && (
            <button type="button" className="btn btn-secondary"
                    onClick={() => { setEditId(null); setForm(empty) }}>Cancel</button>
          )}
        </div>
      </form>

      {/* ---------- the bank table ---------- */}
      <table className="table table-bordered bg-white">
        <thead className="table-light">
          <tr><th>#</th><th>Question</th><th>Topic</th><th>Answer</th><th style={{ width: 150 }}>Actions</th></tr>
        </thead>
        <tbody>
          {questions.map((q) => (
            <tr key={q.questionId}>
              <td>{q.questionId}</td>
              <td>{q.questionText}</td>
              <td>{q.topicName}</td>
              <td>{q.correctOption}</td>
              <td>
                <button className="btn btn-sm btn-outline-primary me-2" onClick={() => startEdit(q)}>Edit</button>
                <button className="btn btn-sm btn-outline-danger" onClick={() => remove(q.questionId)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
