// ============================================================
// Topics.jsx — Manage Topics screen (PERSON B's first file).
// START HERE: this small screen teaches you the two ideas that
// Questions.jsx and ManageExams.jsx repeat with bigger forms.
//
// NEW IDEA 1: useEffect — "run this when the page opens".
// A component is just a function that draws the screen. But when
// the Topics page opens, we ALSO need to fetch the topic list from
// the backend. useEffect(fn, []) means: run fn once, right after
// the page first appears. The empty [] means "only once" (do not
// repeat on every re-draw).
//
// NEW IDEA 2: .map() — "turn a list into rows".
// topics is an array from the backend. topics.map((t) => <tr>...)
// converts EVERY topic object into one table row. This is how all
// lists and tables are drawn in React. The key={...} attribute
// gives each row a unique id so React can update rows efficiently
// (React warns in the console if you forget it).
//
// THE CRUD PATTERN (used by all three of your screens):
//   load()  -> GET the list into state (useEffect calls it on open)
//   save()  -> POST (add) or PUT (edit), then call load() again
//   remove()-> DELETE, then call load() again
// "Change something, then reload the list" keeps screen and
// database always in sync.
// ============================================================
import { useEffect, useState } from 'react'
import client from '../api/client'   // Person A's connection (token auto-attached)

export default function Topics() {
  const [topics, setTopics] = useState([])   // the list shown in the table
  const [name, setName] = useState('')       // the text box value
  const [editId, setEditId] = useState(null) // null = adding; a number = editing that topic
  const [error, setError] = useState('')     // red message text

  // Run load() once when the page opens.
  useEffect(() => { load() }, [])

  async function load() {
    const res = await client.get('/topics')   // your own backend endpoint!
    setTopics(res.data)                       // putting it in state redraws the table
  }

  // One function handles BOTH add and edit:
  // if editId is set we PUT (update), otherwise we POST (create).
  async function save(e) {
    e.preventDefault()
    setError('')
    try {
      if (editId) {
        await client.put(`/topics/${editId}`, { topicName: name })
      } else {
        await client.post('/topics', { topicName: name })
      }
      setName(''); setEditId(null)
      load()                                  // refresh the table
    } catch (err) {
      // A duplicate name -> your backend's 400 "Topic already exists"
      // appears here, in your own UI.
      setError(err.response?.data?.error || 'Save failed')
    }
  }

  async function remove(id) {
    setError('')
    try {
      await client.delete(`/topics/${id}`)
      load()
    } catch (err) {
      // Deleting a topic still used by questions -> your friendly
      // foreign-key message from the backend shows up here.
      setError(err.response?.data?.error || 'Delete failed')
    }
  }

  // The Edit button doesn't open a new page — it just copies the row
  // into the form and remembers the id. The same form then updates.
  function startEdit(t) {
    setEditId(t.topicId)
    setName(t.topicName)
  }

  return (
    <div className="container">
      <h4>Manage Topics</h4>
      {error && <div className="alert alert-danger py-2">{error}</div>}

      {/* The add/edit form. Note the button label changes with the mode. */}
      <form className="d-flex gap-2 my-3" onSubmit={save}>
        <input className="form-control" style={{ maxWidth: 320 }} placeholder="Topic name"
               value={name} onChange={(e) => setName(e.target.value)} required />
        <button className="btn btn-primary">{editId ? 'Update' : 'Add Topic'}</button>
        {editId && (
          <button type="button" className="btn btn-secondary"
                  onClick={() => { setEditId(null); setName('') }}>Cancel</button>
        )}
      </form>

      {/* The list, drawn with .map() */}
      <table className="table table-bordered bg-white">
        <thead className="table-light">
          <tr><th>ID</th><th>Topic</th><th style={{ width: 160 }}>Actions</th></tr>
        </thead>
        <tbody>
          {topics.map((t) => (
            <tr key={t.topicId}>
              <td>{t.topicId}</td>
              <td>{t.topicName}</td>
              <td>
                <button className="btn btn-sm btn-outline-primary me-2"
                        onClick={() => startEdit(t)}>Edit</button>
                <button className="btn btn-sm btn-outline-danger"
                        onClick={() => remove(t.topicId)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
