// ============================================================
// Register.jsx — the sign-up page.
//
// NEW IDEA HERE: one state OBJECT for several inputs.
// Login had 2 inputs so we used 2 useState calls. This form has 4,
// so we keep them together in one object:
//   { name:'', email:'', password:'', confirm:'' }
// and ONE update function handles all four inputs, by reading
// which input fired the event (e.target.name).
// ============================================================
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from './authService'

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '', confirm: '' })
  const [error, setError] = useState('')
  const navigate = useNavigate()

  // Generic updater: if the email input changes, e.target.name is
  // "email" and we copy the old object with that one field replaced.
  // The three dots (...form) mean "copy everything from form".
  function update(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    // A quick check we can do without the backend:
    if (form.password !== form.confirm) { setError('Passwords do not match'); return }
    try {
      await register(form.name, form.email, form.password)
      navigate('/student')   // new accounts are always students
    } catch (err) {
      // The backend sends errors in two shapes:
      //  { error: "Email is already registered" }  (our own checks)
      //  { password: "must be at least 8 characters" } (validation)
      // This line shows whichever arrived.
      const data = err.response?.data
      setError(data?.error || Object.values(data || {})[0] || 'Registration failed')
    }
  }

  return (
    <div className="d-flex justify-content-center mt-5">
      <div className="card p-4 shadow-sm" style={{ width: 420 }}>
        <h4 className="text-center mb-3">Create Student Account</h4>
        {error && <div className="alert alert-danger py-2">{error}</div>}
        <form onSubmit={handleSubmit}>
          <input className="form-control mb-2" name="name" placeholder="Full name"
                 value={form.name} onChange={update} required />
          <input className="form-control mb-2" name="email" type="email" placeholder="Email"
                 value={form.email} onChange={update} required />
          <input className="form-control mb-2" name="password" type="password"
                 placeholder="Password (min 8 chars)" value={form.password} onChange={update} required />
          <input className="form-control mb-3" name="confirm" type="password"
                 placeholder="Confirm password" value={form.confirm} onChange={update} required />
          <button className="btn btn-primary w-100">Register</button>
        </form>
        <div className="text-center mt-3">
          <Link to="/login">Already have an account? Login</Link>
        </div>
      </div>
    </div>
  )
}
