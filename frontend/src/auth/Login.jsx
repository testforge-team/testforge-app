// ============================================================
// Login.jsx — the login page. YOUR FIRST REACT COMPONENT.
//
// WHAT IS A COMPONENT?
// A component is just a JavaScript function that RETURNS some
// HTML-like markup (JSX). React calls the function and shows
// whatever it returns. One screen = one component file.
//
// JSX LOOKS LIKE HTML BUT WITH 3 DIFFERENCES:
//  1. class becomes className (class is a reserved word in JS).
//  2. { curly braces } insert JavaScript into the markup,
//     e.g. {error} prints the value of the error variable.
//  3. Events are camelCase and take a function:
//     onSubmit={handleSubmit} instead of onsubmit="...".
//
// WHAT IS STATE (useState)?
// State is a component's memory. In plain HTML, an <input> keeps
// its own text. In React, WE keep the text in a variable and the
// screen re-draws whenever it changes.
//   const [email, setEmail] = useState('')
// gives us: email (the current value) and setEmail (the ONLY
// correct way to change it). Calling setEmail('abc') updates the
// value AND refreshes the screen. That pair-pattern is 90% of React.
// ============================================================
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { login } from './authService'

export default function Login() {
  // Three pieces of memory for this page:
  const [email, setEmail] = useState('')        // what is typed in the email box
  const [password, setPassword] = useState('')  // what is typed in the password box
  const [error, setError] = useState('')        // the red message (empty = hidden)

  // useNavigate gives us a function to jump to another page from code.
  const navigate = useNavigate()

  // Runs when the form is submitted (button click or Enter key).
  async function handleSubmit(e) {
    e.preventDefault()   // stop the browser's old-style full page reload
    setError('')
    try {
      const data = await login(email, password)   // talk to the backend
      // The backend told us the role — send each role to its own home.
      navigate(data.role === 'ADMIN' ? '/admin' : '/student')
    } catch (err) {
      // If the backend said 401, show its message ("Invalid credentials").
      // The ?. is "optional chaining": safely dig into the object,
      // and if anything on the path is missing, use the fallback text.
      setError(err.response?.data?.error || 'Login failed')
    }
  }

  // What this component SHOWS. Notice:
  //  - {error && <div>...} means: only render the div IF error is not empty.
  //  - value={email} + onChange makes the input a "controlled input":
  //    React state is the single source of truth for the typed text.
  return (
    <div className="d-flex justify-content-center mt-5">
      <div className="card p-4 shadow-sm" style={{ width: 400 }}>
        <h3 className="text-center mb-1">TestForge</h3>
        <p className="text-center text-muted mb-3">Forging skills through assessment</p>

        {error && <div className="alert alert-danger py-2">{error}</div>}

        <form onSubmit={handleSubmit}>
          <input className="form-control mb-2" type="email" placeholder="Email"
                 value={email} onChange={(e) => setEmail(e.target.value)} required />
          <input className="form-control mb-3" type="password" placeholder="Password"
                 value={password} onChange={(e) => setPassword(e.target.value)} required />
          <button className="btn btn-primary w-100">Login</button>
        </form>

        {/* Link = an <a> tag that changes the page WITHOUT reloading */}
        <div className="text-center mt-3">
          <Link to="/register">New user? Register here</Link>
        </div>
      </div>
    </div>
  )
}
