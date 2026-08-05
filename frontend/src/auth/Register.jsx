// ============================================================
// Register.jsx — the sign-up page.
// ============================================================
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from './authService'

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '', confirm: '' })
  const [error, setError] = useState('')
  const navigate = useNavigate()
  
  // Separate states for the two password fields
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  function update(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    
    if (form.password !== form.confirm) { setError('Passwords do not match'); return }
    
    try {
      await register(form.name, form.email, form.password)
      navigate('/student')
    } catch (err) {
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

          {/* Main Password Input */}
          <div className="input-group mb-3">
            <input className="form-control"
              name="password"
              type={showPassword ? 'text' : 'password'}
              placeholder="Password (min 8 chars)"
              value={form.password}
              onChange={update} 
              required />
            <button className="btn btn-outline-secondary" type="button"
              onClick={() => setShowPassword(!showPassword)}>
              {showPassword ? 'Hide' : 'Show'}
            </button>
          </div>

          {/* Confirm Password Input */}
          <div className="input-group mb-3">
            <input className="form-control"
              name="confirm"
              type={showConfirmPassword ? 'text' : 'password'}
              placeholder="Confirm password"
              value={form.confirm}
              onChange={update} 
              required />
            <button className="btn btn-outline-secondary" type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}>
              {showConfirmPassword ? 'Hide' : 'Show'}
            </button>
          </div>

          <button className="btn btn-primary w-100">Register</button>
        </form>
        <div className="text-center mt-3">
          <Link to="/login">Already have an account? Login</Link>
        </div>
      </div>
    </div>
  )
}